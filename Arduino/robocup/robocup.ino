#include <Motors.h>
#include <Sensors.h>

#define S_START 1
#define S_FORWARD 2
#define S_TURN_LEFT 3
#define S_TURN_RIGHT 4
#define SPEED_STD 70

int status = S_START;
Motors *motors;
Sensors *sensors;

void setup() {
  Serial.begin(9600);
  Serial.println("Avvio del robot...");  
  motors = new Motors();
  sensors = new Sensors();
}

void loop() {
  char colL = sensors->getColorLeft();
  char colC = sensors->getColorCenter();
  char colR = sensors->getColorRight();
  if(status == S_START) {
  	if(colL == COL_WHITE && colC == COL_BLACK && colR == COL_WHITE) {	// Vai dritto
  		status = S_FORWARD;
  		motors->travel(SPEED_STD);
  	}
  } else if(status == S_FORWARD) {
  	if(colL == COL_WHITE && colC == COL_WHITE && colR == COL_BLACK) {	// Curva a destra
  		status = S_TURN_RIGHT;
  		motors->spin(SPEED_STD, -1);
  	} else if(colL == COL_BLACK && colC == COL_WHITE && colR == COL_WHITE) {	// Curva a sinistra
  		status = S_TURN_LEFT;
  		motors->spin(SPEED_STD, 1);
  	}
  } else if(status == S_TURN_LEFT) {
  	if(colL == COL_WHITE && colR == COL_WHITE) {	// Vai dritto
  		status = S_FORWARD;
  		motors->travel(SPEED_STD);
  	}
  } else if(status == S_TURN_RIGHT) {
  	if(colL == COL_WHITE && colR == COL_WHITE) {	// Vai dritto
  		status = S_FORWARD;
  		motors->travel(SPEED_STD);
  	}
  }
}
