package Robot;

import java.util.Date;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;

public class Main {
	public static final boolean DEBUG = false; // Per stampare pi� informazioni
	private static final int OBSTACLE_DIST = 6; // Centimetri
	private static final int NO_BLACK_DIST = 25; // Centimetri

	private static Sensors sensors = new Sensors();
	private static Motors motors = new Motors();
	private static PID pid = null;

	private static int deltaMax = 0;
	private static int whiteMax = 0;
	private static long startTime = 0;
//	private static boolean loaded = false;
	private static boolean reset = false;

	public static void main(String args[]) throws InterruptedException {
//		try {
//			System.setOut(new PrintStream("main1.out"));
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
		Button.ESCAPE.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				if (motors.isBladeLow()) {
					motors.bladeLift();
				}
				if (motors.isContainerOpen()) {
					motors.containerClose();
				}
				reset = true;
				System.out.println("Fine!");
				// motors.stop();
				System.exit(0);
			}

			@Override
			public void keyReleased(Key k) {
				// TODO Auto-generated method stub

			}
		});
		Button.DOWN.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				if (motors.isBladeLow()) {
					motors.bladeLift();
				}
				if (motors.isContainerOpen()) {
					motors.containerClose();
				}
				reset = true;
			}

			@Override
			public void keyReleased(Key k) {
				return;
			}
		});

//		System.out.println("Reset Arduino");
//		Sensors.resetArduino();
//		Thread.sleep(5000);

		Sound.beepSequenceUp();
		System.out.println("INVIO calibra");
		Button.ENTER.waitForPress();

		// Memorizza l'istante di inizio del percorso
		startTime = new Date().getTime();
		motors.bladeSetZero();
		motors.containerSetZero();
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(sensors.getLuxL() - sensors.getLuxC());
		whiteMax = sensors.getLuxL();
		System.out.println("blackLevel=" + blackLevel);
		System.out.println("whiteMax=" + whiteMax);
		System.out.println("deltaMax=" + deltaMax);
		if (deltaMax < 15) {
			Sound.beepSequence();
			System.exit(0);
		}

		while (true) {
			reset = false;
			lineFollower();
			motors.stop();
		}
	}

	private static void lineFollower() throws InterruptedException {
		int greenLeft = 0, greenRight = 0, silver = 0;
		int[] speeds = new int[2];
//		double pidCorrection = 1;

		Sound.beepSequenceUp();
		System.out.println("INVIO parte");
		Button.ENTER.waitForPress();
		System.out.println("* Partito! *");
		pid = new PID(0, deltaMax);

		while (true) {
			// Se � stato richiesto il reset, esce dalla procedura per farla riavviare
			if (reset) {
				return;
			}

			// Controlla se c'� un ostacolo: nel caso lo aggira
			float distFwdLow = sensors.checkDistanceFwdLow();
			if (distFwdLow < OBSTACLE_DIST && distFwdLow > 0) {
				navigateObstacle();
				continue;
			}

			sensors.checkColors();
			if (Main.DEBUG) {
				System.out.println("Colors LR: " + sensors.getColorsLR());
			}

			// Verifica se si � persa la pista e, se necessario, torna indietro per cercarla
			if (sensors.isAnyBlack()) {
				silver = Math.max(silver - 1, 0);
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				System.out.println("Cerca linea");
				motors.spin(Motors.BASE_SPEED, -10);
				sensors.checkColors();
				if (!sensors.isAnyBlack()) {
					motors.spin(Motors.BASE_SPEED, 20);
					sensors.checkColors();
					if (!sensors.isAnyBlack()) {
						motors.spin(Motors.BASE_SPEED, -10);
						motors.travel(Motors.MAX_SPEED, -NO_BLACK_DIST - 5);
					}
				}

			}

			// Seguilinea con il PID
			speeds = pid.getSpeed(sensors.getDelta(whiteMax));
			// Rallenta se � in salita o discesa
//			sensors.checkGyro();
//			if (Main.DEBUG) {
//				System.out.println("GyroY=" + sensors.getGyroY());
//			}
//
//			pidCorrection = Math.max(0.75, 1 - (Math.abs(sensors.getGyroY()) / 80));
//			for (int i = 0; i < speeds.length; i++) {
//				speeds[i] = (int) Math.round(speeds[i] * pidCorrection);
//			}

			switch (sensors.getColorsLR()) {
			case "ss":
			case "sw":
			case "ws":
				silver++;
				motors.drive(speeds[0], speeds[1]);
				break;
			case "ww":
				// Rettilineo, decrementa prenotazioni verde
				greenLeft = Math.max(greenLeft - 1, 0);
				greenRight = Math.max(greenRight - 1, 0);
				motors.drive(speeds[0], speeds[1]);
				// Se le misure confermano, passa alla modalit� zona vittime
				if (silver > 0) {
//					int distSide = sensors.checkDistanceSide();
//					if (distSide < 110 && distFront < 110) {
					if (sensors.checkDistanceFwdHigh() < 110) {
						evacuationZone();
						sensors.setRescueLineMode();
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
					System.out.println("Inversione di marcia");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, 135);
					do {
						sensors.checkColors();
						motors.drive(-Motors.BASE_SPEED, Motors.BASE_SPEED);
					} while (!sensors.getColorL().equals("b"));
					do {
						sensors.checkColors();
						motors.drive(-Motors.BASE_SPEED, Motors.BASE_SPEED);
					} while (!sensors.getColorL().equals("w"));
					motors.resetTachoCount();
					greenLeft = 0;
					greenRight = 0;
				} else if (greenLeft > 0) {
					// Curva a sinistra
					System.out.println("Curva a sinistra");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, 25);
					do {
						sensors.checkColors();
						motors.drive(-Motors.BASE_SPEED, Motors.BASE_SPEED);
					} while (!sensors.getColorC().equals("b"));
					motors.resetTachoCount();
					greenLeft = 0;
				} else if (greenRight > 0) {
					// Curva a destra
					System.out.println("Curva a destra");
					motors.stop();
					motors.spin(Motors.BASE_SPEED, -25);
					do {
						sensors.checkColors();
						motors.drive(Motors.BASE_SPEED, -Motors.BASE_SPEED);
					} while (!sensors.getColorC().equals("b"));
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

	private static void evacuationZone() throws InterruptedException {
		// zoneOrientation: 0 � larga (120x90), 1 � lunga (90x120)
		// safePosition: 0 � l'angolo in basso a sinistra, gli altri in senso orario
		// gatePosition: da 0 a 3, da sinistra a destra
		int zoneOrientation = -1, safePosition = -1, gatePosition = -1;
//		loaded = false;

		System.out.println("* Zona Vittime *");
		motors.stop();
		Sound.playTone(440, 500); // LA4
		Sound.playTone(523, 1000); // DO5
//		do {
//			sensors.checkColors();
//			motors.drive(-Motors.MAX_SPEED, -Motors.MAX_SPEED);
//		} while (!sensors.isAnySilver());
//		motors.stop();
//		motors.travel(Motors.BASE_SPEED, -5);
//		System.out.println("Raddrizza");
//		for (int i = 1; i <= 20; i++) {
//			int signum = i % 2 == 0 ? 1 : -1;
//			motors.spin(Motors.BASE_SPEED, signum * i * 5);
//			sensors.checkColors();
//			if(sensors.isAcrossLine()) {
//				break;
//			}
//		}

		// Avanza per far entrare tutto il robot nella zona
		motors.travel(Motors.MAX_SPEED, 20);
		motors.stop();

		// Imposta Arduino in modalit� zona vittime
		sensors.setEvacuationZoneMode();

		// Individua la forma della zona vittime
		int d = sensors.checkDistanceFwdHigh();
		if (d < 75) {
			zoneOrientation = 0;
		} else {
			zoneOrientation = 1;
		}
		System.out.println("Orientation=" + zoneOrientation);
		for (int i = 0; i <= zoneOrientation; i++) {
			Sound.playTone(440, 200);
			Thread.sleep(100);
		}

		// Individua la posizione dell'ingresso
		motors.spin(Motors.BASE_SPEED, 90);
		motors.stop();
		int d1 = sensors.checkDistanceFwdHigh();
		if (d1 < 25) {
			gatePosition = 0;
		} else if (d1 < 55) {
			gatePosition = 1;
		} else if (d1 < 85) {
			gatePosition = 2;
		} else {
			gatePosition = 3;
		}
		System.out.println("gatePosition=" + gatePosition);
		for (int i = 0; i <= gatePosition; i++) {
			Sound.playTone(880, 200);
			Thread.sleep(100);
		}

//		if (gatePosition == 0) {
//			motors.spin(Motors.MAX_SPEED, -90);
//			alignBackwards();
//		} else if (gatePosition == 3 && zoneOrientation == 0 || gatePosition == 2 && zoneOrientation == 1) {
//			motors.spin(Motors.MAX_SPEED, 90);
//			alignBackwards();
//		} else if (gatePosition == 2 && zoneOrientation == 0) {
//			motors.stop();
//		} else if (gatePosition == 1) {
//			motors.spin(Motors.MAX_SPEED, 90);
//		}

		// Fa tutto il perimetro per trovare la zona vittime
		// Mentre il robot cammina, verifica:
		// - Se c'� una pallina sul percorso (nel caso, la raccoglie)
		// - Se nell'angolo c'� la zona sicura (nel caso, memorizza la posizione)
		// Ripete per ogni parete finch� non trova la zona sicura
		int sidesExplored = 0;
		if (gatePosition == 0) {
			motors.spin(Motors.BASE_SPEED, -90);
			sidesExplored++;
		}
		do {
			System.out.println("Side #" + sidesExplored);
			motors.bladeLower(); // Modifica 12-4-19
			// Va avanti finch� non trova la parete
			while (sensors.checkDistanceFwdHigh() > 13) {
				if (motors.isBladeLow() && sensors.checkDistanceFwdHigh() < 52) { // Modifica 12-4-19
					motors.bladeLift(); // Modifica 12-4-19
				} // Modifica 12-4-19
				if (reset) {
					return;
				}
				sensors.checkTouches();
				if (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed()) {
					// Se ha toccato con i sensori senza arrivare al muro, ha trovato la zona sicura
					safePosition = sidesExplored;
					System.out.println("Safe=" + safePosition);
					break;
				} else if (sensors.isFwdLeftPressed()) {
					// Se uno dei sensori ha toccato, fa allineare l'altro
					motors.drive(-Motors.BASE_SPEED / 2, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, -Motors.BASE_SPEED / 2);
//				} else if (sensors.checkDistanceFwdLow() < 7) {
//					// Si sta avvicinando a una pallina: si ferma per raccoglierla
//					System.out.println("Pallina");
//					motors.travel(Motors.MAX_SPEED, -3);
//					motors.bladeLower();
//					motors.travel(Motors.MAX_SPEED, 10);
//					motors.bladeLift();
//					loaded = true;
				} else {
					motors.drive(Motors.MAX_SPEED, Motors.MAX_SPEED);
				}
			}
			if (safePosition < 0) {
				// Se non ha trovato la zona sicura, gira a destra di 90 gradi e riparte
				motors.spin(Motors.BASE_SPEED, -90);
				if (sidesExplored != 0 || gatePosition != 0) {
					alignBackwards();
					if (reset) {
						return;
					}
				}
				sidesExplored++;
			}
		} while (safePosition < 0 && sidesExplored < 4);

		// Trovato la zona sicura: fa inversione e vi si appoggia
		motors.travel(Motors.MAX_SPEED, -5);
		motors.spin(Motors.BASE_SPEED, 180);
		alignBackwards();
		unloadVictims(); // Modifica 12-4-19
		if (reset) {
			return;
		}
		// Se ci sono delle palline a bordo fa la manovra di scarico
//		if (loaded) {
//			unloadVictims();
//		}
		motors.travel(Motors.MAX_SPEED, 5);

		// Una volta trovata la zona sicura, parte con le spazzate in orizzontale
		// Il robot si mette in posizione per iniziare le spazzate
		int signum = safePosition % 2 == 0 ? 1 : -1;
		motors.spin(Motors.BASE_SPEED, signum * -45);

		// Ogni spazzata a circa 20cm di distanza dall'altra
		// Ad ogni spazzata raccoglie le palline e le riporta nella zona sicura
		// Se manca meno di un minuto esce dal ciclo e cerca l'uscita
		boolean lastRun = false;
		for (int i = 0; (i < 2 + zoneOrientation) && (new Date().getTime() < startTime + 7 * 60 * 1_000); i++) {
			System.out.println("Spazzata #" + (i + 1));
			// Percorre la zona all'andata
			driveUntilWall();
			if (reset) {
				return;
			}
			// Inversione di marcia
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.bladeLower();
			motors.travel(Motors.MAX_SPEED, 15);
			// Recupera eventuali palline
			motors.bladeLift();
			// Flag per uscire quando � troppo vicino al bordo della zona vittime
			if (sensors.checkDistanceFwdHigh() < 50) { // Modifica 12-4-19, era 40
				System.out.println("Ultima spazzata");
				lastRun = true;
			}
			motors.spin(Motors.BASE_SPEED, signum * 90);
			alignBackwards();
			// Percorre la zona al ritorno
			driveUntilWall();
			// Manovra per scaricare: si appoggia alla zona sicura e scarica palline
			motors.spin(Motors.BASE_SPEED, signum * -90);
//			if (loaded) {
			alignBackwards();
			if (reset) {
				return;
			}
			unloadVictims();

			// Se ha completato i passaggi, smette di spazzare
			if (lastRun || i == 1 + zoneOrientation) {
				if (reset) {
					return;
				}
				break;
			}

			// Si prepara per un'altra spazzata
			motors.bladeLower();
			motors.travel(Motors.MAX_SPEED, 5);
			motors.spin(Motors.BASE_SPEED, signum * 45);
			motors.travel(Motors.MAX_SPEED, 15 + i * 30);
			// Recupera eventuali palline
			motors.bladeLift();
//			} else if (i == 2 + zoneOrientation) {

			motors.travel(Motors.MAX_SPEED, 15);
			motors.spin(Motors.BASE_SPEED, signum * -90);
		}

		// Uscita dalla zona vittime
		motors.resetTachoCount();
		switch (safePosition) {
		case 0:
			motors.spin(Motors.BASE_SPEED, -45);
			motors.travel(Motors.MAX_SPEED, gatePosition * 30);
			motors.spin(Motors.BASE_SPEED, -90);
			break;
		case 1:
			motors.spin(Motors.BASE_SPEED, -45);
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				motors.drive(Motors.MAX_SPEED, Motors.MAX_SPEED);
				if(motors.getTachoCount() > (60+30*zoneOrientation) * Motors.COEFF_CM) {
					break;
				}
			}
			if (gatePosition > 0) {
				motors.spin(Motors.BASE_SPEED, 90);
				motors.travel(Motors.MAX_SPEED, gatePosition * 30);
				motors.spin(Motors.BASE_SPEED, -90);
			}
			break;
		case 2:
			motors.spin(Motors.BASE_SPEED, 45);
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				motors.drive(Motors.MAX_SPEED, Motors.MAX_SPEED);
				if(motors.getTachoCount() > (60+30*zoneOrientation) * Motors.COEFF_CM) {
					break;
				}
			}
			if (gatePosition < 3) {
				motors.spin(Motors.BASE_SPEED, -90);
				motors.travel(Motors.MAX_SPEED, (3 - gatePosition - zoneOrientation) * 30);
				motors.spin(Motors.BASE_SPEED, 90);
			}
			break;
		case 3:
			motors.spin(Motors.BASE_SPEED, 45);
			motors.travel(Motors.MAX_SPEED, (3 - gatePosition) * 30);
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
		motors.travel(Motors.MAX_SPEED, 10);
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
	private static void navigateObstacle() {
		motors.stop();
		System.out.println("Ostacolo");
		motors.travel(Motors.BASE_SPEED, -2);

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
		int direction = 1; // -1 = right, 1 = left
		motors.spin(Motors.BASE_SPEED, 90);
		if (sensors.checkDistanceFwdLow() < 30) {
			motors.spin(Motors.BASE_SPEED, 180);
			direction = -1;
		}
		motors.travel(Motors.MAX_SPEED, 15);
		motors.spin(Motors.BASE_SPEED, -90 * direction);
		if (sensors.checkDistanceFwdLow() < 40) {
			motors.spin(Motors.BASE_SPEED, -90 * direction);
			motors.travel(Motors.MAX_SPEED, 30);
			motors.spin(Motors.BASE_SPEED, 90 * direction);
			direction = -1;
		}
		if (reset) {
			return;
		}
		motors.travel(Motors.MAX_SPEED, 39);
		motors.spin(Motors.BASE_SPEED, -90 * direction);
		if (reset) {
			return;
		}
//		motors.travel(Motors.BASE_SPEED, 15);
		do {
			sensors.checkColors();
			motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
		} while (!sensors.getColorL().equals("b") && !sensors.getColorR().equals("b"));
//		motors.spin(Motors.BASE_SPEED, 90 * direction);
		do {
			sensors.checkColors();
			motors.drive(-direction * Motors.BASE_SPEED, direction * Motors.BASE_SPEED);
		} while (!sensors.getColorC().equals("b"));
		motors.resetTachoCount();
	}

	// Allineamento al muro
	private static void alignBackwards() {
		System.out.println("Allineamento");
		motors.resetTachoCount();
		while (motors.getTachoCount() < 50 * Motors.COEFF_CM) {
			if (reset) {
				return;
			}
			sensors.checkTouches();
			if (sensors.isBackLeftPressed() && sensors.isBackRightPressed()) {
				motors.travel(Motors.BASE_SPEED, -2);
				// sensors.resetGyro();
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

	// Allineamento al muro
	private static void alignForward() {
		System.out.println("Allineamento");
		motors.resetTachoCount();
		while (motors.getTachoCount() < 10 * Motors.COEFF_CM) {
			if (reset) {
				return;
			}
			sensors.checkTouches();
			if (sensors.isBackLeftPressed() && sensors.isBackRightPressed()) {
				motors.travel(Motors.BASE_SPEED, 2);
				// sensors.resetGyro();
				return;
			} else if (sensors.isFwdLeftPressed()) {
				motors.drive(Motors.BASE_SPEED / 2, Motors.BASE_SPEED);
			} else if (sensors.isFwdRightPressed()) {
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED / 2);
			} else {
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
		}
	}

	// Va avanti finch� non trova la parete
	private static void driveUntilWall() {
		System.out.println("Va fino al muro");
		motors.bladeLower();
		while (sensors.checkDistanceFwdHigh() > 25) {
			if (reset) {
				return;
			}
			motors.drive(Motors.MAX_SPEED, Motors.MAX_SPEED);
//			}
		}
		motors.bladeLift();
		alignForward();
		motors.travel(Motors.MAX_SPEED, -7);
	}

	// Scarica palline
	private static void unloadVictims() {
		System.out.println("Scarica");
		motors.containerOpen();
		for (int i = 0; i < 2; i++) {
			motors.travel(Motors.MAX_SPEED, 4);
			motors.drive(-Motors.MAX_SPEED, -Motors.MAX_SPEED);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		motors.containerClose();
	}
}