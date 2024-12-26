from ultralytics import YOLO

model = YOLO("YoloV8.pt")

results = model.train(data="data.yaml", epochs=30, imgsz=640)
