package Robot;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.I2CSensor;

public class Sensors {
	static int I2CSlaveAddress = 8;
	private I2CSensor arduino = new I2CSensor(SensorPort.S1, I2CSlaveAddress);
}


