#include "SoftwareWire.h"
#include "Adafruit_TCS34725.h"

#define TCAADDR 0x70	// Indirizzo I2C del MUX
#define CSLAddr 2		// Canale per il sensore sinistro
#define CSRAddr 5		// Canale per il sensore destro
#define CSCAddr 4		// Canale per il sensore centrale

const char COL_GREEN = 'g', COL_WHITE = 'w', COL_BLACK = 'b', COL_SILVER = 's';

class Sensors {
public:
	Sensors();
	int detectBlack();
	void readAllColors();
	char getColorCenter();
	char getColorLeft();
	char getColorRight();
	int getLuxCenter();
	int getLuxLeft();
	int getLuxRight();
	void debugColors();

private:
	static const int RED = 0, GREEN = 1, BLUE = 2;
	static const float GREEN_MULTIPLIER;
	Adafruit_TCS34725 colorSensorL, colorSensorR, colorSensorC;
	int colorsLeft[3], colorsCenter[3], colorsRight[3];
	int luxLeft, luxCenter, luxRight;
	int blackMax, whiteMax;
	void tcaSelect(uint8_t addr);
	char getColorID(int colors[], int lux);
	void readColorSensor(Adafruit_TCS34725& colorSensor, uint8_t addr, int (&colors)[3], int &lux);
};
