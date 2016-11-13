from db import do_insert, do_command_no_return, do_command


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
    grocery = get_grocery(upc)
    cmd = "SELECT id FROM inventory WHERE grocery_id = ? ORDER BY date_purchased LIMIT 1"
    rtVal = do_command(cmd, [grocery['id']])

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