package wc2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import wc2.Constants.MDLShape;

public abstract class Community {
	protected ArrayList<HashSet<Integer>> elems;
	protected double median;
	protected ELTensor t;
	protected WC2 wc;

	protected HashSet<Integer> edgesRepresented;
	protected HashSet<Integer> edgesCovered;
	
	protected int minweight;
	
	protected double currentMDLScore;
	
	protected Community(ELTensor _t, WC2 _wc) {
		t = _t;
		wc = _wc;
		elems = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i<Constants.D; i++)
			elems.add(new HashSet<Integer>());
		
		edgesCovered = new HashSet<Integer>();
		if (Constants.isWeighted)
			edgesRepresented = new HashSet<Integer>();
		else
			edgesRepresented = edgesCovered;
				
		median = 0;
	}
	

	public HashSet<Integer> getMode(int k) {
		return elems.get(k);
	}
	
	public double getCurrentMDL() {
		return currentMDLScore;
	}

	public void updateScore() {
		currentMDLScore = getMDLScore();
	}
	
	public void addElement(int mode, int val) {
		elems.get(mode).add(val);
	}
	
	public void deleteElement(int mode, int val) {
		elems.get(mode).remove(val);
	}
	
	public void addEdges(HashSet<Integer> edges) {
		edgesCovered.addAll(edges);
		if (Constants.isWeighted)
			updateMedian();
		else
			updateScore();
	}
	
	public void removeEdges(HashSet<Integer> edges) {
		edgesCovered.removeAll(edges);
		if (Constants.isWeighted)
			updateMedian();
		else
			updateScore();
	}
	
	public double getMDLDiffAdd(int mode, int element, HashSet<Integer> edges) {
		addElement(mode,  element);
		addEdges(edges);
		
		double cost = getMDLScore();

		deleteElement(mode,  element);
		removeEdges(edges);

		currentMDLScore = getMDLScore();

		return cost-currentMDLScore;
	}

	public double getMDLDiffDel(int mode, int element) {
		//printState();
		deleteElement(mode, element);
		HashSet<Integer> edgesRemoved = removeEdgesFromElement(mode, element);
		if (edgesRepresented.size() < 1) {// can't remove everything
			addElement(mode, element);
			addEdges(edgesRemoved);
			currentMDLScore = getMDLScore();
			return 1;
		}
		if (Constants.isWeighted)
			updateMedian();
		
		double cost = getMDLScore();
		
		//edgesCovered = temp;
		addElement(mode, element);
		addEdges(edgesRemoved);
		
		currentMDLScore = getMDLScore();
		
		if (Constants.isWeighted)
			updateMedian();
		return cost - currentMDLScore;
	}
	

	public HashSet<Integer> removeEdgesFromElement(int mode, int elem) {
		HashSet<Integer> edgesRemoved = new HashSet<Integer>();
		Iterator<Integer> it = edgesCovered.iterator();
		while (it.hasNext()) {
			int k = it.next();
			for (int i = 0; i<Constants.D; i++) {
				if (Constants.equivalentDimension[i] == mode && t.edges[k][i] == elem) {
					edgesRemoved.add(k);
					it.remove();
					break;
				}
			}
		}
		if (Constants.isWeighted)
			updateMedian();
		else
			updateScore();
		return edgesRemoved;
	}
	
	public double getMedian() {
		return median;
	}
	
	private double calcMedian(HashSet<Integer> ar) {
		// O(NlogN) median calculation - can be improved to O(N) with quickselect
		//System.out.println("calcMedian, edgesCovered = " + edgesCovered.size());
		double[] v = new double[edgesCovered.size()];
		int i = 0;
		for (Integer a : edgesCovered) {
			v[i++] = wc.invWeightDist.get(t.weights[a]);
		}
		Arrays.sort(v);
		return v[v.length/2];
	}
	
	public void updateMedian() {
		// TODO iterative median update?

		// single iteration update
		median = calcMedian(edgesCovered);
		edgesRepresented = new HashSet<Integer>();
		//System.out.println("Median is " + median);
		for (Integer a : edgesCovered)
			if (wc.invWeightDist.get(t.weights[a]) >= median*(1/Constants.WINDOW) && wc.invWeightDist.get(t.weights[a]) <= median*1*Constants.WINDOW)
				edgesRepresented.add(a);
		currentMDLScore = getMDLScore();
		
		minweight = Integer.MAX_VALUE;
		for (Integer a : edgesRepresented)
			minweight = Math.min(minweight, t.weights[a]);
	}

	public int getMinWeight() {
		return minweight;
	}
	
	public HashSet<Integer> getEdgesRepresented() {
		if (Constants.isWeighted)
			return edgesRepresented;
		else
			return edgesCovered;					
	}
	
	public void printState() {
		int k = 0;
		System.out.println("Edges represented: " + edgesRepresented.size());
		System.out.println("Community size (median: " + median + "):");
		for (int i=0;i<Constants.D;i++)
			System.out.println(elems.get(i).size());
		
		for (HashSet<Integer> hsi : elems) {
			System.out.println("Printing elements in " + k++);
			for (Integer a : hsi)
				System.out.println(a);
		}
		
		System.out.println("Edges represented:");
		for (Integer a : edgesRepresented)
			System.out.println(a);
	}
	
	public abstract double getMDLScore();
	public abstract void writeToCSV(FileWriter f) throws IOException;
	public abstract boolean isEdgeRepresented(int e);

	protected double log2(long n) {
		//return 31 - Integer.numberOfLeadingZeros(n);
		return Math.log(n)/Math.log(2);
	}
	
	public static Community createCommunity(ELTensor t, WC2 wc) {
		if (Constants.shape == MDLShape.RECTANGLE)
			return new RectangleCommunity(t, wc);
		else
			return new HyperbolaCommunity(t, wc);
	}
	
	public static void writeCSVHeader(FileWriter f) throws IOException {
		if (Constants.shape == MDLShape.RECTANGLE)
			RectangleCommunity.writeCSVHeader(f);
		else
			HyperbolaCommunity.writeCSVHeader(f);
	}
}
