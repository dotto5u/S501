from ultralytics import YOLO
from datetime import datetime

model = YOLO("YoloV8.pt")

StartTime = datetime.now()
print(StartTime)

results = model.train(data="data.yaml", epochs=60, imgsz=512)

EndTime = datetime.now()
print(f"\nStarted at : {StartTime}\nEnded at {EndTime}")