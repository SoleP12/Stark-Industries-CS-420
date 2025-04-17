import os
import mysql.connector

db_config = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'admin'),
    'password': os.getenv('DB_PASS', 'admin123'),
    'database': os.getenv('DB_NAME', 'traffic_monitor')
}

def get_traffic_level(count):
    if count >= 10:
        return 'Heavy'
    elif count >= 5:
        return 'Moderate'
    else:
        return 'Light'

def save_traffic_data(car_count):
    traffic_level = get_traffic_level(car_count)

    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO traffic_data (car_count, traffic_level) VALUES (%s, %s)",
            (car_count, traffic_level)
        )
        conn.commit()
        cursor.close()
        conn.close()
        print(f"[DB] Logged: {car_count} cars ({traffic_level})")
    except mysql.connector.Error as err:
        print(f"[DB ERROR] {err}")
