import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import wc2.*;
import wc2.Constants.MDLShape;

public class MainYoutube {

	private static void init() {
		Constants.N = 5975248;
		Constants.D = 2;
		Constants.NS = new int[Constants.D];
		Constants.NS[0] = 1134890;
		Constants.NS[1] = 1134890;
		Constants.ELfile = "undyoutube.nz";
		Constants.shape = MDLShape.HYPERBOLA;
		Constants.isWeighted = false;
		Constants.speedupDeletions = true;
		Constants.equivalentDimension = new int[Constants.D];
		Constants.equivalentDimension[0] = 0;
		Constants.equivalentDimension[1] = 0;
	}
	
	public static void main(String[] args) throws IOException {
		init();
		FileWriter f = new FileWriter("hyperbola_youtube.csv");
		ELTensor t = new ELTensor(Constants.ELfile, false);
		
		WC2 wc = new WC2(t);
		ArrayList<Community> coms = new ArrayList<Community>();
		int ncoms = 0;
		int tries = 0;
		while (t.nelements > 0 && ncoms < 100) {
			System.out.println(" Try number " + (++tries) + " found " + ncoms);
			Community com = wc.getCommunity();
			if (com.getMode(0).size() < 10) continue;
			//com.printState();
			com.writeToCSV(f);
			coms.add(com);
			t.removeCommunity(com);
			ncoms++;
			System.out.println("Found " + ncoms + ", " + t.nelements + " left");
		}
		f.close();
	}

}
