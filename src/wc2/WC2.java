// TODO: WHEN REMOVING AN ELEMENT, AFTER REMOVING ALL THE EDGES IT PARTICIPATES, SOME NODES MIGHT BE LEFT WITH DEG = 0
// THEY NEED TO BE REMOVED AND CHANGES PROPAGATED

package wc2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class WC2 {
	ELTensor t;
	
	public HashMap<Integer, Double> invWeightDist;
	public double score[][];
	public double cumscore[][];
	
	ArrayList<ArrayList<Integer>> possibleElems; // the arraylist and the hashset have the same elems, 
	ArrayList<HashSet<Integer>> possibleElemsSet; // because we need fast lookup and order preserving

	ArrayList<ArrayList<Double>> possCumScore; // cumulative sum of the CP score of each element
	ArrayList<Double> totalPossScore;
	
	Random r;
	
	public WC2(ELTensor _t) {
		t = _t;
		r = new Random();
	}
	
	public void calcWeightDistribution() {
		int[] w = t.weights.clone();
		Arrays.sort(w);
	
		// build inverse lookup map
		invWeightDist = new HashMap<Integer, Double>();
		int firstpos = 0;
		for (int i = 1; i<w.length; i++)
			if (w[i] != w[i-1]) {
				invWeightDist.put(w[i-1], ((i-1 - firstpos)/2+firstpos+1.0)/w.length);
				firstpos = i-1;
			}
		invWeightDist.put(w[w.length-1], ((w.length - firstpos)/2+firstpos+1.0)/w.length);
	}
	
	void normalize(double v[]) {
		double sum = 0;
		for (int i = 0; i<v.length; i++)
			sum+=v[i];
		for (int i = 0; i<v.length; i++)
			v[i]/=sum;
	}
	
	public void calcCP() {
		//System.out.println("- Creating arrays");
			
		double oldscore[][] = new double[Constants.D][];
		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				oldscore[i] = new double[Constants.NS[i]];
		
		double newscore[][] = new double[Constants.D][];
		for (int i=0;i<Constants.D;i++)
			newscore[i] = oldscore[i];

		// Initialize vectors
		for (int i=0;i<Constants.D;i++) {
			if (Constants.equivalentDimension[i] == i) {
				for (int j=0;j<oldscore[i].length; j++)
					oldscore[i][j] = r.nextDouble();
				normalize(oldscore[i]);
			}
		}
				
		// iterate
		//System.out.println("- Iterating");
		int niterations = 100;
		for (int k = 0; k<niterations; k++) {
			//System.out.println("- Iterating " + k);
			//System.out.println("-- Declaring arrays");
			for (int i=0;i<Constants.D;i++)
				if (Constants.equivalentDimension[i] == i)
					newscore[i] = new double[Constants.NS[i]];
			
			//System.out.println("-- Iterating edges");
			for (int i = 0; i<t.edges.length; i++) {
				if (t.weights[i] == 0) continue;
				for (int j=0;j<Constants.D;j++) {
					if (Constants.equivalentDimension[j] == j) {
						double temp = t.weights[i];
						for (int jj=0;jj<Constants.D;jj++)
							if (Constants.equivalentDimension[jj] == jj)
								if (jj != j)
									temp *= oldscore[jj][t.edges[i][jj]];
						newscore[j][t.edges[i][j]] += temp;
					}
				}
			}
			for (int i = 0;i<Constants.D;i++)
				if (Constants.equivalentDimension[i] == i)
					normalize(newscore[i]);
			//System.out.println("-- Normalizing");
			
			if (k < niterations-1)
				for (int i = 0; i<Constants.D;i++)
					if (Constants.equivalentDimension[i] == i)
						oldscore[i] = newscore[i];
		}
		
		score = newscore;
		
		// create cumulative arrays
		cumscore = new double[Constants.D][];
		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i)
				cumscore[i] = new double[Constants.NS[i]];
		for (int i=0;i<Constants.D;i++) {
			if (Constants.equivalentDimension[i] == i) {
				cumscore[i][0] = score[i][0];
				for (int j=1;j<Constants.NS[i];j++)
					cumscore[i][j] = cumscore[i][j-1]+score[i][j];
			}
		}
	}
	
	private class Comparing implements Comparator<Integer> {
		private int mode;
		public Comparing(int k) {
			mode = k;
		}
		public int compare(Integer a, Integer b) {
			return Double.compare(score[mode][b], score[mode][a]);
		}
	}
	
	// where should we start expanding?
	public int getInitialEdge() {
		int invind[][] = new int[Constants.D][];
		for (int i=0;i<Constants.D;i++)
			invind[i] = new int[Constants.NS[i]];
		
		for (int i=0;i<Constants.D;i++) {
			if (Constants.equivalentDimension[i] == i) {
				Integer ind[] = new Integer[Constants.NS[i]];
				for (int j=0;j<ind.length;j++)
					ind[j] = j;
				//System.out.println("Sorting");
				Arrays.sort(ind, new Comparing(i));
				//System.out.println("Finished sorting");
				for (int j=0;j<ind.length;j++)
					invind[i][ind[j]]=j;
			}
		}
		
		/*
		// get edge with "minimum-maximal" distance to optimum
		int best = Integer.MAX_VALUE;
		int besti = -1;
		for (int i = 0; i<t.edges.length; i++) {
			int val = 0;
			if (t.weights[i] == 0)
				continue;
			else {
				for (int j=0;j<Constants.D;j++)
					if (Constants.equivalentDimension[j] == j)
						val = Math.max(val, invind[j][t.edges[i][j]]);
			}
			if (val < best) {
				best = val;
				besti = i;
				//System.out.println("initial edge update: " + besti + " " + best + " " + score[0][t.edges[besti][0]]);
			}
		} 
		return besti;
		*/
		
		// get random edge with prob = 1/(prod invind[])
		double vals[] = new double[Constants.N];
		double sum = 0;
		for (int i = 0; i < Constants.N; i++) {
			if (t.weights[i] == 0)
				continue;
			vals[i] = 1.0;
			for (int j = 0; j < Constants.D; j++)
				vals[i] = vals[i] / (invind[j][t.edges[i][j]]+1) / (invind[j][t.edges[i][j]]+1);
			sum += vals[i];
		}
		double rval = r.nextDouble()*sum;
		double acum = 0;
		for (int i = 0; i < Constants.N; i++) {
			acum += vals[i];
			if (acum > rval)
				return i;
		}
		return Constants.N-1;
	}
	
	// Use weighted sampling (based on 'cum') to find a good new element for 'mode'
	// This new element must be connected somehow to the existing community (no empty row/cols)
	// Return the set of new edges represented if he gets added
	private int _newElement;
	public HashSet<Integer> getNewElement(double cum[], Community com, int mode) {
		HashSet<Integer> edges = new HashSet<Integer>();

		int pos = Collections.binarySearch(possCumScore.get(mode), r.nextDouble()*totalPossScore.get(mode));
		if (pos<0)
			pos = -pos-1;
		_newElement = possibleElems.get(mode).get(pos);
		
		//System.out.println("Mode " + mode + " suggested element " + _newElement + " as a possibility. Total poss score: " + totalPossScore.get(mode));
		
		com.getMode(Constants.equivalentDimension[mode]).add(_newElement);
		// still need to get the edges to be added!
		for (int m = 0; m<Constants.D; m++) { // we need to check our data structure on all equivalent dimensions
			if (Constants.equivalentDimension[m] == mode) {
				int start = t.findStart(_newElement, m);
				while (start < Constants.N && t.edges[t.sorted[m][start]][m] == _newElement) {
					// for this non-zero, are all the other elements already in the community?
					int i = t.sorted[m][start];
					boolean notfound = false;
					for (int j = 0;j<Constants.D; j++)
						if (j != m && !com.getMode(Constants.equivalentDimension[j]).contains(t.edges[i][j])) {
							start++;
							notfound = true;
							break;
						}
					if (notfound)
						continue;
		
					if (t.weights[i] > 0)
						edges.add(i);
					start++;
				}
			}
		}
		com.getMode(Constants.equivalentDimension[mode]).remove(_newElement);
				
		return edges;
	}
	
	// Try to add a new element to the community
	public int tryIncrease(Community com) {
		//com.printState();
		
		// newedges[i] has a set of newedges in case we add the suggested element
		ArrayList<HashSet<Integer>> newedges = new ArrayList<HashSet<Integer>>();
		// isnew[i] has the element to be added to mode i (-1 if none found)
		int isnew[] = new int[Constants.D];
		for (int i=0;i<Constants.D;i++)
			if (Constants.equivalentDimension[i] == i && possibleElems.get(i).size() > 0) {
				newedges.add(getNewElement(cumscore[i], com, i));
				isnew[i] = _newElement;
			}
			else {
				newedges.add(new HashSet<Integer>());
				isnew[i] = -1;
			}
		
		double mdl[] = new double[Constants.D];
		for (int i=0;i<Constants.D;i++)
			if (isnew[i] >= 0 && Constants.equivalentDimension[i] == i)
				mdl[i] = com.getMDLDiffAdd(i, isnew[i], newedges.get(i));
			else mdl[i] = Double.MAX_VALUE;
		
		int minid = 0;
		double minval = mdl[0];
		for (int i=1;i<Constants.D;i++)
			if (mdl[i] < minval) {
				minval = mdl[i];
				minid = i;
			}

		if (minval < 0) {
			com.addElement(minid, isnew[minid]);
			com.addEdges(newedges.get(minid));
			addPossibilities(com, minid, isnew[minid]);
			deletePossibility(minid, isnew[minid]);
			return isnew[minid];
		}

		return -1;
	}
	
	public boolean tryDecrease(Community com) {
		//System.out.println("TRYING DECREASE");
		//com.printState();
		double bestScore = Double.MAX_VALUE;
		int remmode = -1, remel = -1;
		boolean found = false;
		for (int i = 0; i < Constants.D; i++) {
			if (Constants.equivalentDimension[i] == i && com.getMode(i).size()>2) {
				Object elems[] = com.getMode(i).toArray();
				for (Object k : elems) {
					Integer a = (Integer)k;
					double sc = com.getMDLDiffDel(i, a);
					if (sc < 0 && sc < bestScore) {
						if (Constants.speedupDeletions) {
							//System.out.println("Removing " + a + " from mode " + i);
							com.deleteElement(i, a);
							com.removeEdgesFromElement(i, a);
							found = true;
							System.out.println("Decreased! Num elems: " + com.getMode(0).size() + " " + com.getMode(1).size() + ", " + com.getEdgesRepresented().size() + " represented, " + ((HyperbolaCommunity)com).comMistakes + " mistakes");
						}
						else {
							bestScore = sc;
							remmode = i;
							remel = a;
						}
					}
				}
			}
		}
		if (Constants.speedupDeletions)
			return found;
		else {
			if (remmode != -1) {
				//System.out.println("Removing " + remel + " from mode " + remmode);
				com.deleteElement(remmode, remel);
				com.removeEdgesFromElement(remmode, remel);
				//System.out.println("Decreasing!");
				return true;
			}
			else 
				return false;
		}
	}
	
	public HashSet<Integer> fullSearchInitialEdges(Community com) {
		HashSet<Integer> initialSet = new HashSet<Integer>();
		for (int i = 0;i<Constants.N;i++) {
			boolean found = true;
			for (int j = 0; j<Constants.D; j++)
				if (!com.getMode(Constants.equivalentDimension[j]).contains(t.edges[i][j])) {
					found = false;
					break;
				}
			if (found)
				initialSet.add(i);
		}
		return initialSet;
	}
	
	void deletePossibility(int mode, int elem) {
		int eq = Constants.equivalentDimension[mode];
		int pos;
		pos = possibleElems.get(eq).indexOf(elem);
		if (pos == -1) 
			return;
		possibleElems.get(eq).remove(pos);
		possibleElemsSet.get(eq).remove(elem);
		
		double val = (pos == 0? possCumScore.get(eq).get(pos): possCumScore.get(eq).get(pos)-possCumScore.get(eq).get(pos-1));
		totalPossScore.set(eq, totalPossScore.get(eq)-val);
		possCumScore.get(eq).remove(pos);
		for (int i = pos; i < possCumScore.get(eq).size(); i++)
			possCumScore.get(eq).set(i, possCumScore.get(eq).get(i)-val);
	}
	
	void addPossibilities(Community com, int mode, int elem) {
		for (int m = 0; m<Constants.D; m++) {
			if (Constants.equivalentDimension[m] != mode) continue;
			int start = t.findStart(elem, m);
			while (start < Constants.N && t.edges[t.sorted[m][start]][m] == elem) {
				if (t.weights[t.sorted[m][start]] == 0) {
					start++;
					continue;
				}
				for (int i = 0; i<Constants.D; i++) {
					if (i == m) continue;
					int eq = Constants.equivalentDimension[i];
					int e = t.edges[t.sorted[m][start]][i];
					//can we add element e to the possibilities?
					if (!possibleElemsSet.get(eq).contains(e) && // not in the poss set
							!com.getMode(eq).contains(e)) { // and not already in the community
						// do we have all the others? (i.e. in the other modes)
						boolean found = false;
						for (int j = 0; j<Constants.D; j++) {
							if (Constants.equivalentDimension[j] != eq && !com.getMode(Constants.equivalentDimension[j]).contains(t.edges[t.sorted[m][start]][j])) {
								found = true;
								break;
							}
						}
						if (found) continue;
						possibleElems.get(eq).add(e);
						possibleElemsSet.get(eq).add(e);
						if (possCumScore.get(eq).size() == 0)
							possCumScore.get(eq).add(score[eq][e]);
						else
							possCumScore.get(eq).add(possCumScore.get(eq).get(possCumScore.get(eq).size()-1) + 
									score[eq][e]);
						totalPossScore.set(eq, totalPossScore.get(eq) + score[eq][e]);
					}
				}
				start++;
			}
		}
	}
	
	void initPossDS() {
		possibleElems = new ArrayList<ArrayList<Integer>>(); 
		for (int i = 0; i<Constants.D; i++)
			possibleElems.add(new ArrayList<Integer>());
		
		possibleElemsSet = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i<Constants.D; i++)
			possibleElemsSet.add(new HashSet<Integer>());

		possCumScore = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i<Constants.D; i++)
			possCumScore.add(new ArrayList<Double>());
		
		totalPossScore = new ArrayList<Double>();
		for (int i = 0; i<Constants.D; i++)
			totalPossScore.add(0.0);
	}
	
	boolean noPossibilities() {
		for (int i = 0; i<Constants.D; i++)
			if (Constants.equivalentDimension[i] == i && possibleElems.get(i).size() > 0)
				return false;
		return true;
	}
	
	public Community getCommunity() {
		if (Constants.DEBUG) System.out.println("Calculating CP");
		calcCP();
		Community com = Community.createCommunity(t, this);
		
		if (Constants.isWeighted) {
			if (Constants.DEBUG) System.out.println("Calculating Weight Distribution");
			calcWeightDistribution();
		}
		
		if (Constants.DEBUG) System.out.println("Getting initial edge");
		// get initial edge
		int initial = getInitialEdge();
		if (initial == -1) {// no edge to select (full deflation!)
			if (Constants.DEBUG) System.out.println("No edge to return!");
			return com;
		}
		//System.out.println("Edge: " + initial + " " + t.edges[initial][0] + " " + t.edges[initial][1]);
		// initialize possibilities data structures
		initPossDS();
		
		for (int i=0;i<Constants.D;i++) {
			com.addElement(Constants.equivalentDimension[i], t.edges[initial][i]);
			//System.out.println("Adding possibilities after mode " + i);
			addPossibilities(com, Constants.equivalentDimension[i], t.edges[initial][i]);
			deletePossibility(Constants.equivalentDimension[i], t.edges[initial][i]);
			//System.out.println("Initial edge, mode: " + i + " element: " + t.edges[initial][i]);
		}
		System.out.println("Possibilities: " + possibleElems.get(0).size() + " " + possibleElems.get(1).size() + " " + possibleElems.get(2).size() + " " + possibleElems.get(3).size());
		
		HashSet<Integer> initialSet	= fullSearchInitialEdges(com);
		com.addEdges(initialSet);
		//System.out.println("Initial score: " + com.getCurrentMDL());
		
		if (Constants.DEBUG) System.out.println("Building community");
		while (true) {
			int fails = 0;
			while (fails < Constants.increaseFails) {
				if (noPossibilities())
					break;
				if (Constants.DEBUG) {
					System.out.print("Trying increase " + fails + ", current num elems:");
					for (int i = 0; i < Constants.D; i++)
						System.out.print(" " + com.getMode(i).size());
					System.out.println(", " + com.getEdgesRepresented().size() + " edges represented");
					//com.printState();
				}
								
				if (tryIncrease(com) < 0)
					fails++;
				else 
					fails = 0;
			}
			boolean found = false;
			while (tryDecrease(com))
				found = true;
			if (!found)
				break;
		}	
		return com;
	}
}
