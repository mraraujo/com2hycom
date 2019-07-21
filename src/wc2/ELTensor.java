package wc2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class ELTensor {
	public int [][]edges;
	public int []weights;
	public int nelements;
	
	public Integer [][]sorted;
	
	private void readFile(String filename, boolean withWeight) throws IOException {
		if (Constants.DEBUG) System.out.println("- Creating arrays");
		edges = new int[Constants.N][Constants.D];
		weights = new int[Constants.N];
		if (Constants.DEBUG) System.out.println("- Starting read");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		for (int i = 0; i<Constants.N; i++) {
			String temp[] = br.readLine().split(" ");
			for (int j = 0; j<Constants.D; j++) 
				edges[i][j] = Integer.parseInt(temp[j]);
			if (withWeight)
				weights[i] = Integer.parseInt(temp[temp.length-1]);
			else
				weights[i] = 1;
			if (i % 1000000 == 0 && i>0)
				if (Constants.DEBUG) System.out.println("- Loaded " + i/1000000 + " million records");
		}
		br.close();
		
		nelements = Constants.N;
		
		buildIndices();
	}
	
	public ELTensor(String filename, boolean withWeight) throws IOException {
		readFile(filename, withWeight);
	}

	public ELTensor(String filename) throws IOException {
		readFile(filename, true);
	}
	
	private class Comparing implements Comparator<Integer> {
		private int mode;
		public Comparing(int k) {
			mode = k;
		}
		public int compare(Integer a, Integer b) {
			return edges[a][mode] - edges[b][mode];
		}
	}
	
	void buildIndices() {
		if (Constants.DEBUG) System.out.println("Creating indices arrays");
		sorted = new Integer[Constants.D][Constants.N];
		for (int j = 0; j<Constants.D; j++) {
			for (int i = 0; i<Constants.N; i++) 
				sorted[j][i] = i;
			if (Constants.DEBUG) System.out.println("Created indice for mode " + j);
		}
		
		for (int i = 0; i < Constants.D; i++) {
			if (Constants.DEBUG) System.out.println("Sorting mode " + i);
			Arrays.sort(sorted[i], new Comparing(i));
		}
	}
	
	public int findStart(int elem, int mode) {
		int low = 0, high = Constants.N;
		int idx = Constants.N;
		//System.out.println("Searching for " + elem + " in " + mode);
		while (low <= high) {
			int mid = low + (high-low)/2;
			int val = edges[sorted[mode][mid]][mode];
			//System.out.println(mid + " " + val);
			if (elem < val)
				high = mid-1;
			else if (elem > val)
				low = mid+1;
			else {
				if (mid < idx)
					idx = mid;
				high = mid-1;
			}
		}
		if (idx == Constants.N)
			idx = -1;
		return idx;
	}

	public void removeCommunity(Community com) {
		for (Integer a : com.getEdgesRepresented()) {
			if (com.isEdgeRepresented(a)) {
				if (Constants.isWeighted)
					weights[a] -= com.getMinWeight();
				else
					weights[a] = 0;
				if (weights[a] == 0)
					nelements--;
			}
		}
	}
	
	public void removeCommunities(String filename) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		int num = 0;
		while ((line = br.readLine()) != null) {
			if (Constants.DEBUG) System.out.println("Removing community " + num);
			num++;
			String temp[] = line.split(",");
			
			int n[] = new int[Constants.D];
			for (int i=0;i<Constants.D;i++)
				n[i] = Integer.parseInt(temp[i]);
	        
			// jump temp[Constants.D] -> ignore number of non-zeros, we don't need it
			
			int minvalue = Integer.parseInt(temp[Constants.D+1]);
	        
			int v[][] = new int[Constants.D][];
	        for (int i=0;i<Constants.D;i++)
	        	v[i] = new int[n[i]];

	        // load elements in each mode to v[][]
	        for (int i = 0;i<Constants.D; i++) {
	        	String temp2[] = temp[Constants.D + i + 2].split(" ");
	            for (int j = 0; j < n[i]; j++)
	            	v[i][j] = Integer.parseInt(temp2[j]);
	        }

	        // remove them!
	        for (int i = 0; i<Constants.N; i++) {
	        	boolean toberemoved = true;
	        	for (int j=0;j<Constants.D; j++) {
	        		boolean found = false;
	        		for (int k=0;k<n[j];k++) {
	        			if (v[j][k] == edges[i][j]) {
	        				found = true;
	        				break;
	        			}
	        		}
	        		if (!found) {
	        			toberemoved = false;
	        			break;
	        		}
	        	}
	        	if (toberemoved)
	        		weights[i] -= minvalue;
	        }
		}
		br.close();
	}
}
