package wc2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class HyperbolaCommunity extends Community {
	private double alpha[];
	
	private ArrayList<ArrayList<Integer>> sorted; // for each mode i, degree of node in position j
	private ArrayList<HashMap<Integer, Integer>> positions; // for each mode i, what is the id of node in position j?
	
	public double tau; // tau last used (useful for debugging)
	public int comMistakes;
	
	public HyperbolaCommunity(ELTensor _t, WC2 _wc) {
		super(_t, _wc);
		alpha = new double[Constants.D];
		currentMDLScore = 0;
	}

	private void sortElements() {
		//System.out.println("Started sortElements");
		// ArrayList has size = num of dimensions. 
		// The HashMaps maps the element id to the number of edges that element is part of
		ArrayList<HashMap<Integer, Integer>> numEdges = new ArrayList<HashMap<Integer, Integer>>();
		for (int i = 0;i<Constants.D;i++)
			numEdges.add(new HashMap<Integer, Integer>());
		
		// how many edges there are for each ID on each mode
		for (Integer e : edgesRepresented)
			for (int j=0;j<Constants.D;j++)
				if (Constants.equivalentDimension[j] == j) {
					if (numEdges.get(j).containsKey(t.edges[e][j]))
						numEdges.get(j).put(t.edges[e][j], numEdges.get(j).get(t.edges[e][j])+1);
					else
						numEdges.get(j).put(t.edges[e][j],1);
				}
		/*
		for (Entry<Integer, Integer> e : numEdges.get(0).entrySet())
			System.out.println("numedges: " + e.getKey() + " " + e.getValue());
		*/
		
		// sort them
		sorted = new ArrayList<ArrayList<Integer>>();
		for (int i = 0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				sorted.add(new ArrayList<Integer>());
		for (int i = 0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				for (Entry<Integer, Integer> elem : numEdges.get(i).entrySet())
					sorted.get(i).add(elem.getValue());
		for (int i = 0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				Collections.sort(sorted.get(i), Collections.reverseOrder());
		
		/*
		for (int i = 0; i<Constants.D; i++) {
			System.out.println("Mode: " + i);
			for (int j = 0; j<sorted.get(i).size(); j++)
				System.out.print(sorted.get(i).get(j)+ "   ");
			System.out.println();
		}
		*/
		
		// calculate alpha
		for (int i = 0; i<Constants.D; i++) {
			if (Constants.equivalentDimension[i] == i) {
				if (sorted.get(i).size() < 2)
					alpha[i] = 0;
				else {
					double sumx = 0, sumx2 = 0, sumxy = 0, sumy = 0;
					//System.out.println("Mode: " + i);
					for (int j = 0; j<sorted.get(i).size();j++) {
						double x = log2(j+1);
						double y = log2(sorted.get(i).get(j));
						//System.out.println(j+1 + " " + sorted.get(i).get(j) + " " + x + " " + y);
						sumx += x;
						sumy += y;
						sumxy += x*y;
						sumx2 += x*x;
					}
					int n = sorted.get(i).size();
					alpha[i] = (sumxy-sumx*sumy/n)/(sumx2 - sumx*sumx/n);
				}
			}
		}
		
		// create a ID>Position_in_community map
		positions = new ArrayList<HashMap<Integer,Integer>>();
		for (int i = 0; i<Constants.D;i++) {
			HashMap<Integer, Integer> h = new HashMap<Integer, Integer>();
			if (Constants.equivalentDimension[i] == i) {
				ArrayList<Integer> s2 = new ArrayList<Integer>(sorted.get(i));
				for (Entry<Integer, Integer> elem : numEdges.get(i).entrySet()) {
					for (int k = 0; k<s2.size(); k++)
						if (elem.getValue() == s2.get(k)) {
							h.put(elem.getKey(), k);
							//System.out.println("Mode " + i + " set key " + elem.getKey() + " to value " + k);
							s2.set(k, -1);
							break;
						}
				}
			}
			positions.add(h);
		}
	}
	
	int vals[];
	private int countOnes(double tau, int mode, int modeToIgnore) {
		if (mode == modeToIgnore)
			return countOnes(tau, mode+1, modeToIgnore);
		if (mode == Constants.D) { // generated everything but the biggest mode
			// calculate product of everything so far
			double sofar = 1;
			for (int i = 0; i<Constants.D; i++) {
				if (i != modeToIgnore)
					sofar *= Math.pow(vals[i]+1, alpha[Constants.equivalentDimension[i]]);
			}
			
			if (Math.abs(alpha[Constants.equivalentDimension[modeToIgnore]]) <= 1e-6) { 
				// this mode has a uniform dimension, which means all the values in this column are "sofar"
				if (sofar >= tau)
					return elems.get(Constants.equivalentDimension[modeToIgnore]).size();
			}
			else {
				double k = Math.pow(tau/sofar, 1/alpha[Constants.equivalentDimension[modeToIgnore]]);
				//System.out.println("countOnes --- mode: " + mode + "   vals[1]: " + vals[1] + "    sofar: " + sofar + "    k: " + k);
				return Math.min((int)Math.floor(k), elems.get(Constants.equivalentDimension[modeToIgnore]).size());
			}
			
		}
		else {
			int count = 0;
			for (int i = 0; i<elems.get(Constants.equivalentDimension[mode]).size(); i++) {
				vals[mode] = i;
				count += countOnes(tau, mode+1, modeToIgnore);
			}
			return count;
		}
		return 0;
	}
	
	// iterative countOnes, optimized for when d = 2
	int countOnes2optimized(double tau, int mode, int modeToIgnore) {
		int count = 0;
		for (int i = 0; i<elems.get(Constants.equivalentDimension[mode]).size(); i++) {
			double sofar = Math.pow(i+1, alpha[Constants.equivalentDimension[mode]]);
			if (Math.abs(alpha[Constants.equivalentDimension[modeToIgnore]]) <= 1e-6) { 
				// this mode has a uniform dimension, which means all the values in this column are "sofar"
				if (sofar >= tau)
					count += elems.get(Constants.equivalentDimension[modeToIgnore]).size();
			}
			else {
				double k = Math.pow(tau/sofar, 1/alpha[Constants.equivalentDimension[modeToIgnore]]);
				//System.out.println("countOnes --- mode: " + mode + "   vals[1]: " + vals[1] + "    sofar: " + sofar + "    k: " + k);
				count += Math.min((int)Math.floor(k), elems.get(Constants.equivalentDimension[modeToIgnore]).size());
			}
		}
		return count;
	}
	
	private double areaEstimate2d(double tau) {
		double c = -alpha[Constants.equivalentDimension[0]]; // power law exponent, i.e.  i^(-c)
		int s=elems.get(Constants.equivalentDimension[0]).size(); // columns  i=1....s

		double b = -alpha[Constants.equivalentDimension[1]]; // power law exponent, i.e. j^(-b)
		int t=elems.get(Constants.equivalentDimension[1]).size(); // rows    j=1....t

		// shortcut if uniform or close to uniform
		if (Math.abs(c) < 1e-4 && Math.abs(b) < 1e-4)
			return s*t;
		
		// view 1: b/t horizontal, c/s vertical
		double jstar=Math.exp((-c*Math.log(s)-Math.log(tau))/b);
		int jstart = (int) Math.max(1,Math.ceil(jstar));
		int jend = Math.min(t,(int)Math.ceil(Math.exp(Math.log(tau)/-b)));

		if(jstart>jend) {
			jstart=jend;
		}

		double area2=(c*Math.pow(tau,-1.0/c))*(Math.pow(jend,1.0-b/c)-Math.pow(jstart,1.0-b/c))/(c-b);	
		if(b==c) {
			area2 = Math.pow(tau,-1.0/c)*(Math.log(jend)-Math.log(jstart));
		}

		area2 += (jstart-1)*s;
		int lowerBound2 = Math.max((int) Math.ceil(area2-(jend-jstart)),1); // at least 1
		double f1=Math.exp(Math.log(tau)/-c);
		int upperBound2 = (int)Math.floor(area2+Math.min(s,Math.floor(f1)));
		area2 = Math.max(area2, lowerBound2); // potential improvement....
		area2 = Math.min(area2,upperBound2); // ...due to rounding errors

		// view 2: other way around
		// if b==c (and t==s!!!) the second view is always identical
		jstar=Math.exp((-b*Math.log(t)-Math.log(tau))/c);
		jstart = (int) Math.max(1,Math.ceil(jstar));
		jend = Math.min(s,(int)Math.ceil(Math.exp(Math.log(tau)/-c)));

		if(jstart>jend) {
			jstart=jend;
		}


		double area1=(b*Math.pow(tau,-1.0/b))*(Math.pow(jend,1.0-c/b)-Math.pow(jstart,1.0-c/b))/(b-c);
		if(b==c) { 
			area1 = Math.pow(tau,-1.0/c)*(Math.log(jend)-Math.log(jstart));
		}


		area1 += (jstart-1)*t;
		int lowerBound1 = Math.max((int) Math.ceil(area1-(jend-jstart)),1); // at least 1
		f1=Math.exp(Math.log(tau)/-b);
		int upperBound1 = (int)Math.floor(area1+Math.min(t,Math.floor(f1)));
		area1 = Math.max(area1, lowerBound1); // potential improvement....
		area1 = Math.min(area1,upperBound1); // ...due to rounding errors

		int lowerBound = Math.max(lowerBound1,lowerBound2);
		int upperBound = Math.min(upperBound1,upperBound2);
		//System.out.println(lowerBound + " " + upperBound);
		return lowerBound+(upperBound-lowerBound)/2.0;
	}
	
	private double _overValues;
	private double searchTau() {
		// what is the biggest mode? (because we can binary search it)
		int maxVal = elems.get(Constants.equivalentDimension[0]).size(), modeToIgnore = 0;
		for (int i = 1; i<Constants.D; i++)
			if (elems.get(Constants.equivalentDimension[i]).size() > maxVal) {
				maxVal = elems.get(Constants.equivalentDimension[i]).size();
				modeToIgnore = i;
			}
		vals = new int[Constants.D];
		
		// binary search tau
		double low = 0, high = 1;
		double mid = 0;
		double bestAbove = 0;
		
		while (high - low >= 1e-6) {
			mid = low + (high-low)/2;
			double v;
			// how many elements are bigger than this tau?
			if (Constants.D == 2) {
				// Only run the fast version when the number of nodes gets big
				if (elems.get(modeToIgnore).size() >= 15)
					v = areaEstimate2d(mid);
				else
					v = countOnes2optimized(mid, 1-modeToIgnore, modeToIgnore);
			}
			else
				v = countOnes(mid, 0, modeToIgnore); 
			
			
			//System.out.println("Trying tau = " + mid + " ones = " + v);
			if (Math.abs(v - edgesRepresented.size()) <= 0.1) {
				_overValues = v;
				return mid;
			}
			if (v > edgesRepresented.size()) {
				low = mid;
				bestAbove = mid;
				_overValues = v;
			}
			else
				high = mid;
		}
		return bestAbove;
	}
	
	public boolean isEdgeRepresented(int e) {
		double val = 1;
		for (int j = 0; j<Constants.D; j++) {
			val *= Math.pow(positions.get(Constants.equivalentDimension[j]).get(t.edges[e][j])+1, alpha[Constants.equivalentDimension[j]]);
		}
		return val > tau;
	}
	
	public int numMistakes() {
		// find the right threshold
		tau = searchTau();
		//System.out.println("Tau: " + tau + "Overvalues: " + _overValues + " ER: " + edgesRepresented.size());
		int mistakes = (int) (Math.round(_overValues)-edgesRepresented.size()); // there might be no good tau, but sum m[i][j] >= tau >= edgesRepresented.size()
		if (mistakes < 0) {
			System.out.println(mistakes + " " + tau + " " + alpha[0] + " " + alpha[1] + " " + areaEstimate2d(tau) + " " + countOnes2optimized(tau, 1, 0));
			System.exit(-1);
		}
		//System.out.println("PreMistakes: " + mistakes);
		//printState();
		
		for (Integer e : edgesRepresented) {
			double val = 1;
			//System.out.println(e + " " + t.edges[e][0] + " " + t.edges[e][1]);
			for (int j = 0; j<Constants.D; j++)
				val *= Math.pow(positions.get(Constants.equivalentDimension[j]).get(t.edges[e][j])+1, alpha[Constants.equivalentDimension[j]]);
			if (val < tau) // there shouldn't be an edge in this position
				mistakes++; // for each edge in a 0 position, we will lack an edge in a 1 position
		}	
		//System.out.println("ProMistakes: " + mistakes);
		comMistakes = mistakes;
		return mistakes;
	}
	
	public double getMDLScore() {
		sortElements(); // sort by degree and get alphas
		
		long fulltensor = 1; // number of cells in the tensor
		long prod = 1; // number of cells in the community
		for (int i=0; i<Constants.D; i++) {
			fulltensor *= Constants.NS[i];
			prod *= elems.get(Constants.equivalentDimension[i]).size();
		}
		
		int nummisses = numMistakes();
		
		double cost = 0;
		// encode community sizes
		for (int i = 0; i<Constants.D; i++)
			cost += log2(Constants.NS[i]);
		// encode elements
		for (int i = 0; i<Constants.D; i++)
			cost += elems.get(Constants.equivalentDimension[i]).size()*log2(Constants.NS[i]); 
		// encode alphas with 32 bits
		cost += Constants.D*32;
		// encode number of edges in community
		cost += log2(prod);
				
		// number of mistakes
		cost += log2(prod+1);
		// encoding mistakes
		cost += nummisses*log2(prod+1);
		// rest of the tensor
		cost += (t.nelements-(edgesRepresented.size() - nummisses))*log2(fulltensor); // rest of the tensor
		return cost;
	}

	public double[] getAlpha() {
		return alpha;
	}
	
	public ArrayList<HashMap<Integer, Integer>> getPositions() {
		return positions;
	}
	
	public void writeToCSV(FileWriter f) throws IOException {
		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				f.write(elems.get(i).size() + ", ");
		f.write(edgesRepresented.size()+", ");
		f.write(comMistakes+", ");
		
		for (int i = 0; i<Constants.D; i++)
			if (Constants.equivalentDimension[i] == i)
				f.write(alpha[i] + ", ");
		
		for (HashMap<Integer, Integer> hsi : positions) {
			for (Entry<Integer, Integer> a : hsi.entrySet())
				f.write(a.getKey() + " ");
			f.write(",");
		}	
		f.write('\n');
		f.flush();
	}
	
	public static void writeCSVHeader(FileWriter f) throws IOException {
		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				f.write("#elements in " + i + ", ");
		f.write("#edges,");
		f.write("# mistakes,");
		
		for (int i = 0; i<Constants.D; i++)
			if (Constants.equivalentDimension[i] == i)
				f.write("alpha " + i + ", ");
		

		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				f.write("List of elements in " + i + ",");
		f.write('\n');
		f.flush();
	}
}
