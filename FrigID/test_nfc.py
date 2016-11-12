import nfc
import errno
import signal
import sys

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
    if tag.ndef:
        print("NDEF Capabilities:")
        print("  readable  = %s" % ("no","yes")[tag.ndef.is_readable])
        print("  writeable = %s" % ("no","yes")[tag.ndef.is_writeable])
        print("  capacity  = %d byte" % tag.ndef.capacity)
        print("  message   = %d byte" % tag.ndef.length)
        if tag.ndef.length > 0:
            print("NDEF Message:")
            print(tag.ndef.message.pretty())
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
