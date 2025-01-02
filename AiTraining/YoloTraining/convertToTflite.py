from ultralytics import YOLO

model = YOLO("best.pt")

res = model.export(format="tflite")

print(res)