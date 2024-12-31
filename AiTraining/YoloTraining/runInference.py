import cv2
import torch
from ultralytics import YOLO
import matplotlib.pyplot as plt

model = YOLO('YoloV8.pt')

image_path = 'couch.jpg'
image = cv2.imread(image_path)
image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

results = model(image_rgb)
print(results)
annotated_frame = results[0].plot()

plt.figure(figsize=(10, 10))
plt.imshow(cv2.cvtColor(annotated_frame, cv2.COLOR_BGR2RGB))
plt.axis('off')
plt.show()

cv2.imwrite('output.jpg', annotated_frame)
