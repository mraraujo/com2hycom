import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import wc2.*;


public class MainDensityExperiment {

	static FileWriter f;
	
	private static void init() {
		//Constants.N = 113030;
		Constants.NA = 1000;
		Constants.NB = 1000;
		Constants.NC = 1000;
		Constants.MAXW = 1;
		//Constants.ELfile = infile;
	}
	
	private static void createInput() throws IOException {
		double dens = 0.05;
		Constants.ELfile = "dexp-05";
		f = new FileWriter("dexpe-05.csv");
		
		Constants.N = (int) (50*10*10*10*dens) + 1000;
		
		FileWriter fout = new FileWriter(Constants.ELfile);
		
		HashSet<String> present = new HashSet<String>();
		ArrayList<Integer> pa = new ArrayList<Integer>();
		ArrayList<Integer> pb = new ArrayList<Integer>();
		ArrayList<Integer> pc = new ArrayList<Integer>();
		
		for (int i = 0;i<50;i++) { // create coms
			// choose a unique block 
			int ai;
			boolean repeated;
			do {
				ai = (int) (Math.random()*(Constants.NA-10));
				repeated = false;
				for (int j = 0; j<i; j++)
					if (Math.abs(ai-pa.get(j)) < 10)
						repeated = true;
			} while (repeated);
			pa.add(ai);
			
			int bi;
			do {
				bi = (int) (Math.random()*(Constants.NB-10));
				repeated = false;
				for (int j = 0; j<i; j++)
					if (Math.abs(bi-pb.get(j)) < 10)
						repeated = true;
			} while (repeated);
			pb.add(bi);

			int ci;
			do {
				ci = (int) (Math.random()*(Constants.NC-10));
				repeated = false;
				for (int j = 0; j<i; j++)
					if (Math.abs(ci-pc.get(j)) < 10)
						repeated = true;
			} while (repeated);
			pc.add(ci);
			
			// create elements
			for (int j = 0; j<10*10*10*dens; j++) {
				int ap = (int) (ai+Math.random()*10);
				int bp = (int) (bi+Math.random()*10);
				int cp = (int) (ci+Math.random()*10);
				String val = String.valueOf(ap) + " " + String.valueOf(bp) + " " + String.valueOf(cp);
				if (present.contains(val)) {
					j--;
					continue;
				}
				else
					present.add(val);
				
				fout.write(val + " 1\n");
			}
		}
		
		// create dust
		for (int i = 0; i < 1000; i++) {
			int ap = (int) (Math.random()*Constants.NA);
			int bp = (int) (Math.random()*Constants.NB);
			int cp = (int) (Math.random()*Constants.NC);
			String val = String.valueOf(ap) + " " + String.valueOf(bp) + " " + String.valueOf(cp);
			if (present.contains(val)) {
				i--;
				continue;
			}
			else
				present.add(val);
			
			fout.write(val + " 1\n");
		}
			
		
		fout.close();
	}
	
	public static void main(String[] args) throws IOException {
		init();
		createInput();
		
		ELTensor t = new ELTensor(Constants.ELfile);
		Arrays.fill(t.weights, 1);
		WC2 wc = new WC2(t);
		ArrayList<RectangleCommunity> coms = new ArrayList<RectangleCommunity>();
		int ncoms = 0;
		while (t.nelements > 0 && ncoms < 500) {
			RectangleCommunity com = wc.getCommunity();
			//com.printState();
			com.writeToCSV(f);
			ncoms++;
			System.out.println("Found " + ncoms + ", " + t.nelements + " left");
			coms.add(com);
			t.removeCommunity(com);
		}
		f.close();
	}

}
