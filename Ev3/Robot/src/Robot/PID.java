package Robot;

public class PID {
	static final float TARGET = 0;
	static final float P_CONTROL = 0.5F;
	static final float I_CONTROL = 0.25F;
	static final float D_CONTROL = 0.1f;
	static final float BASE_SPEED = 30;
	static final float MAX_SPEED = 80;

	private static float[] lastErrs = { 0, 0, 0 };

	public static int[] getSpeed(float delta) {
		float leftSpeed, rightSpeed;
		int[] speed = new int[2];
		float integral = 0;
		float deriv = 0;
		float err = TARGET + delta;

		integral *= 0.75;
		integral += err * 0.25;
		deriv = (float) (11.0 / 6 * err - 3 * lastErrs[0] + 3.0 / 2 * lastErrs[1] - 1.0 / 3 * lastErrs[2]);

		leftSpeed = BASE_SPEED + P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv;

//		Se supero la velocità massima, forzo lo spin
		if ((Math.abs(leftSpeed) - MAX_SPEED) > 0) {
			leftSpeed = Math.signum(leftSpeed) * MAX_SPEED;
			rightSpeed = -leftSpeed;
		} else {
			rightSpeed = BASE_SPEED - (P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv);
		}

		speed[0] = (int) Math.round(leftSpeed);
		speed[1] = (int) Math.round(rightSpeed);

//		System.out.println("Delta in ingresso: " + delta);
//		System.out.println("Errore: " + err);
//		System.out.println("Derivata" + deriv);
//		System.out.println("Integrale" + integral);
//		System.out.println("LeftSpeed: " + leftSpeed);
//		System.out.println("RightSpeed: " + rightSpeed);

		lastErrs[2] = lastErrs[1];
		lastErrs[1] = lastErrs[0];
		lastErrs[0] = err;

		return speed;
	}
}