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
    return rtVal[0]


def get_grocery_name(upc):
    cmd = "SELECT name FROM grocery WHERE upc = ?"
    rtVal = do_command((cmd, [upc]))
    return rtVal[0]


def grocery_exists(upc):
    cmd = "SELECT id FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    return bool(len(rtVal))