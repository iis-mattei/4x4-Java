package Robot;

import java.util.Date;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;

public class Main {
	public static final boolean DEBUG = true; // Per stampare più informazioni

	private static Sensors sensors = new Sensors();
	private static Motors motors = new Motors();
	private static MainThread mainThread = null;

	private static int deltaMax = 0;
	private static long startTime = 0;

	public static void main(String args[]) {
		Button.DOWN.addKeyListener(new KeyListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void keyPressed(Key k) {
				if (mainThread != null) {
					System.out.println("Riavvio...");
					try {
						mainThread.stop();
					} catch (ThreadDeath e) {
						mainThread = new MainThread(deltaMax, startTime, sensors, motors);
						mainThread.start();
					}

//					mainThread.interrupt();
//					try {
//						// Aspetta (fino a 3 secondi) che il thread sia terminato
//						mainThread.join(3_000);
//					} catch (InterruptedException e) {
//						System.out.println("InterruptedException");
//					}

				}
			}

			@Override
			public void keyReleased(Key k) {
				return;
			}
		});
		Button.ESCAPE.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				System.out.println("Fine!");
				System.exit(0);
			}

			@Override
			public void keyReleased(Key k) {
				return;
			}
		});

		Sound.beepSequenceUp();
		System.out.println("INVIO per calibrare");
		Button.ENTER.waitForPress();

		// Memorizza l'istante di inizio del percorso
		startTime = new Date().getTime();
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(sensors.getLuxL() - sensors.getLuxC());
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);

		// Avvia il thread per seguilinea e zona vittime
		mainThread = new MainThread(deltaMax, startTime, sensors, motors);
		mainThread.start();
	}
}

class MainThread extends Thread {
	private static final int OBSTACLE_DIST = 8; // Centimetri
	private static final int NO_BLACK_DIST = 25; // Centimetri
	private static final int DOUBLE_WHITE_MAX = 5;

	private int deltaMax;
	private long startTime;
	private boolean loaded = false;

	private Sensors sensors;
	private Motors motors;
	private PID pid = null;

	public MainThread(int deltaMax, long startTime, Sensors sensors, Motors motors) {
		super();
		this.deltaMax = deltaMax;
		this.startTime = startTime;
		this.sensors = sensors;
		this.motors = motors;
	}

	@Override
	public void run() {
		lineFollower();
	}

	@Override
	public void interrupt() {
		super.interrupt();
		motors.stop();
	}

	private void lineFollower() {
		int greenLeft = 0, greenRight = 0, silver = 0, doubleWhiteCounter = 0;
		int[] speeds = new int[2];
		double pidCorrection = 1;

		Sound.beepSequenceUp();
		System.out.println("INVIO per partire");
		Button.ENTER.waitForPress();
		System.out.println("\n*** Partito ***");
		pid = new PID(0, deltaMax);

		while (true) {
			// Controlla se c'è un ostacolo: nel caso lo aggira
			float distFwdLow = sensors.checkDistanceFwdLow();
			if (distFwdLow < OBSTACLE_DIST && distFwdLow > 0) {
				navigateObstacle();
				continue;
			}

			sensors.checkColors();
			if (Main.DEBUG) {
				System.out.println((new Date()).getTime() + " - Colors LR: " + sensors.getColorsLR());
			}

			// Verifica se si è persa la pista e, se necessario, torna indietro per cercarla
			if (sensors.isAnyBlack()) {
				doubleWhiteCounter = 0;
				silver = Math.max(silver - 1, 0);
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				System.out.println("Cerca la linea...");
				motors.travel(Motors.BASE_SPEED, -NO_BLACK_DIST - 5);
			}

//			sensors.checkGyro();
//			if (Math.abs(sensors.getGyroY()) > 10) {
//				pidCorrection = 0.5;
//			} else{
//				pidCorrection = 1;
//			}

			// Seguilinea con il PID
			speeds = pid.getSpeed(sensors.getDelta());
			// Adatta il PID alle condizioni della pista
//			for (int i = 0; i < speeds.length; i++) {
//				speeds[i] = (int) Math.round(speeds[i] * pidCorrection);
//			}

			switch (sensors.getColorsLR()) {
			case "ss":
				silver++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;
			case "sw":
			case "ws":
				silver++;
				motors.drive(speeds[0], speeds[1]);
				break;
			case "ww":
				doubleWhiteCounter = Math.min(doubleWhiteCounter + 1, DOUBLE_WHITE_MAX);
				// Rettilineo, decrementa prenotazioni verde
				greenLeft = Math.max(greenLeft - 1, 0);
				greenRight = Math.max(greenRight - 1, 0);
				motors.drive(speeds[0], speeds[1]);
				// Se le misure confermano, passa alla modalità zona vittime
				if (silver > 0) {
					int distSide = sensors.checkDistanceSide();
					int distFront = sensors.checkDistanceFwdHigh();
					if (distSide < 110 && distFront < 120 && distFront > 80) {
						evacuationZone();
						// Resetta tutto per ricominciare
						pid = new PID(0, deltaMax);
						greenLeft = 0;
						greenRight = 0;
						silver = 0;
						doubleWhiteCounter = 0;
					}
				}
				break;

			case "wb":
			case "bw":
				if (sensors.getColorC().equals("b")) {
					// Incrocio a T
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				} else {
					// Curva normale
					motors.drive(speeds[0], speeds[1]);
				}
				break;

			case "bb":
				if (greenLeft == 0 && greenRight == 0) {
					// Segue la linea
					motors.drive(speeds[0], speeds[1]);
				} else if (greenLeft > 0 && greenRight > 0) {
					// Inversione di marcia
					System.out.println((new Date()).getTime() + "\tInversione di marcia");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, 180);
					motors.resetTachoCount();
					greenLeft = 0;
					greenRight = 0;
				} else if (greenLeft > 0) {
					// Curva a sinistra
					System.out.println((new Date()).getTime() + "\tCurva a sinistra");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, 60);
					motors.resetTachoCount();
					greenLeft = 0;
				} else if (greenRight > 0) {
					// Curva a destra
					System.out.println((new Date()).getTime() + "\tCurva a destra");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, -60);
					motors.resetTachoCount();
					greenRight = 0;
				}
				break;

			case "gw":
			case "gb":
				// Prenota curva a sinistra e va dritto
				greenLeft++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "wg":
			case "bg":
				// Prenota curva a destra e va dritto
				greenRight++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "gg":
				// Prenota inversione di marcia e va dritto
				greenLeft++;
				greenRight++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;
			}
		}
	}

	private void evacuationZone() {
		System.out.println("*** Zona Vittime ***");
		Sound.playTone(440, 500); // LA4
		Sound.playTone(523, 1000); // DO5
		// zoneOrientation: 0 è larga (120x90), 1 è lunga (90x120)
		// safePosition: 0 è l'angolo in basso a sinistra, gli altri in senso orario
		// gatePosition: da 0 a 3, da sinistra a destra
		int zoneOrientation = -1, safePosition = -1, gatePosition = -1;
		loaded = false;

		// Imposta Arduino in modalità zona vittime
		sensors.setEvacuationZoneMode();

		// Avanza per far entrare tutto il robot nella zona
		motors.travel(Motors.BASE_SPEED, 20);

		// Individua la forma della zona vittime
		if (sensors.checkDistanceFwdHigh() < 85) {
			zoneOrientation = 0;
		} else {
			zoneOrientation = 1;
		}
		System.out.println("zoneOrientation=" + zoneOrientation);

		// Individua la posizione dell'ingresso
		float d = sensors.checkDistanceSide();
		if (d < 25) {
			gatePosition = 3;
		} else if (d < 55) {
			gatePosition = 2;
		} else if (d < 85) {
			gatePosition = 1;
		} else {
			gatePosition = 0;
		}
		System.out.println("gatePosition=" + gatePosition);

		// Fa tutto il perimetro per trovare la zona vittime, inizia girando a sinistra
		motors.spin(Motors.BASE_SPEED, 90);

		// Mentre il robot cammina, verifica:
		// - Se c'è una pallina sul percorso (nel caso, la raccoglie)
		// - Se nell'angolo c'è la zona sicura (nel caso, memorizza la posizione)
		// Ripete per ogni parete finché non trova la zona sicura
		int sidesExplored = 0;
		do {
			// Va avanti finché non trova la parete
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				sensors.checkTouches();
				if (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed()) {
					// Se ha toccato con i sensori senza arrivare al muro, ha trovato la zona sicura
					safePosition = sidesExplored;
					break;
				} else if (sensors.isFwdLeftPressed()) {
					// Se uno dei sensori ha toccato, fa allineare l'altro
					motors.drive(-Motors.BASE_SPEED / 2, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, -Motors.BASE_SPEED / 2);
				} else if (sensors.checkDistanceFwdLow() < 7) {
					// Si sta avvicinando a una pallina: si ferma per raccoglierla
					System.out.println("Raccogli pallina");
					motors.travel(Motors.BASE_SPEED, -3);
					motors.bladeLower();
					motors.travel(Motors.BASE_SPEED, 10);
					motors.bladeLift();
					loaded = true;
				} else {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				}
			}
			if (safePosition < 0) {
				// Se non ha trovato la zona sicura, gira a destra di 90 gradi e riparte
				motors.spin(Motors.BASE_SPEED, -90);
				if (sidesExplored != 0 || gatePosition != 0) {
					alignBackwards();
				}
				sidesExplored++;
			}
		} while (safePosition < 0 && sidesExplored < 4);

		// Trovato la zona sicura: fa inversione e vi si appoggia
		motors.travel(Motors.BASE_SPEED, -10);
		motors.spin(Motors.BASE_SPEED, 180);
		alignBackwards();
		// Se ci sono delle palline a bordo fa la manovra di scarico
		if (loaded) {
			unloadVictims();
		}
		motors.travel(Motors.BASE_SPEED, 10);

		// Una volta trovata la zona sicura, parte con le spazzate in orizzontale
		// Il robot si mette in posizione per iniziare le spazzate
		int signum = safePosition % 2 == 0 ? 1 : -1;
		motors.spin(Motors.BASE_SPEED, signum * -45);

		// Ogni spazzata a circa 20cm di distanza dall'altra
		// Ad ogni spazzata raccoglie le palline e le riporta nella zona sicura
		// Se manca meno di un minuto esce dal ciclo e cerca l'uscita
		for (int i = 0; (i < 3 + zoneOrientation) && (new Date().getTime() < startTime + 7 * 60 * 1_000); i++) {
			// Percorre la zona all'andata
			driveUntilWall();
			// Inversione di marcia
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.bladeLower();
			motors.travel(Motors.BASE_SPEED, 15);
			// Recupera eventuali palline
			motors.bladeLift();
			motors.bladeLower();
			motors.spin(Motors.BASE_SPEED, signum * 90);
			// Percorre la zona al ritorno
			driveUntilWall();
			// Manovra per scaricare: si appoggia alla zona sicura e scarica palline
			motors.spin(Motors.BASE_SPEED, signum * -90);
			if (loaded) {
				alignBackwards();
				unloadVictims();
				// Si prepara per un'altra spazzata
				motors.bladeLower();
				motors.travel(Motors.BASE_SPEED, 10);
				motors.spin(Motors.BASE_SPEED, signum * 45);
				motors.travel(Motors.BASE_SPEED, 15 + i * 30);
				// Recupera eventuali palline
				motors.bladeLift();
			} else if (i == 2 + zoneOrientation) {
				// Se ha completato i passaggi, smette di spazzare
				alignBackwards();
				break;
			}
			motors.travel(Motors.BASE_SPEED, 15);
			motors.spin(Motors.BASE_SPEED, signum * -90);
		}

		// Uscita dalla zona vittime
		switch (safePosition) {
		case 0:
			motors.spin(Motors.BASE_SPEED, -45);
			motors.travel(Motors.BASE_SPEED, gatePosition * 30);
			motors.spin(Motors.BASE_SPEED, -90);
			break;
		case 1:
			motors.spin(Motors.BASE_SPEED, -45);
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
			if (gatePosition > 0) {
				motors.spin(Motors.BASE_SPEED, 90);
				motors.travel(Motors.BASE_SPEED, gatePosition * 30);
				motors.spin(Motors.BASE_SPEED, -90);
			}
			break;
		case 2:
			motors.spin(Motors.BASE_SPEED, 45);
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
			if (gatePosition < 3) {
				motors.spin(Motors.BASE_SPEED, -90);
				motors.travel(Motors.BASE_SPEED, (3 - gatePosition - zoneOrientation) * 30);
				motors.spin(Motors.BASE_SPEED, 90);
			}
			break;
		case 3:
			motors.spin(Motors.BASE_SPEED, 45);
			motors.travel(Motors.BASE_SPEED, (3 - gatePosition) * 30);
			motors.spin(Motors.BASE_SPEED, 90);
			break;
		}

		// All'uscita dalla zona vittime si torna al seguilinea
		sensors.setRescueLineMode();
		sensors.checkColors();
		while (!sensors.isAnySilver()) {
			motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			sensors.checkColors();
		}
		motors.travel(Motors.BASE_SPEED, 10);
		boolean foundBlack = false;
		for (int i = 0; i < 7; i++) {
			sensors.checkColors();
			if (sensors.isAnyBlack()) {
				foundBlack = true;
				break;
			}
			motors.spin(Motors.BASE_SPEED, 10);
		}
		if (!foundBlack) {
			for (int i = 0; i < 14; i++) {
				sensors.checkColors();
				if (sensors.isAnyBlack()) {
					foundBlack = true;
					break;
				}
				motors.spin(Motors.BASE_SPEED, -10);
			}
		}
		motors.resetTachoCount();
		return;
	}

	/***** Procedure di servizio *****/

	// Aggiramento ostacolo
	private void navigateObstacle() {
		motors.stop();
		System.out.println("Aggira ostacolo");

		// Raddrizzo il robot rispetto alla linea nera
		if (sensors.getColorsLR().equals("wb")) {
			while (!sensors.getColorsLR().equals("ww")) {
				motors.spin(Motors.BASE_SPEED, 10);
				sensors.checkColors();
			}
		} else if (sensors.getColorsLR().equals("bw")) {
			while (!sensors.getColorsLR().equals("ww")) {
				motors.spin(Motors.BASE_SPEED, -10);
				sensors.checkColors();
			}
		}

		// Cerca il lato giusto per passare
		System.out.println((new Date()).getTime() + "\tAggiramento ostacolo");
		int direction = 1; // -1 = right, 1 = left
		motors.spin(Motors.BASE_SPEED, 90);
		motors.travel(Motors.BASE_SPEED, 10);
		if (sensors.checkDistanceFwdLow() < 20 || sensors.checkDistanceSide() < 30) {
			motors.spin(Motors.BASE_SPEED, -180);
			direction = -1;
		}

		motors.travel(Motors.BASE_SPEED, 5);
		motors.spin(Motors.BASE_SPEED, -90 * direction);
		motors.travel(Motors.BASE_SPEED, 35);
		motors.spin(Motors.BASE_SPEED, -90 * direction);
		motors.travel(Motors.BASE_SPEED, 10);
		motors.spin(Motors.BASE_SPEED, 90 * direction);
		motors.resetTachoCount();
	}

	// Allineamento al muro
	private void alignBackwards() {
		System.out.println("Allineamento");
		while (true) {
			sensors.checkTouches();
			if (sensors.isBackLeftPressed() && sensors.isBackRightPressed()) {
				sensors.resetGyro();
				return;
			} else if (sensors.isBackLeftPressed()) {
				motors.drive(-Motors.BASE_SPEED / 2, -Motors.BASE_SPEED);
			} else if (sensors.isBackRightPressed()) {
				motors.drive(-Motors.BASE_SPEED, -Motors.BASE_SPEED / 2);
			} else {
				motors.drive(-Motors.BASE_SPEED, -Motors.BASE_SPEED);
			}
		}
	}

	// Va avanti finché non trova la parete
	private void driveUntilWall() {
		while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
			if (sensors.checkDistanceFwdLow() < 7) {
				// Si sta avvicinando a una pallina: si ferma per raccoglierla
				motors.travel(Motors.BASE_SPEED, -3);
				motors.bladeLower();
				motors.travel(Motors.BASE_SPEED, 10);
				motors.bladeLift();
				loaded = true;
			} else {
				motors.drive(Motors.MAX_SPEED, Motors.MAX_SPEED);
			}
		}
	}

	// Scarica palline
	private void unloadVictims() {
		motors.containerOpen();
		motors.travel(Motors.BASE_SPEED, 2);
		motors.travel(Motors.MAX_SPEED, -3);
		motors.containerClose();
	}
}
