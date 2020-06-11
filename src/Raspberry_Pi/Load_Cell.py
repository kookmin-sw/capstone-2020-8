import time
import sys
import paho.mqtt.client as mqtt

EMULATE_HX711=False

referenceUnit = 21500 # 21500

if not EMULATE_HX711:
    import RPi.GPIO as GPIO
    from hx711 import HX711
else:
    from emulated_hx711 import HX711

def cleanAndExit():
    print("Cleaning...")

    if not EMULATE_HX711:
        GPIO.cleanup()
        
    print("Bye!")
    sys.exit()

hx = HX711(20, 16)
hx.set_reading_format("MSB", "MSB")

hx.set_reference_unit(referenceUnit)
hx.reset()
hx.tare()

mqtt = mqtt.Client("Pi1") # MQTT Client Name
mqtt.connect("192.168.137.1", 1883) # MQTT Broker Add

isSit = "false"
mqtt.publish("Pi1", "{\"s1_isSit\":false}") # initialization

while True:
    try:
        val = hx.get_weight(5)
        print(val)
        
        if val > 20: # load over 20Kg
            if isSit != "true":
                mqtt.publish("Pi1", "{\"s1_isSit\":true}")
                isSit = "true"
        else: # load under 20Kg
            if isSit != "false":
                mqtt.publish("Pi1", "{\"s1_isSit\":false}")
                isSit = "false"
        
        hx.power_down()
        hx.power_up()
        time.sleep(1)

    except (KeyboardInterrupt, SystemExit):
        cleanAndExit()
        
mqtt.loop(5)
