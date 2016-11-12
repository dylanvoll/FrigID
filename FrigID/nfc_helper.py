import nfc.ndef
from db_util import db_helper

def get_inventory_ndef():
    list = ''
    inventory = db_helper.get_current_inventory()
    list = list.join("{} {}\n".format(dict['upc'], dict['count']) for dict in inventory)
    textRecord = nfc.ndef.TextRecord(list)

    return textRecord