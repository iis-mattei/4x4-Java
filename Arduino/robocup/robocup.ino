#include <Wire.h>
#include <Sensors.h>
// #include <NewPing.h>

#define SLAVE_ADDRESS 0x04
#define FWD_RIGHT 12
#define FWD_LEFT 11
#define BACK_RIGHT 10
#define BACK_LEFT 9
#define SILVER 8
// #define Trigger_U_Ant_I 7
// #define Echo_U_Ant_I 6
// #define Max_Distance 100

Sensors *sensors;
byte request;
int lSilver;

// long dist;
// Trigger_U_Ant_S= , Trigger_U_Post= , Trigger_U_Dx= , Trigger_U_Sx= ;
// Echo_U_Ant_S= , Echo_U_Post= , Echo_U_Dx= , Echo_U_Sx= ;
// NewPing U_Ant_S(Trigger_U_Ant_S, Echo_U_Ant_S, Max_Distance);
// NewPing U_Post(Trigger_U_Post, Echo_U_Post, Max_Distance);
// NewPing U_Dx(Trigger_U_Dx, Echo_U_Dx, Max_Distance);
// NewPing U_Sx(Trigger_U_Sx, Echo_U_Sx, Max_Distance);


void setup() {
  sensors = new Sensors(1.2); // greenMultiplier
  pinMode(FWD_RIGHT, INPUT_PULLUP);
  pinMode(FWD_LEFT, INPUT_PULLUP);
  pinMode(BACK_RIGHT, INPUT_PULLUP);
  pinMode(BACK_LEFT, INPUT_PULLUP);
  pinMode(SILVER, INPUT_PULLUP);
  Serial.begin(9600);
  Wire.begin(SLAVE_ADDRESS);
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
  Serial.println("Ready!");
}

void loop() {
  sensors->readAllColors();
  // lSilver = digitalRead(SILVER);
}

void receiveData(int byteCount) {
  while (Wire.available() > 0) {
    request = Wire.read();
  }
}

void sendData() {
  if (request == 'B') {  // rilevazione livello del nero
    int blackLevel = sensors->detectBlack();
    Wire.write(lowByte(blackLevel));
    Wire.write(highByte(blackLevel));
  } else if (request == 'C') {  // checkColors: lux + colori
    Wire.write(lowByte(sensors->getLuxLeft()));
    Wire.write(highByte(sensors->getLuxLeft()));
    Wire.write(lowByte(sensors->getLuxRight()));
    Wire.write(highByte(sensors->getLuxRight()));
    Wire.write(sensors->getColorLeft());
    Wire.write(sensors->getColorCenter());
    Wire.write(sensors->getColorRight());
  }


  if (request == 'T') {
    if (digitalRead(FWD_RIGHT) == HIGH) {
      //Serial.println("Anteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(FWD_LEFT) == HIGH) {
      //Serial.println("Anteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(BACK_RIGHT) == HIGH) {
      //Serial.println("Posteriore Destro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
    if (digitalRead(BACK_LEFT) == HIGH) {
      //Serial.println("Posteriore Sinistro premuto");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  if (request == 'S') {
    if (lSilver == LOW) {
      //Serial.println("Argento Trovato");
      Wire.write(true);
    } else {
      Wire.write(false);
    }
  }

  // if(request=='C'){
  //   dist=U_Ant_I.ping_cm();
  //   Wire.write(dist);
  // }
}
