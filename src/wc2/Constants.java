package wc2;

public final class Constants {
	public static int N; // number of edges
	public static int D; // number of dimensions
	
	public static int[] NS; // number of elements of each dimension
	public static int[] equivalentDimension; // equivalentDimension[i] != i means i should not be searched
	
	public static String ELfile; // edge list file
	
	public enum MDLShape { RECTANGLE, HYPERBOLA}; // shape to fit
	public static MDLShape shape = MDLShape.RECTANGLE;
	
	// variables for weights
	public static boolean isWeighted = false;
	public static double WINDOW = 1.1; 
	public static long MAXW = 1; // maximum weight
	
	public static boolean speedupDeletions = false; // don't search for the best element to delete, delete immediately if MDL is improved
	
	public static int increaseFails = 100;
	
	public static String CSVfile;
	
	public static boolean DEBUG = false;
	
	private Constants() {}
}
