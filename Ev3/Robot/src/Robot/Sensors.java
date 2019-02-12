package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.I2CSensor;

public class Sensors {
	private final int I2CSlaveAddress = 8;
	private I2CSensor arduino = new I2CSensor(SensorPort.S4, I2CSlaveAddress);
	
	private String colorsLR, colorC;
	private int luxL, luxC, luxR;
	private boolean touchFwdRight, touchFwdLeft, touchBackRight, touchBackLeft, silver;
	private float distanceFwd;
	
	private int uintToInt(byte lsb, byte msb) {
		return (int)((lsb & 0xFF) | ((msb & 0x7F) << 8));
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
	
	public float getDistanceFwd() {
		return distanceFwd;
	}

	// public int U_Ant_I(){
	// arduino.getData('C', buffReadResponse, buffReadResponse.length);
	// dist = (int) buffReadResponse[0];
	// return dist;
	// }

}
