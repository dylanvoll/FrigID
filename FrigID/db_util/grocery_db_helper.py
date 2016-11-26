from db import do_command, do_command_no_return, do_insert


def get_grocery(upc):
    cmd = "SELECT name FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    length = len(rtVal)

    if length > 0:
        return {'success': bool(len(rtVal)), 'grocery': rtVal[0]}

    return {'success': bool(len(rtVal))}


def grocery_input(upc, name):
    cmd = "INSERT INTO grocery (name, upc) VALUES (?, ?)"
    rtVal = do_insert(cmd, [name, upc])

    return rtVal


def get_grocery_id(upc):
    cmd = "SELECT id FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    if len(rtVal) > 0:
        return rtVal[0]['id']
    else:
        return -1


def get_grocery_name(upc):
    cmd = "SELECT name FROM grocery WHERE upc = ?"
    rtVal = do_command((cmd, [upc]))
    return rtVal[0]


def grocery_exists(upc):
    cmd = "SELECT id FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    return bool(len(rtVal))


def remove_grocery(upc):
    id = get_grocery_id(upc)

    if id != -1:
        cmd = "DELETE FROM inventory WHERE grocery_id = ?"
        do_command_no_return(cmd, [id])
        cmd = "DELETE FROM changes where grocery_id = ?"
        do_command_no_return(cmd, [id])
        cmd = "DELETE FROM grocery where id = ?"
        do_command_no_return(cmd, [id])
