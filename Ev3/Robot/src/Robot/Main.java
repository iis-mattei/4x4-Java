package Robot;

import lejos.hardware.Button;

public class Main {
	static Sensors sensori = new Sensors();
	static Motors motori = new Motors();
	static int VEL_STD = 1000;
	static int[] speed = new int[2];

	public static void ZonaVittime() {
		System.exit(0);
	}

	public static void AggiraOstacolo() {

	}

	public static void main(String args[]) {
		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
		System.out.println("Sto partendo...");
//		while(true) {
//			speed = PID.getSpeed(sensori.Delta());
//			if (Button.ESCAPE.isDown()) {
//				System.exit(0);
//			}
//			Button.waitForAnyPress();
//		}
		while (true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
			if (sensori.Argento()) {
				ZonaVittime();
			}
			speed = PID.getSpeed(sensori.Delta());
			// System.out.println(sensori.colA());
			switch (sensori.colA()) {

			case "ww":
//				System.out.println("bianco-bianco");
				motori.drive(speed[0], speed[1]);
				break;

			case "wb":
//				System.out.println("bianco-nero");
				motori.drive(speed[0], speed[1]);
				break;

			case "bw":
//				System.out.println("nero-bianco");
				motori.drive(speed[0], speed[1]);
				break;

			case "gw":
//
				break;

			case "wg":

				break;

			case "gg":
//				inversione di marcia
				break;
			}

		}

	}
}
