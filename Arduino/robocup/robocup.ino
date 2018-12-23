#include <Wire.h>
#include <SoftwareWire.h>
#include <Sensors.h>
#define SLAVE_ADDRESS 0x04
byte request;
Sensors *sensors;
char colL, colR, colC;

void setup() {
  sensors = new Sensors();
  Serial.begin(9600);
  Wire.begin(SLAVE_ADDRESS);
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
  Serial.println("Ready!");
}

void loop() {
  colL = sensors->getColorLeft();
  colC = sensors->getColorCenter();
  colR = sensors->getColorRight();

}

void receiveData(int byteCount) {
    while(Wire.available()>0) {
      request=Wire.read(); 
    }
}

void sendData(){
  if(request == 'L'){
     Wire.write(colL);
     //Serial.println("Fatto");
     //Serial.println(colL);
  }

  if(request == 'C'){
     Wire.write(colC);
     //Serial.println("Fatto");
     //Serial.println(colC);
  }

  if(request == 'R'){
     Wire.write(colR);
     //Serial.println("Fatto");
     //Serial.println(colR);
  }

  if(request == 'A') {
    Wire.write(colL);
    Wire.write(colC);
    Wire.write(colR);
  }

}
