import tensorflow as tf

saved_model_dir = "modelData/saved_model/"
tflite_model_path = "model.tflite"

converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
tflite_model = converter.convert()

with open(tflite_model_path, 'wb') as f:
    f.write(tflite_model)

print(f"TFLite model saved to {tflite_model_path}")
