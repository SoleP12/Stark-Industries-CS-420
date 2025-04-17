# db_logger.py
import sqlite3
from datetime import datetime
import os

# Create the path to the local .db file (stored in the same folder as this script)
DB_FILE = os.path.join(os.path.dirname(__file__), 'traffic_data.db')

def create_table_if_not_exists():
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS traffic_data (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
            car_count INTEGER NOT NULL,
            traffic_level TEXT NOT NULL
        )
    ''')
    conn.commit()
    conn.close()

def get_traffic_level(count):
    if count >= 10:
        return 'Heavy'
    elif count >= 5:
        return 'Moderate'
    else:
        return 'Light'

def save_traffic_data(car_count):
    traffic_level = get_traffic_level(car_count)
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO traffic_data (car_count, traffic_level)
        VALUES (?, ?)
    ''', (car_count, traffic_level))
    conn.commit()
    conn.close()
    print(f"[SQLite] Logged: {car_count} cars ({traffic_level})")
