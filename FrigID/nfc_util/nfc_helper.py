import nfc.ndef
import re
from db_util import inventory_db_helper, grocery_db_helper
from frigid_util import ingredient, notification_helper

REMOVE_GROCERY = -1


def get_inventory_ndef():
    list = ''
    inventoryRows = inventory_db_helper.get_current_inventory()
    inventoryList = list.join("{} {}\n".format(dict['upc'], dict['count']) for dict in inventoryRows)
    notifySettings = notification_helper.get_notification_settings()
    notifyString = "{} {} {}\n".format(notifySettings['project_id'], notifySettings['device_id'], notifySettings['reminder_weeks'])
    inventoryList = notifyString + inventoryList
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
            itemCount += inventory_db_helper.get_chantity_changed(item['upc'])

            inventory_db_helper.set_changes_count(grocery_db_helper.get_grocery_id(item['upc']), 0)

            invenntoryId = inventory_db_helper.get_first_inventory(item['upc'])
            if invenntoryId != -1:  # Case where we know that we have to figure out the inventory count
                inventoryCount = inventory_db_helper.get_inventory_count(item['upc'])

                if itemCount < 0:  # If both the Pi and the app checkout all of the same grocery
                    inventory_db_helper.resolve_inventory_count(item['upc'], inventoryCount, 0)
                elif inventoryCount != itemCount:
                    inventory_db_helper.resolve_inventory_count(item['upc'], inventoryCount, itemCount)

            else:  # This branch saves us a DB call
                inventory_db_helper.resolve_inventory_count(item['upc'], 0, itemCount)
        else:
            # Check-in item so we have it in the grocery table

            if len(item['upc']) != 4:
                newItem = ingredient.Ingredient(item['upc'])
                newItem.check_in(False)
            elif len(item['upc']) == 4 and itemCount == 0:
                newItem = ingredient.Ingredient(item['upc'])
                grocery_db_helper.grocery_input(newItem.upc, newItem.name)

            if itemCount > 1:
                inventory_db_helper.resolve_inventory_count(item['upc'], 1, itemCount)  # Take care of the rest


def get_notification_settings_from_ndef(textRecord):
    if re.search('[a-zA-Z]', textRecord):
        firstLine = textRecord.split('\n', 1)[0]
        notifySettings = firstLine.split(' ')

        if len(notifySettings) is not 3:  # We expect 3 items to be on this first line seperated by spaces
            return {'success', False}

        eolIndex = textRecord.find('\n')
        return {'success': True, 'projectId': notifySettings[0], 'deviceId': notifySettings[1], 'weeks': notifySettings[2], 'text': textRecord[eolIndex + 1: len(textRecord)]}

    return {'success': False}

def __parse_list(list):
    split = list.split('\n')
    set = []

    for item in split:
        if item != '':
            subsplit = item.split(" ")
            set.append({
                'upc': subsplit[0],
                'count': subsplit[1]
            })

    return set
