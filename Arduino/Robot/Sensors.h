#include <SoftwareWire.h>
#include <Adafruit_TCS34725.h>

#define TCAADDR 0x70
#define CSLAddr 2
#define CSRAddr 5
#define CSCAddr 4

const char COL_GREEN = 'g', COL_WHITE = 'w', COL_BLACK = 'b', COL_SILVER = 's';

class Sensors {
public:
	Sensors(float greenMultiplier);
	int detectBlack();
	void readAllColors();
	char getColorCenter();
	char getColorLeft();
	char getColorRight();
	int getLuxCenter();
	int getLuxLeft();
	int getLuxRight();

private:
	const int RED = 0, GREEN = 1, BLUE = 2;
	Adafruit_TCS34725 colorSensorL, colorSensorR, colorSensorC;
	int colorsLeft[3], colorsCenter[3], colorsRight[3];
	int luxLeft, luxCenter, luxRight;
	int blackMax, whiteMax;
	float greenMultiplier;
	void tcaSelect(uint8_t addr);
	char getColorID(int colors[], int lux);
	void readSensor(Adafruit_TCS34725& colorSensor, uint8_t addr, int (&colors)[3], int &lux);
};
