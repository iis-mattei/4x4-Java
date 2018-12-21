package Robot;

import lejos.hardware.motor.Motor;
import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;

public class Motors {
	public static final float diametro = 5.6f;
	public static final float asse = 15f;
	public static final float asseEsternoRuote = 17.8f;
	public static final float diffAsse = asseEsternoRuote - asse;
	public static final float coeffCm = 360 / ((float) Math.PI * diametro);
	public static final float coeffSpin = asse / diametro;
	public static final float aD = asse + diffAsse;
	public static final float coeffCorrRuote = 1f; // da calcolare
	// se i motori sono nel verso giusto
	// public static RegulatedMotor MB=Motor.B;
	// public static RegulatedMotor MC=Motor.C;
	// public static RegulatedMotor MC=Motor.A;
	// altrimenti
	public static RegulatedMotor MB = MirrorMotor.invertMotor(Motor.B);
	public static RegulatedMotor MC = MirrorMotor.invertMotor(Motor.C);
	public static RegulatedMotor MA = MirrorMotor.invertMotor(Motor.A);

	public static void drive(float l, float r) {
		// B-> to left C-> to right

		MB.setSpeed((int) Math.abs(l));
		MC.setSpeed((int) Math.abs(r));
		if (l > 0) {
			MB.forward();
		} else if (l < 0) {
			MB.backward();
		} else {
			MB.stop(true);
		}
		if (r > 0) {
			MC.forward();
		} else if (r < 0) {
			MC.backward();
		} else {
			MC.stop(true);
		}
	}

	public static void travel(int vel, float dist, boolean no_wait) { // va dritto alla vel. e per una certa distanza
																		// (in cm)
		MB.resetTachoCount();
		MC.resetTachoCount();
		MB.startSynchronization();
		MB.setSpeed(vel);
		MC.setSpeed((int) (vel * coeffCorrRuote));
		MC.rotate((int) (dist * coeffCm), true);
		MB.rotate((int) (dist * coeffCm * (2 - coeffCorrRuote)), true);
		MB.endSynchronization();
		if (!no_wait)
			while (isMoving())
				Thread.yield();
	}

	public static void travel(int vel, float dist) {
		travel(vel, dist, false);
	}

	public static void spin(int vel, int arc, boolean no_wait) {
		MB.resetTachoCount();
		MC.resetTachoCount();
		MB.startSynchronization();
		MB.setSpeed(vel);
		MC.setSpeed((int) (vel * coeffCorrRuote));
		// antiorario

		MC.rotate((int) (arc * coeffSpin), true);
		MB.rotate((int) (-arc * coeffSpin * (2 - coeffCorrRuote)), true);
		MB.endSynchronization();
		if (!no_wait)
			while (isMoving())
				Thread.yield();
	}

	public static void spin(int vel, int arc) {
		spin(vel, arc, false);
	}

	// rapporto: % di quanto deve essere rallentata la ruota interna rispetto a
	// quella esterna
	public static void spinContinuo(int vel, int rapporto, int direzione) {
		MB.startSynchronization();
		if (direzione < 0) { // se la direzione e' negativa, giro a sinistra
			MC.setSpeed(vel);
			MB.setSpeed(vel * (100 - rapporto) / 100);
			MC.forward();
			MB.backward();
		} else { // altrimenti, con la direzione positiva giro a destra
			MC.setSpeed(vel * (100 - rapporto) / 100);
			MB.setSpeed(vel);
			MC.backward();
			MB.forward();
		}
		MB.endSynchronization();
	}

	public static void arc(int vel, float raggio, float arc, boolean no_wait) {
		float rapportoIntEst = (raggio + diffAsse / 2) / (raggio + aD - diffAsse / 2);
		MB.resetTachoCount();
		MC.resetTachoCount();
		if (arc > 0) {
			MB.setSpeed((int) (vel * rapportoIntEst));
			MC.setSpeed((int) (coeffCorrRuote * vel));
			// controllo l'arco rispetto alla ruota interna
			MB.rotate((int) (Math.signum(vel) * arc * (raggio + diffAsse / 2) * coeffCm * Math.PI / 180), true);
			// controllo l'arco rispetto alla ruota esterna
			MC.rotate((int) (Math.signum(vel) * arc * (raggio + aD - diffAsse / 2) * coeffCm * coeffCorrRuote * Math.PI
					/ 180), true);
		} else {
			arc *= -1;
			MC.setSpeed((int) (vel * rapportoIntEst * coeffCorrRuote));
			MB.setSpeed((int) (vel));
			// controllo l'arco rispetto alla ruota interna
			MC.rotate(
					(int) (Math.signum(vel) * arc * (raggio + diffAsse / 2) * coeffCm * coeffCorrRuote * Math.PI / 180),
					true);
			// controllo l'arco rispetto alla ruota esterna
			MB.rotate((int) (Math.signum(vel) * arc * (raggio + aD - diffAsse / 2) * coeffCm * Math.PI / 180), true);
		}
		if (!no_wait)
			while (isMoving())
				Thread.yield();
	}

	public static void arc(int vest, float raggio, float arc) {
		arc(vest, raggio, arc, false);
	}

	public static void alzaPinza() {
		// metodo per aòzare la pinza

	}

	public static void abbassaPinza() {
		// metodo per abbasare la pinza

	}

	public static boolean isMoving() {
		return (MC.isMoving() || MB.isMoving());
	}

	public static void stop() {
		MB.stop();
		MC.stop();
	}

}
