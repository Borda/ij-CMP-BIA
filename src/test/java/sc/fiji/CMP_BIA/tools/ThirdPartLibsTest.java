/**
 * 
 */
package sc.fiji.CMP_BIA.tools;

import jml.classification.Classifier;
import jml.classification.LogisticRegressionMCLBFGS;
import jml.clustering.Clustering;
import jml.clustering.SpectralClustering;
import jml.options.Options;
import jml.options.SpectralClusteringOptions;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

/**
 * @author borovji3
 *
 */
public class ThirdPartLibsTest {
	
	@Test
	public void sample_LogicRegres() {
		Prints.printTitle("JML - Logic Regres");
		
		double[][] data = { {3.5, 4.4, 1.3}, {5.3, 2.2, 0.5}, {0.2, 0.3, 4.1}, {-1.2, 0.4, 3.2} };
		double[][] labels = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
		Options options = new Options();
		options.epsilon = 1e-6; 
		// Multi-class logistic regression by using limited-memory BFGS method 
		Classifier logReg = new LogisticRegressionMCLBFGS(options); 
		logReg.feedData(data);
		logReg.feedLabels(labels);
		logReg.train();
		RealMatrix Y_pred = logReg.predictLabelScoreMatrix(data);
		System.out.println( Y_pred.toString() );
	}
	
	@Test
	public void sample_SpectralClustering() {
		Prints.printTitle("JML - Spectral Clustering");
		
		double[][] data = { {3.5, 4.4, 1.3}, 
							{5.3, 2.2, 0.5}, 
							{0.2, 0.3, 4.1}, 
							{-1.2, 0.4, 3.2} }; 
		SpectralClusteringOptions options = new SpectralClusteringOptions(); 
		options.nClus = 2; 
		options.verbose = false; 
		options.maxIter = 100; 
		options.graphType = "nn"; 
		options.graphParam = 2; 
		options.graphDistanceFunction = "cosine"; 
		options.graphWeightType = "heat"; 
		options.graphWeightParam = 1; 
		Clustering spectralClustering = new SpectralClustering(options); 
		spectralClustering.feedData(data); 
		spectralClustering.clustering(); 
		System.out.println( spectralClustering.getIndicatorMatrix() );
	}
	
	@Test
	public void sample_LBFGS() {
		Prints.printTitle("JML - Limited-memory BFGS");
		
//		double eps = 1e-6; 
//		RealMatrix W = repmat(zeros(nFea, 1), new int[]{1, K}); 
//		RealMatrix A = X.transpose().multiply(W); 
//		RealMatrix V = sigmoid(A); 
//		RealMatrix G = X.multiply(V.subtract(Y)).scalarMultiply(1.0 / nSample); 
//		double fval = -sum(sum(times(Y, log(plus(V, eps))))).getEntry(0, 0) / nSample;
//		boolean flags[] = null; 
//		
//		while (true) { 
//			flags = LBFGS.run(G, fval, eps, W); // Update W 
//			if (flags[0]) { // flags[0] means if LBFGS converges
//				break; 
//			}
//			A = X.transpose().multiply(W); 
//			V = sigmoid(A); 
//			// Compute the new objective function value at W
//			fval = -sum(sum(times(Y, log(plus(V, eps))))).getEntry(0, 0) / nSample; 
//			if (flags[1]) { // flags[1] means if gradient at W is required
//				// Compute the new gradient at W
//				G = rdivide(X.multiply(V.subtract(Y)), nSample); 
//			}
//		}
	}
	
}
