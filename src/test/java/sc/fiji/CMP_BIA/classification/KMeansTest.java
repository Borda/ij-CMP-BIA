/**
 * 
 */
package sc.fiji.CMP_BIA.classification;

import org.junit.Test;

import sc.fiji.CMP_BIA.tools.Prints;
import sc.fiji.CMP_BIA.tools.converters.ConvertStructure;

/**
 * @author borovji3
 *
 */
public class KMeansTest {

	// input data
	protected static double[][] data = { 
		{3.5, 4.4, 1.3},
	    {5.3, 2.2, 0.5},
	    {0.2, 0.3, 4.1},
	    {-1.2, 0.4, 3.2},
	    {-0.2, 1.4, 2.2},
	    {1.2, -0.4, 4.2},
	    {5.3, 2.2, 0.5} };
	// init clusters
	protected static double[][] clusters = { 
		{3.5, 4.4, 1.3},
	    {-1.2, 0.4, 3.2},
	    {5.3, 2.2, 0.5} };

	@Test
	public void test_raw() {
		Prints.printTitle("K-Means - RAW");

		KMeans kmeans = new KMeans( ConvertStructure.doubleMatrix2floatMatrix(data) );
		System.out.println("Data:");
		Prints.printMatrix(data);
		
		kmeans.process(ConvertStructure.doubleMatrix2floatMatrix(clusters), 9);
		System.out.println("\n Labelling:");
		Prints.printArray( kmeans.getLabels() );
	}
	
	@Test
	public void test_random() {
		Prints.printTitle("K-Means - random init");

		KMeans kmeans = new KMeans( ConvertStructure.doubleMatrix2floatMatrix(data) );
		System.out.println("Data:");
		Prints.printMatrix(data);
		
		kmeans.process(3, 9);
		System.out.println("\n Labelling:");
		Prints.printArray( kmeans.getLabels() );
	}

}
