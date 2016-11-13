from db_util import db
from frigid_util import ingredient

if __name__ == "__main__":
    dbConn = db.get_connection()
    upc= ""
    in_out = 1
    print("Checking In")
    while(1):
        upc = raw_input('Please Scan UPC:')
        if(upc == "01"):
            in_out = 1
            print("Checking In")
        elif(upc == "02"):
            in_out = 2
            print("Checking out")
        elif not upc:
            print("Error in reading UPC")
        else:
            print(in_out)
            if(in_out == 1):
                item = ingredient.Ingredient(upc)
                item.check_in()
            if(in_out == 2):
                item = ingredient.Ingredient(upc)
                item.check_out()

