

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import wc2.*;


public class MainLBLB {

	private static void init() {
		Constants.N = 113030;
		Constants.NA = 1646 + 1;
		Constants.NB = 13782 + 1;
		Constants.NC = 29 + 1;
		Constants.MAXW = 1;
		Constants.ELfile = "lbl-parsed";
	}
	
	public static void main(String[] args) throws IOException {
		init();
		
		FileWriter f = new FileWriter("lblb.csv");
		ELTensor t = new ELTensor(Constants.ELfile);
		Arrays.fill(t.weights, 1);
		WC2 wc = new WC2(t);
		ArrayList<RectangleCommunity> coms = new ArrayList<RectangleCommunity>();
		int ncoms = 0;
		while (t.nelements > 0) {
			RectangleCommunity com = wc.getCommunity();
			//com.printState();
			com.writeToCSV(f);
			System.out.println("Found " + ncoms + ", " + t.nelements + " left");
			coms.add(com);
			t.removeCommunity(com);
			ncoms++;
		}
		f.close();
	}

}
