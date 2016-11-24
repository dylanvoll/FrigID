from db import do_insert, do_command_no_return, do_command
from grocery_db_helper import get_grocery_id


def exists_in_inventory(upc):
    cmd = "SELECT * FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    if len(rtVal) == 0:
        return False

    id = rtVal[0]['id']
    cmd = "SELECT * FROM inventory WHERE grocery_id = ?"
    rtVal = do_command(cmd, [id])
    return bool(len(rtVal))


def get_first_inventory(upc):
    id = get_grocery_id(upc)
    cmd = "SELECT id FROM inventory WHERE grocery_id = ? ORDER BY date_purchased LIMIT 1"
    rtVal = do_command(cmd, [id])

    if len(rtVal) > 0:
        return rtVal[0]['id']

    return -1


def get_current_inventory():
    cmd = """SELECT upc,
                    count(inventory.id) AS count
             FROM grocery
             LEFT JOIN inventory  ON inventory.grocery_id = grocery.id
             GROUP BY grocery.id
             ORDER BY grocery.id"""
    rtVal = do_command(cmd)
    return rtVal


def add_grocery_to_inventory(groceryId):
    cmd = "INSERT INTO inventory (grocery_id) VALUES(?)"
    rtVal = do_insert(cmd, [groceryId])

    return rtVal


def checkout_grocery(upc):
    id = get_first_inventory(upc)
    cmd = "DELETE FROM inventory WHERE id = ?"
    do_command_no_return(cmd, [id])


def get_inventory_count(upc):
    id = get_grocery_id(upc)
    cmd = "SELECT COUNT(*) as count FROM inventory WHERE grocery_id = ?"
    rtVal = do_command(cmd, [id])

    return rtVal[0]['count']


def resolve_inventory_count(upc, currentCount, newCount):
    id = get_grocery_id(upc)
    for i in range(abs(currentCount-newCount)):
        if currentCount < newCount:
            add_grocery_to_inventory(id)
        else:
            checkout_grocery(upc)
