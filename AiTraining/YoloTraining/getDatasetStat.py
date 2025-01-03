import os
from shutil import copyfile
import glob
import xml.etree.ElementTree as ET
import json
from random import shuffle
from PIL import Image

class DatasetStatistics:

    def __init__(self) -> None:
        self.__target_image_size = 512

        self.__labelled_data_path = '../Labelling/labelled'
        self.__labelmap_path = './labelmap.json'

        self.__label_map = self.readLabelmap()

        self.__dataCounts = self.initializeDataCounts()

        self.__unlabelled = 0

        #Copies and edits dataset
        self.exploreLabelledDir()

        self.displayResults()

    def displayResults(self):
        print("Statistics results :\n")
        print(f"Unabelled : {self.__unlabelled}")

        total = 0
        for key, value in self.__dataCounts.items():
            total += value
            print(f"{key} : {value}")
        
        print(f"\nTotal : {total}")

    def initializeDataCounts(self):
        dataCounts = {}

        for cls_name in self.__label_map.keys():
            dataCounts[cls_name] = 0
        
        return dataCounts

    def readLabelmap(self):
        tempData =  []
        data = {}

        with open(self.__labelmap_path, 'r') as reader:
            content = reader.read()
            tempData = json.loads(content)
        
        for cls in tempData:
            data[str(cls["label"]).replace(" ", "_")] = cls["id"]

        return data

    def exploreLabelledDir(self):
        FilesOrDirs = os.listdir(self.__labelled_data_path)

        for i in range(len(FilesOrDirs)) :
            cls_name = str(FilesOrDirs[i]).replace(" ", "_")
            currentPath = os.path.join(self.__labelled_data_path, FilesOrDirs[i])
            if (os.path.isdir(currentPath)):
                self.exploreDataDir(currentPath, cls_name)


    def redimensionAndCopyImageAndLabel(self, path, imageName, targetPath):
        
        baseImagePath = os.path.join(path, imageName)
        targetImagePath = os.path.join(targetPath, "images", imageName)

        baseFileName = ((str(imageName).split("."))[0])

        baseLabelPath = os.path.join(path, baseFileName + '.xml')
        targetLabelPath = os.path.join(targetPath, "labels", baseFileName + '.txt')

        if not os.path.exists(baseImagePath):
            print(f"Redimensionning : Original img file doesn't exist : {baseImagePath}")
            return
        if not os.path.exists(baseLabelPath):
            print(f"Redimensionning : Original xml file doesn't exist : {baseLabelPath}")
            return

        #Redimensioning image
        tempImage = Image.open(baseImagePath)

        originalWidth = tempImage.width
        originalHeight = tempImage.height

        resizedImage = tempImage.resize((self.__target_image_size, self.__target_image_size))
        resizedImage.save(targetImagePath)

        widthFactor = self.__target_image_size/originalWidth
        heightFactor = self.__target_image_size/originalHeight

        #Redimensioning labels
        with open(baseLabelPath, 'r') as file:
            tree = ET.parse(file)
            root = tree.getroot()

            object = tree.find("object")

            name = object.find('name').text.replace(" ", "_")

            boundingBox = object.find("bndbox")

            xmin = float(boundingBox.find("xmin").text) * widthFactor
            ymin = float(boundingBox.find("ymin").text) * heightFactor
            xmax = float(boundingBox.find("xmax").text) * widthFactor
            ymax = float(boundingBox.find("ymax").text) * heightFactor

            classId = self.__label_map[name]

            with open((targetLabelPath), 'w') as writer:
                writer.write(f"{classId} {xmin/self.__target_image_size} {ymin/self.__target_image_size} {xmax/self.__target_image_size} {ymax/self.__target_image_size}")

    def exploreDataDir(self, path, cls_name):
        FilesOrDirs = os.listdir(path)
        
        for fileName in FilesOrDirs:
            if (str(fileName).endswith(".jpg")):
                xmlFileName = (str(fileName).split("."))[0] + ".xml"
                xmlFilePath = (os.path.join(path, xmlFileName))
                if (not os.path.exists(xmlFilePath)):
                    self.__unlabelled += 1
                    continue
                else:
                    self.__dataCounts[cls_name] += 1


DatasetStatistics()