

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import wc2.*;


public class MainLBLW {

	private static void init() {
		Constants.N = 113030;
		Constants.NA = 1646 + 1;
		Constants.NB = 13782 + 1;
		Constants.NC = 29 + 1;
		Constants.MAXW = 5844;
		Constants.ELfile = "lbl-parsed";
	}
	
	public static void main(String[] args) throws IOException {
		init();
		
		FileWriter f = new FileWriter("lblw.csv");
		ELTensor t = new ELTensor(Constants.ELfile);
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
