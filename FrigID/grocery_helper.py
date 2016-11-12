from db_util import db_helper
from upc_api import get_product_name

def check_in(upc):
    if db_helper.grocery_exists(upc):
        groceryId = db_helper.get_grocery_by_upc(upc)
        db_helper.add_grocery_to_inventory(groceryId['id'])
    else:
        name = get_product_name(upc)
        groceryId = db_helper.grocery_input(upc, name)
        db_helper.add_grocery_to_inventory(groceryId)

def check_out(upc):
    if not db_helper.exists_in_inventory(upc):
        return
    else:
        db_helper.checkout_grocery(upc)