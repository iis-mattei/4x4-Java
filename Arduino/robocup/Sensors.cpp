#include <Arduino.h>
#include "Sensors.h"

const float Sensors::GREEN_MULTIPLIER = 1.2f;

Sensors::Sensors() {
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
void Sensors::readColorSensor(Adafruit_TCS34725& colorSensor, uint8_t addr, int (&colors)[3], int &lux) {
	uint16_t clear, red, green, blue;
	tcaSelect(addr);
	colorSensor.getRawData(&red, &green, &blue, &clear);
	colors[RED] = red;
	colors[GREEN] = green;
	colors[BLUE] = blue;
	lux = (int) colorSensor.calculateLux(red, green, blue);
}
char Sensors::getColorID(int colors[], int lux) {
//	if (colors[GREEN] > GREEN_MULTIPLIER * (colors[RED] + colors[BLUE]) / 2) {
	if (colors[GREEN] >= colors[RED]) {
			return COL_GREEN;
	} else if (lux < blackMax) {
		return COL_BLACK;
	} else if (lux < whiteMax) {
		return COL_WHITE;
	} else {
		return COL_SILVER;
	}
}
void Sensors::readAllColors() {
	this->readColorSensor(colorSensorC, CSCAddr, colorsCenter, luxCenter);
	this->readColorSensor(colorSensorL, CSLAddr, colorsLeft, luxLeft);
	this->readColorSensor(colorSensorR, CSRAddr, colorsRight, luxRight);
}
void Sensors::debugColors() {
	Serial.print("Left:\tRGB:");
	Serial.print(colorsLeft[RED]);
	Serial.print(" ");
	Serial.print(colorsLeft[GREEN]);
	Serial.print(" ");
	Serial.print(colorsLeft[BLUE]);
	Serial.print("\tLux:");
	Serial.print(luxLeft);
	Serial.print("\tCol:");
	Serial.println(getColorLeft());
	Serial.print("Center:\tRGB:");
	Serial.print(colorsCenter[RED]);
	Serial.print(" ");
	Serial.print(colorsCenter[GREEN]);
	Serial.print(" ");
	Serial.print(colorsCenter[BLUE]);
	Serial.print("\tLux:");
	Serial.print(luxCenter);
	Serial.print("\tCol:");
	Serial.println(getColorCenter());
	Serial.print("Right:\tRGB:");
	Serial.print(colorsRight[RED]);
	Serial.print(" ");
	Serial.print(colorsRight[GREEN]);
	Serial.print(" ");
	Serial.print(colorsRight[BLUE]);
	Serial.print("\tLux:");
	Serial.print(luxRight);
	Serial.print("\tCol:");
	Serial.println(getColorRight());
	delay(1000);
}
int Sensors::detectBlack() {
	int whiteLevel = (luxLeft+luxRight)/2;
	blackMax = (luxCenter + whiteLevel) / 2;
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
