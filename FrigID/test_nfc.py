import nfc
import errno
import signal
import sys

import nfc_helper

from db_util import db

clf = None
continue_reading = True

def end_read(signal, frame):
    print("Terminating")
    global continue_reading
    continue_reading = False
    sys.exit()
    

signal.signal(signal.SIGINT, end_read)

def connected(tag):
    print(tag)
    currentRecord = None
    
    if tag.ndef:
        if tag.ndef.length > 0:
            print("Current Message:")
            print(tag.ndef.message.pretty())
        else:
            print("Empty Card")

    textRecord = nfc_helper.get_inventory_ndef()

    print("New Record:")
    print(textRecord.pretty())
    
    if tag.ndef is not None:
        tag.ndef.message = nfc.ndef.Message(textRecord)
    return True

def pollNFC():
    try:
        clf = nfc.ContactlessFrontend('tty:AMA0:pn532')
    except IOError as error:
        if error.errno == errno.ENODEV:
            print("no contactless reader found on tty:AMA0:pn532")
        elif error.errno == errno.EACCES:
            print("access denied for device with path tty:AMA0:pn532")
        elif error.errno == errno.EBUSY:
            print("the reader on tty:AMA0:pn532 is busy")
        else:
            print(repr(error) + "when trying tty:AMA0:pn532")
        return

    try:
        clf.connect(rdwr={'on-connect': connected})
        print(clf)
    finally:
        clf.close()


if __name__ == "__main__":

    print('starting')
    
    while continue_reading:
        pollNFC()
