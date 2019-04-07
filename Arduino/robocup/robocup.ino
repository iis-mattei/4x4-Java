// Librerie custom, nella stessa directory del file .ino
#include "Sensors.h"
#include "MPU6050_4x4.h"
#include "SoftwareWire.h"

// Librerie di sistema
#include <NewPing.h>
#include <Wire.h>

#define SLAVE_ADDRESS 0x04
#define FWD_RIGHT 12
#define FWD_LEFT 11
#define BACK_RIGHT 10
#define BACK_LEFT 9
//  #define SILVER 13
#define Trigger 7
#define Echo 6
#define Max_Distance 150

long timer = 0;
byte request;
//  int lSilver;
int gyroX, gyroY, gyroZ, dist;
boolean rescueLineMode;

Sensors *sensors;
NewPing ultra(Trigger, Echo, Max_Distance);
MPU6050 mpu6050(Wire2);

void setup() {
  sensors = new Sensors();
  rescueLineMode = true;
  pinMode(FWD_RIGHT, INPUT_PULLUP);
  pinMode(FWD_LEFT, INPUT_PULLUP);
  pinMode(BACK_RIGHT, INPUT_PULLUP);
  pinMode(BACK_LEFT, INPUT_PULLUP);
  //  pinMode(SILVER, INPUT_PULLUP);

  Serial.begin(9600);
  Wire.begin(SLAVE_ADDRESS);
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
  Serial.println("Ready!");

  // Per il test dei sensori di colore
  //  sensors->readAllColors();
  //  int blackLevel = sensors->detectBlack();
  //  Serial.println(blackLevel);
  Wire2.begin();
  mpu6050.begin();
  mpu6050.calcGyroOffsets(true);
}

void loop() {
  if (rescueLineMode) {
    sensors->readAllColors();
    //    sensors->debugColors();
  }

  dist = ultra.ping_cm();

  mpu6050.update();
  if (millis() - timer > 1000) {
    gyroX = (int) mpu6050.getAngleX();
    gyroY = (int) mpu6050.getAngleY();
    gyroZ = (int) mpu6050.getAngleZ();
    //     Serial.print("angleX : ");Serial.print(gyroX);
    //     Serial.print("\tangleY : ");Serial.print(gyroY);
    //     Serial.print("\tangleZ : ");Serial.println(gyroZ);
    timer = millis();
  }
}

void receiveData(int byteCount) {
  while (Wire.available() > 0) {
    request = Wire.read();
  }
}

/* Comandi per il dialogo con Arduino:
   B = rilevazione del livello del nero
   C = checkColors: lux + colori di tutti i sensori
   D = misurazione distanza con sensore a ultrasuoni
   G = dati giroscopio
   R = reset giroscopio
   T = tutti i 4 sensori di tocco
   Z = attiva modalità zona vittime
   L = attiva modalità seguilinea
*/
void sendData() {
  if (request == 'B') {
    int blackLevel = sensors->detectBlack();
    Wire.write(lowByte(blackLevel));
    Wire.write(highByte(blackLevel));
  } else if (request == 'C') {
    Wire.write(lowByte(sensors->getLuxLeft()));
    Wire.write(highByte(sensors->getLuxLeft()));
    Wire.write(lowByte(sensors->getLuxCenter()));
    Wire.write(highByte(sensors->getLuxCenter()));
    Wire.write(lowByte(sensors->getLuxRight()));
    Wire.write(highByte(sensors->getLuxRight()));
    Wire.write(sensors->getColorLeft());
    Wire.write(sensors->getColorCenter());
    Wire.write(sensors->getColorRight());
  } else if (request == 'D') {
    //     Serial.println(dist);
    Wire.write(lowByte(dist));
    Wire.write(highByte(dist));
  } else if (request == 'G') {
    Wire.write(lowByte(gyroX));
    Wire.write(highByte(gyroX));
    Wire.write(lowByte(gyroY));
    Wire.write(highByte(gyroY));
    Wire.write(lowByte(gyroZ));
    Wire.write(highByte(gyroZ));
  } else if(request == 'R') {
    mpu6050.reset();
    Wire.write(true);
  } else if (request == 'T') {
    if (digitalRead(FWD_RIGHT) == HIGH) {
      //      Serial.println("Anteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(FWD_LEFT) == HIGH) {
      //      Serial.println("Anteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(BACK_RIGHT) == HIGH) {
      //      Serial.println("Posteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(BACK_LEFT) == HIGH) {
      //      Serial.println("Posteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  } else if (request == 'Z') {
    rescueLineMode = false;
    Wire.write(true);
  } else if (request == 'L') {
    rescueLineMode = true;
    Wire.write(true);
  }
}
