//To run this: First need to download weka-3-8-0 or some other equivalent
//version of weka and put the folder next to this file in the same directory then run:
//javac -cp .:weka-3-8-0/weka.jar learnAndClassify.java
//java -cp .:weka-3-8-0/weka.jar learnAndClassify

import weka.core.Instances;
import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;

class learnAndClassify {

    public static void main(String[] args) {
	String trainingArffFileName = "mice2And3.arff";	
	String testingArffFileName = "mouse1EEG.arff";
	

	BufferedReader reader;
	Instances trainingData;
	Instances testingData;
	
	//Flags to save and load a trained classifier, specify whether or not we classify "testing" data.
	boolean save = false; //flag to choose whether or not to save the learned classifer
	boolean load = false;
	boolean classifying = false;


	for (int i=0; i < args.length; i++){
	    if (args[i].equals("save")){
		save = true;
	    }
	    if (args[i].equals("load")){
		load = true;
		if (i < args.length-1){
		    testingArffFileName = args[i+1]; //the next string after load is the test file
		}
	    }
	    if (args[i].equals("classify")){
		classifying = true;
	    }
	}
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

	/*
	try {
	    nb.buildClassifier(trainingData);
	    if (save){
		weka.core.SerializationHelper.write(nbString, nb); //added to save the given classifier
	    }
	} catch (Exception e) {
	    System.out.println("couldn't build naive bayes");
	    return;
	}

	System.out.println("finished building nb");

	try {
	    smo1.buildClassifier(trainingData);
	    if (save) {
		weka.core.SerializationHelper.write(smo1String, smo1); //added to save the given classifier
	    }
	} catch (Exception e) {
	    System.out.println("couldn't build linear SMO");
	    return;
	}

	System.out.println("finished building smo1");

	try {
	    smo2.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 2.0\""));
	    smo2.buildClassifier(trainingData);
	    if (save) {
		weka.core.SerializationHelper.write(smo2String, smo2); //added to save the given classifier
	    }
	} catch (Exception e) {
	    System.out.println("couldn't build second order polynomial SMO");
	    return;
	}

	System.out.println("finished building smo2");

	try {
	    j48.buildClassifier(trainingData);
	    if (save) {
		weka.core.SerializationHelper.write(j48String, j48); //added to save the given classifier
	    }
	} catch (Exception e) {
	    System.out.println("couldn't build decision tree");
	    return;
	}

	System.out.println("finished building j48");

	try {
	    ibk1.buildClassifier(trainingData);
	    if (save) {
		weka.core.SerializationHelper.write(ibk1String, ibk1); //added to save the given classifier
	    }
	} catch (Exception e) {
	    System.out.println("couldn't build 1 nearest neigbhor");
	    return;
	}

	System.out.println("finished building ibk 1");

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
	int smo1NumWrong = 0;
	int smo2NumWrong = 0;
	int nbNumWrong = 0;
	int j48NumWrong = 0;
	int ibk1NumWrong = 0;
	//int ibk3NumWrong = 0;
	//int ibk5NumWrong = 0;
	
	int cumulativeNumWrong = 0;
	int numClassified = 0;
	

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
	
	String fileWithClassifications = "";
	String line;
	if (classifying){
	    try{
	    reader = new BufferedReader(new FileReader(testingArffFileName));
	    while ((line = reader.readLine()) != null) fileWithClassifications += line + "\n";
	    reader.close();
	    } catch (Exception e){
		System.out.println("Problem reading testing file");
	    }
	}
	
	for (int i = 0; i < testingData.numInstances(); i++){
	    if (i % 10 == 0) {
		System.out.println("classifying testcase" + i);
	    }


	    double smo1Pred, smo2Pred, nbPred, j48Pred, ibk1Pred; 
	    double cumulativePred = -5.0;
	    double[] predDistribution;


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

		if (((int) testingData.instance(i).classValue()) != ((int) cumulativePred) && !classifying) {
		    cumulativeNumWrong++;
		}

	    }
		
	    if (classifying){//We modify the file to reflect the classifications.
		int j = fileWithClassifications.indexOf("?");
		fileWithClassifications = fileWithClassifications.substring(0, j) + (int)cumulativePred + fileWithClassifications.substring(j+1);//fix this hacked shit
	    }
	    //for (int j = 0; j < predDistribution.length; j++) {
	    //		System.out.println(j + " hey check out this distribution: " + predDistribution[j]);
	    //}

	    //System.out.print("ID: " + testingData.instance(i).value(0));
	    //System.out.print(", actual: " + testingData.classAttribute().value((int) testingData.instance(i).classValue()));
	    //System.out.println(", predicted: " + testingData.classAttribute().value((int) pred));
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
	    //TODO: Write the file output back to the file
	    try{
		FileOutputStream dataOut = new FileOutputStream(testingArffFileName.substring(0, testingArffFileName.indexOf(".arff")) + "Classified.arff");
		dataOut.write(fileWithClassifications.getBytes());
		dataOut.close();
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
	    System.out.println("Numclassified: " + numClassified + " percent classified: " + (float) numClassified / (float) testingData.numInstances());

	}
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
