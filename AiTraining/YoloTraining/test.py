import cv2
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt

# Load the TFLite model
interpreter = tf.lite.Interpreter(model_path='YoloV8_base.tflite')
interpreter.allocate_tensors()

# Get input and output tensor details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Load and preprocess the image
image_path = 'couch.jpg'
image = cv2.imread(image_path)
image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

# Resize image to match the input size of the TFLite model
input_shape = input_details[0]['shape'][1:3]  # H, W
resized_image = cv2.resize(image_rgb, (input_shape[1], input_shape[0]))

# Normalize the image to [0, 1]
normalized_image = resized_image / 255.0
input_data = np.expand_dims(normalized_image, axis=0).astype(np.float32)

# Run inference
interpreter.set_tensor(input_details[0]['index'], input_data)
interpreter.invoke()

# Get output tensors
# The exact post-processing depends on your YOLO TFLite model's output format
output_data = interpreter.get_tensor(output_details[0]['index'])

# Process the output (this example assumes bounding box detection output)
# You may need to adapt this to your model's specific output format
boxes = output_data[..., :4]  # Coordinates of bounding boxes
scores = output_data[..., 4]  # Confidence scores
classes = output_data[..., 5]  # Class IDs

# Filter results based on confidence threshold
confidence_threshold = 0.5
selected_indices = np.where(scores > confidence_threshold)
selected_boxes = boxes[selected_indices]
selected_scores = scores[selected_indices]
selected_classes = classes[selected_indices]

# Visualize the results
for box, score, cls in zip(selected_boxes, selected_scores, selected_classes):
    x1, y1, x2, y2 = box  # Assuming box format is [x_min, y_min, x_max, y_max]
    x1 = int(x1 * image.shape[1])
    y1 = int(y1 * image.shape[0])
    x2 = int(x2 * image.shape[1])
    y2 = int(y2 * image.shape[0])
    cv2.rectangle(image, (x1, y1), (x2, y2), (255, 0, 0), 2)
    label = f"Class {int(cls)}: {score:.2f}"
    cv2.putText(image, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 2)

# Show the annotated image
plt.figure(figsize=(10, 10))
plt.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
plt.axis('off')
plt.show()

# Save the annotated image
cv2.imwrite('output.jpg', image)
