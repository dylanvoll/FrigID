CREATE TABLE  grocery
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  upc TEXT NOT NULL
);
CREATE TABLE inventory
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  grocery_id INTEGER NOT NULL,
  date_purchased DATE DEFAULT CURRENT_TIMESTAMP,
  date_notified DATE DEFAULT '1970-01-1 00:00:00',
  notified_count INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY(grocery_id) REFERENCES grocery(id)
);
CREATE TABLE changes
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  grocery_id INTEGER NOT NULL,
  quantity_changed INTEGER NOT NULL,
  FOREIGN KEY(grocery_id) REFERENCES grocery(id)
);
CREATE TABLE notification_settings
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id TEXT NOT NULL,
  device_id TEXT NOT NULL,
  reminder_weeks INTEGER NOT NULL DEFAULT 2
);