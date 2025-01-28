from ultralytics import YOLO
from datetime import datetime

model = YOLO("yolov8s.pt")

StartTime = datetime.now()
print(StartTime)

results = model.train(
    data="data.yaml",
    epochs=30,
    imgsz=512)

EndTime = datetime.now()
print(f"\nStarted at : {StartTime}\nEnded at {EndTime}")