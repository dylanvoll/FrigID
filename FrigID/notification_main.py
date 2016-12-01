from frigid_util import notification_helper
from db_util import inventory_db_helper
import requests
import json

def notify_device():
    notifify_settings = notification_helper.get_notification_settings()

    if notifify_settings is None:
        return

    items = inventory_db_helper.get_inventory_to_notify()

    # Lets break if there's nothing to notify
    if items is None:
        return

    dict = build_json_dict(notifify_settings['device_id'], items)
    post_fcm_message(notifify_settings['project_id'], dict)

def build_json_dict(deviceId, items):
    json_dict = {}

    json_dict['to'] = deviceId
    json_data = build_data_dict(items)
    json_dict['notification'] = {'body': json_data['body'],
                            'title': "Stuff you're out of",
                            'sound': 'default',
                            'icon': 'icon_notification'}

    json_dict['data'] = json_data['data']

    return json_dict


def build_data_dict(items):
    data = {}
    body = ''

    i = 0

    for item in items:
        data[item['upc']] = str(item['count'])
        body += item['name']

        if i < len(items):
            body += ', '

    return {'data': data, 'body': body}


def post_fcm_message(projectId, data):
    headers = {'Authorization': 'key='+projectId,
               'Content-Type': 'application/json'
               }
    r = requests.post('https://fcm.googleapis.com/fcm/send', data=json.dumps(data), headers=headers)

    print(r.content)


if __name__ == "__main__":
    notify_device()
