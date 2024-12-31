from ultralytics import YOLO

model = YOLO("YoloV8.pt")

res = model.export(format="tflite")

print(res)