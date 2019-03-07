package Robot;

import lejos.hardware.motor.Motor;
import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;

public class Test {
	static Motors motors = new Motors();
	private static RegulatedMotor MA = Motor.A;
	private static RegulatedMotor MB = MirrorMotor.invertMotor(Motor.B);
	private static RegulatedMotor MC = MirrorMotor.invertMotor(Motor.C);

//	// Apri il portapalline
//	private static void containerOpen() {
//		MA.setSpeed(120);
//		MA.rotate(130);
//		MA.stop();
//	}
//
//	// Chiudi il portapalline
//	private static void containerClose() {
//		MA.setSpeed(120);
//		MA.rotate(-130);
//		MA.stop();
//	}

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
//		containerOpen();
//		System.out.println("inizio chiusura");
//		containerClose();
//		motors.spin(Motors.BASE_SPEED, 180);
//		motors.arc(Motors.BASE_SPEED, 30, -90);
//		motors.travel(Motors.BASE_SPEED, 60);
//		motors.containerOpen();
//		motors.containerClose();
//		MB.setSpeed(400);
//		MC.setSpeed(400);
//		MB.forward();
//		MC.forward();
//		Thread.sleep(5000);
	}

}
