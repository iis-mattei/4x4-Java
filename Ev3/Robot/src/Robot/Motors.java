package Robot;

import lejos.hardware.Battery;
import lejos.hardware.motor.Motor;
//import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;

public class Motors {
	public static final int BASE_SPEED = 25;	// Scala 0-100
	public static final int MAX_SPEED = 50;		// Oltre i motori diventano imprecisi
	
	private static final float diameter = 5.6f;
	private static final float axis = 15.8f;
	private static final float externalAxis = 18.6f;
	private static final float axisDiff = externalAxis - axis;
	private static final float coeffCm = 360 / ((float) Math.PI * diameter);
	private static final float coeffSpin = axis / diameter;
	private static final float aD = axis + axisDiff;
	private static final float wheelCorrectionRatio = 1f; // da calcolare
	
	// se i motori sono nel verso giusto
	private RegulatedMotor MB = Motor.B;
	private RegulatedMotor MC = Motor.C;
	// public RegulatedMotor MD = MirrorMotor.invertMotor(Motor.D);
	
	// altrimenti
	private RegulatedMotor MD = Motor.D;
	// public RegulatedMotor MB = MirrorMotor.invertMotor(Motor.B);
	// public RegulatedMotor MC = MirrorMotor.invertMotor(Motor.C);

	// Converte la velocità 0-100 in quella effettiva dei motori
	private int calcActualSpeed(int centVel) {
		float voltage = Battery.getVoltage();
		float maxSpeed = voltage * 100;
		int actualSpeed = (int) Math.round(centVel * maxSpeed / 100);
		return actualSpeed;
	}

	public void drive(float leftSpeed, float rightSpeed) { // B-> to left C-> to right
		MB.setSpeed(calcActualSpeed((int) Math.abs(leftSpeed)));
		MC.setSpeed(calcActualSpeed((int) Math.abs(rightSpeed)));
		if (leftSpeed > 0) {
			MB.forward();
		} else if (leftSpeed < 0) {
			MB.backward();
		} else {
			MB.stop(true);
		}
		if (rightSpeed > 0) {
			MC.forward();
		} else if (rightSpeed < 0) {
			MC.backward();
		} else {
			MC.stop(true);
		}
	}

	public void travel(int speed, float distance, boolean noWait) { // va dritto, distanza in cm
		MB.resetTachoCount();
		MC.resetTachoCount();
		MB.startSynchronization();
		MB.setSpeed(calcActualSpeed(speed));
		MC.setSpeed(calcActualSpeed((int) (speed * wheelCorrectionRatio)));
		MC.rotate((int) (distance * coeffCm), true);
		MB.rotate((int) (distance * coeffCm * (2 - wheelCorrectionRatio)), true);
		MB.endSynchronization();
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void travel(int speed, float distance) {
		travel(speed, distance, false);
	}

	// velocità positiva => senso antiorario
	public void spin(int speed, int arc, boolean noWait) {
		MB.resetTachoCount();
		MC.resetTachoCount();
		MB.startSynchronization();
		MB.setSpeed(calcActualSpeed(speed));
		MC.setSpeed(calcActualSpeed((int) (speed * wheelCorrectionRatio)));
		MC.rotate((int) (arc * coeffSpin), true);
		MB.rotate((int) (-arc * coeffSpin * (2 - wheelCorrectionRatio)), true);
		MB.endSynchronization();
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void spin(int speed, int arc) {
		spin(speed, arc, false);
	}

	// rapporto: % di rallentamento della ruota interna rispetto all'esterna
	public void spinContinuous(int speed, int ratio, int direction) {
		MB.startSynchronization();
		if (direction < 0) { // se la direzione e' negativa, giro a sinistra
			MC.setSpeed(calcActualSpeed(speed));
			MB.setSpeed(calcActualSpeed(speed * (100 - ratio) / 100));
			MC.forward();
			MB.backward();
		} else { // altrimenti, con la direzione positiva giro a destra
			MC.setSpeed(calcActualSpeed(speed * (100 - ratio) / 100));
			MB.setSpeed(calcActualSpeed(speed));
			MC.backward();
			MB.forward();
		}
		MB.endSynchronization();
	}

	public void arc(int speed, float radius, float arc, boolean noWait) {
		float intToExtRatio = (radius + axisDiff / 2) / (radius + aD - axisDiff / 2);
		MB.resetTachoCount();
		MC.resetTachoCount();
		if (arc > 0) {
			MB.setSpeed(calcActualSpeed((int) (speed * intToExtRatio)));
			MC.setSpeed(calcActualSpeed((int) (wheelCorrectionRatio * speed)));
			// controllo l'arco rispetto alla ruota interna
			MB.rotate((int) (Math.signum(speed) * arc * (radius + axisDiff / 2) * coeffCm * Math.PI / 180), true);
			// controllo l'arco rispetto alla ruota esterna
			MC.rotate((int) (Math.signum(speed) * arc * (radius + aD - axisDiff / 2) * coeffCm * wheelCorrectionRatio
					* Math.PI / 180), true);
		} else {
			arc *= -1;
			MC.setSpeed(calcActualSpeed((int) (speed * intToExtRatio * wheelCorrectionRatio)));
			MB.setSpeed(calcActualSpeed((int) (speed)));
			// controllo l'arco rispetto alla ruota interna
			MC.rotate((int) (Math.signum(speed) * arc * (radius + axisDiff / 2) * coeffCm * wheelCorrectionRatio
					* Math.PI / 180), true);
			// controllo l'arco rispetto alla ruota esterna
			MB.rotate((int) (Math.signum(speed) * arc * (radius + aD - axisDiff / 2) * coeffCm * Math.PI / 180), true);
		}
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void arc(int extSpeed, float radius, float arc) {
		arc(extSpeed, radius, arc, false);
	}

	public void bladeLift() { // alza la pinza
		MD.setSpeed(120);
		MD.rotate(190);
	}

	public void bladeLower() { // abbassa la pinza
		MD.setSpeed(120);
		MD.rotate(-190);
	}

	public boolean isMoving() {
		return (MC.isMoving() || MB.isMoving());
	}

	public void stop() {
		MB.stop();
		MC.stop();
	}
}