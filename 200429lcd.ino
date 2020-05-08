#include <Wire.h>
#include <LiquidCrystal_I2C.h>

#define satPos 16,3
#define pulPos 2,0
#define rrgPos 3,2
#define rrdPos 7,2
#define temPos 4,3
/*
 * Puls: 0-256bpm
 * Oddech: czas wdechu | przerwa1 | czas wydechu | przerwa 2
 * Zastawki: nie mam kurwa pojecia
 * Cisnienie: skurcz 0-256 + rozkurcz 0-256
 * Temperatura: 0-512, (* 0,1 stopnia)
 * Saturacja 0-100 [%]
 */
LiquidCrystal_I2C lcd(0x3F, 20, 4);

char str1[]="     bpm   ODDECH  ";
char str2[]="ZASTAWKI xxxxx     ";
char str3[]="RR    /            ";
char str4[]="TEMP 36.6 SATUR  90%";

byte puls[] = {
  B00000,
  B00000,
  B01010,
  B11111,
  B11111,
  B01110,
  B00100,
  B00000
};

byte kutas[] = {
  B00100,
  B01010,
  B01010,
  B01010,
  B01010,
  B01010,
  B10101,
  B01010
};

byte wde[] = {
  B00100,
  B01110,
  B11111,
  B00100,
  B00100,
  B00100,
  B00100,
  B00100
};

byte wyd[] = {
  B00100,
  B00100,
  B00100,
  B00100,
  B00100,
  B11111,
  B01110,
  B00100
};



//zastawki

int okr;

int sat = 15;
int pul = 60;
unsigned int wdech = 2500;
unsigned int wydech = 2500;

unsigned long start;
unsigned long start_puls;

int cisnienie_g = 120;
int cisnienie_d = 80;

double temp = 36.6;

String data = "";

void setup(){
  Serial.begin(9600);
  
  lcd.init();
  lcd.setBacklight(1);
  lcd.noAutoscroll();
  lcd.leftToRight();
  lcd.noBlink();  
  lcd.noCursor();
  
  lcd.setCursor(0,0);
  lcd.print(str1);
  lcd.setCursor(0,1);
  lcd.print(str2);
  lcd.setCursor(0,2);
  lcd.print(str3);
  lcd.setCursor(0,3);
  lcd.print(str4);

  lcd.createChar(0, puls);
  lcd.createChar(1, kutas);
  lcd.createChar(2, wde);
  lcd.createChar(3, wyd);

  start = millis();
}



void loop(){

  if(Serial.available() > 0){
    data = Serial.readStringUntil('#');
    decodeString(data);
  }
// oddech
  if (millis() - start <= wdech){
    lcd.setCursor(18, 0);
    lcd.write(2);
  } else if (millis() - start < (wdech + wydech)){
    lcd.setCursor(18, 0);
    lcd.write(3);
  } 
  if (millis() - start >= (wdech + wydech)){
    start = millis();
    lcd.setCursor(18, 0);
    lcd.write(2);
  }

//serduszko

  okr =  60000 / pul;

  if(millis() - start_puls > okr){
    start_puls = millis();

  } else if (millis() - start_puls <= (okr/2) ){
    lcd.setCursor (0, 0);
    lcd.write(0);
  } else {
    lcd.setCursor(0, 0);
    lcd.print (" ");
  }

//dane
  
  writeNumber(sat, satPos);
  writeNumber(pul, pulPos);

  writeNumber(cisnienie_g, rrgPos);
  writeNumber(cisnienie_d, rrdPos);

  writeNumber(temp, temPos);
  
}
void writeNumber(int num, int pos1, int pos2){
  lcd.setCursor (pos1, pos2);
  if(num < 100){
    lcd.print(" ");
    if(num < 10){
      lcd.print(" ");  
    }
  }
  lcd.print(num);
}


void decodeString(String data){
  
  char a = data.charAt(0);
  String v = data.substring(1,4);
  int val = v.toInt();
  
  switch (a){
    case 'A':
      pul = val;
      break;

    case 'B':
      wdech = val*10;
      break;

    case 'C':
      wydech = val*10;
      break;

    case 'D':
      cisnienie_g = val;
      break;

    case 'E':
      cisnienie_d = val;
      break;

    case 'F':
      temp = (double)val/10.0;
      break;

    case 'G':
      if(val > 100){
        val = val % 101;
      }
      sat = val;
      break;
  }

  if (data[4]=='!'){
    decodeString(data.substring(5));
  } 

  return;  
  
}
