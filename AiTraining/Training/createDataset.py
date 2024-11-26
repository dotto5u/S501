import os
from shutil import copyfile
import tensorflow as tf
import glob
import xml.etree.ElementTree as ET

class datasetCreator:

    def __init__(self) -> None:
        self.__labelled_data_path = '../Labelling/labelled'
        self.__dataset_path = './dataset'

        #To do - Read this labelMap from the file
        self.__label_map = {'onion rings': 1, 'beef tartare': 2}

        self.emptyDataset()

        self.exploreDir(self.__labelled_data_path)

        self.createTfRecord()

        print("Dataset successfully created.")

    def emptyDataset(self):

        imagesPath = os.path.join(self.__dataset_path, "images")
        for file in os.listdir(imagesPath):
            file_path = os.path.join(imagesPath, file)
            if os.path.isfile(file_path):
                os.remove(file_path)

        annotationPath = os.path.join(self.__dataset_path, "annotations")
        for file in os.listdir(annotationPath):
            file_path = os.path.join(annotationPath, file)
            if os.path.isfile(file_path):
                os.remove(file_path)

    def exploreDir(self, path):
        FilesOrDirs = os.listdir(path)

        for i in range(len(FilesOrDirs)) :
            currentPath = os.path.join(path, FilesOrDirs[i])
            if (os.path.isfile(currentPath)):
                if (str(FilesOrDirs[i]).endswith(".xml")):
                    copyfile(currentPath, os.path.join(self.__dataset_path, "annotations", FilesOrDirs[i]))
                elif (str(FilesOrDirs[i]).endswith(".jpg")):
                    copyfile(currentPath, os.path.join(self.__dataset_path, "images", FilesOrDirs[i]))
            if (os.path.isdir(currentPath)):
                self.exploreDir(currentPath)

    def createTfRecord(self):
        writer = tf.io.TFRecordWriter(os.path.join(self.__dataset_path, "output.tfrecord"))

        for xml_file in glob.glob(os.path.join(self.__dataset_path, "annotations", "*.xml")):
            tree = ET.parse(xml_file)
            root = tree.getroot()

            image_path = os.path.join(self.__dataset_path, "images", root.find('filename').text)
            image_data = tf.io.read_file(image_path)

            width = int(root.find("size/width").text)
            height = int(root.find("size/height").text)

            boxes = []
            labels = []

            for obj in root.findall("object"):
                label = obj.find("name").text
                labels.append(self.__label_map[label])

                bbox = obj.find("bndbox")
                xmin = float(bbox.find("xmin").text) / width
                ymin = float(bbox.find("ymin").text) / height
                xmax = float(bbox.find("xmax").text) / width
                ymax = float(bbox.find("ymax").text) / height
                boxes.append([ymin, xmin, ymax, xmax])

            tf_example = tf.train.Example(features=tf.train.Features(feature={
                'image': tf.train.Feature(bytes_list=tf.train.BytesList(value=[image_data.numpy()])),
                'bboxes': tf.train.Feature(float_list=tf.train.FloatList(value=sum(boxes, []))),
                'labels': tf.train.Feature(int64_list=tf.train.Int64List(value=labels)),
            }))
            writer.write(tf_example.SerializeToString())
        writer.close()

datasetCreator()