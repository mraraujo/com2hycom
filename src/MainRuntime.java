import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import wc2.*;
import wc2.Constants.MDLShape;


public class MainRuntime {

	static FileWriter f;
	
	private static void init() {
		Constants.N = 212;
		Constants.D = 2;
		Constants.NS = new int[Constants.D];
		Constants.NS[0] = 47;
		Constants.NS[1] = 47;
		Constants.ELfile = "graph100";
		Constants.shape = MDLShape.HYPERBOLA;
		Constants.isWeighted = false;
		Constants.speedupDeletions = true;
		Constants.equivalentDimension = new int[Constants.D];
		Constants.equivalentDimension[0] = 0;
		Constants.equivalentDimension[1] = 0;
		Constants.increaseFails = 100;
	}
	
	public static void main(String[] args) throws IOException {
		init();
		long startTime = System.currentTimeMillis();
		FileWriter f = new FileWriter("hyperbola_graph100.csv");
		ELTensor t = new ELTensor(Constants.ELfile, false);
		
		WC2 wc = new WC2(t);
		ArrayList<Community> coms = new ArrayList<Community>();
		int ncoms = 0;
		int tries = 0;
		int lastsuccess=0;
		while (t.nelements > 0) {
			System.out.println(" Try number " + (++tries) + " found " + ncoms + " " + (tries-lastsuccess));
			Community com = wc.getCommunity();
			/*if (com.getMode(0).size() < 10) {
				if (tries - lastsuccess > 50)
					break;
				continue;
			}*/
			lastsuccess = tries;
			com.printState();
			com.writeToCSV(f);
			coms.add(com);
			t.removeCommunity(com);
			ncoms++;
			System.out.println("Found " + ncoms + ", " + t.nelements + " left");
		}
		f.close();
		System.out.println("Time elapsed: " + (System.currentTimeMillis()-startTime)/1000.0);
	}

}
