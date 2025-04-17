import mysql.connector
import os
from dotenv import load_dotenv

load_dotenv()

db_config = {
    'host': os.getenv("DB_HOST", "localhost"),
    'user': os.getenv("DB_USER", "admin"),
    'password': os.getenv("DB_PASS", "admin123")
}

# Connect to MySQL server (no database yet)
conn = mysql.connector.connect(**db_config)
cursor = conn.cursor()

# Create database if not exists
cursor.execute("CREATE DATABASE IF NOT EXISTS traffic_monitor")
cursor.execute("USE traffic_monitor")

# Create table if not exists
cursor.execute("""
CREATE TABLE IF NOT EXISTS traffic_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    car_count INT NOT NULL,
    traffic_level VARCHAR(20) NOT NULL
)
""")

conn.commit()
cursor.close()
conn.close()
print("✅ Database and table are ready.")
