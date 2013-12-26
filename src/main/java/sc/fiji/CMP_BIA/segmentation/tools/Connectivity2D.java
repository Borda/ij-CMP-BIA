/**
 * @file
 */
package sc.fiji.CMP_BIA.segmentation.tools;


import java.util.ArrayList;
import java.util.Arrays;



/**
 * @class Connected-component labeling
 * @version 0.1
 * @date 25/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief This class compute the connected components on multi-class segmentation
 * and it uses the region growing technique to estimate independent regions.
 */
abstract public class Connectivity2D {

	/**
	 * Estimation of connected components for multi-class segmentation on 2D 
	 * using the grow region connectivity and the neighbourhood is defined 
	 * by array of position of neighbouring pixels 
	 * 
	 * @param segm matrix int[width][height] of initial labeling
	 * @param neighborCoords is type of connectivity, assume 4 or 8
	 * @return int[width][height] is new relabeled segmentation
	 */
	public static int[][] enforceIndividualRegions(final int[][] segm, final int[][] neighborCoords) {
		// segmentation sizes
		int width = segm.length;
		int height = segm[0].length;
		// create and reset new labeling
		int[][] segmNew = new int[width][height];
		for(int[] subarray : segmNew) {	Arrays.fill(subarray, -1);	}
		// list of positions that has to be verify 
		ArrayList<int[]> positions = new ArrayList<int[]>();
		// create counter for segments
		//ArrayList<Integer> segSizes = new ArrayList<Integer>();
		// create label history of new segments
		//ArrayList<Integer> segOLabel = new ArrayList<Integer>();
		// label of actual pixel [i,j] in original image
		int origLabel;
		// iterator of assigned labels
		int labelIter = 0;
		// actual position in image
		int x, y;
		// counting assigned pixel to a region
		//int count;
		
		// go over all pixels in image
		for( int j = 0; j < height; j++ ) { // Height
			for( int i = 0; i < width; i++ ) { // Width
				
				// if the label this pixel has assign label, skip it
				if (segmNew[i][j] > -1) {	continue;	}
				
				// assign label in the original multi-class segmentation
				origLabel = segm[i][j];
				// set initial position
				positions.add( new int[]{i,j} );
				// assign new label - label of following region
				segmNew[i][j] = labelIter;
				
				// count pixels in this region
				//count = 1;
				
				// go over all stored position (at least the previous position i,j)
				//for (int[] pos : positions) {
				for (int p = 0; p < positions.size(); p++) {
					
					int[] pos = positions.get(p);
					
					// go over all neighboring pixels
					for( int n = 0; n < neighborCoords.length; n++ ) {
						// compute position of the neighbor pixel
						x = pos[0] + neighborCoords[n][0];
						y = pos[1] + neighborCoords[n][1];
						
						// check if it is still inside image
						if( (x >= 0 && x < width) && (y >= 0 && y < height) ) {
							// if the pixel has the same label in original labeling as this region 
							// and in new labeling it still does not have a label (to avoid visiting some labels several times)
							if (segm[x][y]==origLabel && segmNew[x][y]==-1) {
								// assign new label - label of following region
								segmNew[x][y] = labelIter;
								// continue with this position
								positions.add( new int[]{x,y} ); 
								// increment nb pixels
								//count ++;
							}							
						}
					}
					
				}
				// after labeling previous region  increase actual iterator
				labelIter ++;
				// store count and original label
				//segSizes.add(count);
				//segOLabel.add(origLabel);
				// reset list of positions
				positions.clear();
			}
		}

		return segmNew;
	}

	
	/**
	 * It goes over all pixels and by defined connectivity finds all neighbouring 
	 * segments in whole segmentation and return their indexes
	 * 
	 * @param labels is the initial labelling of size int[Width][Height]
	 * @param neighbors defines relative position of neighbouring pixels 
	 * of size int[connect][2]
	 * @param nbLabels is integer number of all segments in segmentation
	 * @return ArrayList<ArrayList<Integer>> is a matrix neighbours to each 
	 * segment of size nbSegments*nbNeighbors
	 */
	public static ArrayList<ArrayList<Integer>> findSegmetNeighbors(final int[][] labels, final int nbLabels, final int[][] neighbors) {
		// neighbors to each segment
		ArrayList<ArrayList<Integer>> segmNeighbors = new ArrayList<ArrayList<Integer>>();
		for (int i=0; i<=nbLabels; i++) {
			segmNeighbors.add(i, new ArrayList<Integer>() );
		}
		// actual position in image
		int x, y;
		// segmentation sizes
		int width = labels.length;
		int height = labels[0].length;
				
		// go over all pixels in image without booundaries
		for( int i = 0; i < width; i++ ) {
			for( int j = 0; j < height; j++ ) {
				// go over all neighboring pixels
				for( int n = 0; n < neighbors.length; n++ ) {
					// compute position of the neighbor pixel
					x = i + neighbors[n][0];
					y = j + neighbors[n][1];
					
					// check if it is still inside image
					if( (x >= 0 && x < width) && (y >= 0 && y < height) ) {
						// if the label is different
						if (labels[i][j] != labels[x][y]) {
							// if the neighboring label is not in list, add it
							if (! segmNeighbors.get(labels[i][j]).contains(labels[x][y])) {
								segmNeighbors.get(labels[i][j]).add(labels[x][y]);
							}
						}
					}
				}
			}
		}
		
		return segmNeighbors;
	}
	
	/**
	 * It goes over all pixels and finds all points where are 3 and more 
	 * different classes in  defined connectivity
	 * 
	 * @param labels is the initial labelling of size int[Width][Height]
	 * @param neighbors defines relative position of neighbouring pixels 
	 * of size int[connect][2]
	 * @return ArrayList<int[2]> is list of all boundary points
	 */
	public static ArrayList<int[]> findBoundaryPoints(final int[][] labels, final int[][] neighbors) {
		// init key points
		ArrayList<int[]> boundaryPoints = new ArrayList<int[]>();
		// temporary list of unique labels
		ArrayList<Integer> tmpLb = new ArrayList<Integer>();
		// counting similar pixels
		int count;
				
		// go over all pixels in image without boundaries
		for( int i = 1; i < labels.length-1; i++ ) {
			for( int j = 1; j < labels[0].length-1; j++ ) {
				// clean the unique labels
				tmpLb.clear();
				tmpLb.add(labels[i][j]);
				// go over all neighbouring pixels
				for( int n = 0; n < neighbors.length; n++ ) {
					// check if it is the unique label
					if (! tmpLb.contains(labels[ i+neighbors[n][0] ][ j+neighbors[n][1] ])) {
						tmpLb.add(labels[ i+neighbors[n][0] ][ j+neighbors[n][1] ]);
					}
				}
				
				// if in neighbourhood are 3 and more different labels
				if (tmpLb.size() >= 3) {
					count = 0;
					// go over all connectivity neighbours
					for( int n = 0; n < neighbors.length; n++ ) {
						// go over all already added points
						for (int m=0; m<boundaryPoints.size(); m++) {
							// check it there is is neighbouring pixel defined by connectivity
							if (boundaryPoints.get(m)[0]==i+neighbors[n][0] && boundaryPoints.get(m)[1]==j+neighbors[n][1]) {
								count ++;
							}
						}
					}
					// if the neighbouring label is not in list, add it
					if (count == 0) {
						boundaryPoints.add( new int[]{i,j} );
					}
				}
			}
		}
		
		return boundaryPoints;
	}
	
	/**
	 * returns coordinates of neighbouring points belonging to the boundaries 
	 * among different labels in given segmentation, it assume that each segment 
	 * can be enclosed by a single close curve and also each segment in the whole 
	 * segmentation has unique label (index)
	 * Note: there is no order in these boundary points
	 * Note2: the chosen connectivity is 8
	 * 
	 * @param neighborhood is matrix int[nbPints][nbDims] describing the 
	 * connectivity around one pixel in given regular grid
	 * @param nbLabels is number of all labelles in segmentation
	 */
	public static ArrayList<ArrayList<int[]>> segmentBoundaries(final int[][] labels, final int nbLabels) {
		// init...
		ArrayList<ArrayList<int[]>> boundaryCoords = new ArrayList<ArrayList<int[]>>();
		for (int i=0; i<nbLabels; i++) {
			boundaryCoords.add(i, null );
		}

		// segmentation sizes
		int width = labels.length;
		int height = labels[0].length;
		final int[][] neighbors = CONNECT8;
		boolean bound;
		int label, count, countMax = width*height;
		int k, c, x, y, xT, yT;
		
		// go over all pixels in distance 1 from image boundaries
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {

				// if this element was not explored
				if (boundaryCoords.get(labels[i][j]) == null) {
					// init the array of coords
					x = i;
					y = j;
					label = labels[x][y];
					count = 0;
					// init boundary segment and add first element
					boundaryCoords.set(label, new ArrayList<int[]>() );
					//boundaryCoords.get(label).add( new int[]{x, y} );
					// direction of last added element
					k = 0;
					// exploring the boundary of the segment by pixel
					do {
						bound = false;
						// over all defined neighbours starting from  previous direction
						for (c=0; c<neighbors.length+1; c++) {
							count ++;
							// get index in bounds of the array
							k = ++k % neighbors.length;
							// temporary coordinates
							xT = x+neighbors[k][0];
							yT = y+neighbors[k][1];
							
							// check if it is inside image
							if (xT<0 || xT>=width || yT<0 || yT>=height) {
								bound = true;
							// if this is not the label set outside the segm.
							} else if (label != labels[ xT ][ yT ]) {
								bound = true;
							// if this is the first point inside the segment
							} else if (bound==true && label==labels[xT][yT]) {
								// add boundary point
								boundaryCoords.get(label).add( new int[]{x, y} );
								x = xT;
								y = yT;
								// next time star in following direction -4
								k += neighbors.length -4;
								break;
							}
						}
					
					// until you come to the first point 
					} while ((x!=boundaryCoords.get(label).get(0)[0] || y!=boundaryCoords.get(label).get(0)[1]) && count<countMax);
										
					// add the initial point again
					boundaryCoords.get(label).add( new int[]{x, y} );
				}
			}
		}
		
		return boundaryCoords;
	}
	
	/**
	 * Simplify the polygon such that it remove all redundant vertexes
	 * 
	 * @param bounds is ArrayList<ArrayList<int[]>> of vertexes to be reduced
	 */
	public static void simplifyPolygon(ArrayList<ArrayList<int[]>> bounds) {
		// directions
		int dx, dy, dxNew, dyNew;
		// go over all elements - boundaries
		for (int i = 0; i < bounds.size(); i++) {
			// if the boundary is not empty list
			if (bounds.get(i) != null) {
				// initial direction
				dx = bounds.get(i).get(1)[0] - bounds.get(i).get(0)[0];
				dy = bounds.get(i).get(1)[1] - bounds.get(i).get(0)[1];
				// go over rest of the polygon
				for (int j = 2; j < bounds.get(i).size(); j++) {
					// compute new direction
					dxNew = bounds.get(i).get(j)[0] - bounds.get(i).get(j-1)[0];
					dyNew = bounds.get(i).get(j)[1] - bounds.get(i).get(j-1)[1];
					// if the direction is equal
					if (dx==dxNew && dy==dyNew) {
						bounds.get(i).remove(j-1);
						j--;
					}
					// copy new direction
					dx = dxNew;
					dy = dyNew;
				}
			}
		}
	}
	
	/**
	 * returns coordinates of all points belonging to the boundaries among 
	 * different labels in given segmentation, all image boundaries are 
	 * automatically considered also as segments boundaries
	 * Note: there is no order in these boundary points
	 * 
	 * @param neighborhood is matrix int[nbPints][nbDims] describing the 
	 * connectivity around one pixel in given regular grid
	 * @param nbLabels is number of all labelles in segmentation
	 * @return int[nbPixels][nbDims] coordinates of the bordering points
	 */
	public static ArrayList<ArrayList<int[]>> segmentBoundariesRaw(final int[][] labels, final int nbLabels, final int[][] neighbors) {
		ArrayList<ArrayList<int[]>> boundaryCoords = new ArrayList<ArrayList<int[]>>();
		for (int i=0; i<nbLabels; i++) {
			boundaryCoords.add(i, new ArrayList<int[]>() );
		}
		// segmentation sizes
		int width = labels.length;
		int height = labels[0].length;
		int k;

		// add just boundaries because all image boundaries has to be segment boundaries
		// parallel first and last row
		for ( int i = 0; i < width; i++ ) {
			boundaryCoords.get(labels[i][0]).add(new int[]{i,0});
			boundaryCoords.get(labels[i][height-1]).add(new int[]{i,height-1});
		}
		// parallel first and last column
		for ( int i = 1; i < height-1; i++ ) {
			boundaryCoords.get(labels[0][i]).add(new int[]{0,i});
			boundaryCoords.get(labels[width-1][i]).add(new int[]{width-1,i});
		}
		
		// go over all pixels in distance 1 from image boundaries which means 
		// that segm[0][0], segm[0][end], segm[end][0], segm[end][end] may 
		// not be not covered in case of 4-neighborhood
		for (int i=1; i<width-1; i++) {
			for (int j=1; j<height-1; j++) {
				// over all defined neighbors
				for (k=0; k<neighbors.length; k++) {
					if (labels[i][j] != labels[ i+neighbors[k][0] ][ j+neighbors[k][1] ]) {
						boundaryCoords.get(labels[i][j]).add( new int[]{i, j} );
						break;
					}
				}
			}
		}
		
		return boundaryCoords;
	}
	
	
	
	/**
	 * static parameterization of 4-neighbor connectivity
	 * gives the coordinates on 2D grid
	 */
	public static final int[][] CONNECT4 = {{-1,0},{0,-1},{1,0},{0,1}};
	// public static int[][] CONNECT4 = {{-1,0,1,0},{0,-1,0,1}}
	
	/**
	 * static parameterization of 8-neighbor connectivity 
	 * gives the coordinates on 2D grid
	 */
	public static final int[][] CONNECT8 = {{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1},{1,0},{1,1},{0,1}};
	// public static int[][] CONNECT8 = {{-1,-1,-1,0,1,1,1,0},{1,0,-1,-1,-1,0,1,1}}

}
