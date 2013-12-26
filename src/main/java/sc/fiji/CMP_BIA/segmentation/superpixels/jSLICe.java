/**
 * @file
 */

package sc.fiji.CMP_BIA.segmentation.superpixels;

import java.util.ArrayList;
import java.util.Arrays;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.tools.Connectivity2D;
import sc.fiji.CMP_BIA.tools.Logging;
import ij.ImagePlus;

/**
 * @class SLIC superpixels
 * @version 0.1
 * @date 10/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief This is derivation of SLIC superpixel in 3D for RGB and gray images
 * with several proposed approximations and speedups such as multi-threading, etc. 
 * 
 * @see http://ivrg.epfl.ch/research/superpixels 
 * @see http://rsbweb.nih.gov/ij 
 * 
 */
public class jSLICe extends jSLIC {
		
	// TODO - extension for 3D
	
	/**
	 * Constructor that sets the input image.
	 * @param im is the input ImagePlus
	 */
	public jSLICe(ImagePlus im) {
		super(im);
	}
	

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignment () {
		//assignmentSimple();
		//assignmentFast();
		assignmentParallel();
	} 
	
	/**
	 * Update the cluster centers for a given assignment (colours and positions)
	 */
	protected void update () {
		//updateSimple();
		updateParallel();
	}
	
	/**
	 * counting the colour means over superpixels
	 * 
	 * @param lb is initial labelling
	 * @return int[nbLabels][3] colour means in CEIlab
	 */
	protected int[][] computeColourMeans(Labelling2D lb) {
		int l;
		// colour means
		int[][] means = new int[lb.getMaxLabel()+1][3];
		// put minimal distances to maximum
		for(int[] subarray : means) {
			Arrays.fill(subarray, 0);
		}
		// counting
		int[] counts = new int[lb.getMaxLabel()+1];
		Arrays.fill(counts, 0);
		
		// summing
		for (int i = 0; i < lb.getWidth(); i++) {
			for (int j = 0; j < lb.getHeight(); j++) {
				l = lb.getLabel(i, j);
				means[l][0] += img2D[i][j][0];
				means[l][1] += img2D[i][j][1];
				means[l][2] += img2D[i][j][2];
				counts[l] ++;
			}
		}
		
		// norming
		for (int i = 0; i < means.length; i++) {
			means[i][0] /= counts[i];
			means[i][1] /= counts[i];
			means[i][2] /= counts[i];
		}
		
		return means;
	}
	
	/**
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	protected float clrDiff(int[] c1, int[] c2) {
		float dL = (float)(c2[0]-c1[0]);
		float dA = (float)(c2[1]-c1[1]);
		float dB = (float)(c2[2]-c1[2]);
		float diff = (dL*dL) + (dA*dA) + (dB*dB);
		diff = (float) Math.sqrt(diff);
		return diff;
	}
		
	/**
	 * 
	 * @param sizeTrashold
	 */
	@Override
	protected void enforceLabelConnectivity(){
		Logging.logMsg(" -> running...");
		// split all disconnected components
		Labelling2D lb = new Labelling2D( Connectivity2D.enforceIndividualRegions(labels2D, Connectivity2D.CONNECT4) );
		nbLabels = lb.getMaxLabel()+1;
		
		// find the interconnectivity among segments
		int mSize = 4*gridSize*gridSize;
		int[] hist = lb.getLabelHist();
		ArrayList<ArrayList<Integer>> connect = Connectivity2D.findSegmetNeighbors(lb.getData(), lb.getMaxLabel(), Connectivity2D.CONNECT4);
		
		// compute the colour means of all segments
		int[][] clrMeans = computeColourMeans(lb);
		
		int similarID;
		float similarVal, rltSize;
		// init LUT
		int[] lut = lb.getLUT();
		// over all segments
		for (int i = 0; i < lut.length; i++) {
			
			// find closest colour
			similarID = -1;
			similarVal = Float.MAX_VALUE;
			float diff;
			for (int j = 0; j<connect.get(i).size(); j++) {
				// compute diff
				rltSize = (float)hist[ lut[connect.get(i).get(j)] ] / (float)mSize;
				diff = clrDiff(clrMeans[i], clrMeans[ lut[connect.get(i).get(j)] ] ) / rltSize;
				//diff = clrDiff(clrMeans[i], clrMeans[ lut[connect.get(i).get(j)] ] );
				// find min
				if (diff < similarVal) {
					similarID = connect.get(i).get(j);
					similarVal = diff;
				}
			}

			//System.out.print(Integer.toString(similarID)+" / "+Integer.toString(connect.get(i).size()));
			
			rltSize = (float)hist[i] / (float)mSize;
			//if ( (rltSize*rltSize*Math.log(Math.E+similarVal)) < 1. ) {
			if ( (rltSize*rltSize*(1+similarVal)) < 0.25 ) {  // BEST
			//if ( (rltSize*rltSize*(1+similarVal)*regul) < 1. ) {
			//if ( (rltSize*rltSize*Math.log(Math.E+similarVal)/regul) < 0.05 ) {
			//if (hist[i]>0  &&  hist[i] < (mSize/16)) {

				//System.out.println("  size: "+Float.toString(rltSize)+" clr diff: "+Float.toString(similarVal));
				//System.out.println("  relabel("+Integer.toString(i)+"): "+Integer.toString(lut[i])+" -> "+Integer.toString(lut[connect.get(i).get(similarID)])+" ("+Integer.toString(lut[connect.get(i).get(0)])+")");

				lut[i] = lut[ similarID ];
				//lut[i] = lut[  connect.get(i).get(similarID) ];
				//lut[i] = lut[ connect.get(i).get(0) ];
				
				if (i != lut[i]) {
					updateClrMeansAndHist(clrMeans, hist, i, lut[i]);
				}
				
				for (int j = 0; j < i; j++) {
					if (lut[j] == i) {
						lut[j] = lut[i];
						if (lut[j] != lut[i]) {
							updateClrMeansAndHist(clrMeans, hist, lut[j], lut[i]);
						}
					}
				}
			}
		}
		
		// relabelling
		lb.reLabel(lut);
		
		labels2D = lb.getData();
		
		//super.enforceLabelConnectivity();

		Logging.logMsg(" -> done.");
	}

	/**
	 * @param clrMeans
	 * @param hist
	 * @param i
	 * @param iNew
	 */
	protected void updateClrMeansAndHist(int[][] clrMeans, int[] hist, int i, int iNew) {
		// update colours - sum
		clrMeans[iNew][0] = clrMeans[iNew][0]*hist[iNew] + clrMeans[i][0]*hist[i]*hist[i];
		clrMeans[iNew][1] = clrMeans[iNew][1]*hist[iNew] + clrMeans[i][1]*hist[i]*hist[i];
		clrMeans[iNew][2] = clrMeans[iNew][2]*hist[iNew] + clrMeans[i][2]*hist[i]*hist[i];
		
		// update hist
		hist[iNew] += hist[i];
		hist[i] = 0;
		
		// update colours - norm
		clrMeans[iNew][0] /= hist[iNew];
		clrMeans[iNew][1] /= hist[iNew];
		clrMeans[iNew][2] /= hist[iNew];
	}
			
}

/**
 * 
 * @author JB
 *
 */
abstract class ThreadParticularImg2D extends Thread {
	// source image
	protected int[][][] img = null;
	// cluster centres
    protected int[][] clusterPositions = null;
    protected int[][] clusterColours = null;
    // labelling
    protected int[][] labels = null;
    // grid size
    protected int gridSize;
    // set range
    protected int[] rangeWidth, rangeHeight;
    	
    { setPriority(Thread.NORM_PRIORITY); }  
                	
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param lab - given labelling
     * @param dGrid - recomputed grid
     */
    public ThreadParticularImg2D(int[][][] im, int[][] cPos, int[][] cClr, int[][] lab, final int gSize) {
		img = im;
		clusterPositions = cPos;
		clusterColours = cClr;
		labels = lab;
    	gridSize = gSize;
	}
    
    /**
     * setting the particular rectangle in image to be processed
     * 
     * @param sW - start in width dim
     * @param eW - end in width dim
     * @param sH - start in height dim
     * @param eH - end in height dim
     */
    public void setRangeImg(final int bW, final int eW, final int bH, final int eH) {
    	rangeWidth = new int[2];
    	rangeWidth[0] = (bW>=0) ? bW : 0;
    	rangeWidth[1] = (eW<img.length) ? eW : img.length;
    	rangeHeight = new int[2];
    	rangeHeight[0] = (bH>=0) ? bH : 0;
    	rangeHeight[1] = (eH<img[0].length) ? eH : img[0].length;
	}
}

/**
 * The particular thread for assignment in given region
 * @author JB
 *
 */
class ThreadAssignment extends ThreadParticularImg2D {  
    // precomputed distances
    protected float[] distGrid = null;
    // estimated distances
    protected float[][] distances = null;
    		
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param gSize - grid size
     * @param dGrid - recomputed grid
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param dist - the internal distances
     * @param lab - given labelling
     */
    public ThreadAssignment(final int[][][] im, final int gSize, final float[] dGrid, final int[][] cPos, final int[][] cClr, final float[][] dist, int[][] lab) {
		super(im, cPos, cClr, lab, gSize);
		distGrid = dGrid;
		distances = dist;
	}
    
    /**
     * the main body of the thread
     */
    @Override
    public void run() {  
    	// init
    	int xB, xE, yB, yE, i;
		float dist, dL, dA, dB;
		int sz = 2*gridSize +1;
		// temporary variables - differences
		float distLAB;
		            	
    	// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPositions.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(rangeHeight[0], (int)(clusterPositions[k][0]-gridSize));
			xE = Math.min((int)(clusterPositions[k][0]+gridSize), rangeWidth[1]);
			yB = Math.max(rangeHeight[0], (int)(clusterPositions[k][1]-gridSize));
			yE = Math.min((int)(clusterPositions[k][1]+gridSize), rangeHeight[1]);

			//i=clusterPosition[k][0]-xB+gridSize;
			
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++ ) {

				//j=clusterPosition[k][1]-yB+gridSize;
				
				i = (clusterPositions[k][0]-x+gridSize) * sz;
				i += clusterPositions[k][1]-yB+gridSize;
				
				for (int y=yB; y<yE; y++, i-- ) {

					// faster then the for cycle...
					dL = img[x][y][0]-clusterColours[k][0];
					dA = img[x][y][1]-clusterColours[k][1];
					dB = img[x][y][2]-clusterColours[k][2];
					distLAB = (dL * dL) + (dA * dA) + (dB * dB);
					
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					//dist = distLAB + distGrid[i][j];
					dist = distLAB + distGrid[i];
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances[x][y]) {
						labels[x][y] = k;
						distances[x][y] = dist;
					}
				}
			}			
		}
    	
    }
}

/**
 * The particular thread for update in given region
 * @author JB
 */
class ThreadUpdate extends ThreadParticularImg2D {
	// number per cluster
	protected int[] nbPixels = null;
    // set range
    protected int beginK, endK;
	
	
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param lab - given labelling
     */
    public ThreadUpdate(final int[][][] im, final int gSize, int[][] cPos, int[][] cClr, final int[][] lab, int[] nbPx) {
		super(im, cPos, cClr, lab, gSize);
    	nbPixels = nbPx;
	}
    
    public void setRange(final int start, final int stop) {
    	beginK = (start>=0) ? start : 0;
    	endK = (stop<nbPixels.length) ? stop : nbPixels.length;
	}

//    @Override
//    public void run() {
//		int k, nb;
//    	// cycle over whole image and by labels add current value to given cluster center
//		for (int x=0; x<img.length; x++ ) {
//			for (int y=0; y<img[0].length; y++ ) {
//				// only for selected K in range
//				if (labels[x][y] >= beginK && labels[x][y] < endK) {	
//					// save the k
//					k = labels[x][y];
//					// over all image channels
//					clusterColours[k][0] += img[x][y][0];
//					clusterColours[k][1] += img[x][y][1];
//					clusterColours[k][2] += img[x][y][2];
//					// over all positions
//					clusterPositions[k][0] += x;
//					clusterPositions[k][1] += y;
//					nbPixels[k] ++;
//				}
//			}
//		}
//		
//		// cycle over all clusters and divide them by nb assigned pixels (get mean)
//		for (k=beginK; k<endK; k++) {
//			if (nbPixels[k] == 0) {		continue;	}
//			nb = nbPixels[k];
//			// over all image channels
//			clusterColours[k][0] = clusterColours[k][0] / nb;
//			clusterColours[k][1] = clusterColours[k][1] / nb;
//			clusterColours[k][2] = clusterColours[k][2] / nb;
//			// over all positions
//			clusterPositions[k][0] = clusterPositions[k][0] / nb;
//			clusterPositions[k][1] = clusterPositions[k][1] / nb;
//		}
//    	
//    }
    
    @Override
    public void run() {
		nbPixels = new int[nbPixels.length];
		Arrays.fill(nbPixels, 0);
		clusterColours = new int[nbPixels.length][3];
		for(int[] subarray : clusterColours) {			Arrays.fill(subarray, 0);		}
		clusterPositions = new int[nbPixels.length][2];
		for(int[] subarray : clusterPositions) {			Arrays.fill(subarray, 0);		}
		int k;    	
   		// cycle over all pixels in region
		for (int x=rangeWidth[0]; x<rangeWidth[1]; x++ ) {
			for (int y=rangeHeight[0]; y<rangeHeight[1]; y++) {
				k = labels[x][y];
				// over all image channels
				clusterColours[k][0] += img[x][y][0];
				clusterColours[k][1] += img[x][y][1];
				clusterColours[k][2] += img[x][y][2];
				// over all positions
				clusterPositions[k][0] += x;
				clusterPositions[k][1] += y;
				nbPixels[k] ++;
			}
		}	    	
    }
    
    public int[][] getClusterColours() {
		return clusterColours;
	}
    
    public int[][] getClusterPositions() {
		return clusterPositions;
	}
    
    public int[] getNbPixels() {
		return nbPixels;
	}
	
}