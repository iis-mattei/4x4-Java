package Robot;

import lejos.hardware.Button;

public class TestAllineamento {
	static Sensors sensori = new Sensors();

	public static void main(String args[]) {
		while (true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
		
			if (sensori.AntDx() == true) {
				System.out.println("AntDx");
			}

			if (sensori.AntSx() == true) {
				System.out.println("AntSx");
			}

			if (sensori.PostDx() == true) {
				System.out.println("PostDx");
			}

			if (sensori.PostSx() == true) {
				System.out.println("PostSx");

			}

			if (sensori.Argento() == true) {
				System.out.println("Argento");

			}

		}
	}

}
