/**
 * @file
 */
package sc.fiji.CMP_BIA;

import jml.classification.Classifier;
import jml.classification.LogisticRegressionMCLBFGS;
import jml.clustering.Clustering;
import jml.clustering.KMeans;
import jml.clustering.SpectralClustering;
import jml.optimization.LBFGS;
import jml.options.KMeansOptions;
import jml.options.Options;
import jml.options.SpectralClusteringOptions;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.junit.Test;

import sc.fiji.CMP_BIA.tools.Prints;

/**
 * @author borovji3
 *
 */
@SuppressWarnings("deprecation")
public class TestThirdPartLibsJML {
	// input data
	protected static double[][] data = { 
			{3.5, 4.4, 1.3},
		    {5.3, 2.2, 0.5},
		    {0.2, 0.3, 4.1},
		    {-1.2, 0.4, 3.2},
		    {-0.2, 1.4, 2.2},
		    {1.2, -0.4, 4.2},
		    {5.3, 2.2, 0.5} };
	protected static double[][] labels = { 
			{1, 0, 0}, 
			{0, 1, 0}, 
			{0, 0, 1} };
	protected static int nbClasses = 3;
	
	@Test
	public void sample_LogicRegres() {
		Prints.printTitle("JML - Logic Regres");
		
		Options options = new Options();
		options.epsilon = 1e-6; 
		// Multi-class logistic regression by using limited-memory BFGS method 
		Classifier logReg = new LogisticRegressionMCLBFGS(options); 
		logReg.feedData(data);
		logReg.feedLabels(labels);
		logReg.train();
		RealMatrix Y_pred = logReg.predictLabelScoreMatrix(data);
		Prints.printRealMatrix( Y_pred );
	}
	
	@Test
	public void sample_SpectralClustering() {
		Prints.printTitle("JML - Spectral Clustering");
		
		RealMatrix m = new RealMatrixImpl(data);
		double[][] dataT = m.transpose().getData();
		
		SpectralClusteringOptions options = new SpectralClusteringOptions(); 
		// settings
		options.nClus = nbClasses; 
		options.verbose = false; 
		options.maxIter = 100; 
		options.graphType = "nn"; 
		options.graphParam = 2; 
		options.graphDistanceFunction = "cosine"; 
		options.graphWeightType = "heat"; 
		options.graphWeightParam = 1; 
		// sun
		Clustering spectralClustering = new SpectralClustering(options); 
		spectralClustering.feedData(dataT); 
		spectralClustering.clustering(); 
		// output
		System.out.println("Indicator:");
		Prints.printRealMatrix( spectralClustering.getIndicatorMatrix() );
		System.out.println("Centers:");
		System.out.println( spectralClustering.getCenters() );
	}
	
	@Test
	public void sample_Kmeans() {
		Prints.printTitle("JML - Kmeans");
		
		RealMatrix m = new RealMatrixImpl(data);
		double[][] dataT = m.transpose().getData();
		
		// initial settings
		KMeansOptions options = new KMeansOptions();
		options.nClus = nbClasses;
		options.verbose = true;
		options.maxIter = 100;

		KMeans kmeans= new KMeans(options);

		kmeans.feedData(dataT);
		 // Use null for random initialization
		kmeans.clustering(null);

		System.out.println("Indicator:");
		Prints.printRealMatrix( kmeans.getIndicatorMatrix() );
		System.out.println("Centers:");
		Prints.printRealMatrix( kmeans.getCenters() );
	}
	
	@Test
	public void sample_LBFGS() {
		Prints.printTitle("JML - Limited-memory BFGS");

		// Current matrix (vector) you want to optimize
		double[][] w = {{0.0, 0.0}};
		RealMatrix W = new RealMatrixImpl(w); 
		// Current objective function value
		double fval = computeFval(W); 
		// Tolerance
		double epsilon = 1e-3; 
		// Gradient at the current matrix variable
		RealMatrix G = computeGrad(W); 
		
		while (true) { 
			 // Update W
			boolean[] flags = LBFGS.run(G, fval, epsilon, W);
			// flags[0] means if LBFGS converges
			if (flags[0]) { 
				break; 
			}
			// Compute the new objective function value at W
			fval = computeFval(W); 
			// flags[1] means if gradient at W is required
			if (flags[1]) { 
				// Compute the new gradient at W
				G = computeGrad(W); 
			}
		}

		// Results
		System.out.println("Results fval:");
		System.out.println( fval );
		System.out.println("Results params:");
		Prints.printRealMatrix( W );
	}

	
	protected double computeFval(RealMatrix W) {
		double[][] w = W.getData();
		// (x-1)^2 + (y+2)^2 
		double fval = Math.pow(w[0][0]-1., 2) + Math.pow(w[0][1]+2., 2);
		return fval;
	}
	
	protected RealMatrix computeGrad(RealMatrix W) {
		double[][] w = W.getData();
		// (x-1)*2 + (y+2)*2
		double[][] g = new double[][]{{2*(w[0][0]-1.), 2*(w[0][1]+2.)}};
		RealMatrix G = new RealMatrixImpl(g);
		return G;
	}
}
