import cv2
import matplotlib.pyplot as plt
from random import randint
import os

#This script aims to easily show a dataset image with it's bounding box (we don't care about the class here)
#It was built to test the image redimensioning in createDataset.py

datasetPath = "./dataset"
imgSize = 512


trainTestOrVaild = randint(1,3)

selectedFolder = "train" if trainTestOrVaild == 1 else ("test" if trainTestOrVaild == 2 else "valid")

allImages = os.listdir(os.path.join(datasetPath, selectedFolder, "images"))
selectedImage = allImages[randint(0, len(allImages)-1)]
selectedLabel = (str(selectedImage).split("."))[0] + ".txt"

imagePath = os.path.join(datasetPath, selectedFolder, "images", selectedImage)
labelPath = os.path.join(datasetPath, selectedFolder, "labels", selectedLabel)

labelData = []

with open(labelPath, 'r') as reader:
    labelData = reader.read().split(" ")

image = cv2.imread(imagePath)
image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

xmin = int(float(labelData[1]) * imgSize)
ymin = int(float(labelData[2]) * imgSize)
xmax = int(float(labelData[3]) * imgSize)
ymax = int(float(labelData[4]) * imgSize)

color = (255, 0, 0)
thickness = 2
cv2.rectangle(image, (xmin, ymin), (xmax, ymax), color, thickness)

plt.imshow(image)
plt.axis("off")
plt.show()