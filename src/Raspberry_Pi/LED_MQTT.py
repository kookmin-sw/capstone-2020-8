#-*- coding:utf-8 -*-
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import time

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
LED_All, LED_Res = 17, 18
GPIO.setup(LED_All, GPIO.OUT, initial = GPIO.LOW)
GPIO.setup(LED_Res, GPIO.OUT, initial = GPIO.LOW)

def led_pattern(ptn_type):
    
    if str(ptn_type) == "1":
        GPIO.output(LED_All, GPIO.HIGH)
    elif str(ptn_type) == "2":
        GPIO.output(LED_Res, GPIO.HIGH)
    else:
        GPIO.output(LED_All, GPIO.LOW)
        GPIO.output(LED_Res, GPIO.LOW)

# 서버로부터 CONNTACK 응답을 받을 때 호출되는 콜백
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    #client.subscribe("$SYS/#")
    client.subscribe("LED601") #구독 "LED2"

# 서버로부터 publish message를 받을 때 호출되는 콜백
def on_message(client, userdata, msg):
    #토픽과 메세지를 출력한다.
    print(str(msg.payload))
    led_pattern(msg.payload)

    
client = mqtt.Client() #client 오브젝트 생성
client.on_connect = on_connect #콜백설정
client.on_message = on_message #콜백설정

client.connect("192.168.137.1", 1883) 
#라즈베리파이3 MQTT 브로커에 연결
client.loop_forever()