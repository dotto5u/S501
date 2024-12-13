import tensorflow as tf

tfrecord_path = "dataset/output.tfrecord"

raw_tfrecord = tf.data.TFRecordDataset(tfrecord_path)

def parse_record(example_proto):
    features = tf.io.parse_single_example(
        example_proto,
        features={
            'image': tf.io.FixedLenFeature([], tf.string),
            'bboxes': tf.io.VarLenFeature(tf.float32),
            'labels': tf.io.VarLenFeature(tf.int64)
        }
    )
    image = features['image']
    bboxes = tf.sparse.to_dense(features['bboxes'])
    labels = tf.sparse.to_dense(features['labels'])

    return image, bboxes, labels

parsed_dataset = raw_tfrecord.map(parse_record)

for i in parsed_dataset:
    decoded_image_shape = tf.io.decode_jpeg(i[0]).shape
    print(f"Bounding boxes : {i[1]}\nLabels : {i[2]}\nImage Dimensions : {decoded_image_shape[1]}x{decoded_image_shape[0]}\n")

print(f"\nDataset info : {raw_tfrecord}")