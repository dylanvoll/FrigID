from db_util.db import do_command


def set_notification_settings(projectId, deviceId, weeks):
    if not notification_settings_exist():
        cmd = "INSERT INTO notification_settings (project_id, device_id, reminder_weeks) VALUES (?, ?, ?)"
    else:
        cmd = "UPDATE notification_settings SET project_id = ?, device_id = ?, reminder_weeks = ?"

    do_command(cmd, [projectId, deviceId, weeks])


def notification_settings_exist():
    cmd = "SELECT id FROM notification_settings"
    rtVal = do_command(cmd)

    return bool(len(rtVal))