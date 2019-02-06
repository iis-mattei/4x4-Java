package Robot;

import lejos.hardware.Button;

public class Main {
	static Sensors sensori = new Sensors();
	static Motors motori = new Motors();
	static int[] speed = new int[2];
	static PID pid = new PID();

	public static void ZonaVittime() {
		System.exit(0);
	}

	public static void AggiraOstacolo() {

	}

	public static void main(String args[]) throws InterruptedException {
		System.out.println("Premi per partire...");
		Button.waitForAnyPress();
//		System.out.println("Sto partendo...");
		
//		while(true) {
//			System.out.println(sensori.Delta());
//			if (Button.ESCAPE.isDown()) {
//				System.exit(0);
//			}
//			Button.waitForAnyPress();
//		}
		
		System.out.println("Stabilizzazione in corso...");
		for (int i = 0; i < 5; i++) {
			System.out.println(""+(5-i));
			Thread.sleep(1000);
		}
		
		while (true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
			if (sensori.Argento()) {
				ZonaVittime();
			}
			speed = pid.getSpeed(sensori.Delta());
// 				System.out.println(sensori.colA());
			switch (sensori.colA()) {

			case "ww":
			case "wb":
			case "bw":
				motori.drive(speed[0], speed[1]);
				break;

			case "gw":

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
