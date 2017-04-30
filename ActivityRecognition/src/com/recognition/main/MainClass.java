package com.recognition.main;

import com.recognition.wisdm.StandAloneFeat;

public class MainClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String read="WISDM_ar_v1.1_raw.txt",write="WISDM_ar_v1.1_transformed.txt";
		String[] fileArgs = {read, write};
       StandAloneFeat.main(fileArgs);
	}

}
