/**
 * @file
 */

package sc.fiji.CMP_BIA.segmentation.superpixels;

import ij.IJ;
import ij.ImagePlus;

import java.lang.Math;
import java.util.Arrays;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.Threading;
import sc.fiji.CMP_BIA.tools.converters.ConvertImage;


/**
 * @class SLIC superpixels
 * @version 1.0
 * @date 19/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief This is SLIC superpixel segmentation for 2D images only mainly 
 * transcription of the original EPFL code for RGB and and gray images 
 * 
 * @see http://ivrg.epfl.ch/research/superpixels 
 * @see http://rsbweb.nih.gov/ij/
 * 
 * @details Superpixels are becoming increasingly popular for use in computer 
 * vision applications. However, there are few algorithms that output a desired 
 * number of regular, compact superpixels with a low computational overhead. 
 * We introduce a novel algorithm called SLIC (Simple Linear Iterative Clustering) 
 * that clusters pixels in the combined five-dimensional color and image plane 
 * space to efficiently generate compact, nearly uniform superpixels. 
 * The simplicity of our approach makes it extremely easy to use - a lone 
 * parameter specifies the number of superpixels - and the efficiency of 
 * the algorithm makes it very practical. Experiments show that our approach 
 * produces superpixels at a lower computational cost while achieving a 
 * segmentation quality equal to or greater than four state-of-the-art methods, 
 * as measured by boundary recall and under-segmentation error. We also 
 * demonstrate the benefits of our superpixel approach in contrast to existing 
 * methods for two tasks in which superpixels have already been shown to increase 
 * performance over pixel-based methods.
 * 
 * References:
 * 
 * [1] Achanta, Radhakrishna, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine S??sstrunk. 
 * "Slic superpixels." ??cole Polytechnique Federal de Lausssanne (EPFL), Tech. Rep 149300 (2010).
 */
public class jSLIC {

	// clone of original image we work with
	protected ImagePlus image;
	protected int width, height;
	// image converted into LAB colour space - dim int[Width][Height][channels]
	protected int[][][] img2D = null;
	// initial regular grid size
	protected int gridSize;
	// superpixel elasticity in range (0,1)  
	protected float regul;
	// labeling per each image pixel - dim int[Width][Height]
	protected int[][] labels2D = null;
	// protected ShortProcessor labels; // it is 7x slower then int[][]
	// minimal distance according the assigned label 
	protected float[][] distances2D = null;
	// number of estimated segments (labels)
	protected int nbLabels;
	// vector of cluster's colours - dim int[nbClusters][channels]
	protected int[][] clusterColour = null;
	// vector of cluster's positions - dim int[nbClusters][positions]
	protected int[][] clusterPosition = null;
	// stopping treshold value in percent of initial error
	protected float errTreshold = 0.1f;
	// according the VLFeat library the regul is in range {0,1}
	protected float factor;
	// number of channel
	protected int nbChannels = 3;
	// precomputed distances
	protected float[] distGrid = null;

	// TODO - avoiding computations with real numbers
	
	/**
	 * Constructor that sets the input image.
	 * 
	 * @param im is the input ImagePlus
	 */
	public jSLIC (ImagePlus im) {
		// clone image locally
		this.image = im;
		
		initInternalVaribales();		
	}
	
	protected void initInternalVaribales() {
		// image sizes
		this.width = image.getWidth();
		this.height = image.getHeight();
		
		// init other local variables according selected image 
		labels2D = new int[width][height];
		distances2D = new float[width][height];
		
		Logging.logMsg("SLIC: image convert.");
		
		// (width, height, nChannels, nSlices, nFrames)
		int[] dims = image.getDimensions();
		// process according image dimension 2D / 3D
		if (dims[3]==1 && dims[4]==1) {
			convertImage();
		} else {
			IJ.error("ERROR: Not supported image dimensions!");
		}
	}
	
	
	/**
	 * Initialise the 2D space which means initialise the label array and distances
	 */
	protected void convertImage() {

		switch (image.getType()) {
			// convert image from RGB to CIE LAB colour space]
			case ImagePlus.COLOR_RGB:
				// converting RGB image to LAB
				//this.img2D = ConvertImage.rgb2cieLAB(image.getProcessor());
				this.img2D = ConvertImage.rgb2cieLABfast(image.getProcessor());
				//this.nbChannels = 3;
				break;
			// convert the gray images
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32:
				// converting Gray image to the same format as LAB but only one channel
				//this.img = ConvertImage.gray2bright(image.getProcessor());
				//this.nbChannels = 1;
				// converting Gray image to the same format as LAB but only one channel
				this.img2D = ConvertImage.gray2cieLAB(image.getProcessor());
				//this.nbChannels = 3;
				break;
			default:
				Logging.logMsg("ERROR: Not supported colour space!");
				break;
		}
	}
	
	
	/**
	 * Process the whole segmentation process
	 * 
	 * @param grid integer number defining the initial regular grid size
	 * @param reg float defining the superpixel elasticity in range (0,1)  
	 */
	public void process (int grid, float reg) {
		process (grid, reg, 9, 0.1f);
	}
	
	
	/**
	 * Process the whole segmentation process
	 * 
	 * @param grid integer number defining the initial regular grid size
	 * @param reg float defining the superpixel elasticity in range (0,1)  
	 * @param maxIter number of maximal iterations   
	 * @param sizeTrashold says till which size superpixels will by terminated
	 */
	public void process (int grid, float reg, int maxIter, float sizeTrashold) {
		this.gridSize = (grid < 5) ? 5 : grid;
		this.regul = (reg < 0) ? 0 : reg;
		// according the VLFeat library the regul is in range {0,1}
		this.factor = (regul*regul) * (float)(gridSize);
		float err, lastErr = Float.MAX_VALUE;
		long startTime, estimTime;
		
		Logging.logMsg("SLIC: running with gridSize: " + Integer.toString(gridSize) + " regularity " + Float.toString(regul));
		
		initClusters();
		
		float initErr = computeResidualError();
				
		for (int i=0; i<maxIter; i++) {

			startTime = System.currentTimeMillis();
			assignment();

			err = computeResidualError();
			Logging.logMsg("SLIC:  iter " + Integer.toString(i+1) + ", inter. distance is " + Float.toString(err));
			
			update();

			// STOP criterion, if consecutive errors are smaller then given treshold
			if ( (lastErr-err) < (initErr*errTreshold)) {
				Logging.logMsg("SLIC: terminate with diff error " + (lastErr-err));
				i = maxIter;
			} else {
				lastErr = err;
			}
			
			estimTime = System.currentTimeMillis() - startTime;
			Logging.logMsg(" -> took " + Float.toString((float)estimTime/1000) + "s");
						
		}
				
		// At the end of the clustering procedure, some ?orphaned? pixels that 
		// do not belong to the same connected component as their cluster center 
		// may remain. To correct for this, such pixels are assigned the label 
		// of the nearest cluster center using a connected components algorithm.
		startTime = System.currentTimeMillis();
		
		// the original post-processing by authors which relabel by label on top
		Logging.logMsg("SLIC: enforce label connectivity.");
		enforceLabelConnectivity();
		
		estimTime = System.currentTimeMillis() - startTime;
		Logging.logMsg(" -> took " + Float.toString((float)estimTime/1000) + "s");
		
		Logging.logMsg("SLIC: DONE.");
	}
	
	
	/**
	 * Initialisation of all local variables as well as providing initial 
	 * cluster generating values by positions
	 */
	protected void initClusters () {
		// compute needed number of clusters
		int nbClusters = (int) (Math.ceil((float)width/(float)gridSize) * Math.ceil((float)height/(float)gridSize));
		// init arrays
		clusterColour = new int[nbClusters][img2D[0][0].length];
		clusterPosition = new int[nbClusters][2];
		
		// do initial assignment - assign labels by initial regular grid
		int maxColumn = (int) Math.ceil(width / (float)gridSize);
		for (int x=0; x<width; x++ ) {
			for (int y=0; y<height; y++ ) {

				labels2D[x][y] = (int) ((y/gridSize)*maxColumn + (x/gridSize));
				
			}
		}

		update();
		distGrid = null;
		
		// OR - The centers are moved to seed locations corresponding to the 
		// lowest gradient position in a 3 ?????? 3 neighborhood. This is done to 
		// avoid centering a superpixel on an edge, and to reduce the chance 
		// of seeding a superpixel with a noisy pixel.
				
	}

	
	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignment () {
		//assignmentSimple();
		assignmentFast();
	} 
	
	/**
	 * Update the cluster centers for a given assignment (colours and positions)
	 */
	protected void update () {
		updateSimple();
	}
	
	
	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignmentSimple () {
		int xB, xE, yB, yE;
		float dist, dL, dA, dB;
		// temporary variables - differences
		double distLAB, distPos, dx, dy;
		// double dLAB

		// put minimal distances to maximum
		for(float[] subarray : distances2D) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPosition.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(0, (int)(clusterPosition[k][0]-gridSize));
			xE = Math.min((int)(clusterPosition[k][0]+gridSize), width);
			yB = Math.max(0, (int)(clusterPosition[k][1]-gridSize));
			yE = Math.min((int)(clusterPosition[k][1]+gridSize), height);
			
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++ ) {
				for (int y=yB; y<yE; y++ ) {

					// compute distance between given point and cluster center
					dx = x-clusterPosition[k][0];
					dy = y-clusterPosition[k][1];
					// compute position distance
					distPos = (dx*dx) + (dy*dy);
					// compute colour distance over all colour channels
				//	distLAB = 0;
				//	for (int i=0; i<nbChannels; i++) {
				//		dLAB = img2D[x][y][i]-clusterColour[k][i];
				//		distLAB += dLAB*dLAB;
				//	}
					// faster then the for cycle...
					dL = img2D[x][y][0]-clusterColour[k][0];
					dA = img2D[x][y][1]-clusterColour[k][1];
					dB = img2D[x][y][2]-clusterColour[k][2];
					distLAB = (dL * dL) + (dA * dA) + (dB * dB);
										
				//	distLAB = (img[x][y][0]-clusterColour[k][0]) * (img[x][y][0]-clusterColour[k][0]);
				//	if (nbChannels == 3) {
				//		distLAB += (img[x][y][1]-clusterColour[k][1]) * (img[x][y][1]-clusterColour[k][1]);
				//		distLAB += (img[x][y][2]-clusterColour[k][2]) * (img[x][y][2]-clusterColour[k][2]);
				//	} else if (nbChannels == 3) {
				//		// to have in sum similar nb as 3 channels
				//		distLAB += distLAB + distLAB;
				//	}
												
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					dist = (float) (distLAB + (distPos * factor));
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances2D[x][y]) {
						labels2D[x][y] = k;
						distances2D[x][y] = dist;
					}					
				}
			}			
		}
		
	}
	
	protected void computeDistGrid() {
		// if grid is not init
		int sz = 2*gridSize +1;
		if (distGrid == null || distGrid.length != sz) {
			Logging.logMsg(" -> pre-computing the distance grid matrix...");
			// if it is not for actual grid size
			distGrid = new float[sz*sz];
			float dx, dy;
			// fill the array
			for (int x=0; x<sz; x++ ) {
				for (int y=0; y<sz; y++ ) {
					dx = x-gridSize+1;
					dy = y-gridSize+1;
					// compute position distance
					//distGrid[x][y] = ((dx*dx) + (dy*dy))  * factor;
					distGrid[x*sz +y] = ((dx*dx) + (dy*dy))  * factor;
				}
			}
		}
	}

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignmentFast () {
		int xB, xE, yB, yE, i, j;
		int sz = 2*gridSize +1;
		float dist, dL, dA, dB;
		// temporary variables - differences
		float distLAB;
		computeDistGrid();
		Logging.logMsg(" -> fast assignement running...");

		// put minimal distances to maximum
		for(float[] subarray : distances2D) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPosition.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(0, (int)(clusterPosition[k][0]-gridSize));
			xE = Math.min((int)(clusterPosition[k][0]+gridSize), width);
			yB = Math.max(0, (int)(clusterPosition[k][1]-gridSize));
			yE = Math.min((int)(clusterPosition[k][1]+gridSize), height);

			i=clusterPosition[k][0]-xB+gridSize;
								
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++, i-- ) {

				j=clusterPosition[k][1]-yB+gridSize;
				
				//i = (clusterPosition[k][0]-x+gridSize) * gridSize;
				//i += clusterPosition[k][1]-yB+gridSize;
				
				for (int y=yB; y<yE; y++, j-- ) {

					// faster then the for cycle...
					dL = img2D[x][y][0]-clusterColour[k][0];
					dA = img2D[x][y][1]-clusterColour[k][1];
					dB = img2D[x][y][2]-clusterColour[k][2];
					distLAB = (dL * dL) + (dA * dA) + (dB * dB);
					
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					//dist = distLAB + distGrid[i][j];
					dist = distLAB + distGrid[i*sz +j];
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances2D[x][y]) {
						labels2D[x][y] = k;
						distances2D[x][y] = dist;
					}	
				}
			}			
		}
		
	}
	

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignmentParallel () {
		computeDistGrid();
		Logging.logMsg(" -> fast parallel assignement running...");

		// put minimal distances to maximum
		for(float[] subarray : distances2D) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		final ThreadAssignment[] threads = new ThreadAssignment[Threading.nbAvailableThread()];
		int deltaImg = (int) Math.ceil(width / (float)threads.length);
		int endRange;
		
		for (int iThread = 0; iThread < threads.length; iThread++) {
			
			// Concurrently run in as many threads as CPUs  
			threads[iThread] = new ThreadAssignment(img2D, gridSize, distGrid, clusterPosition, clusterColour, distances2D, labels2D);
			// for all regular regions
			// because of a rounding the last has to cover rest of image
			//endRange = (iThread < (threads.length-1)) ? (iThread+1)*deltaImg : width;
			endRange = (iThread+1)*deltaImg;
			threads[iThread].setRangeImg(iThread*deltaImg, endRange, 0, height);
			
		}
		
		Threading.startAndJoin(threads); 
				
	}
	
	/**
	 * Update the cluster centers for a given assignment (colours and positions)
	 */
	protected void updateSimple () {
		int k; // segment index (local)
		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[clusterPosition.length];
		Arrays.fill(nbPixels, 0);
		// reset all previous cluster centers
		for(int[] subarray : clusterColour) {			Arrays.fill(subarray, 0);		}
		for(int[] subarray : clusterPosition) {			Arrays.fill(subarray, 0);		}
		
		// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<width; x++ ) {
			for (int y=0; y<height; y++ ) {
				k = labels2D[x][y];
				// over all image channels
				clusterColour[k][0] += img2D[x][y][0];
				clusterColour[k][1] += img2D[x][y][1];
				clusterColour[k][2] += img2D[x][y][2];
				// over all positions
				clusterPosition[k][0] += x;
				clusterPosition[k][1] += y;
				nbPixels[k] ++;
			}
		}
		
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (k=0; k<clusterPosition.length; k++) {
			if (nbPixels[k] == 0) {		continue;	}
			// over all image channels
			clusterColour[k][0] = clusterColour[k][0] / nbPixels[k];
			clusterColour[k][1] = clusterColour[k][1] / nbPixels[k];
			clusterColour[k][2] = clusterColour[k][2] / nbPixels[k];
			// over all positions
			clusterPosition[k][0] = clusterPosition[k][0] / nbPixels[k];
			clusterPosition[k][1] = clusterPosition[k][1] / nbPixels[k];
		}
	}


	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
//	protected void updateParallel () {
//		Logging.logMsg(" -> fast parallel update running...");
//
//		// reset counting pixels belongs to given clusters
//		int nbPixels[] = new int[clusterPosition.length];
//		Arrays.fill(nbPixels, 0);
//		// reset all previous cluster centers
//		for(int[] subarray : clusterColour) {			Arrays.fill(subarray, 0);		}
//		for(int[] subarray : clusterPosition) {			Arrays.fill(subarray, 0);		}
//		
//		final ThreadUpdate[] threads = new ThreadUpdate[Threading.nbAvailableThread()];
//		int deltaImg = (int) Math.ceil(width / (float)threads.length);
//		int deltaK = (int) Math.ceil(clusterPosition.length / (float)threads.length);
//		int endRange;
//		
//		for (int iThread = 0; iThread < threads.length; iThread++) {
//			
//			// Concurrently run in as many threads as CPUs  
//			threads[iThread] = new ThreadUpdate(img2D, gridSize, clusterPosition, clusterColour, labels2D, nbPixels);
//			// for all regular regions
//			// because of a rounding the last has to cover rest of image
//			//endRange = (iThread < (threads.length-1)) ? (iThread+1)*deltaImg : width;
//			endRange = (iThread+1)*deltaImg;
//			threads[iThread].setRangeImg(iThread*deltaImg, endRange, 0, height);
//			// for all regular regions
//			//endRange = (iThread < (threads.length-1)) ? (iThread+1)*deltaK : clusterPosition.length;
//			endRange = (iThread+1)*deltaK;
//			threads[iThread].setRange(iThread*deltaK, endRange);
//			
//		}
//		
//		Threading.startAndJoin(threads); 
//				
//	}

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void updateParallel () {
		Logging.logMsg(" -> fast parallel update running...");

		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[clusterPosition.length];
		Arrays.fill(nbPixels, 0);
		// reset all previous cluster centers
		for(int[] subarray : clusterColour) {			Arrays.fill(subarray, 0);		}
		for(int[] subarray : clusterPosition) {			Arrays.fill(subarray, 0);		}
		
		final ThreadUpdate[] threads = new ThreadUpdate[Threading.nbAvailableThread()];
		int deltaImg = (int) Math.ceil(width / (float)threads.length);
		//int deltaK = (int) Math.ceil(clusterPosition.length / (float)threads.length);
		int endRange;
		
		for (int iThread = 0; iThread < threads.length; iThread++) {
			
			// Concurrently run in as many threads as CPUs  
			threads[iThread] = new ThreadUpdate(img2D, gridSize, clusterPosition, clusterColour, labels2D, nbPixels);
			// for all regular regions
			// because of a rounding the last has to cover rest of image
			//endRange = (iThread < (threads.length-1)) ? (iThread+1)*deltaImg : width;
			endRange = (iThread+1)*deltaImg;
			threads[iThread].setRangeImg(iThread*deltaImg, endRange, 0, height);
			// for all regular regions
			//endRange = (iThread < (threads.length-1)) ? (iThread+1)*deltaK : clusterPosition.length;
			//endRange = (iThread+1)*deltaK;
			//threads[iThread].setRange(iThread*deltaK, endRange);
		}
		
		Threading.startAndJoin(threads); 
		
		int nb, cL, cA, cB, X, Y;
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (int k=0; k<clusterPosition.length; k++) {
			// init
			nb=0; cL=0; cA=0; cB=0; X=0; Y=0;
			// sum over threads
			for (int i = 0; i < threads.length; i++) {
				nb += threads[i].getNbPixels()[k];
				// over all image channels
				cL += threads[i].getClusterColours()[k][0];
				cA += threads[i].getClusterColours()[k][1];
				cB += threads[i].getClusterColours()[k][2];
				// over all positions
				X += threads[i].getClusterPositions()[k][0];
				Y += threads[i].getClusterPositions()[k][1];
			}
			if (nb == 0) {		continue;	}
			nbPixels[k] = nb;
			// over all image channels
			clusterColour[k][0] = cL / nb;
			clusterColour[k][1] = cA / nb;
			clusterColour[k][2] = cB / nb;
			// over all positions
			clusterPosition[k][0] = X / nb;
			clusterPosition[k][1] = Y / nb;
		}
				
	}

	
	/**
	 * Count residual distance to nearest clusters by given metric
	 * 
	 * @return float returns a sum over all distances to nearest cluster
	 */
	protected float computeResidualError () {
		// error metric
		float err = 0;

		// cycle over all distances
		for (int x=0; x<width; x++ ) {
			for (int y=0; y<height; y++ ) {
				err += distances2D[x][y];
			}
		}
		return err;
	}
	

	/**
	 * Enforce Label Connectivity - Modified original code
	 * At the end of the clustering procedure, some ?orphaned? pixels that do 
	 * not belong to the same connected component as their cluster center may 
	 * remain. To correct for this, such pixels are assigned the label of the 
	 * nearest cluster center using a connected components algorithm.
	 * 
	 * 1. finding an adjacent label for each new component at the start
	 * 2. if a certain component is too small, assigning the previously found
	 *    adjacent label to this component, and not incrementing the label.
	 */
	protected void enforceLabelConnectivity() {
	
		// 4-connectivity
		final int[] dx = {-1,  0,  1,  0};
		final int[] dy = { 0, -1,  0,  1};

		// image size
		int sz = width*height;
		// area of initial superpixel
		int SUPSZ = gridSize*gridSize;
		// create new array of labels and fill by -1
		int[][] nlabels = new int[width][height];
		for(int[] subarray : nlabels) { Arrays.fill(subarray, -1); }
		// coordinates to run in the image
		int x, y;
		int lab = 0;
		int adjlabel = 0; //adjacent label
		// array of coordinates for all elements in the actual segment
		int[] xvec = new int[sz];
		int[] yvec = new int[sz];
        int count;
		
        // cycle over all pixels in image
		for( int j = 0; j < height; j++ ) {
			for( int i = 0; i < width; i++ ) {
				
				if( nlabels[i][j] > -1) { 	continue; 	}
				
				nlabels[i][j] = lab;
				// Start a new segment
				xvec[0] = i;
				yvec[0] = j;
				// Quickly find an adjacent label for use later if needed
				for( int n = 0; n < dx.length; n++ ) {
					x = xvec[0] + dx[n];
					y = yvec[0] + dy[n];
					if( (x >= 0 && x < width) && (y >= 0 && y < height) ) {
						if(nlabels[x][y] >= 0) {
							adjlabel = nlabels[x][y];
						}
					}
				}

				count = 1; // segment size
				// region growing method and storing pixels belongs to segment
				for( int c = 0; c < count; c++ ) {
					for( int n = 0; n < dx.length; n++ ) {
						x = xvec[c] + dx[n];
						y = yvec[c] + dy[n];
						// conditions if it is still the same segment
						if( (x >= 0 && x < width) && (y >= 0 && y < height) ) {
							if( 0 > nlabels[x][y] && labels2D[i][j] == labels2D[x][y] ) {
								xvec[count] = x;
								yvec[count] = y;
								nlabels[x][y] = lab;
								count++;
							}
						}
					}
				}
				// If segment size is less then a limit, assign an
				// adjacent label found before, and decrement label count.
				// shift by 2, which means that it reduces segments 4times smaller
				if(count <= SUPSZ >> 2) {
					for( int c = 0; c < count; c++ ) {
						nlabels[xvec[c]][yvec[c]] = adjlabel;
					}
					lab--;
				}
				lab++;
			}
		}
		this.labels2D = nlabels;
		this.nbLabels = lab;
	}
	
	
	/**
	 * gives segmentation with segmented indexes
	 * 
	 * @return int[Width][Height] returns indexes of segmented superpixels
	 */
	public Labelling2D getSegmentation() {
		return new Labelling2D(labels2D);
	}	
	
	
	/**
	 * gives the number of all various labels in segmentation, where the 
	 * max labels are {0,..,(n-1)}
	 * 
	 * @return int number of labels
	 */
	public int getNbLabels() {
		return this.nbLabels;
	}
	
	
	/**
	 * get the converted image in LAB colour space in case of RGB otherwise 
	 * only gray intensity values
	 * 
	 * @return int[Width][Height][channels]
	 */
	public int[][][] getImage() {
		return this.img2D;
	}
	
}

