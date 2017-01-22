//To run this: First need to download weka-3-8-0 or some other equivalent
//version of weka and put the folder next to this file in the same directory then run:
//javac -cp .:weka-3-8-0/weka.jar learnAndClassify.java
//java -cp .:weka-3-8-0/weka.jar learnAndClassify [trainingArffFileName] <testingArffFileName> <save> <load> <classify>
//things in square brackets are necessary, things in angle brackets are optional

import weka.core.Instances;
import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.core.SerializationHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

class learnAndClassify {

    //veryFirstTime is in format: "10/14/2016 12:59:59"
    public static String computeTimeString(String veryFirstTime, int secondsSince) {
	int startingMonth = Integer.parseInt(veryFirstTime.substring(0, veryFirstTime.indexOf("/")));
	veryFirstTime = veryFirstTime.substring(veryFirstTime.indexOf("/") + 1);
	int startingDay = Integer.parseInt(veryFirstTime.substring(0, veryFirstTime.indexOf("/")));
	veryFirstTime = veryFirstTime.substring(veryFirstTime.indexOf("/") + 1);
	int startingYear = Integer.parseInt(veryFirstTime.substring(0, veryFirstTime.indexOf(" ")));
	veryFirstTime = veryFirstTime.substring(veryFirstTime.indexOf(" ") + 1);
	int startingHour = Integer.parseInt(veryFirstTime.substring(0, veryFirstTime.indexOf(":")));
	veryFirstTime = veryFirstTime.substring(veryFirstTime.indexOf(":") + 1);
	int startingMin = Integer.parseInt(veryFirstTime.substring(0, veryFirstTime.indexOf(":")));
	veryFirstTime = veryFirstTime.substring(veryFirstTime.indexOf(":") + 1);
	int startingSec = Integer.parseInt(veryFirstTime);

	int currentSec = secondsSince + startingSec;
	int currentMin = startingMin + (int)(currentSec / 60);
	currentSec %= 60;
	int currentHour = startingHour + (int)(currentMin / 60);
	currentMin %= 60;
	int currentDay = startingDay + (int)(currentHour / 24);
	currentHour %= 60;
	int currentMonth = startingMonth + (int)(currentDay / 30); //oh god
	currentDay %= 24;
	int currentYear = startingYear + (int)(currentMonth / 365); //oh god
	currentMonth %= 30;


	return String.format("%02d",currentMonth) + "/" + String.format("%02d",currentDay) + "/" + String.format("%02d",currentYear) + " " + String.format("%02d", currentHour) + ":" + String.format("%02d", currentMin) + ":" + String.format("%02d", currentSec);
    }


    public static void main(String[] args) {
	String trainingArffFileName = args[0].trim();
	String testingArffFileName = "";
	//Flags to save and load a trained classifier, specify whether or not we classify "testing" data.
	boolean save = false; //flag to choose whether or not to save the learned classifer
	boolean load = false; //flag to choose whether or not to load the last saved classifier
	boolean classifying = false; 
	//if classifying is set to true: 
	//     * all instances in the testingArffFile will be classified and the resulting classified arff file will be put in a arff file
	// else:
	//     * the performance of the classifier will be judged off of the values in the testingArffFile


	//TODO: maybe move this stuff into a argumentSetter() method or smthing
	if (args.length >= 2 && args[1].contains(".arff")) {
	    testingArffFileName = args[1].trim();
	}
       
	for (int i=1; i < args.length; i++){
	    if (args[i].contains("save")){
		save = true;
	    }
	    if (args[i].contains("load")){
		load = true;
	    }
	    if (args[i].contains("classify")){
		classifying = true;
	    }
	}

	BufferedReader reader;
	Instances trainingData;
	Instances testingData;

	try {
	    reader = new BufferedReader(new FileReader(trainingArffFileName));
	    trainingData = new Instances(reader);
	    reader.close();

	    reader = new BufferedReader(new FileReader(testingArffFileName));
	    testingData = new Instances(reader);
	    reader.close();

	} catch (Exception e) {
	    System.out.println("check your testing and training filenames, silly goose");
	    return;
	}
	
	trainingData.setClassIndex(trainingData.numAttributes() - 1);
	testingData.setClassIndex(testingData.numAttributes() - 1);
	
	SMO smo1 = new SMO(); //linear kernel
	SMO smo2 = new SMO(); //2nd order polynomial
	NaiveBayes nb = new NaiveBayes();
	J48 j48 = new J48();
	IBk ibk1 = new IBk(); //1 nearest neighbor
	// Couldn't figure out how to set the options to ibk3 and ibk5 correctly :(
	//IBk ibk3 = new IBk(); //3 nearest neighbor
	//IBk ibk5 = new IBk(); //5 nearest neighbor

	//File strings for writing and reading classifiers.
	String nbString = "classifiers/NaiveBayes.model";
	String smo1String = "classifiers/SVMLinearKernel.model";
	String smo2String = "classifiers/SVMPolyKernel.model";
	String j48String = "classifiers/DecisionTree.model";
	String ibk1String = "classifiers/k-Nearest.model";

	if (!load) {
	    try {
		nb.buildClassifier(trainingData);
		if (save){
		    SerializationHelper.write(nbString, nb); //added to save the given classifier
		}
	    } catch (Exception e) {
		System.out.println("couldn't build naive bayes. Exception " + e);
		return;
	    }
	    
	    System.out.println("finished building nb");
	    
	    try {
		smo1.buildClassifier(trainingData);
		if (save) {
		    SerializationHelper.write(smo1String, smo1); //added to save the given classifier
		}
	    } catch (Exception e) {
		System.out.println("couldn't build linear SMO " + e);
		return;
	    }

	    System.out.println("finished building smo1");
	    
	    try {
		smo2.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 2.0\""));
		smo2.buildClassifier(trainingData);
		if (save) {
		    SerializationHelper.write(smo2String, smo2); //added to save the given classifier
		}
	    } catch (Exception e) {
		System.out.println("couldn't build second order polynomial SMO " + e);
		return;
	    }

	    System.out.println("finished building smo2");

	    try {
		j48.buildClassifier(trainingData);
		if (save) {
		    weka.core.SerializationHelper.write(j48String, j48); //added to save the given classifier
		}
	    } catch (Exception e) {
		System.out.println("couldn't build decision tree " + e);
		return;
	    }

	    System.out.println("finished building j48");
	    
	    try {
		ibk1.buildClassifier(trainingData);
		if (save) {
		    weka.core.SerializationHelper.write(ibk1String, ibk1); //added to save the given classifier
		}
	    } catch (Exception e) {
		System.out.println("couldn't build 1 nearest neighbor " + e);
		return;
	    }
	    
	    System.out.println("finished building ibk 1");
	    
	}
	/*
	try {
	    ibk3.buildClassifier(trainingData);
	    ibk3.setOptions(weka.core.Utils.splitOptions("-K 3"));
	    //ibk3.setOptions(new String[] {"-K", "3", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""});
	} catch (Exception e) {
	    System.out.println("couldn't build 3 nearest neighbor");
	    return;
	}

	try {
	    ibk5.buildClassifier(trainingData);
	    ibk5.setOptions(weka.core.Utils.splitOptions("-K 5"));
	    //ibk5.setOptions(new String[] {"-K", "5", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""});
	} catch (Exception e) {
	    System.out.println("couldn't build 5 nearest neighbor");
	    return;
	}

	System.out.println("finished building ibk 5");

	*/
	

	if (load) {
	    try {
		nb = (NaiveBayes) weka.core.SerializationHelper.read(nbString);
	    } catch (Exception e){
		System.out.println(e.getClass().getName());
	    }
	    try {
		smo1 = (SMO) weka.core.SerializationHelper.read(smo1String);
	    } catch (Exception e){
		System.out.println(e.getClass().getName());
	    }
	    try {
		smo2 = (SMO) weka.core.SerializationHelper.read(smo2String);
	    } catch (Exception e){
		System.out.println(e.getClass().getName());
	    }
	    try {
		j48 = (J48) weka.core.SerializationHelper.read(j48String);
	    } catch (Exception e){
		System.out.println(e.getClass().getName());
	    }
	    try {
		ibk1 = (IBk) weka.core.SerializationHelper.read(ibk1String);
	    } catch (Exception e){
		System.out.println(e.getClass().getName());
	    }
	
	}

	int smo1NumWrong = 0;
	int smo2NumWrong = 0;
	int nbNumWrong = 0;
	int j48NumWrong = 0;
	int ibk1NumWrong = 0;
	//int ibk3NumWrong = 0;
	//int ibk5NumWrong = 0;
	
	int cumulativeNumWrong = 0;
	int numClassified = 0;

	ArrayList<Integer> classifications = new ArrayList<Integer>(); 
	    /*	ArrayList<String> fileWithClassifications = new ArrayList<String>();
		String line;
	if (classifying){
	    try{
		reader = new BufferedReader(new FileReader(testingArffFileName));
		while ((line = reader.readLine()) != null) fileWithClassifications.add(line + "\n");
		reader.close();
	    } catch (Exception e){
		System.out.println("Problem reading testing file");
	    }
	    }*/
	
	for (int i = 0; i < testingData.numInstances(); i++){
	    if (i % 50 == 0) {
		System.out.println("classifying testcase " + i);
	    }

	    double smo1Pred, smo2Pred, nbPred, j48Pred, ibk1Pred; 
	    double cumulativePred = 255.0;

	    try {
		smo1Pred = smo1.classifyInstance(testingData.instance(i));
		smo2Pred = smo2.classifyInstance(testingData.instance(i));
		nbPred = nb.classifyInstance(testingData.instance(i));
		j48Pred = j48.classifyInstance(testingData.instance(i));
		ibk1Pred = ibk1.classifyInstance(testingData.instance(i));

		//this is how to get a predDistribution. right now that's not useful but maybe later?
		//The reason it's not useful:
		//For naive bayes the predDistribution is almost always like [0.99, epsilon, epsilon]
		//For smo1 and smo2 it's always like [0.333, 0, 0.6666]
		//predDistribution = smo2.distributionForInstance(testingData.instance(i));
	    } catch (Exception e) {
		System.out.println("couldn't classify test instance: " + i);
		return;
	    }

	    //only when all classifiers agree, will we classify something.
	    if ((int) smo1Pred == (int) smo2Pred && 
		(int) smo2Pred == (int) nbPred &&
		(int) nbPred == (int) j48Pred &&
		(int) j48Pred == (int) ibk1Pred) {
		
		cumulativePred = ibk1Pred;
		numClassified++;

		if (!classifying && ((int) testingData.instance(i).classValue()) != ((int) cumulativePred)) {
		    cumulativeNumWrong++;
		}
	    }
		
	    if (classifying){//We modify the file to reflect the classifications.
		classifications.add(new Integer((int)cumulativePred));
		//		int j = fileWithClassifications.indexOf("?"); //ew
		//		fileWithClassifications = fileWithClassifications.substring(0, j) + (int)cumulativePred + fileWithClassifications.substring(j+1);//TODO: fix this hacked shit
	    }

	    if (!classifying){
		if (((int) testingData.instance(i).classValue()) != ((int) smo1Pred)) {
		    smo1NumWrong++;
		}
		if (((int) testingData.instance(i).classValue()) != ((int) smo2Pred)) {
		    smo2NumWrong++;
		}
		if (((int) testingData.instance(i).classValue()) != ((int) nbPred)) {
		    nbNumWrong++;
		}
		if (((int) testingData.instance(i).classValue()) != ((int) j48Pred)) {
		    j48NumWrong++;
		}
		if (((int) testingData.instance(i).classValue()) != ((int) ibk1Pred)) {
		    ibk1NumWrong++;
		}
	    }
	}

	if (classifying){
	    try{
		//TODO: take the veryFirstTime and epochTimes in as arguments
		String veryFirstTime = "10/14/2016 12:59:59";
		final int epochLengthInSeconds = 6;

		PrintWriter writer = new PrintWriter(testingArffFileName.substring(0, testingArffFileName.indexOf(".arff")) + "Classified.csv");
		writer.println("Epoch #,Start Time,End Time,Score #, Score");

		for (int j = 0; j < classifications.size(); j++) {
		    String score;
		    switch (classifications.get(j)) {
		        case 0: score = "Wake";
			    break;
		        case 1: score = "Non REM";
		    	    break;
		        case 2: score = "REM";
			    break;
		        default: score = "Unscored";
			    break;
		    }
		    

		    String startingTime = computeTimeString(veryFirstTime, j * epochLengthInSeconds);
		    String endingTime = computeTimeString(veryFirstTime, (j + 1) * epochLengthInSeconds);

		    writer.println((j+1) + "," + startingTime + "," + endingTime + "," +classifications.get(j) + "," + score);
		}

		writer.close();		
		//		dataOut.write(fileWithClassifications.getBytes());
		//dataOut.close();
	    } catch (Exception e) {
		System.out.println("Problem writing back to the file with classifications. If you've gotten here, this should never fail. There could be a problem...");
	    }
	}

	if (!classifying){
	    System.out.println("smo 1 total wrong: " + smo1NumWrong + " which means error rate of: " + (float) smo1NumWrong / (float) testingData.numInstances());
	    System.out.println("smo 2 total wrong: " + smo2NumWrong + " which means error rate of: " + (float) smo2NumWrong / (float) testingData.numInstances());
	    System.out.println("nb total wrong: " + nbNumWrong + " which means error rate of: " + (float) nbNumWrong / (float) testingData.numInstances());
	    System.out.println("j48 total wrong: " + j48NumWrong + " which means error rate of: " + (float) j48NumWrong / (float) testingData.numInstances());
	    System.out.println("ibk1 total wrong: " + ibk1NumWrong + " which means error rate of: " + (float) ibk1NumWrong / (float) testingData.numInstances());


	    System.out.println("\n\ncumulative total wrong: " + cumulativeNumWrong + " which means error rate of: " + (float) cumulativeNumWrong / (float) numClassified);
	}
	System.out.println("Numclassified: " + numClassified + " percent classified: " + (float) numClassified / (float) testingData.numInstances());
	//I think this is how we include PCA or not... not super sure.
	/*
	PrincipalComponents pca = new PrincipalComponents();
	pca.setMaximumAttributeNames(25);
	pca.buildEvaluator(data);
	data = pca.transformedData(data);
	*/

	//Filter filter = filter.setInputFormat(data);
	//Instances filteredData = Filter.useFilter(data, filter);
	
    }
}
