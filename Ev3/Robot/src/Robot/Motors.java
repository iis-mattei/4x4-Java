package Robot;

import lejos.hardware.motor.Motor;
import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;

public class Motors {
	public static final int BASE_SPEED = 25; // Scala 0-100
	public static final int MAX_SPEED = 40; // Oltre i motori diventano imprecisi
	public static final double WHEEL_DIAM = 3.7;
	public static final double INT_AXIS = 12.2;
	public static final double EXT_AXIS = 16.2;
	public static final double WHEELS_DISTANCE = 8.8;
	public static final double DIFF_AXIS = EXT_AXIS - INT_AXIS;
	public static final double COEFF_CM = 360 / (Math.PI * WHEEL_DIAM); // conversione da cm a gradi
	public static final double COEFF_SPIN = Math.sqrt(Math.pow(EXT_AXIS, 2) + Math.pow(WHEELS_DISTANCE, 2))
			/ WHEEL_DIAM; // conversione da gradi di spin a gradi di rotazione ruote
	public static final double WHEEL_CORRECTION = 1.0; // Modificare solo se necessario
	private float maxSpeed;

	// Motori delle ruote: B-> sinistro C-> destro
	// Da attivare se i motori sono nel senso di marcia
	private RegulatedMotor MA = Motor.A;
//	private RegulatedMotor MB = Motor.B;
//	private RegulatedMotor MC = Motor.C;
//	private RegulatedMotor MD = Motor.D;

	// Da attivare se i motori sono al contrario rispetto al senso di marcia
// 	private RegulatedMotor MA = MirrorMotor.invertMotor(Motor.A);
	private RegulatedMotor MB = MirrorMotor.invertMotor(Motor.B);
	private RegulatedMotor MC = MirrorMotor.invertMotor(Motor.C);
	private RegulatedMotor MD = MirrorMotor.invertMotor(Motor.D);

	public Motors() {
		MB.synchronizeWith(new RegulatedMotor[] { Motor.C });
		maxSpeed = MB.getMaxSpeed();
	}

	public void drive(double leftSpeed, double rightSpeed) {
		MB.setSpeed(calcActualSpeed(leftSpeed));
		MC.setSpeed(calcActualSpeed(rightSpeed));
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

	// Va dritto, distanza in cm
	public void travel(int speed, double distance, boolean noWait) {
		MB.startSynchronization();
		MB.setSpeed(calcActualSpeed(speed));
		MC.setSpeed(calcActualSpeed(speed * WHEEL_CORRECTION));
		MB.rotate((int) (distance * COEFF_CM * (2 - WHEEL_CORRECTION)), true);
		MC.rotate((int) (distance * COEFF_CM), true);
		MB.endSynchronization();
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void travel(int speed, double distance) {
		travel(speed, distance, false);
	}

	// speed: positivo => senso antiorario
	public void spin(int speed, int arc, boolean noWait) {
		MB.startSynchronization();
		MB.setSpeed(calcActualSpeed(speed));
		MC.setSpeed(calcActualSpeed(speed * WHEEL_CORRECTION));
		MB.rotate((int) (-arc * COEFF_SPIN * (2 - WHEEL_CORRECTION)), true);
		MC.rotate((int) (arc * COEFF_SPIN), true);
		MB.endSynchronization();
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void spin(int speed, int arc) {
		spin(speed, arc, false);
	}

	// arc: positivo => senso antiorario
	public void arc(int speed, double radius, double arc, boolean noWait) {
		double intToExtRatio = (radius * 0.66 + DIFF_AXIS / 2) / (radius * 0.66 + INT_AXIS + DIFF_AXIS / 2);
		MB.startSynchronization();
		if (arc > 0) {
			MB.setSpeed(calcActualSpeed(speed * intToExtRatio));
			MC.setSpeed(calcActualSpeed(WHEEL_CORRECTION * speed));
			// controllo l'arco rispetto alla ruota interna
			MB.rotate(
					(int) (Math.signum(speed) * arc * 1.4 * (radius * 0.66 + DIFF_AXIS / 2) * COEFF_CM * Math.PI / 180),
					true);
			// controllo l'arco rispetto alla ruota esterna
			MC.rotate((int) (Math.signum(speed) * arc * 1.4 * (radius * 0.66 + INT_AXIS + DIFF_AXIS / 2) * COEFF_CM
					* WHEEL_CORRECTION * Math.PI / 180), true);
		} else {
			arc *= -1;
			MB.setSpeed(calcActualSpeed(speed));
			MC.setSpeed(calcActualSpeed(speed * intToExtRatio * WHEEL_CORRECTION));
			// controllo l'arco rispetto alla ruota esterna
			MB.rotate((int) (Math.signum(speed) * arc * 1.4 * (radius * 0.66 + INT_AXIS + DIFF_AXIS / 2) * COEFF_CM
					* Math.PI / 180), true);
			// controllo l'arco rispetto alla ruota interna
			MC.rotate((int) (Math.signum(speed) * arc * 1.4 * (radius * 0.66 + DIFF_AXIS / 2) * COEFF_CM
					* WHEEL_CORRECTION * Math.PI / 180), true);
		}
		MB.endSynchronization();
		if (!noWait)
			while (isMoving())
				Thread.yield();
	}

	public void arc(int speed, double radius, double arc) {
		arc(speed, radius, arc, false);
	}

	// Imposta la posizione zero della pala
	public void bladeSetZero() {
		MD.resetTachoCount();
	}

	// Verifica se la pala è in alto o in basso
	public boolean isBladeLow() {
		if (MD.getTachoCount() > 100) {
			return true;
		} else {
			return false;
		}
	}

	// Alza la pala
	public void bladeLift() {
		MD.setSpeed(500);
		MD.rotateTo(50);
		MD.rotateTo(0);
		if(Main.DEBUG) {
			System.out.println("Blade position: " + MD.getTachoCount());
		}
		MD.stop();
	}

	// Abbassa la pala
	public void bladeLower() {
		MD.setSpeed(500);
		MD.rotateTo(290);
		if(Main.DEBUG) {
			System.out.println("Blade position: " + MD.getTachoCount());
		}
		MD.flt();
	}

	// Imposta la posizione zero del portapalline
	public void containerSetZero() {
		MA.resetTachoCount();
	}

	// Verifica se il portapalline è in alto o in basso
	public boolean isContainerOpen() {
		if (MA.getTachoCount() > 50) {
			return true;
		} else {
			return false;
		}
	}

	// Apri il portapalline
	public void containerOpen() {
		MA.setSpeed(120);
		MA.rotateTo(105);
		if(Main.DEBUG) {
			System.out.println("Container position: " + MA.getTachoCount());
		}
		MA.stop();
	}

	// Chiudi il portapalline
	public void containerClose() {
		MA.setSpeed(120);
		MA.rotateTo(0);
		if(Main.DEBUG) {
			System.out.println("Container position: " + MA.getTachoCount());
		}
		MA.stop();
	}

	public boolean isMoving() {
		return (MC.isMoving() || MB.isMoving());
	}

	public void stop() {
		MB.startSynchronization(); // Modifica 12-4-19
		MB.stop();
		MC.stop();
		MC.rotate(-5);
		MC.stop();
		MB.endSynchronization(); // Modifica 12-4-19
	}

	public int getTachoCount() {
		return (int) Math.round((MB.getTachoCount() + MC.getTachoCount()) / 2);
	}

	public void resetTachoCount() {
		MB.resetTachoCount();
		MC.resetTachoCount();
	}

	// Converte la velocità 0-100 in quella effettiva dei motori
	private int calcActualSpeed(double speed) {
		int actualSpeed = (int) Math.round(speed * maxSpeed / 100);
		return actualSpeed;
	}
}