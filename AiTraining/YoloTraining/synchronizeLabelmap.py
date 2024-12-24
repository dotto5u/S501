import json
import yaml

class labelmapSynchronizer:

    path_to_classes_file = "../Labelling/labelImg-master/data/predefined_classes.txt"
    path_to_writing_file = "./labelmap.json"
    path_to_data = "./data.yaml"
    classes = []

    def __init__(self):
        with open(self.path_to_classes_file, "r") as reader:
            readClasses = reader.read().split("\n")

            for i in range(len(readClasses)):
                    createdClass = {
                        "id" : i,
                        "label" : readClasses[i]
                    }
                    
                    self.classes.append(createdClass)

            f = open(self.path_to_data, "r")
            data = yaml.load(f, Loader=yaml.FullLoader)
            data["names"] = readClasses
            data["nc"] = len(readClasses)
            f.close()

            f = open(self.path_to_data, "w")
            yaml.dump(data, f, default_flow_style=False)
            f.close()

        with open(self.path_to_writing_file, "w") as writer:
            writer.write(json.dumps(self.classes))


        print("labelmap successfully synchronized")




labelmapSynchronizer()
            