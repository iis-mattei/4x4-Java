package Robot;

import java.util.Date;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class Main {
	static final float OBSTACLE_DIST = 0.05f; // In metri
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
		// All'inizio la pinza è abbassata: la alza e la lascia bloccata in alto
		motors.bladeLift();
		motors.containerClose();

		Sound.beepSequenceUp();
		System.out.println("Premi per calibrare...");
		Button.waitForAnyPress();
		// Memorizzo l'istante di inizio del percorso
		startTime = new Date().getTime();
		int blackLevel = sensors.detectBlack();
		sensors.checkColors();
		deltaMax = (int) Math.round(((sensors.getLuxL() + sensors.getLuxR()) / 2) - sensors.getLuxC());
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);
	}

	public static void lineFollower() {
		boolean greenLeft = false, greenRight = false;

		Sound.beepSequenceUp();
		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
		pid = new PID(0, deltaMax);
		System.out.println("\n***************Sto partendo...");

		while (true) {
			if (Button.ESCAPE.isDown()) {
				// Termina il programma
				motors.bladeLower();
				motors.containerOpen();
				System.exit(0);
			}
			if (Button.DOWN.isDown()) {
				// Riavvia il ciclo principale
				return;
			}
			if (sensors.isSilver() && (sensors.checkDistanceFwdHigh() < 120 || sensors.checkDistanceFwdHigh() > 70)) {
				// Passo alla modalità zona vittime
				evacuationZone();
				sensors.setRescueLineMode();
			}

			// Controlla se c'è un ostacolo: nel caso lo aggira
			float distFwdLow = sensors.checkDistanceFwdLow();
			if (distFwdLow < OBSTACLE_DIST) {
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
				System.out.println((new Date()).getTime() + "\tAggiramento ostacolo");
				motors.spin(Motors.BASE_SPEED, -90);
				float obstacleDist = sensors.checkDistanceSide();
				while (sensors.checkDistanceSide() < obstacleDist + 0.05) {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				}
				motors.travel(Motors.BASE_SPEED, 10);
				motors.spin(Motors.BASE_SPEED, 90);
				motors.travel(Motors.BASE_SPEED, 10);
				while (sensors.checkDistanceSide() < obstacleDist + 0.05) {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				}
				motors.travel(Motors.BASE_SPEED, 10);
				motors.spin(Motors.BASE_SPEED, 90);
				sensors.checkColors();
				while (!sensors.isAnyBlack()) {
					motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
					sensors.checkColors();
				}
				motors.spin(Motors.BASE_SPEED, -45);
				motors.resetTachoCount();
				continue;
			}

			sensors.checkColors();
			System.out.println((new Date()).getTime() + " - Colors LR: " + sensors.getColorsLR());

			// Calcola per quanto cammino non si è visto nero
			// Misura la distanza percorsa in giri ruota (con il tachoCount)
			if (sensors.isAnyBlack()) {
				motors.resetTachoCount();
			} else if (motors.getTachoCount() > NO_BLACK_DIST * Motors.COEFF_CM) {
				// Se necessario, attiva la procedura per ritrovare la linea nera
				while (!sensors.isAnyBlack()) {
					motors.drive(-Motors.BASE_SPEED, -Motors.BASE_SPEED);
				}
			}

			// Seguilinea con il PID
			speeds = pid.getSpeed(sensors.getDelta());
			switch (sensors.getColorsLR()) {
			case "ss":
			case "sw":
			case "ws":
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
				} else if (greenLeft && greenRight) {
					// Inversione di marcia
//					System.out.println((new Date()).getTime() + "\tInversione di marcia");
					motors.spin(Motors.BASE_SPEED, 180);
					motors.travel(Motors.BASE_SPEED, 5);
					motors.resetTachoCount();
					greenLeft = false;
					greenRight = false;
				} else if (greenLeft) {
					// Curva a sinistra
//					System.out.println((new Date()).getTime() + "\tCurva a sinistra");
					motors.travel(Motors.BASE_SPEED, 2);
					motors.spin(Motors.BASE_SPEED, 60);
					motors.resetTachoCount();
					greenLeft = false;
				} else if (greenRight) {
					// Curva a destra
//					System.out.println((new Date()).getTime() + "\tCurva a destra");
					motors.travel(Motors.BASE_SPEED, 2);
					motors.spin(Motors.BASE_SPEED, -60);
					motors.resetTachoCount();
					greenRight = false;
				}
				break;

			case "gw":
			case "gb":
				// Prenoto curva a sinistra e vado dritto
				greenLeft = true;
				motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
				break;

			case "wg":
			case "bg":
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
		// zoneOrientation: 0 è larga (120x90), 1 è lunga (90x120)
		// safePosition: 0 è l'angolo in basso a sinistra, gli altri in senso orario
		// gatePosition: da 0 a 3, da sinistra a destra
		int zoneOrientation = -1, safePosition = -1, gatePosition = -1;
		boolean loaded = false;

		// Imposto Arduino in modalità zona vittime
		sensors.setEvacuationZoneMode();
		System.out.println("************\nZona vittime\n************");

		// Avanzo fino a far entrare tutto il robot nella zona
		motors.travel(Motors.BASE_SPEED, 15);

		// Individuo la forma della zona vittime
		if (sensors.checkDistanceFwdHigh() < 0.90) {
			zoneOrientation = 0;
		} else {
			zoneOrientation = 1;
		}
		System.out.println("zoneOrientation=" + zoneOrientation);

		// Individuo la posizione dell'ingresso
		float d = sensors.checkDistanceSide();
		if (d < 0.30) {
			gatePosition = 0;
		} else if (d < 0.60) {
			gatePosition = 1;
		} else if (d < 0.90) {
			gatePosition = 2;
		} else {
			gatePosition = 3;
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
		pid = new PID(5, 20);
		int sidesExplored = 0;
		do { // Ripeto per ogni parete, finché non trovo la zona sicura
			while (sensors.checkDistanceFwdHigh() > 0.05) { // Vado avanti finché non trovo la parete
				if (sensors.isFwdLeftPressed() && sensors.isFwdRightPressed()) {
					// Se ho toccato con i sensori senza arrivare al muro, ho trovato la zona sicura
					safePosition = sidesExplored;
					break;
				} else if (sensors.isFwdLeftPressed()) {
					// Se uno dei sensori ha toccato, faccio allineare l'altro
					motors.drive(0, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, 0);
				} else if (sensors.checkDistanceFwdLow() < 0.05 && sensors.checkDistanceFwdHigh() > 0.40) {
					// Mi sto avvicinando a una pallina: mi fermo per raccoglierla
					motors.travel(Motors.BASE_SPEED, -5);
					motors.bladeLower();
					motors.travel(Motors.BASE_SPEED, 10);
					motors.bladeLift();
					loaded = true;
				} else {
					speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
					// Devo scambiare i lati destro e sinistro del PID per curvare nel verso giusto
					motors.drive(speeds[1], speeds[0]);
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
		// Uso sempre il PID per mantenere la distanza dalla parete
		// Ad ogni spazzata raccolgo le palline e le riporto nella zona sicura
		// Se manca meno di un minuto esco dal ciclo e cerco l'uscita
		motors.bladeLower();
		for (int i = 0; (i < 3 + zoneOrientation) && (new Date().getTime() < startTime + 7*60*1000); i++) {
			// Percorro la zona all'andata
			if (signum == 1) {
				pid = new PID(75 + zoneOrientation * 30 - i * 30, 20);
			} else {
				pid = new PID(15 + i * 30, 20);
			}

			while (sensors.checkDistanceFwdHigh() > 0.05) {
				// Vado avanti finché non trovo la parete
				speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
				motors.drive(speeds[1], speeds[0]);
			}
			// Recupero eventuali palline
			motors.bladeLift();
			// Inversione di marcia
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.travel(Motors.BASE_SPEED, 15);
			motors.spin(Motors.BASE_SPEED, signum * 90);
			motors.bladeLower();
			// Percorro la zona al ritorno
			if (signum == 1) {
				pid = new PID(10 + i * 30, 20);
			} else {
				pid = new PID(70 + zoneOrientation * 30 - i * 30, 20);
			}
			while (sensors.checkDistanceFwdHigh() > 0.05) {
				// Vado avanti finché non trovo la parete
				speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
				motors.drive(speeds[1], speeds[0]);
			}
			// Recupero eventuali palline
			motors.bladeLift();
			// Manovra per scaricare
			motors.spin(Motors.BASE_SPEED, signum * 90);
			if (signum == 1) {
				pid = new PID(105 - zoneOrientation * 30, 20);
			} else {
				pid = new PID(5, 20);
			}
			// Mi appoggio alla zona sicura
			while (!sensors.isFwdLeftPressed() || !sensors.isFwdRightPressed()) {
				if (sensors.isFwdLeftPressed()) {
					motors.drive(0, Motors.BASE_SPEED);
				} else if (sensors.isFwdRightPressed()) {
					motors.drive(Motors.BASE_SPEED, 0);
				} else {
					speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
					motors.drive(speeds[1], speeds[0]);
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
			pid = new PID(105 - zoneOrientation * 30, 20);
			while (sensors.checkDistanceFwdHigh() > 0.05) {
				speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
				motors.drive(speeds[1], speeds[0]);
			}
			if (gatePosition > 0) {
				motors.spin(Motors.BASE_SPEED, 90);
				motors.travel(Motors.BASE_SPEED, gatePosition * 30);
				motors.spin(Motors.BASE_SPEED, -90);
			}
			break;
		case 2:
			motors.spin(Motors.BASE_SPEED, 45);
			pid = new PID(5, 20);
			while (sensors.checkDistanceFwdHigh() > 0.05) {
				speeds = pid.getSpeed(sensors.checkDistanceSide() * 100);
				motors.drive(speeds[1], speeds[0]);
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
			if(sensors.isAnyBlack()) {
				foundBlack = true;
				break;
			}
			motors.spin(Motors.BASE_SPEED, 10);
		}
		if(!foundBlack) {
			for (int i = 0; i < 14; i++) {
				if(sensors.isAnyBlack()) {
					foundBlack = true;
					break;
				}
				motors.spin(Motors.BASE_SPEED, -10);
			}
		}
		motors.resetTachoCount();
		// Ripassa il controllo al ciclo while della lineFollower()
		return;
	}

}
