package Robot;

public class Main {
	static Sensors sensori = new Sensors();
	
	public static void main(String args[]) {
		char col;
		
		col=sensori.colL();
		System.out.println("Sensore Sinistro"+ col);
		
		//col=sensori.colR();
		//System.out.println("Sensore Destro"+ col);
		//col=sensori.colC();
		//System.out.println("Sensore Centrale"+ col);
	
	}
 
}
