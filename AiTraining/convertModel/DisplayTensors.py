import tensorflow as tf

tflite_model_path = "model.tflite"

interpreter = tf.lite.Interpreter(model_path=tflite_model_path)
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print("Input Tensors:")
for input_detail in input_details:
    print(f"Name : {input_detail['name']}\nTensor shape : {input_detail['shape']}")

print("\nOutput Tensors:")
for output_detail in output_details:
    print(f"Name : {output_detail['name']}\nTensor shape : {output_detail['shape']}")
