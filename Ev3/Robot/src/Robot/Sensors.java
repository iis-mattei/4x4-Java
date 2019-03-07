package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.SampleProvider;

public class Sensors {
	private final int I2CSlaveAddress = 8;
	private EV3UltrasonicSensor usFwdLow;
	private EV3UltrasonicSensor usFwdHigh;
	private EV3UltrasonicSensor usSide;
	private SampleProvider spFwdLow, spFwdHigh, spSide;
	private float[] sample;
	private I2CSensor arduino = new I2CSensor(SensorPort.S4, I2CSlaveAddress);
	
	private String colorsLR, colorC;
	private int luxL, luxC, luxR;
	private boolean touchFwdRight, touchFwdLeft, touchBackRight, touchBackLeft, silver;
	
	private int uintToInt(byte lsb, byte msb) {
		return (int)((lsb & 0xFF) | ((msb & 0x7F) << 8));
	}
	
	public Sensors() {
		boolean sensorOk = false;
		do {
			try {
				usFwdLow = new EV3UltrasonicSensor(SensorPort.S1);
				sensorOk = true;
			} catch (Exception e) {
				System.out.println("Errore ultrasuoni S1...");
				sensorOk = false;
			}
		} while (!sensorOk);
		System.out.println("S1 OK");
		
		sensorOk = false;
		do {
			try {
				usFwdHigh = new EV3UltrasonicSensor(SensorPort.S2);
				sensorOk = true;
			} catch (Exception e) {
				System.out.println("Errore ultrasuoni S2...");
				sensorOk = false;
			}
		} while (!sensorOk);
		System.out.println("S2 OK");
		
		sensorOk = false;
		do {
			try {
				usSide = new EV3UltrasonicSensor(SensorPort.S3);
				sensorOk = true;
			} catch (Exception e) {
				System.out.println("Errore ultrasuoni S3...");
				sensorOk = false;
			}
		} while (!sensorOk);
		System.out.println("S3 OK");
		
		spFwdLow = usFwdLow.getDistanceMode();
		spFwdHigh = usFwdHigh.getDistanceMode();
		spSide = usSide.getDistanceMode();
		sample = new float[spFwdLow.sampleSize()];
	}
	
	public boolean setRescueLineMode() {
		byte[] buffReadResponse = new byte[1];
		arduino.getData('L', buffReadResponse, buffReadResponse.length);
		boolean retval = buffReadResponse[0] != 0;
		return retval;
	}
	
	public boolean setEvacuationZoneMode() {
		byte[] buffReadResponse = new byte[1];
		arduino.getData('Z', buffReadResponse, buffReadResponse.length);
		boolean retval = buffReadResponse[0] != 0;
		return retval;
	}
	
	public int detectBlack() {
		byte[] buffReadResponse = new byte[2];
		arduino.getData('B', buffReadResponse, buffReadResponse.length);
		return uintToInt(buffReadResponse[0], buffReadResponse[1]);
	}
	
	public void checkColors() {
		byte[] buffReadResponse = new byte[9];
		arduino.getData('C', buffReadResponse, buffReadResponse.length);
		luxL = uintToInt(buffReadResponse[0], buffReadResponse[1]);
		luxC = uintToInt(buffReadResponse[2], buffReadResponse[3]);
		luxR = uintToInt(buffReadResponse[4], buffReadResponse[5]);
		colorsLR = "" + (char) buffReadResponse[6] + (char) buffReadResponse[8];
		colorC = "" + (char) buffReadResponse[7];
	}

	public String getColorsLR() {
		return colorsLR;
	}
	
	public String getColorC() {
		return colorC;
	}
	
	public boolean isAnyBlack() {
		return ( colorC.equals("b") || colorsLR.equals("bn") || colorsLR.equals("nb") );
	}
	
	public int getDelta() {
		return (luxL - luxR);
	}
	
	public int getLuxL() {
		return luxL;
	}
	
	public int getLuxC() {
		return luxC;
	}
	
	public int getLuxR() {
		return luxR;
	}
	
	public void checkTouches() {
		byte[] buffReadResponse = new byte[4];
		arduino.getData('T', buffReadResponse, buffReadResponse.length);
		touchFwdRight = buffReadResponse[0] != 0;
		touchFwdLeft = buffReadResponse[1] != 0;
		touchBackRight = buffReadResponse[2] != 0;
		touchBackLeft = buffReadResponse[3] != 0;
	}

	public boolean isFwdRightPressed() {
		return touchFwdRight;
	}

	public boolean isFwdLeftPressed() {
		return touchFwdLeft;
	}

	public boolean isBackRightPressed() {
		return touchBackRight;
	}

	public boolean isBackLeftPressed() {
		return touchBackLeft;
	}

	public boolean isSilver() {
		byte[] buffReadResponse = new byte[1];
		arduino.getData('S', buffReadResponse, buffReadResponse.length);
		silver = buffReadResponse[0] != 0;
		return silver;
	}
	
	public float checkDistanceFwdLow() {
		spFwdLow.fetchSample(sample, 0);
		return sample[0];
	}
	
	public float checkDistanceFwdHigh() {
		spFwdHigh.fetchSample(sample, 0);
		return sample[0];
	}
	
	public float checkDistanceSide() {
		spSide.fetchSample(sample, 0);
		return sample[0];
	}

	// public int U_Ant_I(){
	// arduino.getData('C', buffReadResponse, buffReadResponse.length);
	// dist = (int) buffReadResponse[0];
	// return dist;
	// }

}
