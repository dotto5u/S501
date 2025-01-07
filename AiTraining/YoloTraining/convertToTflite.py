from ultralytics import YOLO

model = YOLO("train7Last.pt")

res = model.export(format="tflite")

print(res)