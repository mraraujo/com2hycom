import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import wc2.Community;
import wc2.Constants;
import wc2.ELTensor;
import wc2.WC2;
import wc2.Constants.MDLShape;


public class MainConfiguration {
	
	static int minComSize = -1;
	static int maxComs = -1;
	static int maxConsecutive = -1;
	
	static void loadProperties(Properties prop) {
		Constants.ELfile = prop.getProperty("edgelistfile");
		Constants.N = Integer.parseInt(prop.getProperty("numEdges"));
		
		Constants.D = Integer.parseInt(prop.getProperty("numDimensions"));
		Constants.NS = new int[Constants.D];
				
		for (int i = 0; i < Constants.D; i++)
			Constants.NS[i] = Integer.parseInt(prop.getProperty("dimensionSize" + i));
		
		if (prop.getProperty("shape").equals("block"))
			Constants.shape = MDLShape.RECTANGLE;
		else if (prop.getProperty("shape").equals("hyperbola"))
			Constants.shape = MDLShape.HYPERBOLA;
		else {
			System.out.println("ERROR: shape is not know");
			System.exit(1);
		}
			
		Constants.isWeighted = false;
		if (prop.getProperty("speedupDeletions") != null)
			Constants.speedupDeletions = (prop.getProperty("speedupDeletions").equals("true")?true:false);
		else
			Constants.speedupDeletions = false;


		if (prop.getProperty("maxTries") != null)
			Constants.increaseFails = Integer.parseInt(prop.getProperty("maxTries"));
		else
			Constants.increaseFails = 100;

		Constants.equivalentDimension = new int[Constants.D];
		for (int i = 0; i < Constants.D; i++) {
			Constants.equivalentDimension[i] = Integer.parseInt(prop.getProperty("sharedDimension" + i));
			if (Constants.NS[i] != Constants.NS[Constants.equivalentDimension[i]]) {
				System.out.println("ERROR: modes " + i + " and " + Constants.equivalentDimension[i] + " can't be shared as they are not the same size.");
				System.exit(1);
			}	
		}
		
		Constants.CSVfile = prop.getProperty("outputfile"); 
		
		if (prop.getProperty("minCommunitySize") != null)
			minComSize = Integer.parseInt(prop.getProperty("minCommunitySize"));
		if (prop.getProperty("numComs") != null)
			maxComs = Integer.parseInt(prop.getProperty("numComs"));
		if (prop.getProperty("maxConsecutive") != null)
			maxConsecutive = Integer.parseInt(prop.getProperty("maxConsecutive"));
	}
	
	public static void help() {
		System.out.println("Usage: java -jar comdet.jar <path to configuration file>\n");
		System.out.println("Options:");
		System.out.println("\t--help\t\tDisplay this information");
		System.out.println("\t--debug\t\tDisplay debug information");
		System.exit(1);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		
		String configfile = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--debug"))
				Constants.DEBUG = true;
			else if (args[i].equals("--help"))
				help();
			else
				configfile = args[i];
		}
		if (configfile == null) {
			System.out.println("Usage: java -jar comdet.jar <path to configuration file>");
			System.out.println("Try \"java -jar comdet.jar --help\" for more options.\n");
			System.out.print("Run examples/exampleBlockHyperbola.config (Y/n)?");
			Scanner s = new Scanner(System.in);
			String line = s.nextLine();
			if (line.length() > 0 && line.toLowerCase().charAt(0) == 'n')
				System.exit(0);
			else
				prop.load(new FileInputStream("examples/exampleBlockHyperbola.config"));
			s.close();
		}
		else
			prop.load(new FileInputStream(configfile));
		
		loadProperties(prop);

		ELTensor t = new ELTensor(Constants.ELfile, Constants.isWeighted);
		FileWriter f = new FileWriter(Constants.CSVfile);
		Community.writeCSVHeader(f);
		
		WC2 wc = new WC2(t);
		ArrayList<Community> coms = new ArrayList<Community>();
		int ncoms = 0;
		int tries = 0;
		int lastsuccess=0;
		while (t.nelements > 0 && (maxComs == -1 || ncoms < maxComs)) {
			tries++;
			Community com = wc.getCommunity();
			System.out.println("Found community " + (ncoms+1));
			
			if (minComSize != -1 && com.getMode(0).size() < minComSize) {
				System.out.println("... Too small, ignoring it ... (" + (tries-lastsuccess) + " consecutive ignores)");
				if (maxConsecutive != -1 && tries - lastsuccess + 1 > maxConsecutive)
					break;
				continue;
			}
			
			lastsuccess = tries;
			//com.printState();
			com.writeToCSV(f);
			coms.add(com);
			t.removeCommunity(com);
			ncoms++;
			System.out.println("Saved in CSV file, " + t.nelements + " non-zeros to be compressed.");
		}
		f.close();
	}
}
