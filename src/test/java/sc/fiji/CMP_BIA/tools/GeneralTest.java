package sc.fiji.CMP_BIA.tools;

import java.util.HashMap;

import org.junit.Test;

import sc.fiji.CMP_BIA.tools.Generators;
import sc.fiji.CMP_BIA.tools.MatrixTools;
import sc.fiji.CMP_BIA.tools.OptionalParameters;


/**
 * @class Generators
 * @version 0.1
 * @date 1#/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @brief ...
 */
public class GeneralTest {

	/**
	 * 
	 */
	@Test
	public void test_RandomIndexes() {
		Prints.printTitle("Randomised numbers");
				
		System.out.println("permutation from {1,3,5,7,9}");
		Prints.printArray(Generators.gPermutation(new int[]{1,3,5,7,9}));
		System.out.println("permutation till 9");
		Prints.printArray(Generators.gPermutation(9));
		System.out.println("random 5 indexes from 99");
		Prints.printArray(Generators.gUniqueRandomIndexes(5, 99));
		System.out.println("random 7 indexes from 9");
		Prints.printArray(Generators.gUniqueRandomIndexes(7, 9));
	}
	
	/**
	 * 
	 */
	@Test
	public void test_OptimalParams() {
		Prints.printTitle("Optimal Parameters");
		
		HashMap<String,Double> p = new HashMap<String,Double>();
		p.put("java", 1e-1);
		OptionalParameters param = new OptionalParameters(p);
		param.putParam("abc", 1e-2);		
		param.printParams();

		System.out.println("Edit param 'abc'...");
		param.putParam("abc", 5e-2);
		param.printParams();

		System.out.println("Remove param 'java'...");
		param.removeParam("java");
		param.printParams();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_Matrixes() throws Exception {
		Prints.printTitle("Matrix Operations");
		
		float[][] a = new float[][]{{1, 2, 3, 4, 5, 6}, {7, 8, 9, 10, 11, 12}};
		Prints.printMatrix(a);
		
		float[][] b = MatrixTools.reshape(a, 3, 4);
		Prints.printMatrix(b);

		System.out.println("Find minimum");
		Prints.printArray(MatrixTools.matrixMin(a));

		System.out.println("Find maximas in dim 0");
		Prints.printArray(MatrixTools.matrixMaxInDim(a, 0));
		System.out.println("Find maximas in dim 1");
		Prints.printArray(MatrixTools.matrixMaxInDim(a, 1));
	}
	
	public static void wait(int t) {
		try {
			Thread.sleep(t*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
