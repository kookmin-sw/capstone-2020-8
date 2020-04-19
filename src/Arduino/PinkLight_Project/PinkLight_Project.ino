#include <ESP8266WiFi.h>
#include "PubSubClient.h"
#include <DHT11.h>

char ssid[] = "와이파이 SSID";
char password[] = "와이파이 비밀번호";

byte server1[] = {0, 0, 0, 0}; // MQTT 브로커 IP
int port = 1883; // MQTT 브로커 PORT 번호

DHT11 dht11(4); // DHT11 온습도 센서에 아두이노 4번 핀 할당
WiFiClient client; // WiFi 정보를 넣어주도록 client 선언

// 서버에서 받은 조작데이터를 처리하는 함수(메세지 리시버)
void msgReceived(char *topic, byte *payload, unsigned int uLen){
  char pBuffer[uLen + 1]; //서버에서 날아온 Payload 데이터를 담을 수 있는 버퍼

  int i;
  for(i=0; i < uLen; i++){
    pBuffer[i] = payload[i];
  }
  pBuffer[i] = NULL; // 데이터의 끝을 알려주는 NULL
  
  if(pBuffer[0] == '1'){
    digitalWrite(14, HIGH);
  }
  else if(pBuffer[0] == '2'){
    digitalWrite(14, LOW);
  }  // 1 이면 LED 켜고 2 라면 LED를 끈다.
}

// MQTT 브로커 접속을 위한 선언
PubSubClient mqttClient(server1, port, msgReceived, client);

void setup() {
  pinMode(14, OUTPUT); // LED 조작을 위한 핀번호 선언

  Serial.begin(115200); // WeMos 보드는 시리얼통신속도가 9600이 아니라 115200
  delay(10); // 시리얼 통신을 위한 딜레이
  
  Serial.println("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);

  while(WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("...");
  }

  Serial.println("WiFi is Connected");
  Serial.println(WiFi.localIP());

  //MQTT 서버 접속
  if(mqttClient.connect("Arduino")){ // MQTT 서버에 접속할 때 나타나는 이름
    Serial.println("MQTT Broker Connected");
    mqttClient.subscribe("Led"); // Led 토픽을 구독하는 MQTT 구독자 등록
  }
}

void loop() {
  mqttClient.loop();

  float tmp, hum; // 온도, 습도 
  int err = dht11.read(hum, tmp);

  if(err == 0){ // 데이터가 제대로 입력받아졌으면
    char message[64] = "", pTmpBuf[50], pHumBuf[50];
    dtostrf(tmp, 5, 2, pTmpBuf);
    dtostrf(hum, 5, 2, pHumBuf); // tmp, hum를 5자리까지, 소숫점 2째 자리에서 자르기

    sprintf(message, "{\"tmp\":%s, \"hum\":%s}", pTmpBuf, pHumBuf); // Json 구조로 만들기

    mqttClient.publish("DHT11", message);
  }
  delay(1000);
}
