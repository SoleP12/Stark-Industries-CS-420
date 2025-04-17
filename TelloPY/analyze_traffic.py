import threading
import sqlite3
import time

stop_flag = False

def listen_for_quit():
    global stop_flag
    input("🔍 Press Enter to stop live traffic analysis...\n")
    stop_flag = True

def fetch_and_print():
    conn = sqlite3.connect("traffic_data.db")
    cursor = conn.cursor()
    cursor.execute("""
        SELECT strftime('%H', timestamp) AS hour, AVG(car_count)
        FROM traffic_data
        GROUP BY hour
        ORDER BY hour
    """)
    results = cursor.fetchall()
    conn.close()

    print("\n📊 Average Cars by Hour:")
    for hour, avg in results:
        print(f"{hour}:00 - {round(avg, 2)} cars")

# Start quit-listener thread
threading.Thread(target=listen_for_quit, daemon=True).start()

while True:
    if stop_flag:
        print("🛑 Analysis ended.")
        break
    fetch_and_print()
    for _ in range(5):  # Sleep 5s in 1s increments, so we can stop quickly
        if stop_flag:
            print("🛑 Analysis ended.")
            break
        time.sleep(1)
