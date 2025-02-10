import os
from shutil import copyfile
import glob
import xml.etree.ElementTree as ET
import json
from random import shuffle
from PIL import Image

class datasetCreator:

    def __init__(self) -> None:
        self.__target_image_size = 512

        self.__labelled_data_path = '../Labelling/labelled'
        self.__dataset_path = './dataset'
        self.__labelmap_path = './labelmap.json'

        self.__images_per_class = 0 #0 if not capped

        self.__label_map = self.readLabelmap()

        self.createDatasetDirs()

        self.emptyDataset()

        #Copies and edits dataset
        self.exploreLabelledDir()

        #Changing label format is now done while copying it to the dataset
        #self.changeLabelsFormat()

        print("Dataset successfully created.")

    def emptyDataset(self):
        self.emptyDir(self.__dataset_path)

    def emptyDir(self, path):
        for file in os.listdir(path):
            file_path = os.path.join(path, file)
            if os.path.isfile(file_path):
                os.remove(file_path)
            if (os.path.isdir(file_path)):
                self.emptyDir(file_path)

    def readLabelmap(self):
        tempData =  []
        data = {}

        with open(self.__labelmap_path, 'r') as reader:
            content = reader.read()
            tempData = json.loads(content)
        
        for cls in tempData:
            data[str(cls["label"]).replace(" ", "_")] = cls["id"]

        return data

    def createDatasetDirs(self):
        if (not os.path.exists(self.__dataset_path)):
            os.mkdir(self.__dataset_path)

        if (not os.path.exists(os.path.join(self.__dataset_path, "valid"))):
            os.mkdir(os.path.join(self.__dataset_path, "valid"))
        if (not os.path.exists(os.path.join(self.__dataset_path, "train"))):
            os.mkdir(os.path.join(self.__dataset_path, "train"))
        if (not os.path.exists(os.path.join(self.__dataset_path, "test"))):
            os.mkdir(os.path.join(self.__dataset_path, "test"))

        if (not os.path.exists(os.path.join(self.__dataset_path, "valid", "labels"))):
            os.mkdir(os.path.join(self.__dataset_path, "valid", "labels"))
        if (not os.path.exists(os.path.join(self.__dataset_path, "valid", "images"))):
            os.mkdir(os.path.join(self.__dataset_path, "valid", "images"))

        if (not os.path.exists(os.path.join(self.__dataset_path, "test", "images"))):
            os.mkdir(os.path.join(self.__dataset_path, "test", "images"))
        if (not os.path.exists(os.path.join(self.__dataset_path, "test", "labels"))):
            os.mkdir(os.path.join(self.__dataset_path, "test", "labels"))

        if (not os.path.exists(os.path.join(self.__dataset_path, "train", "images"))):
            os.mkdir(os.path.join(self.__dataset_path, "train", "images"))
        if (not os.path.exists(os.path.join(self.__dataset_path, "train", "labels"))):
            os.mkdir(os.path.join(self.__dataset_path, "train", "labels"))

    def exploreLabelledDir(self):
        FilesOrDirs = os.listdir(self.__labelled_data_path)

        for i in range(len(FilesOrDirs)) :
            currentPath = os.path.join(self.__labelled_data_path, FilesOrDirs[i])
            if (os.path.isdir(currentPath)):
                if (self.__images_per_class == 0):
                    self.exploreDataDir(currentPath)
                else:
                    self.exploreDataDirWithImageCap(currentPath)


    def redimensionAndCopyImageAndLabel(self, path, imageName, targetPath):
        
        baseImagePath = os.path.join(path, imageName)
        targetImagePath = os.path.join(targetPath, "images", imageName)

        baseFileName = ((str(imageName).split("."))[0])

        baseLabelPath = os.path.join(path, baseFileName + '.xml')
        targetLabelPath = os.path.join(targetPath, "labels", baseFileName + '.txt')

        if not os.path.exists(baseImagePath):
            print(f"Redimensionning : Original img file doesn't exist : {baseImagePath}")
            os.remove(baseLabelPath)
            return
        if not os.path.exists(baseLabelPath):
            print(f"Redimensionning : Original xml file doesn't exist : {baseLabelPath}")
            os.remove(baseImagePath)
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

    def exploreDataDir(self, path):
        FilesOrDirs = os.listdir(path)

        nbFiles = len(FilesOrDirs)
        test = nbFiles/20
        valid = nbFiles/10

        trainImg, validImg, testImg = self.chooseValidTestTrainFiles(
            allFiles=FilesOrDirs,
            test=test,
            valid=valid
            )

        for fileName in trainImg :
            self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "train"))
    
        for fileName in testImg:
           self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "test"))
        
        for fileName in validImg:
            self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "valid"))

    def exploreDataDirWithImageCap(self, path):
        FilesOrDirs = os.listdir(path)

        nbFiles = self.__images_per_class * 2
        test = nbFiles/20
        valid = nbFiles/10

        trainImg, validImg, testImg = self.chooseValidTestTrainFilesWithCap(
            allFiles=FilesOrDirs,
            nbFiles=nbFiles,
            test=test,
            valid=valid
            )

        for fileName in trainImg :
            self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "train"))
    
        for fileName in testImg:
           self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "test"))
        
        for fileName in validImg:
            self.redimensionAndCopyImageAndLabel(path, fileName, os.path.join(self.__dataset_path, "valid"))

    def changeLabelsFormat(self):

        trainPath = os.path.join(self.__dataset_path, "train", "labels")
        testPath = os.path.join(self.__dataset_path, "test", "labels")
        validPath = os.path.join(self.__dataset_path, "valid", "labels")

        self.changeLabelFormatByPath(trainPath)
        self.changeLabelFormatByPath(testPath)
        self.changeLabelFormatByPath(validPath)

    def changeLabelFormatByPath(self, path):
        labelFiles = os.listdir(path)

        for i in range(len(labelFiles)):
            if (str(labelFiles[i]).endswith(".xml")):

                newLabelName = str(labelFiles[i]).split(".")[0] + ".txt"

                with open(os.path.join(path, labelFiles[i]), 'r') as file:
                    tree = ET.parse(file)
                    root = tree.getroot()

                    width = 1
                    height = 1

                    for obj in root.findall('size'):
                        width = float(obj.find('width').text)
                        height = float(obj.find('height').text)
                        break

                    for obj in root.findall('object'):
                        name = obj.find('name').text.replace(" ", "_")
                        
                        bndbox = obj.find('bndbox')
                        xmin = float(bndbox.find('xmin').text)
                        ymin = float(bndbox.find('ymin').text)
                        xmax = float(bndbox.find('xmax').text)
                        ymax = float(bndbox.find('ymax').text)

                        classId = self.__label_map[name]

                        with open(os.path.join(path, newLabelName), 'w') as writer:
                            writer.write(f"{classId} {xmin/width} {ymin/height} {xmax/width} {ymax/height}")

                        break
                
                os.remove(os.path.join(path, labelFiles[i]))

    def chooseValidTestTrainFiles(self, allFiles, test, valid):
        
        shuffle(allFiles)

        testImg = []
        validImg = []
        trainImg = []

        for i in range(len(allFiles)):
            file = allFiles[i]
            if (str(file).endswith(".jpg")):
                if (file in testImg or file in validImg or file in trainImg):
                    continue
                else:
                    
                    if (i <= test):
                        testImg.append(file)
                    elif (i <= valid):
                        validImg.append(file)
                    else:
                        trainImg.append(file)

        return [trainImg, validImg, testImg]
    
    def chooseValidTestTrainFilesWithCap(self, allFiles, nbFiles, test, valid):
        
        shuffle(allFiles)

        testImg = []
        validImg = []
        trainImg = []

        for i in range(len(allFiles)):
            file = allFiles[i]
            if (str(file).endswith(".jpg")):
                if (file in testImg or file in validImg or file in trainImg):
                    continue
                else:
                    
                    if (i <= test):
                        testImg.append(file)
                    elif (i <= valid):
                        validImg.append(file)
                    else:
                        if (i >= nbFiles):
                            break
                        trainImg.append(file)

        return [trainImg, validImg, testImg]

datasetCreator()