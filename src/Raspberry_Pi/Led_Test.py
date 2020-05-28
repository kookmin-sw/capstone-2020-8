import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
LED = 17
GPIO.setup(LED, GPIO.OUT, initial = GPIO.LOW)

# 서버로부터 CONNTACK 응답을 받을 때 호출되는 콜백
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    #client.subscribe("$SYS/#")
    client.subscribe("1LED") #구독 "LED"

# 서버로부터 publish message를 받을 때 호출되는 콜백
def on_message(client, userdata, msg):
    print(str(msg.payload)) 
    #토픽과 메세지를 출력한다.

    if str(msg.payload) == "b'1'":
        GPIO.output(LED, GPIO.HIGH)
        time.sleep(1)
        GPIO.output(LED, GPIO.LOW)
        time.sleep(1)
        GPIO.output(LED, GPIO.HIGH)
    else:
        GPIO.output(LED, GPIO.LOW)
    
client = mqtt.Client() #client 오브젝트 생성
client.on_connect = on_connect #콜백설정
client.on_message = on_message #콜백설정

client.connect("192.168.137.1", 1883) 
#라즈베리파이3 MQTT 브로커에 연결
client.loop_forever()