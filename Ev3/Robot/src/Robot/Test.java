package Robot;

import lejos.hardware.motor.Motor;
import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;

public class Test {
	static Motors motors = new Motors();
	static Sensors sensors = new Sensors();
	private static PID pid = null;
	// private static RegulatedMotor MA = Motor.A;
	// private static RegulatedMotor MB = MirrorMotor.invertMotor(Motor.B);
	// private static RegulatedMotor MC = MirrorMotor.invertMotor(Motor.C);

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
		
		int[] speeds = new int[2];
		int position;
		sensors.checkGyro();
		position = sensors.getGyroZ();
		System.out.println("pos_start: "+position);
		pid = new PID(position, 50);
		while(true) {
		sensors.checkGyro();
		speeds = pid.getSpeed(-sensors.getGyroZ());
		System.out.println("pos: " + sensors.getGyroZ());
		motors.drive(speeds[0], speeds[1]);
		}	
//		while (true) {
////			sensors.checkGyro();
////			System.out.print("X: " + sensors.getGyroX() + " ");
////			System.out.print("Y: " + sensors.getGyroY() + " ");
////			System.out.println("Z: " + sensors.getGyroZ());
////			System.out.println("D: " + sensors.checkDistanceFwdLow());
////			sensors.checkColors();
////			System.out.print("R: " + sensors.getLuxR());
////			System.out.print("\tC: " + sensors.getLuxC());
////			System.out.println("\tL: " + sensors.getLuxL());
////			sensors.checkDistanceFwdHigh();
////			sensors.checkDistanceSide();
//			Thread.sleep(1000);
//		}
 
		//motors.containerOpen();
//		System.out.println("inizio chiusura");
		//motors.containerClose();
//		sensors.checkGyro();
//		System.out.println("Z: " + sensors.getGyroZ());
//		motors.spin(Motors.BASE_SPEED, 180);
//		sensors.checkGyro();
//		System.out.println("Z: " + sensors.getGyroZ());
//		System.out.println("Reset Gyro");
//		sensors.resetGyro();
//		Thread.sleep(1000);
//		sensors.checkGyro();
//		System.out.println("Z: " + sensors.getGyroZ());
//		motors.spin(Motors.BASE_SPEED, 180);
//		sensors.checkGyro();
//		System.out.println("Z: " + sensors.getGyroZ());
//		motors.arc(Motors.BASE_SPEED, 30, -90);
//		motors.travel(Motors.BASE_SPEED, 60);
//		motors.containerOpen();
//		motors.containerClose();
//		MB.setSpeed(400);
//		MC.setSpeed(400);
//		MB.forward();
//		MC.forward();
//		Thread.sleep(5000);
//		motors.bladeLift();
//		
//		motors.travel(500, 5);
//		motors.containerOpen();
//		motors.travel(500, -5);
//		motors.travel(500, 2);
//		motors.travel(500, -2);

	}

}
