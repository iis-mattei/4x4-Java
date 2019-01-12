#include <SoftwareWire.h>
#include <Adafruit_TCS34725.h>

#define TCAADDR 0x70
#define CSLAddr 2
#define CSRAddr 5
#define CSCAddr 4

const char COL_RED='r', COL_GREEN='g', COL_WHITE='w', COL_BLACK='b', COL_SILVER='s', COL_UNKNOWN='u';

class Sensors {
	public :
		Sensors();
		char getColorRight();
		char getColorLeft();
		char getColorCenter();
		int getLuxLeft();
		int getLuxRight();
	private :
		Adafruit_TCS34725 colorSensorL, colorSensorR, colorSensorC;
		void tcaSelect(uint8_t addr);
		char getColorID(int colors[]);
		char ReadColorSensor(Adafruit_TCS34725& colorSensor);
};
