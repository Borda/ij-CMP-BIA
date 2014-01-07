/**
 * @file
 */

package sc.fiji.CMP_BIA.classification;

import java.util.ArrayList;
import java.util.Arrays;

import sc.fiji.CMP_BIA.tools.Generators;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.converters.ConvertStructure;


/**
 * @class K-Means
 * @version 1.0
 * @date 12/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category data clustering
 * 
 * @brief implementation of basic K-means clustering generalised to process 
 * matrix of floats
 * 
 * @see http://en.wikipedia.org/wiki/K-means_clustering
 */
public class KMeans {

	// input date of type T and size [nbSamples][nbFeatures]
	protected float[][] data = null;
	// internal cluster
	protected Clusters clusters = null;

	
	/**
	 * Constructor which asks only for initial data
	 * 
	 * @param d is matrix float[nbSamples][nbDataElements] 
	 * representing data to be clustered assuming the fist dimension for number 
	 * of samples and the second for the sample elements
	 */
	public KMeans(float[][] d) {
		// check empty data
		if (d.length < 1) {
			Logging.logMsg("ERROR: empty data!");
			return;
		}
		
		// clone data into internal structure
		data = d;
		
		// init assignments
		clusters = new Clusters(data.length);
	}
			

	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * For initialisation the early clusters are used randomly taken data 
	 * from input data
	 * 
	 * @param nbClusters is int of number of clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public void process(int nbClusters, int maxIter) {
		
		Logging.logMsg("KMeans: random init. for "+Integer.toString(nbClusters)+" clusters... ");
		
		// take random subset of all data
		int nbSubData = (int) Math.round(data.length * 0.1);
		nbSubData = (nbSubData < nbClusters*10) ? nbClusters*10 : nbSubData;
		float[][] subData = Generators.randomSamples(data, nbSubData);
		
		// init random clustering
		float[][] initClts = null;
		float dist, minDist = Float.MAX_VALUE;
		// find best initialisation
		for (int i = 0; i < nbClusters*nbClusters; i++) {
			// randomly init clusteers
			clusters.centers = Generators.randomSamples(data, nbClusters);
			dist = process(subData, clusters, 1);
			Logging.logMsg("KMeans: -> random init. distance is " + Float.toString(dist));
			// if better init
			if (dist <  minDist) {
				minDist = dist;
				initClts = clusters.centers.clone();
			}
		}

		// run main clustering
		clusters.centers = initClts.clone();		
		process(data, clusters, maxIter);
		
	}

	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * 
	 * @param clts is a matrix of float[nbClusters][nbDataElemnts] which is 
	 * used as initial clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public void process(float[][] clts, int maxIter) {
		
		clusters.centers = clts.clone();
		
		process(data, clusters, maxIter);
		
	}

	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * 
	 * @param data is a matrix of float[nbSamples][nbDataElemnts] 
	 * @param clts is a matrix of float[nbClusters][nbDataElemnts] which is 
	 * used as initial clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public static float process(final float[][] data, Clusters clusters, int maxIter) {
		//Logging.logMsg("KMeans: enter main process for "+Integer.toString(nbClusters)+" clusters and max "+Integer.toString(maxIter)+" iteration ");
		
		// register clusters
		//float[][] clusters = clts.clone();
		float dist = Float.MAX_VALUE;
						
		// remembering last assignment
		int[] labelsLast = new int[data.length];
		Arrays.fill(labelsLast, -1);
		
		clusters.initVariables(data.length);
		
		for (int iter=0; iter<maxIter; iter++) {
			
			// computing distances
			assigne(data, clusters);
			
			// update clusters
			update(data, clusters);

			// print iteration information
			dist = clusters.sumInterDist();
			Logging.logMsg("KMeans: inter. distance for iter " + 
					Integer.toString(iter+1) + "/"+ Integer.toString(maxIter) +
					" is " + Float.toString(dist));
			

			// compare with last assignment and if they are same stop iterating
			if (compareAssignments(clusters.labels,labelsLast) == 0) {
				iter = maxIter;
				Logging.logMsg("KMeans: terminated becase of no changes.");
			} else {
				System.arraycopy(clusters.labels, 0, labelsLast, 0, clusters.labels.length);
			}

			// if empty cluster reinitiate
			if (clusters.countEmptyClusters() > 0) {
				int[] emptyClrs = clusters.getEmptyClusters();
				
				// to previously empty clusters assign random data samples
				int[] randIdx = Generators.gUniqueRandomIndexes(emptyClrs.length, data.length);
				for (int i=0; i<emptyClrs.length; i++) {
					for (int j=0; j<data[randIdx[i]].length; j++) {
						clusters.centers[emptyClrs[i]][j] = data[randIdx[i]][j];
					}
				}
				
				// update the labeling and compute new centers
				assigne(data, clusters);
				update(data, clusters);

				// TODO - split clusters with largest deviation
			}
			
		}
		return dist;
	}

	
	/**
	 * Compare tho different labeling of the same size and return number of 
	 * unequally labeled data in both comparing labeling
	 * 
	 * @param A array int[] of labeling, assuming A.lenght==B.lenght
	 * @param B array int[] of labeling, assuming A.lenght==B.lenght
	 * @return int number of unequally labeled data in both comparing labeling
	 */
	public static int compareAssignments(int[] A, int[] B) {
		int size = A.length;
		// check the array dimensions
		if (A.length != B.length) {
			if (A.length < B.length) {  size = A.length;  }  else  {  size = B.length;  }
			throw new IndexOutOfBoundsException("WARRING: array A has size "+ Integer.toString(A.length)+
												" and array B has size "+ Integer.toString(B.length));
		}
		int diff = 0;
		// go over all elements and count if they are different
		for (int i=0; i<size; i++) {
			if (A[i] != B[i]) {
				diff ++;
			}
		}
		return diff;
	}
	
	/**
	 * Label assignment to all samples according the smallest distance to all 
	 * clusters (both labeling and the smallest distance are stored) 
	 * 
	 * @param data
	 * @param centers
	 * @param distances
	 * @param labels
	 */
	protected static void assigne(final float[][] data, Clusters cls) {
		
		int nbClusters = cls.getNbClusters();
		
		// over all data for summing
		for(int i=0; i<data.length; i++) {
			for (int k=0; k<nbClusters; k++) {
				float sum = 0;
				for (int j=0; j<data[i].length; j++) {
					sum += (float)(cls.centers[k][j]-data[i][j]) * (float)(cls.centers[k][j]-data[i][j]);
				}
				// if actual distance is larger then the smallest so far
				if ( sum < cls.distances[i]) {
					cls.distances[i] = sum;
					cls.labels[i] = k;
				}
			}
		}
		
	}
	
	/**
	 * According the labelling minimising the distance the cluster centres are 
	 * computed (updated)
	 * 
	 * @param data
	 * @param centers
	 * @param labels
	 * @return
	 */
	protected static void update(final float[][] data, Clusters cls) {
		// init
		int nbClusters = cls.getNbClusters();
		cls.counts = new int[nbClusters];
		
		Arrays.fill(cls.counts, 0);
		// init temporary array for summing features
		float[][] tmp = new float[nbClusters][data[0].length];
		for(float[] subarray : tmp) {   Arrays.fill(subarray, 0);   }
		
		// over all data for summing
		for(int i=0; i<data.length; i++) {
			for (int j=0; j<data[0].length; j++) {
				tmp[cls.labels[i]][j] += data[i][j];
			}
			cls.counts[cls.labels[i]] ++;
		}
		
		// over all cluster for summing
		for(int i=0; i<nbClusters; i++) {
			for (int j=0; j<data[0].length; j++) {
				cls.centers[i][j] = tmp[i][j] / (float)cls.counts[i];
			}
		}
	}
	
	/**
	 * Returns the estimated cluster centres
	 * 
	 * @return float[nbClusters][nbDataElements]
	 */
	public float[][] getClusterCenters() {
		// if not processed yet
		if (clusters == null) {   return null;   }
		
		return clusters.centers.clone();
	}
	
	
	/**
	 * Returns the final labeling
	 * 
	 * @return int[nbSamples] labeling assigning each data to a cluster 
	 */
	public int[] getLabels() {
		return clusters.labels.clone();
		
	}

	/**
	 * @author borovji3
	 *
	 */
	public class Clusters {

		// final clusters of type T and size [nbClusters][nbFeatures]
		protected float[][] centers = null;
		// distance to nearest cluster or size [nbSamples]
		protected float[] distances = null;
		// assignment of nearest cluster or size [nbSamples]
		protected int[] labels = null;
		// count elements in each cluster
		int[] counts = null;
		
		public Clusters(int sz) {
			initVariables(sz);
		}
		
		/**
		 * 
		 * @return
		 */
		public int getNbClusters() {
			return centers.length;
		}
		
		/**
		 * Initialisation of local variables such as distances of labelling 
		 */
		protected void initVariables(final int size) {
			// init distances
			distances = new float[size];
			Arrays.fill(distances, Float.MAX_VALUE);	
			// init assignments
			labels = new int[size];
			Arrays.fill(labels, -1);
		}
		
		/**
		 * Sum all minimal internal distances for actual assignment
		 * 
		 * @param distances
		 * @return total distance
		 */
		public float sumInterDist() {
			float dist = 0;
			for (int i=0; i<distances.length; i++) {
				dist += distances[i];
			}
			return dist;
		}

		/**
		 * Counting empty clusters. It would be useful in case of random cluster 
		 * initialization where is no guarantee that the init is meaningful
		 * 
		 * @param counts
		 * @return int number of empty cluster, clusters with zero assigned samples
		 */
		public int countEmptyClusters() {
			int count = 0;
			// go over all clusters
			for (int i=0; i<counts.length; i++) {
				// check empty clusters
				if (counts[i] == 0) {
					count ++;
				}
			}
			return count;
		}
		
		/**
		 * Find all empty clusters meaning clusters with zero assigned samples
		 * 
		 * @param counts
		 * @return int[nbEmpty] indexes of clusters which does not cover any sample
		 */
		public int[] getEmptyClusters() {
			// if array counts is not defined yet return void array
			if ( counts == null ) {   return null;   }
			// init array of indexes to the empty clusters
			ArrayList<Integer> empty = new ArrayList<Integer>();
			
			// go over all clusters
			for (int i=0; i<counts.length; i++) {
				// check empty clusters
				if (counts[i] == 0) {
					empty.add(i);
				}
			}
			
			return ConvertStructure.arrayList2intArray(empty);
		}
	}
	
}


