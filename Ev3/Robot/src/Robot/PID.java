package Robot;

import java.util.Date;

public class PID {
	private static final int TARGET = 0;
	private static final double P_COEFF = 12.5;	// 12.5
	private static final double I_COEFF = 2;	// 1
	private static final double D_COEFF = 60;	// 60
	private static final double I_DECAY = 0.8;
	
	private double[] lastErrs = new double[3];
	private double integral = 0;
	private int maxDelta;
	
	public PID(int maxDelta) {
		this.maxDelta = maxDelta;
	}

	public int[] getSpeed(double delta) {
		int[] speeds = new int[2];
		double leftSpeed, rightSpeed, error, derivative, correction;

		error = (TARGET + delta) * 100 / maxDelta;	// Riporto l'errore in scala 0-100
		integral = I_DECAY * integral + error;
		derivative = error - lastErrs[0];
		if(Math.signum(error) != Math.signum(derivative)) {
			derivative = 0;
		}
		
		correction = (P_COEFF * error + I_COEFF * integral + D_COEFF * derivative) / 100;
		if(correction > 0) {
			leftSpeed = Motors.BASE_SPEED + 0.5*correction;
			rightSpeed = Motors.BASE_SPEED - 3.5*correction; 
		} else {
			leftSpeed = Motors.BASE_SPEED + 3.5*correction;
			rightSpeed = Motors.BASE_SPEED - 0.5*correction; 
		}
		
		// Impongo il rispetto delle soglie
		leftSpeed = (leftSpeed > Motors.MAX_SPEED) ? Motors.MAX_SPEED : leftSpeed;
		leftSpeed = (leftSpeed < -Motors.MAX_SPEED) ? -Motors.MAX_SPEED : leftSpeed;
		rightSpeed = (rightSpeed > Motors.MAX_SPEED) ? Motors.MAX_SPEED : rightSpeed;
		rightSpeed = (rightSpeed < -Motors.MAX_SPEED) ? -Motors.MAX_SPEED : rightSpeed;
		
		speeds[0] = (int) Math.round(leftSpeed);
		speeds[1] = (int) Math.round(rightSpeed);
		
		lastErrs[2] = lastErrs[1];
		lastErrs[1] = lastErrs[0];
		lastErrs[0] = error;

		System.out.print((new Date()).getTime());
		System.out.print(" - Delta: " + Math.round(delta));
		System.out.print("\tP: " + Math.round(P_COEFF * error / 100));
		System.out.print("\tI: " + Math.round(I_COEFF * integral / 100));
		System.out.print("\tD: " + Math.round(D_COEFF * derivative / 100));
		System.out.print("\tCorr: " + Math.round(correction));
		System.out.println("\tL: " + speeds[0] + "\tR: " + speeds[1]);

		return speeds;
	}
}