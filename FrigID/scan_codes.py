from collections import Counter
from db_util import db
from grocery_helper import check_in, check_out

def list_ingredients():
	upc_file = open('UPCcodes.txt', 'r')
	lines = []
	for line in upc_file:
		lines.append(line)
	print(Counter(lines))


if __name__ == "__main__":
    dbConn = db.get_connection()
    upc= ""
    in_out = 1
    list_ingredients()
    print("Checking In")
    while(1):
        upc = input('Please Scan UPC:')
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
                check_in(upc)
            if(in_out == 2):
                check_out(upc)

