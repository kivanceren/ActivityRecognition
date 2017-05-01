package com.recognition.main;

import com.recognition.wisdm.StandAloneFeat;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.classifiers.kivanc.KnnClassifier;
import com.datastructre.kivanc.Data;
import com.exceptions.kivanc.IncorrectParameterException;


public class MainClass {
	public static Map<String,Integer> classMap;
	public static String[] classNames = {"Downstairs", "Jogging", "Upstairs",  "Sitting", "Standing", "Walking"};
	public static void main(String[] args) {
		/* Bu kısım yeni data set yaratmak için kullanıldı. Sonra düzeltilebilir.
		//String read="WISDM_ar_v1.1_raw.txt",write="WISDM_ar_v1.1_transformed.txt";
	    //String[] fileArgs = {read, write};
        StandAloneFeat.main(fileArgs);*/
		
		//DataSet oluşturuluyor.
		classMap = new HashMap<>();
		for(int i=0 ; i<classNames.length; i++) classMap.put(classNames[i],i); 
		readFromFile.readDataFile("WISDM_ar_v1.1_transformed.txt", classMap);
		List<Data> dataSet = readFromFile.getDataSet();
		
		//KNN Sınıflandırıcı
		KnnClassifier knnClassifier = new KnnClassifier(0,3,6,dataSet,dataSet);
		knnClassifier.setClassLabels(classNames);
		
		try {
			  knnClassifier.kNNClassifierMethod(dataSet.get(1000));
			  knnClassifier.createConfusionMatrix();
			  knnClassifier.printConfusionMatrix();
			  
		} catch (IllegalStateException | IncorrectParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(knnClassifier.accuaryOfClassifiers());
	}
	

}
