package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import wc2.*;
import wc2.Constants.MDLShape;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestHBipartiteBlock {
	static WC2 wc;
	static ELTensor t;
	
	@BeforeClass
	public static void init() throws IOException {
		Constants.N = 26;
		Constants.D = 3;
		Constants.NS = new int[Constants.D];
		Constants.NS[0] = 2 + 1;
		Constants.NS[1] = 10 + 1;
		Constants.NS[2] = 1;
		Constants.equivalentDimension = new int[Constants.D];
		Constants.equivalentDimension[0] = 0;
		Constants.equivalentDimension[1] = 1;
		Constants.equivalentDimension[2] = 2;
		Constants.MAXW = 1;
		Constants.ELfile = "testBipartite";
		Constants.shape = MDLShape.HYPERBOLA;
		Constants.isWeighted = false;

		t = new ELTensor(Constants.ELfile);
		wc = new WC2(t);		
	}
	
	/*@Test
	public void test1FileReading() {
		System.out.println("Starting test1");
		for (int i = 0; i<Constants.N; i++)
			System.out.println(t.edges[i][0] + " " + t.edges[i][1] + " " + t.edges[i][2]);
	}*/
	
	@Test
	public void test2WeightDistribution() {
		System.out.println("Starting test2");
		wc.calcWeightDistribution();
		assertTrue(wc.invWeightDist.containsKey(1));
		assertEquals(14/26.0, wc.invWeightDist.get(1), 1e-6);
	}

	@Test
	public void test3CalcCP() {
		System.out.println("Starting test3");
		wc.calcCP();
		assertEquals(0.415362, wc.score[0][0], 1e-4);
		assertEquals(0.415362, wc.score[0][1], 1e-4);
		assertEquals(0.101884, wc.score[1][0], 1e-4);
		assertEquals(0.101884, wc.score[1][2], 1e-4);
		assertEquals(0.101884, wc.score[1][3], 1e-4);
		assertEquals(1.0, wc.score[2][0], 1e-6);
	}
	
	@Test
	public void test4InitialEdge() {
		System.out.println("Starting test4");
		assertEquals(0, wc.getInitialEdge());
		
		RectangleCommunity com = new RectangleCommunity(t, wc);
		com.addElement(0,  0);com.addElement(1,  0);com.addElement(2,  0);
		HashSet<Integer> initialSet = new HashSet<Integer>();
		initialSet.add(0);
		com.addEdges(initialSet);
		
		assertEquals(137.198641, com.getMDLScore(),1e-3);
	}
	
	@Test
	public void test5getNewElement() {
		System.out.println("Starting test5");
		RectangleCommunity com = new RectangleCommunity(t, wc);
		com.addElement(0,  0);com.addElement(1,  0);com.addElement(2,  0);
		HashSet<Integer> initialSet = new HashSet<Integer>();
		initialSet.add(0);
		com.addEdges(initialSet);
		int v = wc.getNewElement(wc.cumscore[0], com, 0).iterator().next();
		int nexta = t.edges[v][0];
		assertTrue(nexta == 1 || nexta == 2);
		int nextb = t.edges[wc.getNewElement(wc.cumscore[1], com, 1).iterator().next()][1];
		assertTrue(nextb >= 1 && nextb <= 10);
		assertEquals(0, wc.getNewElement(wc.cumscore[2], com, 2).size());
	}
	/*
	@Test
	public void test6forceDecrease() {
		System.out.println("Starting test6");
		Community com = new Community(t, wc);
		com.addElement(0, 0); com.addElement(0, 1); com.addElement(0, 2); 
		com.addElement(1, 0); com.addElement(1, 1); com.addElement(1, 2); com.addElement(1, 3); com.addElement(1, 4); com.addElement(1, 5); com.addElement(1, 6); com.addElement(1, 7); com.addElement(1, 8); com.addElement(1, 9); com.addElement(1, 10);
		com.addElement(2, 0);
		HashSet<Integer> initialSet = new HashSet<Integer>();
		for (int i = 0; i<Constants.N; i++)
			initialSet.add(i);
		com.addEdges(initialSet);
		assertTrue(com.getMDLDiffDel(0, 2) < 0);
		assertTrue(wc.tryDecrease(com));
	}
	*/
	@Test
	public void test7getCommunity() {
		System.out.println("Starting test7");
		HyperbolaCommunity com = (HyperbolaCommunity) wc.getCommunity();
		com.printState();
		
		HashSet<Integer> expected = new HashSet<Integer>(); 
		expected.add(0); 
		assertEquals(expected, com.getMode(2));
		expected.add(1);expected.add(2);
		assertEquals(expected, com.getMode(0));
		for (int i = 2; i<=10; i++)
			expected.add(i);
		assertEquals(expected, com.getMode(1));
		
		double expectedAlphas[] = {-0.8216453, -0.2327103, 0.0};
		assertArrayEquals(expectedAlphas, com.getAlpha(), 1e-6);
		
		System.out.println("TEST7: first community found correctly!");
		
		t.removeCommunity(com);
		com = (HyperbolaCommunity) wc.getCommunity();
		assertEquals(0, com.getEdgesRepresented().size());
	}
}
