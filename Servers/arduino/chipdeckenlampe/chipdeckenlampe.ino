#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <FastLED.h>
#include "OneWire.h"
#include "DallasTemperature.h"

#define ONE_WIRE_BUS 14
#define COLUMNS 41
#define ROWS 24
#define ROWS_PER_PACKET 4
#define NUM_LEDS COLUMNS*ROWS
#define DATA_PIN 12
#define CLOCK_PIN 13

byte packetBuffer[512];
const char* ssid = "Ultra-LED";
const char* password = "wertertzu";  // set to "" for open access point w/o passwortd

boolean overHeat = false;

CRGB leds[NUM_LEDS];
int offset = 0;
int ledSendAmount = 0;
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
WiFiUDP Udp;

DeviceAddress ds1, ds2, ds3, ds4;
boolean bds1, bds2, bds3, bds4;

void setup() {
  Serial.begin(115200);

  FastLED.addLeds<APA102, DATA_PIN, CLOCK_PIN, RGB, DATA_RATE_MHZ(40)>(leds, NUM_LEDS);
  FastLED.clear();
  FastLED.show();
  
  sensors.begin();
  Serial.print("Found ");
  Serial.print(sensors.getDeviceCount(), DEC);
  Serial.println(" devices.");
  sensors.getAddress(ds1, 0);
  sensors.getAddress(ds2, 1);
  sensors.getAddress(ds3, 2);
  sensors.getAddress(ds4, 3);

  sensors.setHighAlarmTemp(ds1, 50);
  sensors.setHighAlarmTemp(ds2, 50);
  sensors.setHighAlarmTemp(ds3, 50);
  sensors.setHighAlarmTemp(ds4, 50);

  sensors.setLowAlarmTemp(ds1, 5);
  sensors.setLowAlarmTemp(ds2, 5);
  sensors.setLowAlarmTemp(ds3, 5);
  sensors.setLowAlarmTemp(ds4, 5);

  sensors.setWaitForConversion(false);

  Serial.println("Pre WIFI");

  WiFi.persistent(false);
  WiFi.mode(WIFI_AP);
  boolean result = WiFi.softAP(ssid, password);
  if (result == true) {
    Serial.println("Ready");
    Udp.begin(10000);
  } else {
    Serial.println("Failed!");
  }

  Serial.println("Setup complete");
}



int sensor = 0;
void loop() {
  int noBytes = Udp.parsePacket();
  if (noBytes > 0) {
    Udp.read(packetBuffer, noBytes);
    if (noBytes == 1 + (COLUMNS * ROWS_PER_PACKET * 3)) {
      int startRow = packetBuffer[0];
      int startIndex = COLUMNS * startRow;
      boolean reached = false;
      for (int i = 0 ; i < COLUMNS * ROWS_PER_PACKET; i++) {
        int row = (i + startIndex) / COLUMNS;
        int column = i % COLUMNS;
        if (row % 2 == 1) {
          leds[(COLUMNS - 1 - column) + row * COLUMNS].red = packetBuffer[1 + i * 3];
          leds[(COLUMNS - 1 - column) + row * COLUMNS].green = packetBuffer[1 + i * 3 + 1];
          leds[(COLUMNS - 1 - column) + row * COLUMNS].blue = packetBuffer[1 + i * 3 + 2];
        } else {
          leds[column + row * COLUMNS].red = packetBuffer[1 + i * 3];
          leds[column + row * COLUMNS].green = packetBuffer[1 + i * 3 + 1];
          leds[column + row * COLUMNS].blue = packetBuffer[1 + i * 3 + 2];
        }
        reached = (row == 23);
      }
      if(reached){
        FastLED.show(); 
      }
    } else {
      Serial.print("different packetsize , got ");
      Serial.print(noBytes);
      Serial.print(" But expected ");
      Serial.println(1 + (COLUMNS * ROWS_PER_PACKET * 3));
    }
  }

  sensor++;
  if (sensor == 0) {
    sensors.requestTemperatures();
  }
  if (sensor == 10) {
    bds1 = sensors.hasAlarm(ds1);
    if (bds1) {
      float tempC = sensors.getTempC(ds1);
      Serial.print("ds1 Temp C: ");
      Serial.println(tempC);
    }
  }
  if (sensor == 20) {
    bds2 = sensors.hasAlarm(ds2);
    if (bds2) {
      float tempC = sensors.getTempC(ds2);
      Serial.print("ds2 Temp C: ");
      Serial.println(tempC);
    }
  }
  if (sensor == 30) {
    bds3 = sensors.hasAlarm(ds3);
    if (bds3) {
      float tempC = sensors.getTempC(ds3);
      Serial.print("ds3 Temp C: ");
      Serial.println(tempC);
    }
  }
  if (sensor == 40) {
    bds4 = sensors.hasAlarm(ds4);
    if (bds4) {
      float tempC = sensors.getTempC(ds4);
      Serial.print("ds4 Temp C: ");
      Serial.println(tempC);
    }
    sensor = -1;
  }

  overHeat = bds1 || bds2 || bds3 || bds4;

  if (overHeat) {
    for (int led = 0; led < NUM_LEDS; led++) {
      leds[led].red = 5;
      leds[led].green = 5;
      leds[led].blue = 5;
    }
    FastLED.show();
  }
}


