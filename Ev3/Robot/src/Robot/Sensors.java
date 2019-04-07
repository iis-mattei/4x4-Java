package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.I2CSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.*;

public class Sensors {
	private final int I2CSlaveAddress = 8;
	private EV3UltrasonicSensor usFwdHigh;
	private EV3UltrasonicSensor usSide;
	private SampleProvider spFwdHigh, spSide;
	private float[] sample;
	private I2CSensor arduino = new I2CSensor(SensorPort.S4, I2CSlaveAddress);

	private String colorsLR, colorC;
	private int luxL, luxC, luxR, gyroX, gyroY, gyroZ;
	private boolean touchFwdRight, touchFwdLeft, touchBackRight, touchBackLeft;

	public Sensors() {
		boolean sensorOk = false;
		do {
			try {
				usFwdHigh = new EV3UltrasonicSensor(SensorPort.S2);
				sensorOk = true;
			} catch (Exception e) {
				System.out.println("Errore S2");
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
				System.out.println("Errore S3");
				sensorOk = false;
			}
		} while (!sensorOk);
		System.out.println("S3 OK");

		spFwdHigh = usFwdHigh.getDistanceMode();
		spSide = usSide.getDistanceMode();
		sample = new float[spFwdHigh.sampleSize()];
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
		return EndianTools.decodeUShortLE(buffReadResponse, 0);
	}

	public void checkColors() {
		byte[] buffReadResponse = new byte[9];
		arduino.getData('C', buffReadResponse, buffReadResponse.length);
		luxL = EndianTools.decodeUShortLE(buffReadResponse, 0);
		luxC = EndianTools.decodeUShortLE(buffReadResponse, 2);
		luxR = EndianTools.decodeUShortLE(buffReadResponse, 4);
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
		return (colorC.equals("b") || colorsLR.charAt(0) == 'b' || colorsLR.charAt(1) == 'b');
	}

	public boolean isAnySilver() {
		return (colorC.equals("s") || colorsLR.charAt(0) == 's' || colorsLR.charAt(1) == 's');
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

	public int checkDistanceFwdHigh() {
		spFwdHigh.fetchSample(sample, 0);
		return (int) Math.round(sample[0] * 100);
	}

	public int checkDistanceSide() {
		spSide.fetchSample(sample, 0);
		return (int) Math.round(sample[0] * 100);
	}

	public int checkDistanceFwdLow() {
		byte[] buffReadResponse = new byte[2];
		arduino.getData('D', buffReadResponse, buffReadResponse.length);
		int dist = EndianTools.decodeUShortLE(buffReadResponse, 0);
		return dist;
	}

	public void checkGyro() {
		byte[] buffReadResponse = new byte[6];
		arduino.getData('G', buffReadResponse, buffReadResponse.length);
		gyroX = EndianTools.decodeShortLE(buffReadResponse, 0);
		gyroY = EndianTools.decodeShortLE(buffReadResponse, 2);
		gyroZ = EndianTools.decodeShortLE(buffReadResponse, 4);
	}

	public int getGyroX() {
		return (gyroX % 360);
	}

	public int getGyroY() {
		return (gyroY % 360);
	}

	public int getGyroZ() {
		return (gyroZ % 360);
	}

	public boolean resetGyro() {
		byte[] buffReadResponse = new byte[1];
		arduino.getData('R', buffReadResponse, buffReadResponse.length);
		boolean retval = buffReadResponse[0] != 0;
		return retval;
	}
}