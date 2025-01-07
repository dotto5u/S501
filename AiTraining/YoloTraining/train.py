from ultralytics import YOLO
from datetime import datetime

model = YOLO("YoloV8.pt")

StartTime = datetime.now()
print(StartTime)

results = model.train(
    data="data.yaml",
    epochs=120,
    imgsz=512,
    optimizer="AdamW",
    lr0=0.01,
    weight_decay=0.00005)

EndTime = datetime.now()
print(f"\nStarted at : {StartTime}\nEnded at {EndTime}")