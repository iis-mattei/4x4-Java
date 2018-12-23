#include "Sensors.h"

Sensors::Sensors() {
	colorSensorL = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_24MS, TCS34725_GAIN_1X);
	colorSensorR = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_24MS, TCS34725_GAIN_1X);
	colorSensorC = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_24MS, TCS34725_GAIN_1X);
	Wire1.begin();
}
char Sensors::getColorLeft() {
	tcaSelect(CSLAddr);
	delay(10);
	return this->ReadColorSensor(colorSensorL);
}
char Sensors::getColorRight() {
	tcaSelect(CSRAddr);
	delay(10);
	return this->ReadColorSensor(colorSensorR);
}
char Sensors::getColorCenter() {
	tcaSelect(CSCAddr);
	delay(10);
	return this->ReadColorSensor(colorSensorC);
}

void Sensors::tcaSelect(uint8_t addr) {
  Wire1.beginTransmission(TCAADDR);
  Wire1.write(1 << addr);
  Wire1.endTransmission();
}

char Sensors::getColorID(int colors[]) {
    const int RED=0, GREEN=1, BLUE=2;
    const int blackMax = 120;
    const int whiteMax = 200;
    const float greenMultiplier = 1.7;
    float brightness = (colors[RED] + colors[GREEN] + colors[BLUE])/3;
    if(brightness < blackMax) {
    	if(colors[GREEN] > greenMultiplier * (colors[RED]+colors[BLUE])/2) {
    		return COL_GREEN;
    	} else {
    		return COL_BLACK;
    	}
    } else if(brightness < whiteMax) {
    	return COL_WHITE;
    } else {
    	return COL_SILVER;
    }
    /*
    const int Red_min[] = {230, 40, 10} , Red_max[] = {350,90, 70};
    const int White_min[] = {120, 120, 120}, White_max[] = {200 ,350, 350};
    const int Green_min[] = {40, 70, 30}, Green_max[] = {50, 82, 50};
    const int Black_min[] = {0, 0, 0}, Black_max[] = {120, 120, 120};
    const int Silver_min[] = {350, 350, 350}, Silver_max[] = {1000, 1000, 1000};
	// se i valori sono compresi nel cuboide di uno dei colori ....
	if (colors[RED]>=Red_min[RED] && colors[RED]<Red_max[RED] &&
	colors[GREEN]>=Red_min[GREEN] && colors[GREEN]<Red_max[GREEN] &&
	colors[BLUE]>=Red_min[BLUE] && colors[BLUE]<Red_max[BLUE])
		return COL_RED;
	else if (colors[RED]>=White_min[RED] && colors[RED]<White_max[RED] &&
	colors[GREEN]>=White_min[GREEN] && colors[GREEN]<White_max[GREEN] &&
	colors[BLUE]>=White_min[BLUE] && colors[BLUE]<White_max[BLUE])
		return COL_WHITE;
	else if (colors[RED]>=Green_min[RED] && colors[RED]<Green_max[RED] &&
	colors[GREEN]>=Green_min[GREEN] && colors[GREEN]<Green_max[GREEN] &&
	colors[BLUE]>=Green_min[BLUE] && colors[BLUE]<Green_max[BLUE])
		return COL_GREEN;
	else if (colors[RED]>=Black_min[RED] && colors[RED]<Black_max[RED] &&
	colors[GREEN]>=Black_min[GREEN] && colors[GREEN]<Black_max[GREEN] &&
	colors[BLUE]>=Black_min[BLUE] && colors[BLUE]<Black_max[BLUE])
		return COL_BLACK;
	else if (colors[RED]>=Silver_min[RED] && colors[RED]<Silver_max[RED] &&
	colors[GREEN]>=Silver_min[GREEN] && colors[GREEN]<Silver_max[GREEN] &&
	colors[BLUE]>=Silver_min[BLUE] && colors[BLUE]<Silver_max[BLUE])
		return COL_SILVER;
	else
		return COL_UNKNOWN; */
}

char Sensors::ReadColorSensor(Adafruit_TCS34725& colorSensor) {
  uint16_t clear, red, green, blue;
  int colors[3];

  colorSensor.getRawData(&red, &green, &blue, &clear);
  colors[0] = red;
  colors[1] = green;
  colors[2] = blue;
  return this->getColorID(colors);
}
