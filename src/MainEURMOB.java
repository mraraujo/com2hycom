

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import wc2.*;


public class MainEURMOB {

	private static void init() {
		Constants.N = 51119177;
		Constants.NA = 3952631 + 1;
		Constants.NB = 3952631 + 1;
		Constants.NC = 14 + 1;
		Constants.MAXW = 1;
		Constants.ELfile = "tensor.data";
	}
	
	public static void main(String[] args) throws IOException {
		init();
		ELTensor t = new ELTensor(Constants.ELfile);
		System.out.println("Loaded file");
		Arrays.fill(t.weights, 1);
		WC2 wc = new WC2(t);
		ArrayList<RectangleCommunity> coms = new ArrayList<RectangleCommunity>();
		int ncoms = 0;

		System.out.println("Removing previously found communities");
		t.removeCommunities("eurmob.csv");
		
		FileWriter f = new FileWriter("eurmob.csv", true);
		System.out.println("Searching for communities");
		
		long startTime = System.currentTimeMillis();
		while (ncoms<300) {
			RectangleCommunity com = wc.getCommunity();
			//com.printState();
			com.writeToCSV(f);
			System.out.println("Found " + ncoms + ", " + t.nelements + " left");
			coms.add(com);
			t.removeCommunity(com);
			ncoms++;
		}
		f.close();
		System.out.println(System.currentTimeMillis() + " " + startTime);
	}

}
