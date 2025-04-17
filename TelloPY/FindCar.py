import socket
import cv2
import time
from CarDetection import get_car_count, draw_boxes
from db_logger import save_traffic_data, get_traffic_level, create_table_if_not_exists

create_table_if_not_exists()

#UNCOMMENT BELOW WHEN TESTING DRONE
"""
# Tell the drone to enter command mode and start video stream
def start_tello_stream():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.sendto(b'command', ('192.168.10.1', 8889))
    sock.sendto(b'streamon', ('192.168.10.1', 8889))
    sock.close()
    print("[Tello] Sent 'command' and 'streamon' to drone")
# Start command mode + video stream
start_tello_stream()
"""
def main():
    # CHANGE LINE TO 
    # cap = cv2.VideoCapture("udp://0.0.0.0:11111")
    # FOR DRONE
    # BELOW IS FOR LAPTOP WEBCAM 
    cap = cv2.VideoCapture(0)
    last_log_time = time.time()

    if not cap.isOpened():
        print("❌ Error: Could not open webcam.")
        return

    print("📹 Press 'q' to quit.")
    while True:
        ret, frame = cap.read()
        if not ret:
            print("❌ Error: Failed to grab frame.")
            break

        car_count, car_boxes = get_car_count(frame)
        traffic_level = get_traffic_level(car_count)

        frame = draw_boxes(frame, car_boxes)

        # Overlay traffic info
        cv2.putText(frame, f"Cars: {car_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 
                    1, (255, 0, 0), 2)
        cv2.putText(frame, f"Traffic: {traffic_level}", (10, 70), cv2.FONT_HERSHEY_SIMPLEX, 
                    1, (0, 0, 255), 2)

        # Log every 10 seconds
        if time.time() - last_log_time > 10:
            save_traffic_data(car_count)
            last_log_time = time.time()

        cv2.imshow("Car Detection", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == '__main__':
    main()
