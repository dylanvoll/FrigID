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
        itemCount = get_item_count(set, item['upc'])
        if grocery_db_helper.grocery_exists(item['upc']):
            if inventory_db_helper.get_first_inventory(item['upc']) != -1:
                inventoryCount = inventory_db_helper.get_inventory_count(item['upc'])
                if inventoryCount != itemCount:
                    pass
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


def get_item_count(set, upc):
    count = 0

    for item in set:
        if item['upc'] == upc:
            count += int(item['count'])

    return count
