package com.recognition.wisdm;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * WISDM Projecteki hatalı yazılan readFile fonksiyonu @author kivanceren tarafından düzeltilmiştir.
 * 
 * WISDM project research program
 * http://storm.cis.fordham.edu/~gweiss/wisdm
 * 
 * This class takes raw data from the client app and outputs an .arff file suitable
 * for weka interpretation.
 * 
 * @author Jeff Lockhart <a href="mailto:lockhart@cis.fordham.edu">lockhart@cis.fordham.edu</a>
 * @author Jess Timko 
 * @version 4.0
 * @date 7 July 2014
 *
 */
public class StandAloneFeat {

	/**
	 * a threadsafe queue for SplitResults objects added to by TupleSorter and taken from by FeatGen
	 */
	private static LinkedList<TupFeat> que = new LinkedList<TupFeat>();
	
	private static String[] usrList = null;

	private static String[] actList = null;
	
	private static int usrCount = 0;
	
	
	// windowSize = number of seconds for window frame
	private static int windowSize = 10;
	
	//samplingRate = Hz (number of samples collected per second)
	//currently use 20 Hz sampling rate
	private static int samplingRate = 20;
	
	private static BufferedWriter outp = null;
	
	// windowSize*20 entries is this much change in timestamps 
	private static double duration = 1/(double)samplingRate * Math.pow(10, 9) * 5;
	
	/**
	 * holds the boundaries used in binning. values settable in main
	 */
	private static double[] bins = new double[30];

	/**
	 * @param args
	 * args[0] should be the name of the file to read
	 * args[1] should be the name of the file to write
	 */
	public static void main(String[] args) {		
		bins[0] = -2.5;
		bins[1] = 0;
		bins[2] = 2.5;
		bins[3] = 5;
		bins[4] = 7.5;
		bins[5] = 10;
		bins[6] = 12.5;
		bins[7] = 15.0;
		bins[8] = 17.5;
		bins[9] = 20;
		bins[10] = -2.5;
		bins[11] = 0;
		bins[12] = 2.5;
		bins[13] = 5;
		bins[14] = 7.5;
		bins[15] = 10;
		bins[26] = 12.5;
		bins[17] = 15.0;
		bins[18] = 17.5;
		bins[19] = 20;
		bins[20] = -2.5;
		bins[21] = 0;
		bins[22] = 2.5;
		bins[23] = 5;
		bins[24] = 7.5;
		bins[25] = 10;
		bins[26] = 12.5;
		bins[27] = 15.0;
		bins[28] = 17.5;
		bins[29] = 20;
				
		FileInputStream fis;
		BufferedReader read = null;
		try {
			fis = new FileInputStream(args[0]);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8"); 
			read = new BufferedReader(in);
		} catch (FileNotFoundException e1) {
			System.out.println("Error file not found exception:" + e1.getMessage());
			System.exit(0);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error encodinging a file reader:" + e.getMessage());
			System.exit(0);
		}
		usrList = new String[50];
		actList = new String[]{"NoLabel", "Walking", "Jogging", "Stairs", 
				"Sitting", "Standing", "LyingDown"};
		if(args.length > 2){
			
			windowSize = Integer.parseInt(args[2]);
				
		}
		
		try {
			readFile(read);
		
		} catch (IOException e) {
			System.out.println("Error reading file. Operation aborted." + e.getMessage());
			System.exit(0);
		}
		TupFeat tmp = null;
		for(int i = 0; i < que.size(); i++){
			tmp = que.get(i);
			FeatureLib.processTup(tmp, bins);

		}
		
		/*for(int i = 0; i < que.size(); i++){
			System.out.println(que.get(i).toString());

		}*/
		
		writeArff(args[1]);
	}
	

	/**
	 * this function is absurdly long. 
	 * @param read
	 * @throws IOException
	 */
	private static void readFile(BufferedReader read) throws IOException{
		
		System.out.println("Started reading file ");
		int wssr = windowSize*samplingRate; 
		int sayac = 0;
		
		String cusr = "joeUnreal"; // user of current tuple
		String cact = ""; // activity of current tuple
		float[] x = new float[(windowSize*samplingRate)]; // holds the accelerometer data for a single tuple
		float[] y = new float[(windowSize*samplingRate)];
		float[] z = new float[(windowSize*samplingRate)];
		long[] t = new long[(windowSize*samplingRate)];
		String tmpLn = null, tmpLna = null, lastLn = "fakeLine";
		long cTime = 0, tmpt = 1, lastTime = 0; // time of start of current tuple, and temp time
		int i = 0; // counter for tuple members
		int abCount = 0; //abandoned tuple count
		int savTCount = 0; //saved tuple count
		int repCount = 0;
		int cc=0;
		while((tmpLna = read.readLine()) != null ){ //sonuna kadar dön file içinde 
		
		try{
			tmpLn = tmpLna.replace(';', ',');
			String[] values = tmpLn.split(",");	
			
			if(tmpLna.equals(lastLn)){
				repCount++;
				continue; // skip repeated input
			}else{//satır tekrarı yoksa
				lastLn = tmpLna;
				if(!cusr.equals(values[0]) && cact.equals(values[1])){//Kullanıcı değişmiş ise fakat aktivite aynı ise
					
					if(i >= wssr*0.9 ){
											//Kayıt edilecek tuple şeklinde
						savTCount++;
						TupFeat tup = new TupFeat(Long.valueOf(cusr), cact, cTime);
						// all arrays must be copied into new ones because java is pass by reference always
						float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
						long[] tt = new long[(windowSize*samplingRate)];
						for(int j = 0; j<(windowSize*samplingRate); j++){
							xt[j] = x[j];
							yt[j] = y[j];
							zt[j] = z[j];
							tt[j] = t[j];
						}
						
						tup.setRaw(xt, yt, zt, tt);
						tup.setCount(i);
						que.add(tup);
						
					}
					else{
						abCount++;
						
					}
					cusr = values[0];
					lastTime = Long.parseLong(values[2]);
					i=0;
					//xi yi zi ti ekle
					//i++;
				}else if(!cact.equals(values[1]) && cusr.equals(values[0])){//Aktivite değişmiş kullanıcı aynı
					
					if(i >= wssr*0.9 ){
						//Kayıt edilecek tuple şeklinde
						savTCount++;
						TupFeat tup = new TupFeat(Long.valueOf(cusr), cact, cTime);
						// all arrays must be copied into new ones because java is pass by reference always
						float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
						long[] tt = new long[(windowSize*samplingRate)];
						for(int j = 0; j<(windowSize*samplingRate); j++){
							xt[j] = x[j];
							yt[j] = y[j];
							zt[j] = z[j];
							tt[j] = t[j];
						}
						
						tup.setRaw(xt, yt, zt, tt);
						tup.setCount(i);
						que.add(tup);
					}
					else{
						abCount++;
					}
					cact = values[1];
					lastTime = Long.parseLong(values[2]);
					i = 0;
				
					//i++;
					
				}else if(!cusr.equals(values[0]) && !cact.equals(values[1])){//ikiside değişmiş
					
					if(i >= wssr*0.9 ){
						//Kayıt edilecek tuple şeklinde
						savTCount++;
						TupFeat tup = new TupFeat(Long.valueOf(cusr), cact, cTime);

						// all arrays must be copied into new ones because java is pass by reference always
						float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
						long[] tt = new long[(windowSize*samplingRate)];
						for(int j = 0; j<(windowSize*samplingRate); j++){
							xt[j] = x[j];
							yt[j] = y[j];
							zt[j] = z[j];
							tt[j] = t[j];
						}
						
						tup.setRaw(xt, yt, zt, tt);
						tup.setCount(i);
						que.add(tup);
					}
					else{
						abCount++;
					}
					cusr = values[0];
					cact = values[1];
					lastTime = Long.parseLong(values[2]);
					i = 0;
					//xi yi zi ti ekle
					//i++;
					
				}else{//ikiside değişmemiş
					cTime =  Long.parseLong(values[2]);
					
					long timeDif = cTime - lastTime;
			
					if(timeDif <= duration){//Cok uzun sğre yoksa ve sonraki süredeki veriyse
						if(lastTime != cTime && cTime!=0){//Sıknıtısız okuma
							x[i] = Float.valueOf(values[3].trim()).floatValue(); 
							y[i] = Float.valueOf(values[4].trim()).floatValue();
							z[i] = Float.valueOf(values[5].trim()).floatValue();
							t[i] = tmpt;
							lastTime = cTime;
							i++;
						}
					}else if( i >= wssr*0.9){
						savTCount++;						
						TupFeat tup = new TupFeat(Long.valueOf(cusr), cact, cTime);
						// all arrays must be copied into new ones because java is pass by reference always
						float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
						long[] tt = new long[(windowSize*samplingRate)];
						for(int j = 0; j<(windowSize*samplingRate); j++){
							xt[j] = x[j];
							yt[j] = y[j];
							zt[j] = z[j];
							tt[j] = t[j];
						}
						
						tup.setRaw(xt, yt, zt, tt);
						tup.setCount(i);
						que.add(tup);						
						lastTime = cTime;
						i=0;
						
					}else{
						abCount++;
						lastTime = cTime;
						i=0;
					}
					
					if(i == wssr){
						savTCount++;
						TupFeat ttup = new TupFeat(Long.valueOf(cusr), cact, cTime);
						ttup.setCount(i);
						// all arrays must be copied into new ones because java is pass by reference always
						float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
						long[] tt = new long[(windowSize*samplingRate)];
						for(int j = 0; j<(windowSize*samplingRate); j++){
							xt[j] = x[j];
							yt[j] = y[j];
							zt[j] = z[j];
							tt[j] = t[j];
						}
						
						ttup.setRaw(xt, yt, zt, tt);
						que.add(ttup);
						lastTime = cTime;
						i=0;
					}
					
					
				}
				
				
			  
			
			}
		
		}catch(ArrayIndexOutOfBoundsException e){
			continue;
		}
			
			
		
			
		}
		System.out.println("Abandoned tuple count = " + abCount);
		System.out.println("saved tuple count = " + savTCount);
		System.out.println("Repeaded lines: " + repCount);
		
		
		
		
	} // end function readfile
	
	private static void writeArff(String n) {
		// Establish the file connection
		outp = getFileConn(n);
		System.out.println("writing arff function entered ");
		//writeArffHeader(); Bunu arff dosyası yapmak istersek olur.
		writeData();

		System.out.println("Output written to file " + n + ".");
	}

	/**
	 * prints the data in the result set to the file
	 * @param rs the result set
	 * @param rsMeta the meta data for the result set
	 */
	private static void writeData() {
		System.out.println("write data function entered ");
		// Temporary tuple variables
		String tuple = "";
		TupFeat tup = null;
		float[] f = null;
		//int c = 0;
		// Go through the entire result set 		
		try {
			System.out.println("write data try block entered ");
			
			while (!que.isEmpty())
			{				
				tup = que.pop();
				f = tup.getFeat();
				//tuple = "";
				//tuple += c++;
				//tuple += ",";
				String tmp = tup.getAct();
				tuple += tmp;
				tuple += ",";
				for (int i = 0; i < 42; i++){ // the data itself
					tuple += f[i];
					tuple += ",";
				}
				tuple += f[42];
				//tuple += tup.getUsr(); // column 2 is userid
										
				outp.write(tuple + "\n");
				tuple = null;
				tuple = new String("");
			}
			outp.flush();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage() + "data not written");
			System.exit(0);
		}		
	}
	
	/**
	 * writes the Arff Header to the out file for weka processing.
	 * currently these are inaccurate, but it rarely matters since 
	 * they are overwritten in the headless arff process anyway.
	 */
	private static void writeArffHeader() {
		
		System.out.println("write arff function entered ");
		
		try {
			
			System.out.println("write arff header try block entered ");
			
			outp.write("@relation person_activities_labeled \n\n" +
					"@attribute \"UNIQUE_ID\" numeric \n" +
					"@attribute \"ACTIVITY\" { \"");
			outp.write(actList[0] + "\" "); //prints first activity, not preceded by comma
			if (actList.length > 1){ 
				//prints second, third, etc. activities with commas first
				for (int j = 1; j < actList.length; j++){
					outp.write(", \"" + actList[j] + "\" ");
				}
			}
			outp.write( "}\n" +
			"@attribute \"X0\" numeric \n"+
			"@attribute \"X1\" numeric \n"+
			"@attribute \"X2\" numeric \n"+
			"@attribute \"X3\" numeric \n"+
			"@attribute \"X4\" numeric \n"+
			"@attribute \"X5\" numeric \n"+	
			"@attribute \"X6\" numeric \n"+
			"@attribute \"X7\" numeric \n"+
			"@attribute \"X8\" numeric \n"+
			"@attribute \"X9\" numeric \n"+
			"@attribute \"Y0\" numeric \n"+
			"@attribute \"Y1\" numeric \n"+
			"@attribute \"Y2\" numeric \n"+
			"@attribute \"Y3\" numeric \n"+
			"@attribute \"Y4\" numeric \n"+
			"@attribute \"Y5\" numeric \n"+
			"@attribute \"Y6\" numeric \n"+
			"@attribute \"Y7\" numeric \n"+
			"@attribute \"Y8\" numeric \n"+
			"@attribute \"Y9\" numeric \n"+
			"@attribute \"Z0\" numeric \n"+
			"@attribute \"Z1\" numeric \n"+
			"@attribute \"Z2\" numeric \n"+
			"@attribute \"Z3\" numeric \n"+
			"@attribute \"Z4\" numeric \n"+
			"@attribute \"Z5\" numeric \n"+
			"@attribute \"Z6\" numeric \n"+
			"@attribute \"Z7\" numeric \n"+
			"@attribute \"Z8\" numeric \n"+
			"@attribute \"Z9\" numeric \n"+
			"@attribute \"XAVG\" numeric \n"+
			"@attribute \"YAVG\" numeric \n"+
			"@attribute \"ZAVG\" numeric \n"+
			"@attribute \"XPEAK\" numeric \n"+
			"@attribute \"YPEAK\" numeric \n"+
			"@attribute \"ZPEAK\" numeric \n"+
			"@attribute \"XABSOLDEV\" numeric \n"+
			"@attribute \"YABSOLDEV\" numeric \n"+
			"@attribute \"ZABSOLDEV\" numeric \n"+
			"@attribute \"XSTANDDEV\" numeric \n"+
			"@attribute \"YSTANDDEV\" numeric \n"+
			"@attribute \"ZSTANDDEV\" numeric \n"+
			"@attribute \"RESULTANT\" numeric \n"+
			"@attribute class {");
			outp.write("\"" + usrList[0] + "\" "); //prints first user, not preceded by comma
			if (usrCount > 1){ //prints second, third, etc. users with commas first
				for (int j = 1; j < usrCount; j++){
					outp.write(", \"" + usrList[j] + "\"");
				}
			}
			
			outp.write("}\n\n@data\n");
						
			outp.flush();
		} catch (IOException e) {
			System.out.println("Error writing arff header: " + e.getMessage() );
			System.exit(0);
		}
	}

	/**
	 * Gets a buffered writer so that we can write a file to the hard disk.
	 *@param fileName the name of the file to write to disk
	 *@return a BufferedWriter that points to fileName 
	 */
	private static BufferedWriter getFileConn(String fileName)
	{
		BufferedWriter bw = null;
		try 
		{
			bw = new BufferedWriter(new FileWriter(fileName));
		} 
		catch (IOException e) 
		{
			System.out.println("Error making arff file: " + e.getMessage() );
			System.exit(0);
		}

		return bw;

	} 
	

	
	
} // end class