#include <EEPROM.h>
#include "GravityTDS.h"
#include <Wire.h>
#include <SoftwareSerial.h>
 
#define TdsSensorPin A1
#define SensorPin 3
GravityTDS gravityTds;
SoftwareSerial BTserial(10, 11);

int sensorPin = A0;
float volt;
float ntu;
unsigned long int avgValue;
float b;
int buf[10],temp;
float VoutArray[] =  { 0.30769 ,20.00000, 40.00000 ,60.00000 ,120.61538 ,186.76923};
float  O2ConArray[] =  { 0.00018, 2.66129, 5.32258, 8.05300, 16.19851, 25.15367};
float temp1;
int tempPin = 0; 
float temperature = 25,tdsValue = 0;
 
void setup()
{
    Serial.begin(9600);
    gravityTds.setPin(TdsSensorPin);
    gravityTds.setAref(5.0);
    gravityTds.setAdcRange(1024);
    gravityTds.begin();
    pinMode(3,OUTPUT);  
    Serial.begin(9600);  
    Serial.println("Ready");
    BTserial.begin(9600);
}
 
void loop()
{
    volt = 0;
    if(volt < 2.5){
      ntu = 1.1204*(volt)*(volt)+5.7423*volt+4.35308; 
    }else{
      ntu=200;
    }
    gravityTds.setTemperature(temperature);
    gravityTds.update();
    tdsValue = gravityTds.getTdsValue();
    for(int i=0;i<10;i++)
    { 
      buf[i]=analogRead(SensorPin);
      delay(10);
    }
    for(int i=0;i<9;i++)
    {
      for(int j=i+1;j<10;j++)
      {
        if(buf[i]>buf[j])
        {
          temp=buf[i];
          buf[i]=buf[j];
          buf[j]=temp;
        }
      }
    }
    avgValue=0;
    for(int i=2;i<8;i++)
      avgValue+=buf[i];
    float phValue=(float)avgValue*5.0/1024/6;
    phValue=4.4*phValue;   
    temp1 = analogRead(tempPin);
    temp1 = temp1 * 0.15;
    Serial.println("-------------------------AQUACHECK APP----------------------------");
    Serial.println("TDS VALUE=");
    Serial.print(tdsValue,0);
    Serial.println("ppm");
    Serial.println("TURBIDITY VALUE=");
    Serial.print(ntu);
    Serial.print(" NTU\n");
    Serial.println("pH VALUE=");
    Serial.print(phValue,2);
    Serial.println(" ");
    digitalWrite(13, HIGH);
    delay(1);
    digitalWrite(13, LOW);
    Serial.print("Vout =");
    Serial.print(readO2Vout(A0));
    Serial.print(" V, Concentration of O2 is ");
    Serial.println(readConcentration(A0));
    Serial.print("TEMPERATURE = ");
    Serial.print(temp1);
    Serial.print(" *C");
    if(tdsValue==0){
      Serial.println(" ");
    }
    else if(tdsValue>0 && tdsValue<=150){
      Serial.println("\nThe Water is DRINKABLE");
    }
    else if(tdsValue>150 && tdsValue<=450){
      Serial.println("\nThe Water is REUSABLE");
    }
    else{
      Serial.println("\nThe Water is NOT FIT FOR USE");
    }
    Serial.println("\n");
    Serial.println("----------------------------**END**-------------------------------");
    Serial.println("\n");
    BTserial.println("--------AQUACHECK--------");
    String value1= "TDS: ";
    value1+=tdsValue;
    BTserial.println(value1);
    String value2= "TURBIDITY: ";
    value2+=ntu;
    BTserial.println(value2);
    String value3= "pH VALUE: ";
    value3+=phValue;
    BTserial.println(value3);
    String value4= "Vout: ";
    value4+=readO2Vout(A0);
    BTserial.println(value4);
    String value5= "O2 CONCENTRATION: ";
    value5+=readConcentration(A0);
    BTserial.println(value5);
    String value6= "TEMPERATURE: ";
    value6+=temp1;
    BTserial.println(value6);
    if(tdsValue==0){
      BTserial.println(" ");
    }
    else if(tdsValue>0 && tdsValue<=150){
      BTserial.println("\nThe Water is DRINKABLE");
    }
    else if(tdsValue>150 && tdsValue<=450){
      BTserial.println("\nThe Water is REUSABLE");
    }
    else{
      BTserial.println("\nThe Water is NOT FIT FOR USE");
    }
    BTserial.println("\n");
    delay(4000);
}
 
float round_to_dp( float in_value, int decimal_place )
{
  float multiplier = powf( 10.0f, decimal_place );
  in_value = roundf( in_value * multiplier ) / multiplier;
  return in_value;
}

float readO2Vout(uint8_t analogpin)
{
    float MeasuredVout = analogRead(A0) * (3.3 / 1023.0);
    return MeasuredVout;
 
}
 
float readConcentration(uint8_t analogpin)
{
    float MeasuredVout = analogRead(A0) * (3.3 / 1023.0);   
    float Concentration = FmultiMap(MeasuredVout, VoutArray,O2ConArray, 6);
    float Concentration_Percentage=Concentration*100;
 
    return Concentration_Percentage;
}
 
 
float FmultiMap(float val, float * _in, float * _out, uint8_t size)
{
  if (val <= _in[0]) return _out[0];
  if (val >= _in[size-1]) return _out[size-1];
  uint8_t pos = 1;
  while(val > _in[pos]) pos++;
  if (val == _in[pos]) return _out[pos];
  return (val - _in[pos-1]) * (_out[pos] - _out[pos-1]) / (_in[pos] - _in[pos-1]) + _out[pos-1];
}
