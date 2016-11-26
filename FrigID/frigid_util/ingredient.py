from db_util import grocery_db_helper, inventory_db_helper
import upc_api


class Ingredient(object):

    def __init__(self, upc):
        self.upc = upc
        self.name = self.__get_name(upc)

    def check_in(self, changes=True):
        if grocery_db_helper.grocery_exists(self.upc):
            groceryId = grocery_db_helper.get_grocery_id(self.upc)
        else:
            groceryId = grocery_db_helper.grocery_input(self.upc, self.name)

        inventory_db_helper.add_grocery_to_inventory(groceryId, changes)

    def check_out(self, changes=True):
        if not inventory_db_helper.exists_in_inventory(self.upc):
            return
        else:
            inventory_db_helper.checkout_grocery(self.upc, changes)

    def __get_name(self, upc):
        resultDict = grocery_db_helper.get_grocery(upc)
        if resultDict['success']:
            name = resultDict['grocery']['name']
        else:
            name = upc_api.get_product_name(upc)

            #lets add this grocery to the DB to save API usage if we have a name, otherwise lets not in future hopes of retrieving a name
            if name is not None:
                grocery_db_helper.grocery_input(upc, name)

        return name
