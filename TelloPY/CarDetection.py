import logging

import cv2
from ultralytics import YOLO
# 🔇 Silence YOLO's spammy logs
logging.getLogger("ultralytics").setLevel(logging.WARNING)
# Load YOLOv8 model (use YOLOv8n or YOLOv8s for speed; replace with your own if needed)
model = YOLO('yolov8n.pt')  # You can change this to 'yolov8s.pt' or a custom path
# Define class names for COCO dataset
# Class ID 2 is 'car'
CAR_CLASS_ID = 2

def detect_cars(frame):
    """
    Run YOLOv8 detection on the frame and return bounding boxes of detected cars.
    """
    results = model(frame)[0]
    car_boxes = []

    for box in results.boxes:
        cls_id = int(box.cls[0])
        if cls_id == CAR_CLASS_ID:
            car_boxes.append(box.xyxy[0].tolist())

    return car_boxes

def get_car_count(frame):
    car_boxes = detect_cars(frame)
    return len(car_boxes), car_boxes

def draw_boxes(frame, boxes):
    """
    Draw bounding boxes on the frame.
    """
    for box in boxes:
        x1, y1, x2, y2 = map(int, box)
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(frame, 'Car', (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 
                    0.9, (36,255,12), 2)
    return frame
