package Robot;

import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

public class Test {
	static Motors motors = new Motors();
	private static RegulatedMotor MA = Motor.A;

	// Apri il portapalline
	private static void containerOpen() {
		MA.setSpeed(120);
		MA.rotate(130);
		MA.stop();
	}

	// Chiudi il portapalline
	private static void containerClose() {
		MA.setSpeed(120);
		MA.rotate(-130);
		MA.stop();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		containerOpen();
		System.out.println("inizio chiusura");
		containerClose();
	}

}
