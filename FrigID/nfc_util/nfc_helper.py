import nfc.ndef
from db_util import inventory_db_helper, grocery_db_helper
from frigid_util import ingredient

REMOVE_GROCERY = -1

def get_inventory_ndef():
    list = ''
    inventoryRows = inventory_db_helper.get_current_inventory()
    inventoryList = list.join("{} {}\n".format(dict['upc'], dict['count']) for dict in inventoryRows)
    textRecord = nfc.ndef.TextRecord(inventoryList)

    return textRecord


def update_inventory_from_ndef(list):
    set = __parse_list(list)

    for item in set:
        itemCount = int(item['count'])

        if itemCount == REMOVE_GROCERY:  # Lets remove this ingredient completely to not make it show up on grocery list
            grocery_db_helper.remove_grocery(item['upc'])
            continue

        if grocery_db_helper.grocery_exists(item['upc']):
            invenntoryId = inventory_db_helper.get_first_inventory(item['upc'])
            if invenntoryId != -1:  # Case where we know that we have to figure out the inventory count
                inventoryCount = inventory_db_helper.get_inventory_count(item['upc'])
                if inventoryCount != itemCount:
                    inventory_db_helper.resolve_inventory_count(item['upc'], inventoryCount, itemCount)
                else:
                    pass
            else:  # This branch saves us a DB call
                inventory_db_helper.resolve_inventory_count(item['upc'], 0, itemCount)
        else:
            # Check-in item so we have it in the grocery table
            newItem = ingredient.Ingredient(item['upc'])
            newItem.check_in()

            if itemCount > 1:
                inventory_db_helper.resolve_inventory_count(item['upc'], 1, itemCount)  # Take care of the rest


def __parse_list(list):
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
