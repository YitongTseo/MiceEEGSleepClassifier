# MiceEEGSleepClassifier

Yitong Tseo and Zander Majercik's work to fast fourier transform raw EEG and EMG mice data into usable attributes, train several common Machine learning classifiers on said attributes, and finally classify raw 2 channel EEG and EMG data with a focus towards high precision over accuracy. In collaboration with William Duke and Matt Carter's Williams College biology lab.

Must have:
- java compiler (javac)
- java interpreter
- python interpreter
- weka.jar (code written with weka-3-8-0)

the Raw EEG/EMG parser was written to take in output files from the Sirenia software developed by Pinnacle Technology Inc.

Expects raw EEG/EMG data in the following tsv format:

.
..
...
Date	Time	Time Stamp	Time from Start	EEG1	EEG2	EMG	...Further headers/columns of data will be ignored
10/14/2016	12:59:59	42657.708323	0.000000	40.690231	-32.566956	5.489366	  ...any further data will be ignored
10/14/2016	12:59:59	42657.708323	0.002500	44.230958	-35.152876	13.207349	  ...any further data will be ignored
...
..
.	


Expects scoring data in the following csv format:

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


To parse (multiple) tsv and csv files and transform into 1 .arff file run:

python eegFileReader.py <mouse1EEG.tsv> <mouse1Scores.csv> <mouse 1 scoring epoch length in seconds> <mouse2EEG.tsv> <mouse2Scores.csv> <mouse 2 scoring epoch length in seconds> ... for an arbitrary number of mice

e.g. python eegFileReader.py m1EEG.txt m1Scores.txt 6 m2EEG.txt m2Scores.txt 3 m3EEG.txt m3Scores.txt 5

NOTE: the default sampling frequency (fs) is 400 Hz and must be changed manually in thecode.

outputs the combined .arff file as mouseEEG.arff



To classify unknown mice EEG data must run:

javac -cp .:<path>/<to>/<weka>/weka.jar learnAndClassify.java
java -cp .:<path>/<to>/<weka>/weka.jar learnAndClassify

NOTE: the .arff training data file and .arff testing file names are hardcoded and must be changed manually (TODO)

