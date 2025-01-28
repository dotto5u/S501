import cv2
import torch
from ultralytics import YOLO
import matplotlib.pyplot as plt
import os

modelPath = "train10best.pt"

model = YOLO(modelPath)

image_dir_path = './dataset/valid/images'
output_dir_path = './testModelOutput'


allFiles = os.listdir(image_dir_path)
for file in allFiles:
    if not str(file).endswith('.jpg'):
        continue

    filePath = os.path.join(image_dir_path, file)
    image = cv2.imread(filePath, cv2.COLOR_RGB2BGR)
    results = model(image)

    annotated_frame = results[0].plot()

    plt.figure(figsize=(10, 10))
    plt.imshow(cv2.cvtColor(annotated_frame, cv2.COLOR_RGB2BGR))
    plt.axis('off')
    plt.show()

    #cv2.imwrite(os.path.join(output_dir_path, file), annotated_frame)
