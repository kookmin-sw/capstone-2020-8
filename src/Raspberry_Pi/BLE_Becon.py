import subprocess
import socket
import blescan
import sys
import bluetooth._bluetooth as bluez
from time import sleep

bct_BLUETOOTHDEVICE = "hci0"
bct_OGF = "0x08"
bct_OCF_format = "0x0008"
bct_OCF_setting = "0x0006"
bct_OCF_operate = "0x000A"
bct_start = "01"
bct_stop = "00"

bct_IBEACONPROFIX = "1E 02 01 1A 1A FF 4C 00 02 15"
bct_UUID = " 00 00 00 AC E8 B4 E0 C2 7D 20 B6 11 B6 11 C7 74"
bct_MAJOR = "76 50"
bct_MINOR = "06 02"
bct_POWER = "C5 00"


def beacon_TX_config(_param):
  result = subprocess.check_output("sudo hciconfig " + bct_BLUETOOTHDEVICE + " " + _param, shell=True)

def beacon_TX_cmd_format(_ocf, _ibeaconprofix, _uuid, _major, _minor, _power):
  _bct_ogf = bct_OGF + " "
  _ocf = _ocf + " "
  _ibeaconprofix = _ibeaconprofix + " "
  _uuid = _uuid + " "
  _major = _major + " "
  _minor = _minor + " "
  result = subprocess.check_output("sudo hcitool -i " + bct_BLUETOOTHDEVICE + " cmd " + _bct_ogf + _ocf + _ibeaconprofix + _uuid + _major + _minor + _power, shell=True)

def beacon_TX_cmd_setting(_ocf, _interval):
  _bct_ogf = bct_OGF + " "
  _ocf = _ocf + " "
  _intervalHEX = '{:04X}'.format(int(_interval/0.625))
  _minInterval = _intervalHEX[2:] + " " + _intervalHEX[:2] + " "
  _maxInterval = _intervalHEX[2:] + " " + _intervalHEX[:2] + " "
  result = subprocess.check_output("sudo hcitool -i " + bct_BLUETOOTHDEVICE + " cmd " + _bct_ogf + _ocf + _maxInterval + _maxInterval + "00 00 00 00 00 00 00 00 00 07 00", shell=True)

def beacon_TX_cmd_operate(_ocf, _param):
  _bct_ogf = bct_OGF + " "
  _ocf = _ocf + " "
  result = subprocess.check_output("sudo hcitool -i " + bct_BLUETOOTHDEVICE + " cmd " + _bct_ogf + _ocf + _param, shell=True)

def beacon_TX_DevTrigger(_str):
  _bct_uuid = "00 00 " + _str +" AC E8 B4 E0 C2 7D 20 B6 11 B6 11 C7 74"
  beacon_TX_cmd_format(bct_OCF_format, bct_IBEACONPROFIX, _bct_uuid, bct_MAJOR, bct_MINOR, bct_POWER)
  sleep(1)

beacon_TX_config("up")
beacon_TX_cmd_format(bct_OCF_format, bct_IBEACONPROFIX, bct_UUID, bct_MAJOR, bct_MINOR, bct_POWER)
beacon_TX_cmd_setting(bct_OCF_setting, 100)
beacon_TX_cmd_operate(bct_OCF_operate, bct_start)
try:
  print "BLE EMIT START"
  while True:
    beacon_TX_DevTrigger("21")
finally:
  print "BLE EMIT STOP"
  beacon_TX_cmd_operate(bct_OCF_operate, bct_stop)
