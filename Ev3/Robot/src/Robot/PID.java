package Robot;

import java.util.Date;

public class PID {
	private static final double P_COEFF = 45; // 45
	private static final double I_COEFF = 4.5; // 4.5
	private static final double D_COEFF = 450; // 450
	private static final int N_ERRORS = 5;

	private int target;
	private double[] lastErrors = new double[N_ERRORS];
	private double integral;
	private int maxDelta;

	public PID(int target, int maxDelta) {
		this.target = target;
		this.maxDelta = maxDelta;
	}

	public int[] getSpeed(double delta) {
		int[] speeds = new int[2];
		double leftSpeed, rightSpeed, error, derivative, correction;

		double constrainedDelta = (delta > maxDelta) ? maxDelta : delta; // Evito valori fuori scala del delta
		error = (constrainedDelta - target) * 100 / maxDelta; // Riporto l'errore in scala 0-100

		integral = 0;
		for (int i = 0; i < lastErrors.length; i++) {
			integral += lastErrors[i];
		}
		if (Math.signum(integral) != Math.signum(error)) {
			integral = 0;
		}

		derivative = error - lastErrors[0];
		if (Math.signum(derivative) != Math.signum(error)) {
			derivative = 0;
		}

		correction = (P_COEFF * error + I_COEFF * integral + D_COEFF * derivative) / 100;
		if (correction > 0) {
			leftSpeed = Motors.BASE_SPEED;
			rightSpeed = Motors.BASE_SPEED - correction;
		} else {
			leftSpeed = Motors.BASE_SPEED + correction;
			rightSpeed = Motors.BASE_SPEED;
		}

		// Impongo il rispetto delle velocità massime
		leftSpeed = Math.abs(leftSpeed) > Motors.MAX_SPEED ? Math.signum(leftSpeed) * Motors.MAX_SPEED : leftSpeed;
		rightSpeed = Math.abs(rightSpeed) > Motors.MAX_SPEED ? Math.signum(rightSpeed) * Motors.MAX_SPEED : rightSpeed;

		speeds[0] = (int) Math.round(leftSpeed);
		speeds[1] = (int) Math.round(rightSpeed);

		for (int i = lastErrors.length - 1; i >= 1; i--) {
			lastErrors[i] = lastErrors[i - 1];
		}
		lastErrors[0] = error;

		if(Main.DEBUG) {
			System.out.print((new Date()).getTime());
			System.out.print(" - Delta: " + Math.round(delta));
			System.out.print("\tP: " + Math.round(P_COEFF * error / 100));
			System.out.print("\tI: " + Math.round(I_COEFF * integral / 100));
			System.out.print("\tD: " + Math.round(D_COEFF * derivative / 100));
			System.out.print("\tCorr: " + Math.round(correction));
			System.out.println("\tL: " + speeds[0] + "\tR: " + speeds[1]);
		}

		return speeds;
	}
}