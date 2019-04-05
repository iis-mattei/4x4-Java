package Robot;

import java.util.Date;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class Main {
	static final int OBSTACLE_DIST = 7; // In cm
	static final int NO_BLACK_DIST = 25; // In cm

	static Sensors sensors = new Sensors();
	static Motors motors = new Motors();
	static PID pid;

	static int[] speeds = new int[2];
	static int deltaMax;
	static long startTime;

	public static void main(String args[]) {
		init();
		while (true) {
			lineFollower();
		}
	}

	public static void init() {
		Sound.beepSequenceUp();
		System.out.println("Premi per calibrare");
		Button.waitForAnyPress();
		// Memorizza l'istante di inizio del percorso
		startTime = new Date().getTime();
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(((sensors.getLuxL() + sensors.getLuxR()) / 2) - sensors.getLuxC());
		pid = new PID(0, deltaMax);
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);
	}

	public static void lineFollower() {
		int greenLeft = 0, greenRight = 0;
		boolean silver = false;

		Sound.beepSequenceUp();
		System.out.println("Premi per partire");
		Button.waitForAnyPress();
		System.out.println("\n*** Partito ***");

		while (true) {
			// Per terminare il programma
			if (Button.ESCAPE.isDown()) {
				motors.stop();
				System.exit(0);
			}

			// Per riavviare il ciclo principale
			if (Button.DOWN.isDown()) {
				return;
			}

			// Controlla se c'è un ostacolo: nel caso lo aggira
			float distFwdLow = sensors.checkDistanceFwdLow();
			if (distFwdLow < OBSTACLE_DIST && distFwdLow > 0) {
				navigateObstacle();
				continue;
			}

			sensors.checkColors();
//			System.out.println((new Date()).getTime() + " - Colors LR: " + sensors.getColorsLR());

			// Verifica se si è persa la pista e, se necessario, torna indietro per cercarla
			if (sensors.isAnyBlack()) {
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				motors.travel(Motors.BASE_SPEED, -NO_BLACK_DIST - 5);
			}

			// Seguilinea con il PID
			speeds = pid.getSpeed(sensors.getDelta());
			switch (sensors.getColorsLR()) {
			case "ss":
				silver = true;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;
			case "sw":
			case "ws":
				silver = true;
				motors.drive(speeds[0], speeds[1]);
				break;
			case "ww":
				// Se le misure confermano, passa alla modalità zona vittime
				if (silver) {
					int distSide = sensors.checkDistanceSide();
					int distFront = sensors.checkDistanceFwdHigh();
					if (distSide < 110 && distFront < 110 && distFront > 70) {
						evacuationZone();
						// All'uscita dalla zona vittime si torna al seguilinea
						sensors.setRescueLineMode();
					}
				}
				// Rettilineo, azzera prenotazioni verde
				if (greenLeft > 0) {
					greenLeft--;
				} else if (greenRight > 0) {
					greenRight--;
				}
				motors.drive(speeds[0], speeds[1]);
				break;

			case "wb":
			case "bw":
				silver = false;
				if (sensors.getColorC().equals("b")) {
					// Incrocio a T
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				} else {
					// Curva normale
					motors.drive(speeds[0], speeds[1]);
				}
				break;

			case "bb":
				silver = false;
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
				silver = false;
				// Prenota curva a sinistra e va dritto
				greenLeft++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "wg":
			case "bg":
				silver = false;
				// Prenota curva a destra e va dritto
				greenRight++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "gg":
				silver = false;
				// Prenota inversione di marcia e va dritto
				greenLeft++;
				greenRight++;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;
			}
		}
	}

	public static void evacuationZone() {
		System.out.println("Zona Vittime");
//		System.exit(0);
		// zoneOrientation: 0 è larga (120x90), 1 è lunga (90x120)
		// safePosition: 0 è l'angolo in basso a sinistra, gli altri in senso orario
		// gatePosition: da 0 a 3, da sinistra a destra
		int zoneOrientation = -1, safePosition = -1, gatePosition = -1;
		boolean loaded = false;

		// Imposto Arduino in modalità zona vittime
		sensors.setEvacuationZoneMode();
		System.out.println("***Zona vittime***");

		// Avanzo fino a far entrare tutto il robot nella zona
		motors.travel(Motors.BASE_SPEED, 10);

		// Individuo la forma della zona vittime
		if (sensors.checkDistanceFwdHigh() < 90) {
			zoneOrientation = 0;
		} else {
			zoneOrientation = 1;
		}
		System.out.println("zoneOrientation=" + zoneOrientation);

		// Individuo la posizione dell'ingresso
		float d = sensors.checkDistanceSide();
		if (d < 30) {
			gatePosition = 3;
		} else if (d < 60) {
			gatePosition = 2;
		} else if (d < 90) {
			gatePosition = 1;
		} else {
			gatePosition = 0;
		}
		System.out.println("gatePosition=" + gatePosition);

		// Faccio tutto il perimetro per trovare la zona vittime
		// Se non sono entrato dall'angolo sinistro, mi giro per seguire la parete
		if (gatePosition > 0) {
			// Giro a sinistra e cammino per spostarmi dall'ingresso
			motors.spin(Motors.BASE_SPEED, 90);
			motors.travel(Motors.BASE_SPEED, 20);
		}

		// Vado avanti fino alla parete
		// Qui potrei usare il PID per mantenere una distanza costante dalla parete...
		// Mentre il robot cammina devo verificare:
		// Se c'è una pallina sul percorso (nel caso, la raccolgo)
		// Se nell'angolo c'è la zona sicura (nel caso, memorizzo la posizione)
		int sidesExplored = 0;
		do { // Ripeto per ogni parete, finché non trovo la zona sicura
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) { // Vado avanti finché non trovo la parete
				if (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed()) {
					// Se ho toccato con i sensori senza arrivare al muro, ho trovato la zona sicura
					safePosition = sidesExplored;
					break;
				} else if (sensors.isFwdLeftPressed()) {
					// Se uno dei sensori ha toccato, faccio allineare l'altro
					motors.drive(0, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, 0);
				} else if (sensors.checkDistanceFwdLow() < 5 && sensors.checkDistanceFwdHigh() > 40) {
					// Mi sto avvicinando a una pallina: mi fermo per raccoglierla
					motors.travel(Motors.BASE_SPEED, -5);
					motors.bladeLower();
					motors.travel(Motors.BASE_SPEED, 10);
					motors.bladeLift();
					loaded = true;
				} else {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				}
			}
			if (safePosition < 0) {
				// Se non ho trovato la zona sicura, giro a destra di 90 gradi e riparto
				motors.spin(Motors.BASE_SPEED, -90);
			}
		} while (safePosition < 0 && sidesExplored < 4);

		motors.travel(Motors.BASE_SPEED, -5);
		motors.spin(Motors.BASE_SPEED, 180);
		motors.travel(Motors.BASE_SPEED, -10);
		// Se abbiamo delle palline, facciamo la manovra di scarico
		if (loaded) {
			motors.containerOpen();
			motors.travel(Motors.BASE_SPEED, 1);
			motors.travel(Motors.BASE_SPEED, -2);
			motors.containerClose();
		}
		motors.travel(Motors.BASE_SPEED, 10);

		// Una volta trovata la zona sicura, parto con le spazzate in orizzontale
		// Il robot si mette in posizione per iniziare le spazzate
		int signum = safePosition % 2 == 0 ? 1 : -1;
		motors.spin(Motors.BASE_SPEED, -signum * 45);

		// Ogni spazzata a circa 20cm di distanza dall'altra
		// Ad ogni spazzata raccolgo le palline e le riporto nella zona sicura
		// Se manca meno di un minuto esco dal ciclo e cerco l'uscita
		motors.bladeLower();
		for (int i = 0; (i < 3 + zoneOrientation) && (new Date().getTime() < startTime + 7 * 60 * 1000); i++) {
			// Percorro la zona all'andata
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				// Vado avanti finché non trovo la parete
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
			// Recupero eventuali palline
			motors.bladeLift();
			// Inversione di marcia
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.travel(Motors.BASE_SPEED, 15);
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.bladeLower();
			// Percorro la zona al ritorno
			while (sensors.checkDistanceFwdHigh() > OBSTACLE_DIST) {
				// Vado avanti finché non trovo la parete
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
			// Recupero eventuali palline
			motors.bladeLift();
			// Manovra per scaricare
			motors.spin(Motors.BASE_SPEED, signum * 90);
			// Mi appoggio alla zona sicura
			while (!sensors.isFwdLeftPressed() || !sensors.isFwdRightPressed()) {
				if (sensors.isFwdLeftPressed()) {
					motors.drive(0, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, 0);
				} else {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				}
			}
			// Scarico palline
			motors.travel(Motors.BASE_SPEED, -5);
			motors.spin(Motors.BASE_SPEED, 180);
			motors.travel(Motors.BASE_SPEED, -10);
			motors.containerOpen();
			motors.travel(Motors.BASE_SPEED, 1);
			motors.travel(Motors.BASE_SPEED, -2);
			motors.containerClose();
			if (i == 2 + zoneOrientation) {
				// Se ho completato i passaggi, finisco di spazzare
				break;
			}
			// Mi preparo per un'altra spazzata
			motors.bladeLower();
			motors.spin(Motors.BASE_SPEED, signum * 45);
			motors.travel(Motors.BASE_SPEED, 30 + i * 30);
			motors.spin(Motors.BASE_SPEED, -signum * 90);
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
			// Possibile solo se zoneOrientation == 0
			motors.spin(Motors.BASE_SPEED, 45);
			motors.travel(Motors.BASE_SPEED, (3 - gatePosition) * 30);
			motors.spin(Motors.BASE_SPEED, 90);
			break;
		}
		motors.travel(Motors.BASE_SPEED, 30);
		boolean foundBlack = false;
		for (int i = 0; i < 7; i++) {
			if (sensors.isAnyBlack()) {
				foundBlack = true;
				break;
			}
			motors.spin(Motors.BASE_SPEED, 10);
		}
		if (!foundBlack) {
			for (int i = 0; i < 14; i++) {
				if (sensors.isAnyBlack()) {
					foundBlack = true;
					break;
				}
				motors.spin(Motors.BASE_SPEED, -10);
			}
		}
		motors.resetTachoCount();
		// Torna al seguilinea
		return;
	}

	/***** Procedure di servizio *****/

	// Aggiramento ostacolo
	public static void navigateObstacle() {
		motors.stop();

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

}
