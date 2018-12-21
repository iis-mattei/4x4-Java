package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.I2CSensor;

public class Sensors {
	private int I2CSlaveAddress = 8;
	private I2CSensor arduino = new I2CSensor(SensorPort.S1, I2CSlaveAddress);
	private byte[] buffReadResponse = new byte[1];
	private char colL, colC, colR;
	private boolean antDx, antSx, postDx, postSx;

	public char colR() {
		arduino.getData('R', buffReadResponse, buffReadResponse.length);
		colR = (char) buffReadResponse[0];
		return colR;
	}

	public char colC() {
		arduino.getData('C', buffReadResponse, buffReadResponse.length);
		colC = (char) buffReadResponse[0];
		return colC;
	}

	public char colL() {
		arduino.getData('L', buffReadResponse, buffReadResponse.length);
		colL = (char) buffReadResponse[0];
		return colL;
	}

	public boolean antDx() {

		return antDx;
	}

	public boolean antSx() {

		return antSx;
	}

	public boolean postDx() {

		return postDx;
	}

	public boolean postSX() {

		return postSx;
	}
}