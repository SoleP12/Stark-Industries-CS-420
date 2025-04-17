import cv2
from ultralytics import YOLO

def main():
    # Load the YOLOv5s model (will auto‑download if needed)
    model = YOLO("yolov5s.pt")

    # Open default camera (0). (aka your webcam)
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Cannot open camera")
        return

    print("Starting car detection. Press 'q' to quit.")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Can't receive frame. Exiting...")
            break

        # Convert BGR (OpenCV) → RGB (YOLO)
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Run inference
        results = model(rgb_frame)[0]  # get the first (and only) result

        # Iterate detections
        for box, cls, conf in zip(results.boxes.xyxy, results.boxes.cls, results.boxes.conf):
            if int(cls) == 2:  # Yolov5 class ID for car is 2
                # Convert box coordinates to integers
                x1, y1, x2, y2 = map(int, box)
                label = f"Car {conf:.2f}"
                # Draw rectangle and label
                cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                cv2.putText(
                    frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2
                )

        # Show the frame
        cv2.imshow("YOLOv5 Car Detection", frame)

        # Quit on 'q'
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
