import sqlite3

conn = sqlite3.connect("traffic_data.db")
cursor = conn.cursor()

cursor.execute("SELECT * FROM traffic_data ORDER BY timestamp DESC LIMIT 10")
rows = cursor.fetchall()

print("🧾 Last 10 Traffic Logs:")
for row in rows:
    print(row)

cursor.close()
conn.close()
