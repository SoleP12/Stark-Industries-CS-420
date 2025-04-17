import sqlite3
import csv
from datetime import datetime

# Connect to your local SQLite database
conn = sqlite3.connect("traffic_data.db")
cursor = conn.cursor()

# Query average car count per hour of the day
cursor.execute("""
    SELECT strftime('%H', timestamp) AS hour, AVG(car_count)
    FROM traffic_data
    GROUP BY hour
    ORDER BY hour
""")
results = cursor.fetchall()

# Display in terminal
print("📊 Average Cars by Hour:")
for hour, avg_count in results:
    print(f"{hour}:00 - {round(avg_count, 2)} cars")

# Save results to CSV
with open("traffic_summary.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["Hour", "Average Car Count"])
    writer.writerows(results)

print("\n✅ Results exported to 'traffic_summary.csv'")

cursor.close()
conn.close()
