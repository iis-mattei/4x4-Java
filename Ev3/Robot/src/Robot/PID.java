package Robot;

public class PID {
	static final float TARGET = 0;
	static final float P_CONTROL = 85;
	static final float I_CONTROL = 3;
	static final float D_CONTROL = 100;
	static final float BASE_SPEED = 30;
	static final float MAX_SPEED = 70;
	
	public static int[] getSpeed(float delta) {
		float integral = 0;
		float[] lastErrs = { 0, 0, 0 };
		float deriv = 0;
		float err = TARGET + delta;
		float leftSpeed, rightSpeed;
		int[] speed = new int[2];

		integral *= 0.98;
		integral += err;
		deriv = (float) (11.0 / 6 * err - 3 * lastErrs[0] + 3.0 / 2 * lastErrs[1] - 1.0 / 3 * lastErrs[2]);
		int dl = 0, dr = 0;
		leftSpeed = BASE_SPEED + P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv;
		if ((Math.abs(leftSpeed) - MAX_SPEED) > 0) { //
			// forzo lo spin
			leftSpeed = Math.signum(leftSpeed) * MAX_SPEED;
			rightSpeed = -leftSpeed;
		} else {
			rightSpeed = BASE_SPEED - (P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv + dl); //
			if ((Math.abs(rightSpeed) - MAX_SPEED) > 0) { //
				// forzo lo spin
				rightSpeed = Math.signum(rightSpeed) * MAX_SPEED;
				leftSpeed = -rightSpeed;
			}
		}
		lastErrs[2] = lastErrs[1];
		lastErrs[1] = lastErrs[0];
		lastErrs[0] = err;
		speed[0] = (int)Math.round(leftSpeed);
		speed[1] = (int)Math.round(rightSpeed);
		return speed;
	}

}
