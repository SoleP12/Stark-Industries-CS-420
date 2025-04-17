import cv2
from ultralytics import YOLO
from tello import Tello

#
# THIS CODE IS A TESTING THE INTEGRATION OF YOLOv5 WITH THE Tello DRONE. 
# IF YOU WANT TO RUN THE FULL PROGRAM WITH THE DRONE RUNNING, RUN THE app.py FILE.
# Thanks guys!
#
def start_stream(drone):
    drone.streamon()

def detect_loop(model, cap):
    while True:
        ret, frame = cap.read()
        if not ret:
            break

        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = model(rgb)[0]

        for box, cls, conf in zip(results.boxes.xyxy,
                                  results.boxes.cls,
                                  results.boxes.conf):
            if int(cls) == 2:
                x1, y1, x2, y2 = map(int, box)
                cv2.rectangle(frame, (x1,y1), (x2,y2), (0,255,0), 2)
                cv2.putText(frame, f"Car {conf:.2f}",
                            (x1, y1-10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6,
                            (0,255,0), 2)

        cv2.imshow("Tello Car Detection", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

def main():
    drone = Tello('', 8889)
    start_stream(drone)
    cap = cv2.VideoCapture('udp://@0.0.0.0:11111')
    model = YOLO("yolov5s.pt")
    detect_loop(model, cap)

if __name__ == "__main__":
    main()
