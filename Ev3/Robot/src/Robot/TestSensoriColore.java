package Robot;

import lejos.hardware.Button;

public class TestSensoriColore {
	static Sensors sensori = new Sensors();
	static char colL, colC, colR;

	public static void main(String args[]) {
		while (true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
			colL = sensori.colL();
			System.out.println("SX: " + colL);
			colR = sensori.colR();
			System.out.println("DX: " + colR);
			colC = sensori.colC();
			System.out.println("C: " + colC);
		}

	}

}
                                                                     