#include <Wire.h>
#include <SoftwareWire.h>
#include <Sensors.h>
//#include <NewPing.h>
#define SLAVE_ADDRESS 0x04
#define AntDx 12
#define AntSx 11
#define PostDx 10
#define PostSx 9
#define Argento 8
// #define Trigger_U_Ant_I 7
// #define Echo_U_Ant_I 6
//#define Max_Distance 100
Sensors *sensors;
byte request;
int L_Argento;
char colL, colR, colC;
int Delta;
int luxL, luxR;
//long dist;
//  Trigger_U_Ant_S= , Trigger_U_Post= , Trigger_U_Dx= , Trigger_U_Sx= ;
//  Echo_U_Ant_S= , Echo_U_Post= , Echo_U_Dx= , Echo_U_Sx= ;
// NewPing U_Ant_S(Trigger_U_Ant_S, Echo_U_Ant_S, Max_Distance);
// NewPing U_Post(Trigger_U_Post, Echo_U_Post, Max_Distance);
// NewPing U_Dx(Trigger_U_Dx, Echo_U_Dx, Max_Distance);
// NewPing U_Sx(Trigger_U_Sx, Echo_U_Sx, Max_Distance);


void setup() {
  sensors = new Sensors();
  pinMode(AntDx, INPUT_PULLUP);
  pinMode(AntSx, INPUT_PULLUP);
  pinMode(PostDx, INPUT_PULLUP);
  pinMode(PostSx, INPUT_PULLUP);
  pinMode(Argento, INPUT_PULLUP);
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
  L_Argento=digitalRead(Argento);
  luxR=sensors->getLuxRight();
  luxL=sensors->getLuxLeft();
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

  if(request == 'H') {
    Wire.write(colL);
    Wire.write(colC);
    Wire.write(colR);
  }

  if(request == 'X'){
    if(digitalRead(AntDx)==HIGH){
      //Serial.println("Anteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  if(request == 'Y'){
    if(digitalRead(AntSx)==HIGH){
      //Serial.println("Anteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  if(request == 'K'){
    if(digitalRead(PostDx)==HIGH){
      //Serial.println("Posteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  if(request == 'Z'){
    if(digitalRead(PostSx)==HIGH){
      //Serial.println("Posteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  if(request == 'A'){
    if(L_Argento==LOW){
      //Serial.println("Argento Trovato");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }


  if(request=='S'){
    Wire.write(luxL);
  }

  if(request=='D'){
    Wire.write(luxR);
  }

  // if(request=='C'){
  //   dist=U_Ant_I.ping_cm();
  //   Wire.write(dist);
  // }





}
