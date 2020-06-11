#-*- coding:utf-8 -*-
#!/usr/bin/env python3

from rpi_ws281x import *
import argparse
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import time

# LED configuration:
LED_COUNT      = 16      # Number of LED pixels.
LED_PIN        = 18      # GPIO pin connected to the pixels (18 uses PWM!).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 10      # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255     # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL    = 0       # set to '1' for GPIOs 13, 19, 41, 45 or 53

strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
strip.begin()


def led_pattern(ptn_type):

    if str(ptn_type) == "b'1'":
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(255, 165, 0))
        strip.show()
        time.sleep(0.25)
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(0,0,0))
        strip.show()
        time.sleep(0.25)
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(255, 165, 0))
        strip.show()

    elif str(ptn_type) == "b'2'":
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(255, 0, 0))
        strip.show()
        time.sleep(0.25)
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(0,0,0))
        strip.show()
        time.sleep(0.25)
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, Color(255, 0, 0))
        strip.show()

    elif str(ptn_type) == "b'0'":
        for i in range(3):
            for j in range(strip.numPixels()):
                strip.setPixelColor(j, Color(0,0,0))
            strip.show()

# 서버로부터 CONNTACK 응답을 받을 때 호출되는 콜백
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("LED601") #구독 "LED601"

# 서버로부터 publish message를 받을 때 호출되는 콜백
def on_message(client, userdata, msg):
    #토픽과 메세지를 출력한다.
    print(str(msg.payload))
    led_pattern(msg.payload)


if __name__ == '__main__':
    # Create NeoPixel object with appropriate configuration.
    # Intialize the library (must be called once before other functions).

    client = mqtt.Client() #client 오브젝트 생성
    client.on_connect = on_connect #콜백설정
    client.on_message = on_message #콜백설정

    client.connect("192.168.137.1", 1883)
    #라즈베리파이3 MQTT 브로커에 연결
    client.loop_forever()