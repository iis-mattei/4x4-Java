package Robot;

import java.util.Date;
import lejos.hardware.Button;

public class Main {
	static final float OBSTACLE_DIST = 0.08f; // In metri
	static final int NO_BLACK_DIST = 25; // In cm

	static Sensors sensors = new Sensors();
	static Motors motors = new Motors();
	static PID pid;

	static int[] speeds = new int[2];
	static int deltaMax;

	public static void main(String args[]) {
		init();

		while (true) {
			lineFollower();
		}

	}

	public static void init() {
		// All'inizio la pinza è abbassata: la alza e la lascia bloccata in alto
		motors.bladeLift();

		System.out.println("Premi per calibrare...");
		Button.waitForAnyPress();
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(((sensors.getLuxL() + sensors.getLuxR()) / 2) - sensors.getLuxC());
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);
	}

	public static void lineFollower() {
		boolean greenLeft = false, greenRight = false;

		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
		pid = new PID(0, deltaMax);
		System.out.println("\n***************Sto partendo...");

		while (true) {
			if (Button.ESCAPE.isDown()) {
				// Termina il programma
				System.exit(0);
			}
			if (Button.DOWN.isDown()) {
				// Riavvia il ciclo principale
				return;
			}
//			if (sensors.isSilver()) {
//				evacuationZone();
//			}

			// Controlla se c'è un ostacolo: nel caso lo aggira
			float distFwdLow = sensors.checkDistanceFwdLow();
			if (distFwdLow < OBSTACLE_DIST) {
				System.out.println((new Date()).getTime() + "\tAggiramento ostacolo");
				motors.spin(Motors.BASE_SPEED, 45);
				motors.arc(Motors.BASE_SPEED, 30, 30 * Math.PI);
				motors.spin(Motors.BASE_SPEED, -45);
				motors.resetTachoCount();
				continue;
			}

			sensors.checkColors();

			// Calcola per quanto cammino non si è visto nero
			// Misura la distanza percorsa in giri ruota (con il tachoCount)
			if (sensors.isAnyBlack()) {
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				// Se necessario, attiva la procedura per ritrovare la linea nera
				motors.travel(Motors.BASE_SPEED, -NO_BLACK_DIST - 5);
			}

			// Seguilinea con il PID
			speeds = pid.getSpeed(sensors.getDelta());
			switch (sensors.getColorsLR()) {
			case "ww":
				// Rettilineo, azzero prenotazioni verde
				greenLeft = false;
				greenRight = false;
				motors.drive(speeds[0], speeds[1]);
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
				if (!greenLeft && !greenRight) {
					// Segui la linea
					motors.drive(speeds[0], speeds[1]);
				} else if (greenLeft) {
					// Curva a sinistra
					System.out.println((new Date()).getTime() + "\tCurva a sinistra");
					motors.spin(Motors.BASE_SPEED, 90);
					motors.travel(Motors.BASE_SPEED, 2);
					greenLeft = false;
				} else if (greenRight) {
					// Curva a destra
					System.out.println((new Date()).getTime() + "\tCurva a destra");
					motors.spin(Motors.BASE_SPEED, -90);
					motors.travel(Motors.BASE_SPEED, 2);
					greenRight = false;
				} else if (greenLeft && greenRight) {
					// Inversione di marcia
					System.out.println((new Date()).getTime() + "\tInversione di marcia");
					motors.spin(Motors.BASE_SPEED, 180);
					motors.travel(Motors.BASE_SPEED, 6);
					greenLeft = false;
					greenRight = false;
				}
				break;

			case "gw":
				// Prenoto curva a sinistra e vado dritto
				greenLeft = true;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "wg":
				// Prenoto curva a destra e vado dritto
				greenRight = true;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "gg":
				// Prenoto inversione di marcia e vado dritto
				greenLeft = true;
				greenRight = true;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;
			}
		}
	}

	public static void evacuationZone() {
		// Le coordinate partono dall'angolo in basso a sinistra, come assi cartesiani
		// zoneOrientation: 0 è larga (120x90), 1 è lunga (90x120)
		// safePosition: 0 è l'angolo in basso a sinistra, gli altri in senso orario
		int zoneOrientation = -1, safePosition = -1;

		// Avanzo fino a far entrare tutto il robot nella zona
		motors.travel(Motors.BASE_SPEED, 15);

		// Individuo la forma della zona vittime
		if (sensors.checkDistanceFwdHigh() < 90) {
			zoneOrientation = 0;
		} else {
			zoneOrientation = 1;
		}

		// Faccio tutto il perimetro per trovare la zona vittime
		// Giro a sinistra e cammino per spostarmi dall'ingresso
		motors.spin(Motors.BASE_SPEED, 90);
		motors.travel(Motors.BASE_SPEED, 20);

		// Vado avanti fino alla parete
		// Qui potrei usare il PID per mantenere una distanza costante dalla parete...
		// Mentre il robot cammina devo verificare:
		// Se c'è una pallina sul percorso (nel caso, la raccolgo)
		// Se nell'angolo c'è la zona sicura (nel caso, memorizzo la posizione)
		pid = new PID(5, 5);
		int sidesExplored = 0;
		do {
			while (sensors.checkDistanceFwdHigh() > 5) {
				if (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed()) {
					// Se ho toccato con i sensori senza arrivare al muro, ho trovato la zona sicura
					safePosition = sidesExplored;
					break;
				}
				if (sensors.checkDistanceFwdLow() < 5) {
					if (sensors.checkDistanceFwdHigh() > 15) {
						// Mi sto avvicinando a una pallina: mi fermo per raccoglierla
						motors.travel(Motors.BASE_SPEED, -5);
						motors.bladeLower();
						motors.travel(Motors.BASE_SPEED, 10);
						motors.bladeLift();
					} else if (sensors.isFwdLeftPressed()) {
						// Se uno dei sensori ha toccato, faccio allineare l'altro
						motors.drive(0, Motors.BASE_SPEED);
					} else if (sensors.isFwdRightPressed()) {
						motors.drive(Motors.BASE_SPEED, 0);
					}

				}
				speeds = pid.getSpeed(sensors.checkDistanceSide());
				motors.drive(speeds[0], speeds[1]);
			}
			// Giro a destra di 90 gradi e riparto
			motors.spin(Motors.BASE_SPEED, -90);
		} while (safePosition < 0 && sidesExplored < 4);

		// Una volta trovata la zona sicura, parto con 6 spazzate dal lato corto
		// Il robot si mette in posizione per iniziare le spazzate
		if (zoneOrientation == 0 && (safePosition == 0 || safePosition == 2)
				|| zoneOrientation == 1 && (safePosition == 1 || safePosition == 3)) {
			while (sensors.checkDistanceFwdHigh() > 5) {
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			}
			motors.spin(Motors.BASE_SPEED, 45);
		} else {
			motors.spin(Motors.BASE_SPEED, 135);
		}
		
		// Ogni spazzata a circa 20cm di distanza dall'altra
		// Uso sempre il PID per mantenere la distanza dalla parete
		// Ad ogni spazzata raccolgo le palline e le riporto nella zona sicura

		System.exit(0);
	}

}
