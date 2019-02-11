package Robot;

import java.util.Date;

import lejos.hardware.Button;

public class Main {
	static Sensors sensors = new Sensors();
	static Motors motors = new Motors();
	static PID pid;
	static boolean greenLeft, greenRight;
	
	public static void ZonaVittime() {
		System.exit(0);
	}

	public static void AggiraOstacolo() {

	}

	public static void main(String args[]) throws InterruptedException {
		int[] speeds = new int[2];
		
		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
		System.out.println("Calibrazione in corso...");
		int blackLevel = sensors.detectBlack();		
		int deltaMax = (int)Math.round(blackLevel*0.9);
		System.out.println("blackLevel = " + blackLevel + "\tdeltaMax = " + deltaMax);
		pid = new PID(deltaMax);
		System.out.println("Sto partendo...");

		while (true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
//			if (sensors.isSilver()) {
//				ZonaVittime();
//			}
			
			sensors.checkColors();
			speeds = pid.getSpeed(sensors.getDelta());
			switch (sensors.getColorsLR()) {
				case "ww":	// Rettilineo, azzero prenotazioni verde
					greenLeft = false;
					greenRight = false;
					motors.drive(speeds[0], speeds[1]);
					break;
					
				case "wb":
				case "bw":
					if(sensors.getColorC().equals("b")) {	// Incrocio a T
						motors.drive(Motors.BASE_SPEED, Motors.BASE_SPEED);
					} else {	// Curva normale
						motors.drive(speeds[0], speeds[1]);
					}
					break;
					
				case "bb":
					if(!greenLeft && !greenRight) {	// Segui la linea
						motors.drive(speeds[0], speeds[1]);
					} else if(greenLeft) {	// Curva a sinistra
						System.out.println((new Date()).getTime() + "\tCurva a sinistra");
						motors.spin(Motors.BASE_SPEED, 90);
						greenLeft = false;
					} else if(greenRight) {	// Curva a destra
						System.out.println((new Date()).getTime() + "\tCurva a destra");
						motors.spin(Motors.BASE_SPEED, -90);
						greenRight = false;
					} else if(greenLeft && greenRight) {	// Inversione di marcia
						System.out.println((new Date()).getTime() + "\tInversione di marcia");
						motors.spin(Motors.BASE_SPEED, 180);
						greenLeft = false;
						greenRight = false;
					}
					break;
	
				case "gw":	// Prenoto curva a sinistra
					greenLeft = true;
					break;
	
				case "wg":	// Prenoto curva a destra
					greenRight = true;
					break;
	
				case "gg":	// Prenoto inversione di marcia
					greenLeft = true;
					greenRight = true;
					break;
			}

		}

	}
}
