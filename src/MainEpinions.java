import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import wc2.*;
import wc2.Constants.MDLShape;

public class MainEpinions {

	private static void init() {
		Constants.N = 605286;
		Constants.D = 2;
		Constants.NS = new int[Constants.D];
		Constants.NS[0] = 75879;
		Constants.NS[1] = 75879;
		Constants.ELfile = "undepinions.nz";
		Constants.shape = MDLShape.HYPERBOLA;
		Constants.isWeighted = false;
		Constants.speedupDeletions = true;
		Constants.equivalentDimension = new int[Constants.D];
		Constants.equivalentDimension[0] = 0;
		Constants.equivalentDimension[1] = 0;
	}
	
	public static void main(String[] args) throws IOException {
		init();
		FileWriter f = new FileWriter("hyperbola_epinions.csv");
		ELTensor t = new ELTensor(Constants.ELfile, false);
		
		WC2 wc = new WC2(t);
		ArrayList<Community> coms = new ArrayList<Community>();
		int ncoms = 0;
		while (t.nelements > 0 && ncoms < 1) {
			Community com = wc.getCommunity();
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
