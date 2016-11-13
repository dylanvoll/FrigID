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
    print("Tag: {}\n".format(tag))

    if tag.ndef.is_readable:
        if tag.ndef is not None:
            if tag.ndef.length > 0:
                records = tag.ndef.message[0]
                if records.type == "urn:nfc:wkt:T":
                    currentText = nfc.ndef.TextRecord(records)
                    print("Current Text:")
                    print(currentText.text)
                else:
                    print("No text record found")
            else:
                print("Empty Card")
        else:
            print("No NDEF Maybe format?")
    else:
        print("Card not readable")

    textRecord = nfc_helper.get_inventory_ndef()

    print("New Record:")
    print(textRecord.text)

    if tag.ndef.is_writeable:
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
