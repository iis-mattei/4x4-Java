package Robot;

public class Main {
	static Sensors sensori = new Sensors();
	static Motors motori = new Motors();
	static int VEL_STD = 1000;
	static int[] speed = new int[2];

	public static void main(String args[]) {
		motori.travel(VEL_STD, 50);
		motori.spin(50, 90);
		
//		speed = PID.getSpeed(sensori.Delta());
//		System.out.println(sensori.Delta());
//		System.out.println("Sx" + speed[0]);
//		System.out.println("Dx" + speed[1]);
//		speed = PID.getSpeed(sensori.Delta());
//		System.out.println(sensori.Delta());
//		System.out.println("Sx" + speed[0]);
//		System.out.println("Dx" + speed[1]);
//		speed = PID.getSpeed(sensori.Delta());
//		System.out.println(sensori.Delta());
//		System.out.println("Sx" + speed[0]);
//		System.out.println("Dx" + speed[1]);
//		speed = PID.getSpeed(sensori.Delta());
//		System.out.println(sensori.Delta());
//		System.out.println("Sx" + speed[0]);
//		System.out.println("Dx" + speed[1]);
//		while (true) {
//			switch (sensori.colA()) {
//			case "ww":
//				
//				motori.drive(speed[0], speed[1]);
//				break;
//
//			case "wb":
//				if (sensori.colC() == 'b') {
//					// tira avanti icrocio a T
//					// senza prenotazionni per curva
//				}
//
//				break;
//
//			case "bw":
//				if (sensori.colC() == 'b') {
//					// tira avanti icrocio a T
//					// senza prenotazionni per curva
//				}
//
//				break;
//
//			case "gw":
//
//				break;
//
//			case "wg":
//
//				break;
//			}
//
//		}
	}

}
