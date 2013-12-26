/**
 * @file
 */
package sc.fiji.CMP_BIA.tools.converters;

import java.util.ArrayList;

/**
 * @class Structure Convertor
 * @version 0.1
 * @date 10/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category java tools
 * 
 * @brief collection of static function which converts several java structure/types 
 * to another (dynamic <-> static). We basically use it as a transition between 
 * particular method or external libraries
 */
public class ConvertStructure {
	
	/**
	 * convert the 'ArrayList<Integer>' to standard array of 'int[]'
	 * 
	 * @param list
	 * @return int[]
	 */
	public static int[] arrayList2intArray (final ArrayList<Integer> list) {
		// init new array of the same size
		int[] res = new int[list.size()];
		// rewrite all values to new array
		for (int i=0; i<list.size(); i++) {
			res[i] = list.get(i);
		}
		return res;
	}
	
//	/**
//	 * convert the convert ArrayList<Integer>[] to standard int[][]
//	 * @param list
//	 * @return int[][]
//	 */
//	static public int[][] arrayList2intMatrix (ArrayList<Integer>[] list) {
//		// init new array of the same size
//		int[][] res = new int[list.length][];
//		// init arrays
//		for (int i=0; i<list.length; i++) {
//			res[i] = new int[list[i].size()];
//			// rewrite all values to new array
//			for (int j=0; j<list[i].size(); j++) {
//				res[i][j] = list[i].get(j);
//			}
//		}
//		return res;
//	}
	
//	/**
//	 * convert the convert ArrayList<Float>[] to standard float[][]
//	 * @param list
//	 * @return float[][]
//	 */
//	static public float[][] arrayList2floatMatrix (ArrayList<Float>[] list) {
//		// init new array of the same size
//		float[][] res = new float[list.length][];
//		// init arrays
//		for (int i=0; i<list.length; i++) {
//			res[i] = new float[list[i].size()];
//			// rewrite all values to new array
//			for (int j=0; j<list[i].size(); j++) {
//				res[i][j] = list[i].get(j);
//			}
//		}
//		return res;
//	}
	
	/**
	 * convert the convert 'ArrayList<Float>[]' to standard 'float[][]'
	 * 
	 * @param desc
	 * @return float[][]
	 */
	public static ArrayList<float[]> arrayLists2floatList (final ArrayList<ArrayList<Float>> desc) {
		// init new array of the same size
		ArrayList<float[]> res = new ArrayList<float[]>(desc.size());
		// init arrays
		for (int i=0; i<desc.size(); i++) {
			res.add( new float[desc.get(i).size()] );
			// rewrite all values to new array
			for (int j=0; j<desc.get(i).size(); j++) {
				res.get(i)[j] = desc.get(i).get(j);
			}
		}
		return res;
	}

	
	/**
	 * convert the convert 'ArrayList<ArrayList<Float>>' to standard 'float[][]'
	 * 
	 * @param list
	 * @return float[][]
	 */
	public static float[][] arrayLists2floatMatrix (final ArrayList<ArrayList<Float>> list) {
		// init new array of the same size
		float[][] res = new float[list.size()][];
		// init arrays
		for (int i=0; i<list.size(); i++) {
			res[i] = new float[list.get(i).size()];
			// rewrite all values to new array
			for (int j=0; j<list.get(i).size(); j++) {
				res[i][j] = list.get(i).get(j);
			}
		}
		return res;
	}
	
	/**
	 * convert the convert 'ArrayList<ArrayList<Integer>>' to standard 'int[][]'
	 * 
	 * @param list of type 'ArrayList<ArrayList<Integer>>'
	 * @return int[][]
	 */
	public static int[][] arrayLists2intMatrix (final ArrayList<ArrayList<Integer>> list) {
		// init new array of the same size
		int[][] res = new int[list.size()][];
		// init arrays
		for (int i=0; i<list.size(); i++) {
			res[i] = new int[list.get(i).size()];
			// rewrite all values to new array
			for (int j=0; j<list.get(i).size(); j++) {
				res[i][j] = list.get(i).get(j);
			}
		}
		return res;
	}
	
	/**
	 * convert the convert 'ArrayList<int[]>' to standard 'int[][]'
	 * 
	 * @param list of type 'ArrayList<int[]>'
	 * @return int[][]
	 */
	public static int[][] arrayList2intMatrix (final ArrayList<int[]> list) {
		// init new array of the same size
		int[][] res = new int[list.size()][];
		// init arrays
		for (int i=0; i<list.size(); i++) {
			res[i] = list.get(i).clone();
		}
		return res;
	}
	
	
	/**
	 * 
	 * @param m is matrix of a 'Number'
	 * @return float[][]
	 */
	public static <T extends Number> float[][] NumberMatrix2floatMatrix(final T[][] m) {
		// init matrix
		float[][] res = new float[m.length][m[0].length];
		// over all data for summing
		for(int i=0; i<m.length; i++) {
			for (int j=0; j<m[i].length; j++) {
				res[i][j] = m[i][j].floatValue();
			}
		}
		return res;
	}
	
}
