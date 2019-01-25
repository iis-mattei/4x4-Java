package Robot;

public class PID {
	static final float TARGET = 0;
	static final float P_CONTROL = 8.5F;
	static final float I_CONTROL = 0.3F;
	static final float D_CONTROL = 10;
	static final float BASE_SPEED = 100;
	static final float MAX_SPEED = 250;

	public static int[] getSpeed(float delta) {
		System.out.println("Delta in ingresso: "+delta);
		if((delta>-10) && (delta<10)) {
			delta=0;
			System.out.println("Delta normalizzato:" +delta);
			
		}
		float integral = 0;
		float[] lastErrs = { 0, 0, 0 };
		float deriv = 0;
		float err = TARGET + delta;
		//System.out.println("Errore: "+err);
		float leftSpeed, rightSpeed;
		int[] speed = new int[2];

		integral *= 0.98;
		integral += err;
		lastErrs[2] = lastErrs[1];
		lastErrs[1] = lastErrs[0];
		lastErrs[0] = err;
		deriv = (float) (11.0 / 6 * err - 3 * lastErrs[0] + 3.0 / 2 * lastErrs[1] - 1.0 / 3 * lastErrs[2]);
		int dl = 0, dr = 0;
		//System.out.println("Derivata" +deriv);
		//System.out.println("Integrale" +integral);
		leftSpeed = BASE_SPEED + P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv;
		//System.out.println("LeftSpeed: "+leftSpeed);
		
		if ((Math.abs(leftSpeed) - MAX_SPEED) > 0) { //
			// forzo lo spin
			leftSpeed = Math.signum(leftSpeed) * MAX_SPEED;
			rightSpeed = -leftSpeed;
			//System.out.println("RightSpeed: "+rightSpeed);
			//System.out.println("LeftSpeed: "+leftSpeed);
		} else {
			rightSpeed = BASE_SPEED - (P_CONTROL * err + I_CONTROL * integral + D_CONTROL * deriv + dl); //
			
			if ((Math.abs(rightSpeed) - MAX_SPEED) > 0) { //
				// forzo lo spin
				rightSpeed = Math.signum(rightSpeed) * MAX_SPEED;
				leftSpeed = -rightSpeed;
				//System.out.println("RightSpeed: "+rightSpeed);
				//System.out.println("LeftSpeed: "+leftSpeed);
			}
		}
		
		speed[0] = (int) Math.round(leftSpeed);
		speed[1] = (int) Math.round(rightSpeed);
		//System.out.println("LeftSpeed: "+leftSpeed);
		//System.out.println("RightSpeed: "+rightSpeed);
		return speed;
	}

}
