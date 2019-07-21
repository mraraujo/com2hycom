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
public class TestSingleBinaryBlock {
	static WC2 wc;
	static ELTensor t;
	
	@BeforeClass
	public static void init() throws IOException {
		Constants.N = 64;
		Constants.D = 3;
		Constants.NS = new int[Constants.D];
		Constants.NS[0] = 3 + 1;
		Constants.NS[1] = 3 + 1;
		Constants.NS[2] = 3 + 1;
		Constants.equivalentDimension = new int[Constants.D];
		Constants.equivalentDimension[0] = 0;
		Constants.equivalentDimension[1] = 1;
		Constants.equivalentDimension[2] = 2;
		Constants.MAXW = 1;
		Constants.ELfile = "testSingleBinaryBlock";
		Constants.shape = MDLShape.RECTANGLE;

		t = new ELTensor(Constants.ELfile);
		wc = new WC2(t);		
	}
	
	@Test
	public void test1FileReading() {
		System.out.println("Starting test1");
		assertEquals(0, t.edges[7][0]);
		assertEquals(1, t.weights[7]);
		assertEquals(2, t.edges[42][2]);
		assertEquals(1, t.weights[42]);
	}
	
	@Test
	public void test2WeightDistribution() {
		System.out.println("Starting test2");
		wc.calcWeightDistribution();
		assertTrue(wc.invWeightDist.containsKey(1));
		assertEquals(0.515625, wc.invWeightDist.get(1), 1e-6);
	}

	@Test
	public void test3CalcCP() {
		System.out.println("Starting test3");
		wc.calcCP();
		assertEquals(0.25, wc.score[0][2], 1e-6);
		assertEquals(0.25, wc.score[1][2], 1e-6);
		assertEquals(0.25, wc.score[2][3], 1e-6);
	}
	
	@Test
	public void test4InitialEdge() {
		System.out.println("Starting test4");
		assertEquals(0, wc.getInitialEdge());
		
		RectangleCommunity com = new RectangleCommunity(t, wc);
		com.addElement(0,  2);com.addElement(1,  2);com.addElement(2,  2);
		HashSet<Integer> initialSet = new HashSet<Integer>();
		initialSet.add(7);
		com.addEdges(initialSet);
		
		assertEquals(391, com.getMDLScore(),1e-6);
	}
	
	@Test
	public void test5getNewElement() {
		System.out.println("Starting test5");
		RectangleCommunity com = new RectangleCommunity(t, wc);
		com.addElement(0,  0);com.addElement(1,  0);com.addElement(2,  0);
		HashSet<Integer> initialSet = new HashSet<Integer>();
		initialSet.add(0);
		com.addEdges(initialSet);
		com.updateMedian();
		int nexta = t.edges[wc.getNewElement(wc.cumscore[0], com, 0).iterator().next()][0];
		assertTrue(nexta == 1 || nexta == 2 || nexta == 3);
		int nextb = t.edges[wc.getNewElement(wc.cumscore[1], com, 1).iterator().next()][1];
		assertTrue(nextb == 1 || nextb == 2 || nextb == 3);
		int nextc = t.edges[wc.getNewElement(wc.cumscore[2], com, 2).iterator().next()][2];
		assertTrue(nextc == 1 || nextc == 2 || nextc == 3);
	}
	
	@Test
	public void test6getCommunity() {
		System.out.println("Starting test6");
		Community com = wc.getCommunity();
		HashSet<Integer> expected = new HashSet<Integer>(); 
		expected.add(0); expected.add(1);expected.add(2); expected.add(3);
		assertEquals(expected, com.getMode(0));
		assertEquals(expected, com.getMode(1));
		assertEquals(expected, com.getMode(2));
		System.out.println("First community");
		for (int k = 0; k<3; k++) {
			System.out.println("Mode: " + k);
			for (int a : com.getMode(k))
				System.out.println(a);
		}
		t.removeCommunity(com);
	}
}
