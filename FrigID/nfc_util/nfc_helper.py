import nfc.ndef
from db_util import inventory_db_helper, grocery_db_helper

def get_inventory_ndef():
    list = ''
    inventoryRows = inventory_db_helper.get_current_inventory()
    inventoryList = list.join("{} {}\n".format(dict['upc'], dict['count']) for dict in inventoryRows)
    textRecord = nfc.ndef.TextRecord(inventoryList)

    return textRecord

def update_inventory_from_ndef(list):
    set = parse_list(list)

    for item in set:
        if grocery_db_helper.grocery_exists(item['upc']):
            if inventory_db_helper.get_first_inventory(item['upc']) != -1:
                pass



def parse_list(list):
    split = list.split('\n')
    set = []
    count = 0

    for item in split:
        if item != '':
            subsplit = item.split(" ")
            set.append({
                'upc': subsplit[0],
                'count': subsplit[1]
            })

    return set
