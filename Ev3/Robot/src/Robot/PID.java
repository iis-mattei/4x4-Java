package Robot;

import java.util.Date;

public class PID {
	static final float TARGET = 0;
	static final float P_CONTROL = 3F;
	static final float I_CONTROL = 0;
	static final float D_CONTROL = 0;
	static final float BASE_SPEED = 200;
	static final float MAX_SPEED = 400;

	private static float[] lastErrs = { 0, 0, 0 };

	public static int[] getSpeed(float delta) {
		float leftSpeed, rightSpeed;
		int[] speed = new int[2];
		float integral = 0;
		float deriv = 0;
		float err = TARGET + delta;

		integral *= 0.8;
		integral += err * 0.2;
		deriv = (float) (11.0 / 6 * err - 3 * lastErrs[0] + 3.0 / 2 * lastErrs[1] - 1.0 / 3 * lastErrs[2]);
		
		float correction = P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv;
		if (correction > 0) {
			leftSpeed = BASE_SPEED;
			rightSpeed = BASE_SPEED - (correction > 2*BASE_SPEED ? 2*BASE_SPEED : correction); 
		} else {
			leftSpeed = BASE_SPEED + (correction < -2*BASE_SPEED ? -2*BASE_SPEED : correction);
			rightSpeed = BASE_SPEED;
		}

		

//		Se supero la velocità massima, forzo lo spin
//		if ((Math.abs(leftSpeed) - MAX_SPEED) > 0) {
//			leftSpeed = Math.signum(leftSpeed) * MAX_SPEED;
//			rightSpeed = -leftSpeed;
//		} else {
//			rightSpeed = BASE_SPEED - (P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv);
//		}
		
		// Sempre prima il sinistro!!!
		speed[0] = (int) Math.round(leftSpeed);
		speed[1] = (int) Math.round(rightSpeed);

		System.out.print((new Date()).getTime());
		System.out.print("\tD: " + Math.round(delta));
		System.out.print("\tE: " + Math.round(err));
		System.out.print("\tD: " + Math.round(deriv));
		System.out.print("\tI: " + Math.round(integral));
		System.out.print("\tSx: " + Math.round(leftSpeed));
		System.out.println("\tDx: " + Math.round(rightSpeed));

		lastErrs[2] = lastErrs[1];
		lastErrs[1] = lastErrs[0];
		lastErrs[0] = err;

		return speed;
	}
}