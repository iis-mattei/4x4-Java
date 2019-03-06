package Robot;

import java.util.Date;
import lejos.hardware.Button;

public class Main {
	static final float OBSTACLE_DIST = 0.08f;	// In metri
	static final int NO_BLACK_DIST = 25;	// In giri

	static Sensors sensors = new Sensors();
	static Motors motors = new Motors();
	static PID pid;
	static int deltaMax = 0;
	static boolean greenLeft, greenRight;
	static float posX, posY;
	static int angle, zoneX, zoneY;

	public static void evacuationZone() {
		// Misuro le distanze
		posX = sensors.checkDistanceFwdHigh();
		posY = sensors.checkDistanceSide();
		
		// Individuo la forma della zona vittime
		if(posX < 90) {
			zoneX = 120;
			zoneY = 90;
		} else {
			zoneX = 90;
			zoneY = 120;
		}
		
		// Va avanti fino alla parete opposta della zona vittime
		motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
		float lastDistance = sensors.checkDistanceFwdHigh();
		do {
			sensors.checkTouches();
			float newDistance = sensors.checkDistanceFwdHigh(); 
			if(lastDistance == newDistance) {
				// Se viene bloccato da una pallina, la raccoglie e continua
				motors.travel(Motors.BASE_SPEED, -10);
				motors.bladeLower();
				motors.travel(Motors.BASE_SPEED, 15);
				motors.bladeLift();
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
			} else {
				lastDistance = newDistance;
			}
			// Se uno dei sensori ha toccato, faccio allineare l'altro
			if(sensors.isFwdLeftPressed()) {
				motors.drive(0, Motors.BASE_SPEED);
			} else if(sensors.isFwdRightPressed()) {
				motors.drive(Motors.BASE_SPEED, 0);
			} 
		} while (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed());
		
		// Se ho trovato la zona vittime...
		
		// Se ho trovato la parete...
		
		// Vado al centro per cercare la zona vittime
		
		// Vado al centro per cercare le palline
		
		System.exit(0);
	}

	public static void bypassObstacle() {
		motors.spin(Motors.BASE_SPEED, 45);
		motors.arc(Motors.BASE_SPEED, 30, 30 * Math.PI);
		motors.spin(Motors.BASE_SPEED, -45);
		motors.resetTachoCount();
	}

	public static void seekLine() {
		motors.travel(Motors.BASE_SPEED, -NO_BLACK_DIST-5);
	}
	
	public static void mainLoop() {
		int[] speeds = new int[2];
		pid = new PID(deltaMax);
		System.out.println("\n***************\nSto partendo...");

		while (true) {
			if (Button.ESCAPE.isDown()) {
				// Termina il programma
				System.exit(0);
			}
			if(Button.DOWN.isDown()) {
				// Riavvia il ciclo principale
				return;
			}
//			if (sensors.isSilver()) {
//				evacuationZone();
//			}

			float distFwdLow = sensors.checkDistanceFwdLow();
//			System.out.println((new Date()).getTime() + "\tdistFwdLow=" + distFwdLow);
			if (distFwdLow < OBSTACLE_DIST) {
				System.out.println((new Date()).getTime() + "\tAggiramento ostacolo");
				bypassObstacle();
				continue;
			}

			sensors.checkColors();
			System.out.println((new Date()).getTime() + "\tLR: " + sensors.getColorsLR() + "\tC: " + sensors.getColorC());
			if (sensors.isAnyBlack()) {
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				// Misuro la distanza percorsa in giri ruota (con il tachoCount)
				seekLine();
			}

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
					System.out.println((new Date()).getTime() + "\tIncrocio a T");
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
					motors.travel(Motors.BASE_SPEED, 1);
//					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
//					Thread.sleep(1000);
					greenLeft = false;
				} else if (greenRight) {
					// Curva a destra
					System.out.println((new Date()).getTime() + "\tCurva a destra");
					motors.spin(Motors.BASE_SPEED, -90);
					motors.travel(Motors.BASE_SPEED, 1);
//					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
//					Thread.sleep(1000);
					greenRight = false;
				} else if (greenLeft && greenRight) {
					// Inversione di marcia
					System.out.println((new Date()).getTime() + "\tInversione di marcia");
					motors.spin(Motors.BASE_SPEED, 180);
					motors.travel(Motors.BASE_SPEED, 1);
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

	public static void main(String args[]) throws InterruptedException {
		// All'inizio la pinza è abbassata: la alziamo e la lasciamo bloccata su
		motors.bladeLift();
		
		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
		System.out.println("Calibrazione in corso...");
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(((sensors.getLuxL() + sensors.getLuxR()) / 2) - sensors.getLuxC());
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);

		while(true) {
			mainLoop();
			System.out.println("Premi per partire...");
			Button.waitForAnyPress();
		}
		
	}
}
