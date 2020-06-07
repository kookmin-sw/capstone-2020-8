import time
import sys
import paho.mqtt.client as mqtt

EMULATE_HX711=False

referenceUnit = 1

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

mqtt = mqtt.Client("Pi2") # MQTT Client Name
mqtt.connect("192.168.137.1", 1883) # MQTT Broker Add

cnt = 2

while True:
    try:
        val = hx.get_weight(5)
        if val <= 20 * 4: # load under 20Kg
            if cnt % 2 == 0:
                mqtt.publish("Pi2", "{\"s2_isSit\":true}") # Topic Name, Message
            else :
                mqtt.publish("Pi2", "{\"s2_isSit\":false}")
            cnt += 1 
        else: # load over 20Kg
            mqtt.publish("IoT", "{\"s2_isSit\":false}")
            cnt -= 1

        hx.power_down()
        hx.power_up()
        time.sleep(1)

    except (KeyboardInterrupt, SystemExit):
        cleanAndExit()
        
mqtt.loop(5)


