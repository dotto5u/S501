import tensorflow as tf
import numpy as np
from PIL import Image

#Should be run from Training folder, not test_model folder

model = tf.saved_model.load("./model_data/saved_model")

image_path = "./test_model/car1.jpg"
image = Image.open(image_path).convert("RGB")
image_width, image_height = image.size
image = np.array(image)
input_tensor = tf.convert_to_tensor(image)
input_tensor = input_tensor[tf.newaxis, ...]

detections = model(input_tensor)

num_detections = int(detections["num_detections"][0])
detection_boxes = detections["detection_boxes"][0]
detection_scores = detections["detection_scores"][0]
detection_classes = detections["detection_classes"][0]

unnormalized_boxes = []
for box in detection_boxes:
    ymin, xmin, ymax, xmax = box
    unnormalized_boxes.append([
        int(xmin * image_width),
        int(ymin * image_height),
        int(xmax * image_width),
        int(ymax * image_height),
    ])

top_indices = np.argsort(detection_scores)[::-1] #Sorts the array in ASC and flips it
top_boxes = []
top_scores = []
top_classes = []
for i in top_indices:
    if (detection_scores[i] < 0.5):
        break

    top_boxes.append(unnormalized_boxes[i])
    top_scores.append(detection_scores[i])
    top_classes.append(detection_classes[i])


print("Total detections : ", len(top_classes))
for i in range(len(top_boxes)) :
    print(f"\nDetection {i+1}:\nBoundingBox: {top_boxes[i]}\nClass id: {int(top_classes[i])}\nConfidence: {top_scores[i]}\n")
