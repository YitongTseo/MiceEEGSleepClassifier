# MiceEEGSleepClassifier

Yitong Tseo (Yitong.Tseo@williams.edu) and Zander Majercik's (Alexander.Majercik@williams.edu) work to fast fourier transform raw EEG and EMG mice data into usable attributes, train several common Machine learning classifiers on said attributes, and finally classify raw 2 channel EEG and EMG data with a focus towards high precision over accuracy. In collaboration with William Duke and Matt Carter's Williams College biology lab.

Must have:
- java compiler (javac)
- java interpreter
- python interpreter (code written with python 2.7)
- weka.jar (code written with weka-3-8-0)

#EXPECTED RAW INPUT FORMATS

the Raw EEG/EMG parser was written to take in output files from the Sirenia software developed by Pinnacle Technology Inc.

###Expects raw EEG/EMG data in the following tsv format:

.

..

...

Date	Time	Time Stamp	Time from Start	EEG1	EEG2	EMG	...Further headers/columns of data will be ignored

10/14/2016	12:59:59	42657.708323	0.000000	40.690231	-32.566956	5.489366	  ...any further data will be ignored

10/14/2016	12:59:59	42657.708323	0.002500	44.230958	-35.152876	13.207349	  ...any further data will be ignored

...

..

.	


###Expects scoring data in the following csv format:

.

..

...

Epoch #,Start Time,End Time,Score #,Score ...Further headers/columns of data will be ignored

1,10/14/2016 12:59:59,10/14/2016 13:00:05,1,Wake ...any further data will be ignored

2,10/14/2016 13:00:05,10/14/2016 13:00:11,3,REM ...any further data will be ignored

3,10/14/2016 13:00:11,10/14/2016 13:00:17,2,Non REM

4,10/14/2016 13:00:17,10/14/2016 13:00:23,225,Unscored

...

..

.

#HOW TO RUN THE PROGRAM


###To parse *classified* EEG data from (multiple) tsv and csv files and transform into an .arff file:

python eegFileReader.py [mouse1EEG.tsv] [mouse1Scores.csv] [mouse 1 scoring epoch length in seconds] [mouse2EEG.tsv] [mouse2Scores.csv] [mouse 2 scoring epoch length in seconds]

e.g. python eegFileReader.py m1EEG.txt m1Scores.txt 6 m2EEG.tsv m2Scores.csv 3 m3EEG.txt m3Scores.txt 5

NOTE: the default sampling frequency (fs) is 400 Hz and must be changed manually in the code.

outputs the combined .arff file as mouseEEG.arff



###To parse *unclassified* eeg data in a tsv files and transform into an .arff file:

python eegFileReader.py [mouseEEG.tsv] unclassified [mouse epoch length in seconds]

e.g. python eegFileReader.py m1EEG.tsv unclassified 6

NOTE: the default sampling frequency (fs) is 400 Hz and must be changed manually in the code.

outputs the .arff file as mouseEEG.arff



###To classify mice EEG data:

javac -cp .:<path>/<to>/<weka>/weka.jar learnAndClassify.java

java -cp .:<path>/<to>/<weka>/weka.jar learnAndClassify [EEGTrainingArffFile] [EEGTestingArffFile]* [save]* [load]* [classify]*

*optional

save, load, and classify flag the program to save the classifiers once trained, load the last saved trained classifier, or classify the EEGTrainingArffFile respectively.

To clarify, if classify is set to true then the program will assume the epochs in EEGTrainingArffFile have not been classified (each class will have "?" as its class identifier). All instances in testingArffFile will be classified and the resulting classified csv file will be saved as [EEGTrainingArffFile] +"Classified.csv".

if classify is set to false then the program will assume EEGTrainingArffFile contains *ALREADY CLASSIFIED* examples (classes have been manually assigned to each epoch, there are no "?" in the entire arff file). The performance of the trained classifer will be judged off of the EEGTrainingArffFile examples.


#TODO:
- check if the classified csv file can be read by Serena
- allow the user to specify what the threshold of agreeing classifiers should be for an example to be classified (currently all classifiers must agree which may result in very few classifications when the user starts feeding a lot of training data in)
- smooth everything over. fs shouldn't be hard coded in eegFileReader.py, neither should the epochLength, and startingTime be hardcoded in learnAndClassify
- We should be able to extract the epoch lengths in seconds from the .csv score files instead of asking the user to put them in.
- make a big pipeline (might not be necessary? It's not that complicated as is right now)
- get more data!
