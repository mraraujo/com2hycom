package wc2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class RectangleCommunity extends Community {
	
	public RectangleCommunity(ELTensor _t, WC2 _wc) {
		super(_t, _wc);
		currentMDLScore = getMDLScore();
	}
	
	public double getMDLScore() {
		int nels[] = new int[Constants.D];
		int prod = 1;
		int fulltensor = 1;
		for (int i = 0; i<Constants.D; i++) {
			nels[i] = elems.get(Constants.equivalentDimension[i]).size();
			prod *= nels[Constants.equivalentDimension[i]];
			fulltensor *= Constants.NS[Constants.equivalentDimension[i]];
		}
		
		double cost = 0;
		// encode community sizes
		for (int i = 0; i<Constants.D; i++)
			cost += log2(Constants.NS[Constants.equivalentDimension[i]]);
		
		// encode elements
		for (int i = 0; i<Constants.D; i++)
			cost += nels[i]*log2(Constants.NS[Constants.equivalentDimension[i]]); 
		
		// number of holes
		cost += log2(prod+1);
		// encoding holes
		cost += (prod - edgesRepresented.size())*log2(prod+1);
		// rest of the tensor
		cost += (Constants.N-edgesRepresented.size())*log2(fulltensor); // rest of the tensor
		return cost;
	}

	public boolean isEdgeRepresented(int e) {
		return edgesRepresented.contains(e);
	}

	public void writeToCSV(FileWriter f) throws IOException {
		for (int i=0;i<Constants.D;i++)
			f.write(elems.get(i).size() + ", ");
		f.write(""+edgesRepresented.size());
		for (HashSet<Integer> hsi : elems) {
			f.write(",");
			for (Integer a : hsi)
				f.write(" " + a);
		}	
		f.write('\n');
		f.flush();
	}
	
	public static void writeCSVHeader(FileWriter f) throws IOException {
		for (int i=0;i<Constants.D;i++)
			f.write("#elements in " + i + ", ");
		f.write("#edges");
		for (int i=0;i<Constants.D;i++)
			f.write(",List of elements in " + i);
		f.write('\n');
		f.flush();
	}
}
