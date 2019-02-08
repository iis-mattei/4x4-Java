package Robot;

import java.util.Date;

public class PID {
	static final int MAX_SPEED = 200;
	static final int MAX_DELTA = 30;
	static final int TARGET = 0;
	static final float P_PERCENT = 40f;	// Percentuale della correzione massima
	static final float I_PERCENT = 15f;
	static final float D_PERCENT = 5f;
	static final float P_COEFF = 2 * MAX_SPEED * P_PERCENT / 100;
	static final float I_COEFF = 2 * MAX_SPEED * I_PERCENT / 100;
	static final float D_COEFF = 2 * MAX_SPEED * D_PERCENT / 100;
	
	private double lastErr = 0;
	private double integral = 0;

	public int[] getSpeed(double delta) {
		int[] speeds = new int[2];
		double leftSpeed, rightSpeed;

		double error = (TARGET + delta) * 100 / MAX_DELTA;	// Riporto l'errore in scala 0-100
		integral = 0.6*integral + error;
		double derivative = error - lastErr;
		
		double correction = P_COEFF * error / 100 + I_COEFF * integral / 100 + D_COEFF * derivative / 100;
		if (correction > 0) {
			leftSpeed = MAX_SPEED;
			rightSpeed = MAX_SPEED - (correction > 2*MAX_SPEED ? 2*MAX_SPEED : correction); 
		} else {
			leftSpeed = MAX_SPEED + (correction < -2*MAX_SPEED ? -2*MAX_SPEED : correction);
			rightSpeed = MAX_SPEED;
		}
		
		// Sempre prima il sinistro!!!
		speeds[0] = (int) Math.round(leftSpeed);
		speeds[1] = (int) Math.round(rightSpeed);
		
		lastErr = error;

		System.out.print((new Date()).getTime());
		System.out.print("\tD: " + Math.round(delta));
		System.out.print("\tE: " + Math.round(error));
		System.out.print("\tD: " + Math.round(derivative));
		System.out.print("\tI: " + Math.round(integral));
		System.out.print("\tSx: " + Math.round(leftSpeed));
		System.out.println("\tDx: " + Math.round(rightSpeed));

		return speeds;
	}
}