/**
 * @file
 */
package sc.fiji.CMP_BIA.tools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author borovji3
 *
 */
public class Prints {


	/**
	 * 
	 * @param a
	 */
	public static void printArray(int[] a) {
		System.out.println( Arrays.toString(a) );		
	}	
	
	/**
	 * 
	 * @param x
	 */
	public static void printMatrix(float[][] x) {
		for (int i=0; i<x.length; i++) {
			printArray(x[i]);
		}
		System.out.println();
	}
	
	/**
	 * 
	 * @param x
	 */
	public static void printMatrix(double[][] x) {
		for (int i=0; i<x.length; i++) {
			printArray(x[i]);
		}
		System.out.println();
	}
	
	/**
	 * 
	 * @param a
	 */
	public static void printArray(float[] a) {
		System.out.println( Arrays.toString(a) );		
	}

	
	/**
	 * 
	 * @param a
	 */
	public static void printArray(double[] a) {
		System.out.println( Arrays.toString(a) );		
	}
	
	/**
	 * 
	 * @param x
	 */
	public static void printMatrix(int[][] x) {
		for (int i=0; i<x.length; i++) {
			printArray(x[i]);
		}
		System.out.println();
	}
	
	/**
	 * 
	 * @param a
	 */
	public static <N> void printArray(N[] a) {
		// Display array elements    
        System.out.print("[");          
		for (int i=0; i<a.length; i++){        
        	System.out.print(a[i]);
        	if (i<(a.length-1)) {
        		System.out.print(", ");
        	}
        }
        System.out.println("]");
	}
	
	/**
	 * 
	 * @param x
	 */
	public static <N> void printMatrix(N[][] x) {
		for (int i=0; i<x.length; i++) {
			printArray(x[i]);
		}
	}
	
	/**
	 * 
	 * @param title
	 */
	public static void printTitle(String title) {
		System.out.println("\n"+"\n");
		System.out.println(title);
		for(int i=0; i<title.length(); i++) {
			System.out.print("=");
		}
		System.out.println("\n");
	}

	public static void printListListArray(ArrayList<ArrayList<int[]>> x) {
		for (int i=0; i<x.size(); i++ ) {
			if (x.get(i) == null) {
				System.out.println("WARRING: empty list!");
				continue;
			}
			for (int j=0; j<x.get(i).size(); j++ ) {
				System.out.print("["+x.get(i).get(j)[0]);
				for (int k=1; k<x.get(i).get(j).length; k++ ) {
					System.out.print(";"+x.get(i).get(j)[k]);
				}
				System.out.print("], ");
			}
			System.out.println();
		}
	}
	
}
