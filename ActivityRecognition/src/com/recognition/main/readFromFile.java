package com.recognition.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastructre.kivanc.Data;

public class readFromFile {
	
	public static ArrayList<Data> dataSet ;
	
	public static void readDataFile(String fileName, Map<String, Integer> classMap){
		
		dataSet = new ArrayList<>();
		FileInputStream fis;
		BufferedReader read = null;
		try {
			fis = new FileInputStream(fileName);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8"); 
			read = new BufferedReader(in);
			readFile(read, classMap);
		} catch (FileNotFoundException e1) {
			System.out.println("Error file not found exception:" + e1.getMessage());
			System.exit(0);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error encodinging a file reader:" + e.getMessage());
			System.exit(0);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	private static void readFile(BufferedReader read, Map<String,Integer> classMap) throws IOException{
		
		String tmpLine = "";
		while((tmpLine = read.readLine()) != null ){
			String[] values = tmpLine.split(",");
			double[] doubleAttributes = new double[values.length-1];
			for(int i=1; i<values.length; i++)
				doubleAttributes[i-1] = Double.parseDouble(values[i]);
			Data data = new Data(doubleAttributes, classMap.get(values[0]));
			dataSet.add(data);
			
		}
	}
	
	public static List<Data> getDataSet(){return dataSet;}

}
