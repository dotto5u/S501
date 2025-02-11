from ultralytics import YOLO

model = YOLO("train12best.pt")

res = model.export(format="tflite")

print(res)