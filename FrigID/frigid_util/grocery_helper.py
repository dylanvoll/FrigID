from db_util.db import do_command, do_command_no_return

def get_grocery_name(upc):
    cmd = "SELECT name FROM grocery WHERE upc = ?"
    rtVal = do_command(cmd, [upc])

    return bool(len(rtVal))