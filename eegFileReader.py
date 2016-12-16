import sys
import scipy
import numpy as np
from numpy import *
import matplotlib.pyplot as plt

fs = 400.0 #We assume the frequency is 400 Hz. change this if that's not true
numBands = 25 #separate the FFT into equally spaced bands (e.g. 0-4 Hz, 4-8 Hz, 8-12 Hz...)
targetFile = open("mouseEEG.arff", 'w')

class score_datapoint():

    def __init__(self, attributeList):
        self.attributeList = attributeList
        self.start_time = attributeList[0]
        self.end_time = attributeList[1]
        self.className = attributeList[2]

        times_start = self.start_time[self.start_time.index(" ")+1:]
        times_end = self.end_time[self.end_time.index(" ")+1:]

        timesSplitStart = times_start.split(":")
        timesSplitEnd = times_end.split(":")

        start_time_sec = int(timesSplitStart[0])*3600 + int(timesSplitStart[1])*60 + int(timesSplitStart[2]) #we know that times will be a 3 element list (hours, minutes, seconds)
        end_time_sec = int(timesSplitEnd[0])*3600 + int(timesSplitEnd[1])*60 + int(timesSplitEnd[2])
        self.length_in_seconds = (end_time_sec - start_time_sec + 24*3600)%(24*3600) #to handle the case where we pass midnight

    def to_string(self):
        print self.attributeList



class rawEEGDataPoint():
    
    def __init__(self, attributeList):
        self.timeFromStart = attributeList[0]
        self.EEG1 = attributeList[1]
        self.EEG2 = attributeList[2]
        self.EMG = attributeList[3]

    def toString(self):
        print "whats that time from start?\n"
        print self.timeFromStart
        print "whats that EEG1\n"
        print self.EEG1
        print "whats that EEG2?\n"
        print self.EEG2
        print "whats that EMG?\n"
        print self.EMG        

class processedDataPoint():
    
    def __init__(self, attributeList):
        self.timeFromStart = attributeList[0]
        self.EEG1Bands = attributeList[1] #a list of binned frequencies
        self.EEG2Bands = attributeList[2] #a list of binned frequencies
        self.EMGAverage = attributeList[3] #the average EMG over the epoch
        self.EMGSD = attributeList[4]
        self.EMGmin = attributeList[5]
        self.EMGmax = attributeList[6]

    def toString(self):
        print "whats that time from start?\n"
        print self.timeFromStart
        print "what're the values in EEG1?\n"
        for i in range(0, len(self.EEG1Bands)):
            print "band from ", i * 2, "Hz to ", (i + 1) * 2, "Hz"
            print self.EEG1Bands[i]

        print "what're the EEG2 values?\n"
        for i in range(0, len(self.EEG2Bands)):
            print "band from ", i * 2, "Hz to ", (i + 1) * 2, "Hz"
            print self.EEG2Bands[i]

        print "whats the EMG average?\n"
        print self.EMGAverage

        print "whats the EMG standardDeviation?\n"
        print self.EMGSD

        print "whats the EMG max?\n"
        print self.EMGmax

        print "whats the EMG min?\n"
        print self.EMGmin
        

def parseRawData(fileName):
    rawData = []
    file = open(fileName, 'r')
    for line in file:
        if "EEG" and "EMG" in line:
            break
    
    #for every line after the header line that contains 
    #"Date, Time, Time Stamp, Time from Start, EEG1, EEG2, EMG"
    index = 0
    for line in file:
        line = line[line.index("\t") + 1:] #strip the date
        line = line[line.index("\t") + 1:] #strip off the time
        line = line[line.index("\t") + 1:] #strip off the time stamp

        timeFromStart = float(line[:line.index("\t")]) #store the start from time
        line = line[line.index("\t") + 1:] #strip off the time from start

        EEG1 = float(line[:line.index("\t")])
        line = line[line.index("\t") + 1:] #strip off EEG1

        EEG2 = float(line[:line.index("\t")])
        line = line[line.index("\t") + 1:] #strip off EEG2

        EMG = float(line[:line.index("\t")])

        rawData.append(rawEEGDataPoint([timeFromStart, EEG1, EEG2, EMG]))
        
        if index % 1000 == 0:
            print "parsing raw data... @ data point", index
        index += 1

    file.close()
    return rawData


def parseClassificationData(score_file_string):
    score_datapoints = []
    score_file = open(score_file_string, "r")

    for line in score_file:
        if ("Wake" in line or "Non REM" in line or "REM" in line or "Unscored" in line):
            line = line[line.index(",")+1:] #strip the beginning epoch number

            start_date = line[:line.index(",")] #starting time
            line = line[line.index(",")+1:] #strip
            end_date = line[:line.index(",")] #ending time
   
            trace_class = line[line.rindex(",")+1:].rstrip()
            epoch = [start_date, end_date, trace_class]
            d = score_datapoint(epoch)
            score_datapoints.append(d)

    score_file.close()
    
    return score_datapoints

def processData(rawData, epochLengthInSeconds):
    processedData = []
    index = 0
    rawEEG1 = np.array([])
    rawEEG2 = np.array([])
    rawEMG = np.array([])
    timeFromStart = 0

    for datum in rawData:
        index += 1
        rawEEG1 = np.append(rawEEG1, datum.EEG1)
        rawEEG2 = np.append(rawEEG2, datum.EEG2)
        rawEMG = np.append(rawEMG, datum.EMG)

        if (index >= fs * epochLengthInSeconds):
            print "processing data... currently @ the", len(processedData), "th data point"

            EEG1Bands = fastFourierTransformAndBin(rawEEG1)
            EEG2Bands = fastFourierTransformAndBin(rawEEG2)
            EMGAverage = np.average(rawEMG)
            EMGStDev = np.std(rawEMG)
            EMGmin = np.min(rawEMG)
            EMGmax = np.max(rawEMG)

            processedData.append(processedDataPoint([timeFromStart, EEG1Bands, EEG2Bands, EMGAverage, EMGStDev, EMGmin, EMGmax]))
   
            rawEEG1 = np.array([])
            rawEEG2 = np.array([])
            rawEMG = np.array([])
            timeFromStart = datum.timeFromStart #not exactly right b/c should actually be from the next datum but close enough I say.
            index = 0

    return processedData

def fastFourierTransformAndBin(x):
    ff = scipy.fft(x)

    N = len(x)
    maxFrequency = 50.0 #Don't consider any frequency above 50 Hz.

    dataPointsPerBand = int((N * (maxFrequency / fs)) / numBands) #calculate how many datapoints get averaged together to form each band

    bands = [] # create an array to hold all the bands
    for x in range(0, numBands):
        bandValue = 0.0
        for i in range(x * dataPointsPerBand, (x + 1) * dataPointsPerBand):
            bandValue += abs(ff[i].real)
        bandValue /= dataPointsPerBand #gotta average
        bands.append(bandValue)

    return bands
    

#just comment out the attributes you don't want to include here and the arff file will be generated without those attributes
def setAttributeNames():
    attributeNames = []

    attributeNames.append("EMGAverage")
    attributeNames.append("EMGStandardDev")
    attributeNames.append("EMGmin")
    attributeNames.append("EMGmax")

    #now add all in the normalized EEG data
    for i in range(0, numBands):
        name = "EEG1:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributeNames.append(name)

    for i in range(0, numBands):
        name = "EEG2:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributeNames.append(name)    

    for i in range(0, numBands):
        name = "EEGavg:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributeNames.append(name)

    #Delta 0-4 Hz
    attributeNames.append("deltaEEG1")
    attributeNames.append("deltaEEG2")
    attributeNames.append("deltaAvg")
    
    #Theta 4-8 Hz
    attributeNames.append("thetaEEG1")
    attributeNames.append("thetaEEG2")
    attributeNames.append("thetaAvg")

    #Alpha 8 - 12 Hz
    attributeNames.append("alphaEEG1")
    attributeNames.append("alphaEEG2")
    attributeNames.append("alphaAvg")

    #Beta 12 - 20 Hz
    attributeNames.append("betaEEG1")
    attributeNames.append("betaEEG2")
    attributeNames.append("betaAvg")

    #Gamma 20 - 50 Hz
    attributeNames.append("gammaEEG1")
    attributeNames.append("gammaEEG2")
    attributeNames.append("gammaAvg")

    #Think about theta-to-delta range
    attributeNames.append("T-to-D_EEG1")
    attributeNames.append("T-to-D_EEG2")
    attributeNames.append("T-to-D_avg")

    #thnk about beta-to-delta range
    attributeNames.append("B-to-D_EEG1")
    attributeNames.append("B-to-D_EEG2")
    attributeNames.append("B-to-D_avg")

    return attributeNames

#A bit of an explanation for this guy, 
#given a processedDataPoint it'll return: a map from attribute name -> real number value for the processedDataPoint
def computeInterestingAttributes(processedDataPoint):
    attributes = {}

    #first add all the EMG stuff
    attributes["EMGAverage"] = processedDataPoint.EMGAverage
    attributes["EMGStandardDev"] = processedDataPoint.EMGSD
    attributes["EMGmin"] = processedDataPoint.EMGmin
    attributes["EMGmax"] = processedDataPoint.EMGmax

    #now add all in the normalized EEG data
    EEG1Total = sum(processedDataPoint.EEG1Bands)
    for i in range(0, len(processedDataPoint.EEG1Bands)):
        name = "EEG1:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributes[name] = processedDataPoint.EEG1Bands[i] / EEG1Total

    EEG2Total = sum(processedDataPoint.EEG2Bands)
    for i in range(0, len(processedDataPoint.EEG2Bands)):
        name = "EEG2:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributes[name] = processedDataPoint.EEG2Bands[i] / EEG2Total

    #After talking to Will, we thought it could be interesting to also look at EEG1 and EEG2 data averaged
    for i in range(0, len(processedDataPoint.EEG2Bands)): #could also be EEG1Bands, doesn't matter
        name = "EEGavg:" + str( i * 2) +"-" + str((i + 1) * 2)
        attributes[name] = (processedDataPoint.EEG1Bands[i] + processedDataPoint.EEG2Bands[i]) / (EEG1Total + EEG2Total)

    #Delta 0-4 Hz
    deltaEEG1 = sum(processedDataPoint.EEG1Bands[0:2]) / EEG1Total
    deltaEEG2 = sum(processedDataPoint.EEG2Bands[0:2]) / EEG2Total
    deltaAverage = (deltaEEG1 + deltaEEG2) / 2.0
    attributes["deltaEEG1"] = deltaEEG1
    attributes["deltaEEG2"] = deltaEEG2
    attributes["deltaAvg"] = deltaAverage
    
    #Theta 4-8 Hz
    thetaEEG1 = sum(processedDataPoint.EEG1Bands[2:4]) / EEG1Total
    thetaEEG2 = sum(processedDataPoint.EEG2Bands[2:4]) / EEG2Total
    thetaAverage = (thetaEEG1 + thetaEEG2) / 2.0
    attributes["thetaEEG1"] = thetaEEG1
    attributes["thetaEEG2"] = thetaEEG2
    attributes["thetaAvg"] = thetaAverage

    #Alpha 8 - 12 Hz
    alphaEEG1 = sum(processedDataPoint.EEG1Bands[4:6]) / EEG1Total
    alphaEEG2 = sum(processedDataPoint.EEG2Bands[4:6]) / EEG2Total
    alphaAverage = (alphaEEG1 + alphaEEG2) / 2.0
    attributes["alphaEEG1"] = alphaEEG1
    attributes["alphaEEG2"] = alphaEEG2
    attributes["alphaAvg"] = alphaAverage

    #Beta 12 - 20 Hz
    betaEEG1 = sum(processedDataPoint.EEG1Bands[6:10]) / EEG1Total
    betaEEG2 = sum(processedDataPoint.EEG2Bands[6:10]) / EEG2Total
    betaAverage = (betaEEG1 + betaEEG2) / 2.0
    attributes["betaEEG1"] = betaEEG1
    attributes["betaEEG2"] = betaEEG2
    attributes["betaAvg"] = betaAverage

    #Gamma 20 - 50 Hz
    gammaEEG1 = sum(processedDataPoint.EEG1Bands[10:25]) / EEG1Total
    gammaEEG2 = sum(processedDataPoint.EEG2Bands[10:25]) / EEG2Total
    gammaAverage = (gammaEEG1 + gammaEEG2) / 2.0
    attributes["gammaEEG1"] = gammaEEG1
    attributes["gammaEEG2"] = gammaEEG2
    attributes["gammaAvg"] = gammaAverage

    #Think about theta-to-delta range
    attributes["T-to-D_EEG1"] = thetaEEG1 / deltaEEG1
    attributes["T-to-D_EEG2"] = thetaEEG2 / deltaEEG2
    attributes["T-to-D_avg"] = thetaAverage / deltaAverage

    #thnk about beta-to-delta range
    attributes["B-to-D_EEG1"] = betaEEG1 / deltaEEG1
    attributes["B-to-D_EEG2"] = betaEEG2 / deltaEEG2
    attributes["B-to-D_avg"] = betaAverage / deltaAverage

    return attributes

def writeArffHeader(attributeNames):
    targetFile.write("@RELATION miceSleep\n")
    for attrName in attributeNames:
        targetFile.write("@ATTRIBUTE " + attrName.replace(" ", "_") + "  REAL\n") 
    targetFile.write("@ATTRIBUTE class {Wake, Non_REM, REM}\n\n") #NOTE: it's Non_REM, not Non REM
    targetFile.write("@DATA\n")

def createArffFile(processedData, score_datapoints, attributeNames):
    numDataPoints = min(len(processedData), len(score_datapoints))
    for i in range(0, numDataPoints):
        name = score_datapoints[i].className.replace(" ", "_")
        #don't bother with unscored epochs
        if name != "Non_REM" and name != "REM" and name != "Wake":
            continue

        attributeDict = computeInterestingAttributes(processedData[i])
        for attrName in attributeNames:
            targetFile.write("%.4f, " % attributeDict[attrName])
        targetFile.write(name + "\n")


def parseProcessCreate():
    attributeNames = setAttributeNames()

    eegSamples = int((len(sys.argv) - 1) / 3)
    writeArffHeader(attributeNames)

    for i in range(0, eegSamples):
        eegRawDataFileName = str(sys.argv[3 * i + 1])
        scoresFileName = str(sys.argv[3* i + 2])
        epochLengthInSeconds = int(sys.argv[3 * i + 3])

        rawData = parseRawData(eegRawDataFileName)
        score_datapoints = parseClassificationData(scoresFileName)
        processedData = processData(rawData, epochLengthInSeconds)
        createArffFile(processedData, score_datapoints, attributeNames)
    targetFile.close()
    

parseProcessCreate()





        
