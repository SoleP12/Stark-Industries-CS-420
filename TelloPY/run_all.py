""" 
Runs both FindCar (aka the webcam that uses cardetection) & analyze traffic 
"""

import subprocess
import threading
import time

def run_find_car():
    subprocess.run(["python", "FindCar.py"])

def run_analyzer():
    while True:
        subprocess.run(["python", "analyze_traffic.py"])
        time.sleep(30)  # Re-analyze every 30 seconds

# Start both in parallel threads
threading.Thread(target=run_find_car).start()
threading.Thread(target=run_analyzer).start()
