import requests
import sys
API_KEY = "A5D1CB1357FEF9C9"
api_url = "http://eandata.com/feed/?v=3&keycode={}&mode=json&find=".format(API_KEY)


def get_product_name(upc):
    try:
        r = requests.get(api_url + upc)
        json = r.json()
        if json['status']['code'] == '200':
            return json['product']['attributes']['product']
        else:
            return None
    except:
        print(sys.exc_info()[0])
        return None
