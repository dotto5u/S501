# Labelling part of SAE 5.01

This labelling part is using the project on this github repository https://github.com/HumanSignal/labelImg

This project was used with python 3.10. Older versions could have issues with certain libraries.

### Quickstart

To get started, first create a python venv
```
python -m venv .venv
```

Then enter this venv on windows
```
./.venv/Scripts/activate
```

You can then install all dependencies
```
pip install -r requirements.txt
```

You can then go into the labelImg project
```
cd ./labelImg-master/
```

Finally, you can run the software 
```
python labelImg.py
```


### Updating the classes
To update the classes, you must go into the data folder in the labelImg project
```
cd ./labelImg-master/data/
```

In this folder should be a file named predefined_classes.txt, you can edit this file and add all the classes you need.