#include "Sensors.h"
#include "Arduino.h"

Sensors::Sensors(float greenMultiplier) {
	this->greenMultiplier = greenMultiplier;
	colorSensorL = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_2_4MS, TCS34725_GAIN_16X);
	colorSensorR = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_2_4MS, TCS34725_GAIN_16X);
	colorSensorC = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_2_4MS, TCS34725_GAIN_16X);
	Wire1.begin();
}

void Sensors::tcaSelect(uint8_t addr) {
	Wire1.beginTransmission(TCAADDR);
	Wire1.write(1 << addr);
	Wire1.endTransmission();
}
void Sensors::readSensor(Adafruit_TCS34725& colorSensor, uint8_t addr, int (&colors)[3], int &lux) {
	uint16_t clear, red, green, blue;
	tcaSelect(addr);
	colorSensor.getRawData(&red, &green, &blue, &clear);
	colors[RED] = red;
	colors[GREEN] = green;
	colors[BLUE] = blue;
	lux = (int) colorSensor.calculateLux(red, green, blue);
}
char Sensors::getColorID(int colors[], int lux) {
	if (lux < blackMax) {
		if (colors[GREEN] > greenMultiplier * (colors[RED] + colors[BLUE]) / 2) {
			return COL_GREEN;
		} else {
			return COL_BLACK;
		}
	} else if (lux < whiteMax) {
		return COL_WHITE;
	} else {
		return COL_SILVER;
	}
}

void Sensors::readAllColors() {
	this->readSensor(colorSensorC, CSCAddr, colorsCenter, luxCenter);
	this->readSensor(colorSensorL, CSLAddr, colorsLeft, luxLeft);
	this->readSensor(colorSensorR, CSRAddr, colorsRight, luxRight);
}
int Sensors::detectBlack() {
	int whiteLevel = (luxLeft+luxRight)/2;
	blackMax = 1.5 * (luxCenter + whiteLevel) / 2;
	whiteMax = whiteLevel*2;
	return blackMax;
}
char Sensors::getColorCenter() {
	return this->getColorID(colorsCenter, luxCenter);
}
char Sensors::getColorLeft() {
	return this->getColorID(colorsLeft, luxLeft);
}
char Sensors::getColorRight() {
	return this->getColorID(colorsRight, luxRight);
}
int Sensors::getLuxCenter() {
	return luxCenter;
}
int Sensors::getLuxLeft() {
	return luxLeft;
}
int Sensors::getLuxRight() {
	return luxRight;
}