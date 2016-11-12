from db_util.db import do_command, do_insert, do_command_no_return


def grocery_exists(upc):
    cmd = "SELECT * FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    return len(rtVal)


def grocery_input(upc, name):
    cmd = "INSERT INTO grocery (name, upc) VALUES (?, ?)"
    rtVal = do_insert(cmd, [name, upc])

    return rtVal


def add_grocery_to_inventory(groceryId):
    cmd = "INSERT INTO inventory (grocery_id) VALUES(?)"
    rtVal = do_insert(cmd, [groceryId])

    return rtVal


def get_grocery_by_upc(upc):
    cmd = "SELECT id FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])
    return rtVal[0]


def exists_in_inventory(upc):
    cmd = "SELECT * FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    if len(rtVal) == 0:
        return False

    id = rtVal[0]['id']
    cmd = "SELECT * FROM inventory WHERE grocery_id = ?"
    rtVal = do_command(cmd, [id])
    return len(rtVal)


def checkout_grocery(upc):
    id = get_first_inventory(upc)
    cmd = "DELETE FROM inventory WHERE id = ?"
    do_command_no_return(cmd, [id])


def get_first_inventory(upc):
    grocery = get_grocery_by_upc(upc)
    cmd = "SELECT id FROM inventory WHERE grocery_id = ? ORDER BY date_purchased LIMIT 1"
    rtVal = do_command(cmd, [grocery['id']])
    return rtVal[0]['id']

def get_current_inventory():
    cmd = """SELECT upc,
                    count(inventory.id) AS count
             FROM grocery
             LEFT JOIN inventory  ON inventory.grocery_id = grocery.id
             GROUP BY grocery.id
             ORDER BY grocery.id"""
    rtVal = do_command(cmd)
    return rtVal