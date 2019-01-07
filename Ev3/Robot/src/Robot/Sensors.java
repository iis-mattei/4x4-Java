package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.I2CSensor;

public class Sensors {
	private int I2CSlaveAddress = 8;
	private I2CSensor arduino = new I2CSensor(SensorPort.S1, I2CSlaveAddress);
	private byte[] buffReadResponse = new byte[3];
	private char[] colA = new char[3];
	private char colL, colC, colR;
	private boolean AntDx, AntSx, PostDx, PostSx;
	private boolean Argento;
	// private int dist;

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

	public char[] colA() {
		arduino.getData('H', buffReadResponse, buffReadResponse.length);
		for (int i = 0; i < 3; i++) {
			colA[i] = (char) buffReadResponse[i];
		}
		return colA;
	}

	public boolean AntDx() {
		arduino.getData('X', buffReadResponse, buffReadResponse.length);
		AntDx = buffReadResponse[0] != 0;
		return AntDx;
	}

	public boolean AntSx() {
		arduino.getData('Y', buffReadResponse, buffReadResponse.length);
		AntSx = buffReadResponse[0] != 0;
		return AntSx;
	}

	public boolean PostDx() {
		arduino.getData('K', buffReadResponse, buffReadResponse.length);
		PostDx = buffReadResponse[0] != 0;
		return PostDx;
	}

	public boolean PostSx() {
		arduino.getData('Z', buffReadResponse, buffReadResponse.length);
		PostSx = buffReadResponse[0] != 0;
		return PostSx;
	}

	public boolean Argento() {
		arduino.getData('A', buffReadResponse, buffReadResponse.length);
		Argento = buffReadResponse[0] != 0;
		return Argento;
	}

	// public int U_Ant_I(){
	// arduino.getData('C', buffReadResponse, buffReadResponse.length);
	// dist = (int) buffReadResponse[0];
	// return dist;
	// }

}
