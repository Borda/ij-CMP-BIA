/**
 * 
 */
package sc.fiji.CMP_BIA.segmentation.structures;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import sc.fiji.CMP_BIA.segmentation.tools.Connectivity2D;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.converters.ConvertStructure;

/**
 * @class Labelling 2D
 * @version 0.1
 * @date 18/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief Derivation of an abstract class for Segmentation representation. 
 * This particular child handl only 2D images segmentations.
 * 
 */
public class Labelling2D extends Labelling {
	// labelling
	private int[][] data = null;

	/**
	 * Construct empty labelling of given size w x h
	 * 
	 * @param w int width of new segmentation
	 * @param h int height of new segmentation
	 */
	public Labelling2D(int w, int h) {
		// rewrite data dimensions
		dims = new int[2];
		dims[0] = w;
		dims[1] = h;
		
		// init data array
		data = new int[dims[0]][dims[1]];
		for(int[] subarray : data) {   Arrays.fill(subarray, 0);   }
	}
	
	/**
	 * Constructor
	 * 
	 * @param segm is new labelling matrix of int[width][height]
	 */
	public Labelling2D(int[][] segm) {
		resetSegm(segm);
	}
	
	/**
	 * Reset the segmentation such that it copy new labelling and recompute 
	 * the histogram
	 * 
	 * @param segm is new labelling matrix of int[width][height]
	 */
	public void resetSegm(int[][] segm) {
		copyData(segm);
		computeHistogram();
	}

	/**
	 * Copy the data from input matrix to local representation and also update 
	 * the maximal label according new labelling
	 * 
	 * @param d is a new matrix of int[width][height]
	 */
	protected void copyData(int[][] d) {
		// rewrite data dimensions
		dims = new int[2];
		dims[0] = d.length;
		dims[1] = d[0].length;
		
		// init data array
		data = new int[dims[0]][dims[1]];
		
		// copy data
		for (int i=0; i<d.length; i++) {
			for (int j=0; j<d[i].length; j++) {
				data[i][j] = d[i][j];
				if (d[i][j] > maxLabel) {
					maxLabel = d[i][j];
				}
			}
		}
	}
	
	/**
	 * Gets the label in chosen position, in case out of segmentation it throws 
	 * an exception
	 * 
	 * @param x int position in the first dimension
	 * @param y int position in the second dimension
	 * @return int label
	 */
	public int getLabel(int x, int y) {
		// check out of image	
		if (x<0 || y<0 || x>=dims[0] || y>=dims[1]) {
			throw new IndexOutOfBoundsException();
		}
		return data[x][y];
	}
	
	/**
	 * Sets the label in chosen position, in case out of segmentation it throws 
	 * an exception
	 * 
	 * @param x int position in the first dimension
	 * @param y int position in the second dimension
	 * @param l int new label
	 */
	public void setLabel(int x, int y, int l) {
		maxLabel = -1;
		// check out of image
		if (x<0 || y<0 || x>=dims[0] || y>=dims[1]) {
			throw new IndexOutOfBoundsException();
		}
		// update histogram
		if (hist != null) {
			hist[ data[x][y] ] --;
			hist[ l ] ++;
		}
		// assigne
		data[x][y] = l;
		// update max label
		if (l > maxLabel) {
			maxLabel = l;
		}
	}
	
	/**
	 * @see 
	 */
	@Override
	public int getLabel(int[] pos) {
		return getLabel(pos[0], pos[1]);
	}

	/**
	 * @see
	 */
	@Override
	public void setLabel(int[] pos, int l) {
		setLabel(pos[0], pos[1], l);
		
	}
	
	/**
	 * BE CAREFUL ABOUT THIS METHOD !!!
	 * 
	 * @return reference to data of the labelling
	 */
	public int[][] getData() {
		return data;
	}

	/**
	 * 
	 * @return
	 */
	public int getWidth() {
		return data.length;
	}

	/**
	 * 
	 * @return
	 */
	public int getHeight() {
		return data[0].length;
	}
	
	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#computeHistogram()
	 */
	@Override
	public int[] computeHistogram() {
		maxLabel = -1;
		// find new max labels 
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				if (maxLabel < data[i][j]) {
					maxLabel = data[i][j];
				}
			}
		}

		// init hist. array		
		hist = new int[maxLabel+1];
		Arrays.fill(hist, 0);
		
		// compute histogram
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				hist[ data[i][j] ] ++;
			}
		}
		return hist;
	}
	
	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#showLabelling()
	 */
	@Override
	public void showLabelling() {
		// create ImageJ ShortProcessor
		ShortProcessor segm = new ShortProcessor(dims[0], dims[1]);
		for (int x=0; x<dims[0]; x++ ) {
			for (int y=0; y<dims[1]; y++ ) {
				segm.set(x, y, data[x][y]);
			}
		}
		
		// create Processor to ImagePlus
		ImagePlus img = new ImagePlus("Segmentation", segm);
		img.show();
	}

	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#reLabel(int[] LUT)
	 */
	@Override
	public void reLabel(int[] LUT) {
		if ((maxLabel+1) != LUT.length) {
			throw new IndexOutOfBoundsException("segmentation and new labelling LUT are not same.");
		}
		
		assert LUT.length > 0;
		maxLabel = LUT[0];
		// find new max labels 
		for (int i=1; i<LUT.length; i++) {
			if (maxLabel < LUT[i]) {
				maxLabel = LUT[i];
			}
		}
		
		// init hist. array		
		hist = new int[maxLabel+1];
		Arrays.fill(hist, 0);
		
		// relabel actual labelling and compute histogram
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				data[i][j] = LUT[ data[i][j] ];
				hist[ data[i][j] ] ++;
			}
		}
	}
	
	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#findSegmentsConnectivity(int[][])
	 */
	@Override
	public int[][] findSegmentsConnectivity(int[][] neighbors) {
		return ConvertStructure.arrayLists2intMatrix( Connectivity2D.findSegmetNeighbors(data, maxLabel+1, neighbors) );
	}

	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#findElementsBoundaries(int[][] neighborhood)
	 */
	@Override
	public ArrayList<ArrayList<int[]>> findElementsBoundaries(int[][] neighborhood) {
		return Connectivity2D.segmentBoundariesRaw(data, maxLabel+1, neighborhood);
	}
	
	public ArrayList<ArrayList<int[]>> findElementsBoundariesPolygon() {
		// treat all point and then simplify
		Logging.logMsg("   -> segment boundaries..");
		ArrayList<ArrayList<int[]>> bounds = Connectivity2D.segmentBoundaries(data, maxLabel+1);
		Logging.logMsg("   -> simplify polygon...");
		Connectivity2D.simplifyPolygon(bounds);
		return bounds;
	}

	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#showOverlapLabeling(ImagePlus img, float opticaly)
	 */
	@Override
	public void showOverlapLabeling(ImagePlus img, double opticaly) {
		if ( ! checkImgAndSegmDims(img) ) {		return;		}
				
		// create LUT
		Color clr = null;
		Random rnd = new Random();
		int[] lut = new int[maxLabel+1];
		int[][] lutRGB = new int[maxLabel+1][3];
		for (int i=0; i<=maxLabel; i++) {
			// segment colour in single integer
			lut[i] = rnd.nextInt(255*255*255);
			// decomposition by RGB components
			clr = new Color(lut[i]);
			lutRGB[i][0] = (int) (clr.getRed() * (1-opticaly));
			lutRGB[i][1] = (int) (clr.getGreen() * (1-opticaly));
			lutRGB[i][2] = (int) (clr.getBlue() * (1-opticaly));
		}	

		// check if it is colour image
		if (img.getType() != ImagePlus.COLOR_RGB) {		
			Logging.logMsg("WARING: the image is not RGB image."); 
			img.setProcessor( img.getProcessor().convertToRGB() );
		} 

		// pixel values (local)
		int c[] = null; 	
		// create colour segmentation
		ImageProcessor ip = img.getProcessor().convertToRGB();
		ImageProcessor segm = new ColorProcessor(dims[0], dims[1]);
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				// segm.set(i, j, lut[ data[i][j] ]);
				c = ip.getPixel(i, j, c); 
				clr = new Color((int) (opticaly*c[0]) + lutRGB[data[i][j]][0],
								(int) (opticaly*c[1]) + lutRGB[data[i][j]][1], 
								(int) (opticaly*c[2]) + lutRGB[data[i][j]][2]);
				segm.set(i, j, clr.getRGB() );
			}
		}
		
		ImageStack stack = img.getStack();
		stack.addSlice(segm);
		img.setStack(stack);
		img.updateImage();
	}

	/**
	 * @see sc.fiji.CMP_BIA.segmentation.structures.Labelling#showOverlapContours(ImagePlus img)
	 * 
	 * @param neighborhood is one of Connectivity2D.CONNECT4 or CONNECT8
	 * @example showOverlayContours(image, Connectivity2D.CONNECT4, Color.RED);
	 */
	@Override
	public void showOverlapContours(ImagePlus img, java.awt.Color clr) {
		if ( ! checkImgAndSegmDims(img) ) {		return;		}
		
		
//		// estimate boundaries
//		ArrayList<ArrayList<int[]>> coords = findElementsBoundariesPolygon();
//					
//		FloatPolygon poly;
//		Overlay overlay = new Overlay();
//		// for all boundaries
//		for (int i = 0; i < coords.size(); i++) {
//			// skip empty boundaries
//			if (coords.get(i) != null) {
//				poly = new FloatPolygon();
//				for (int j = 0; j < coords.get(i).size(); j++) {
//					poly.addPoint( coords.get(i).get(j)[0] , coords.get(i).get(j)[1] );
//				}
//				overlay.add(new PolygonRoi(poly, Roi.POLYGON));
//			}
//		}
//		
//		overlay.setStrokeColor(clr);
//		img.setOverlay(overlay);
		
		ImageProcessor ip = img.getProcessor();
		// ArrayList<int[]> coords = Connectivity2D.findBoundaryPoints(data, Connectivity2D.CONNECT8);
		ArrayList<ArrayList<int[]>> coords = Connectivity2D.segmentBoundariesRaw(data, maxLabel+1, Connectivity2D.CONNECT8);
		// draw the contours
		for (int i = 0; i < coords.size(); i++) {
			for (int j = 0; j < coords.get(i).size(); j++) {
				ip.set(coords.get(i).get(j)[0], coords.get(i).get(j)[1], clr.getRGB() );
			}		
		}		
		
		img.updateAndRepaintWindow();
		//img.updateAndDraw();
						
		img.updateImage();
	}

	/**
	 * 
	 */
	public void showOverlapROIs(ImagePlus img) {
		if ( ! checkImgAndSegmDims(img) ) {		return;		}
		
		// estimate boundaries
		ArrayList<ArrayList<int[]>> coords = findElementsBoundariesPolygon();
		
		//int currentSlice = img.getCurrentSlice();
		RoiManager manager = RoiManager.getInstance();
		if (manager == null) {
		    manager = new RoiManager();
		}
		
		// for all boundaries
		for (int i = 0; i < coords.size(); i++) {
			// skip empty boundaries
			if (coords.get(i) != null) {
				FloatPolygon poly = new FloatPolygon();
				for (int j = 0; j < coords.get(i).size(); j++) {
					poly.addPoint( coords.get(i).get(j)[0] , coords.get(i).get(j)[1] );
				}
				PolygonRoi roi = new PolygonRoi(poly, Roi.POLYGON);
				roi.setName("superpixel "+Integer.toString(i));
				manager.addRoi(roi);
				//rm.add(img, new PolygonRoi(poly, Roi.POLYGON), 0);
				//manager.add(img, roi, currentSlice);
			}
		}
					
		img.updateAndDraw();
	}
	
	/**
	 * check dimensionality between image and labeling
	 * 
	 * @param img image to be compared with the labeling
	 * @return bool if the dimensions are consistent
	 */
	private boolean checkImgAndSegmDims(ImagePlus img) {
		if (dims[0]!=img.getWidth() || dims[1]!=img.getHeight() ) {
			Logging.logMsg("ERROR: Inconsistent image and labeling size!");
			return false;
		} 
		return true;
	}

	/**
	 * Compute the overlap histogram of two segmentations
	 * 
	 * TODO: not optimise for large number of labels
	 * 
	 * @param lb is the other segmentation of the same dimension
	 * @param shift is the relative shift of the second segmentation in relation to this one
	 * @return int[this.maxLabel][lb.maxLabel] is sparse matrix
	 */
	public int[][] overlaps(Labelling2D lb, int[] shift) {
		// inti the output array
		int[][] overlap = new int[this.maxLabel+1][lb.maxLabel+1];

		// variables depending on segmentation sizes
		final int lDim = 2;
		int[] minDim = new int[lDim];
		int[] lShiftA = new int[lDim];
		int[] lShiftB = new int[lDim];
		int[] end = new int[lDim];
		
		// do for both dimensions
		for (int i=0; i<lDim; i++) {
			// find minimal sizes of both segmentations
			minDim[i] = (this.dims[i] < lb.dims[i]) ? this.dims[i] : lb.dims[i];
			// find shifting for the second image
			lShiftA[i] = (shift[i] >= 0) ? shift[i] : 0;
			lShiftB[i] = (shift[i] < 0) ? -shift[i] : 0;
			// find the ending of common range
			end[i] = (shift[i] < 0) ? this.dims[i]+shift[i] : lb.dims[i]-shift[i];
			// for case of overflow in the segm.
			//end[i] = (end[i] > this.dims[i]) ? this.dims[i] : end[i]; 
		}
		
		// go throw overlap of both segmentations
		for (int i=0; i<end[0]; i++) {
			for (int j=0; j<end[1]; j++) {
				overlap[ this.data[i+lShiftA[0]][j+lShiftA[1]] ][ lb.data[i+lShiftB[0]][j+lShiftB[1]] ] ++;
			}
		}
		
		return overlap;
	}

	/**
	 * @ see {@link sc.fiji.CMP_BIA.segmentation.structures.Labelling#findMultiClassBoundaryPoints(int[][])}
	 */
	@Override
	public int[][] findMultiClassBoundaryPoints(int[][] neighbors) {
		return ConvertStructure.arrayList2intMatrix( Connectivity2D.findBoundaryPoints(data, neighbors) );
	}

	/**
	 * @ see {@link sc.fiji.CMP_BIA.segmentation.structures.Labelling#clone()}
	 */
	@Override
	public Object clone() {
		return new Labelling2D( this.data );
	}

	/**
	 * 
	 */
	@Override
	public void exportToFile(String path) {
		PrintWriter out = null;
		// create the string
		String strDims = new String("Dims:");
		for (int i = 0; i < dims.length; i++) {
			strDims += " " + Integer.toString(dims[i]);
		}
		// IO process
		try {
			out = new PrintWriter(path, "UTF-8");
			// write data
			out.println(strDims);
			for (int i=0; i<data.length; i++) {
				for (int j=0; j<data[i].length; j++) {
					out.print( Integer.toString( data[i][j] ) + " ");
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void printData() {
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				System.out.print( Integer.toString( data[i][j] ) + ", ");
			}
			System.out.println();
		}
	}
		
}
