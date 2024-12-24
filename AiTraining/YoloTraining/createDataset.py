import os
from shutil import copyfile
import glob
import xml.etree.ElementTree as ET
import json
from random import shuffle

class datasetCreator:

    def __init__(self) -> None:
        self.__labelled_data_path = '../Labelling/labelled'
        self.__dataset_path = './dataset'
        self.__labelmap_path = './labelmap.json'

        print("do something")

        self.__label_map = self.readLabelmap()

        self.emptyDataset()

        self.createDatasetDirs()

        #Copies and edits dataset
        self.exploreLabelledDir()

        self.changeLabelsFormat()

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
                self.exploreDataDir(currentPath)


    def exploreDataDir(self, path):
        FilesOrDirs = os.listdir(path)

        nbFiles = len(FilesOrDirs)
        test = nbFiles/20
        valid = nbFiles/10

        trainImg, trainLbl, validImg, validLbl, testImg, testLbl = self.chooseValidTestTrainFiles(
            allFiles=FilesOrDirs,
            test=test,
            valid=valid
            )

        for fileName in trainImg :
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "train", "images", fileName)
                )
        for fileName in trainLbl:
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "train", "labels", fileName)
                )
    
        for fileName in testImg:
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "test", "images", fileName)
                )
        for fileName in testLbl:
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "test", "labels", fileName)
                )
        
        for fileName in validImg:
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "valid", "images", fileName)
                )
        for fileName in validLbl:
            copyfile(
                os.path.join(path, fileName),
                os.path.join(self.__dataset_path, "valid", "labels", fileName)
                )

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

                    for obj in root.findall('object'):
                        name = obj.find('name').text.replace(" ", "_")
                        
                        bndbox = obj.find('bndbox')
                        xmin = bndbox.find('xmin').text
                        ymin = bndbox.find('ymin').text
                        xmax = bndbox.find('xmax').text
                        ymax = bndbox.find('ymax').text

                        classId = self.__label_map[name]

                        with open(os.path.join(path, newLabelName), 'w') as writer:
                            writer.write(f"{classId} {xmin} {ymin} {xmax} {ymax}")

                        break
                
                os.remove(os.path.join(path, labelFiles[i]))

    def chooseValidTestTrainFiles(self, allFiles, test, valid):
        
        shuffle(allFiles)

        testImg = []
        validImg = []
        trainImg = []

        testLbl = []
        validLbl = []
        trainLbl = []


        for i in range(len(allFiles)):
            file = allFiles[i]
            if (str(file).endswith(".jpg")):
                if (file in testImg or file in validImg or file in trainImg):
                    continue
                else:
                    xmlFile = str(file).split(".")[0] + ".xml"
                    if (i <= test):
                        testImg.append(file)
                        testLbl.append(xmlFile)
                    elif (i <= valid):
                        validImg.append(file)
                        validLbl.append(xmlFile)
                    else:
                        trainImg.append(file)
                        validLbl.append(xmlFile)
            if (str(allFiles[i]).endswith(".xml")):
                if (allFiles[i] in testLbl or allFiles[i] in validLbl or allFiles[i] in trainLbl):
                    continue
                else:
                    jpgFile = str(file).split(".")[0] + ".jpg"
                    if (i <= test):
                        testLbl.append(file)
                        testImg.append(jpgFile)
                    elif (i <= valid):
                        validLbl.append(file)
                        validImg.append(jpgFile)
                    else:
                        trainLbl.append(file)
                        validImg.append(jpgFile)

        return [trainImg, trainLbl, validImg, validLbl, testImg, testLbl]

datasetCreator()