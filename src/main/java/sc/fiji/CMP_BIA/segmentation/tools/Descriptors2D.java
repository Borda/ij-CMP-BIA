/**
 * @file
 */
package sc.fiji.CMP_BIA.segmentation.tools;

import java.util.Arrays;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.tools.converters.ConvertImage;
import sc.fiji.CMP_BIA.transform.wavelets.HaarWavelets;
import ij.ImagePlus;


/**
 * @class Descriptor extraction in 2D
 * @version 0.1
 * @date 28/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief Child class of abstract class Descriptors, this class aim to the 2D 
 * images only and its related segmentations 
 * 
 * @see Labelling2D
 */
public class Descriptors2D extends Descriptors<Labelling2D> {
	// local variables
	private int Width, Height;
		
	/**
	 * Constructor 
	 * 
	 * @param im is the input image
	 * @param segm is related segmentation of size int[Width][Height]
	 * @param nbLabs is number of labels in segmentation
	 */
	public Descriptors2D(ImagePlus img, Labelling2D segm) {
		
		super(img, segm);
		
		nbSegments = segmentation.getMaxLabel()+1;
		Width = segmentation.getDims()[0];
		Height = segmentation.getDims()[1];
	}
	
	/**
	 * @see sc.fiji.CMP_BIA.segmentation.tools.Descriptors#checkDimensions(ImagePlus, sc.fiji.CMP_BIA.segmentation.structures.Labelling)
	 */
	@Override
	protected boolean checkDimensions(ImagePlus img, Labelling2D segm) {
		boolean bool = (img.getWidth() == segm.getDims()[0] && img.getHeight() == segm.getDims()[1]);
		return bool;
	}	
	
	/**
	 * compute the colour descriptors and for each segment as a mean value 
	 * and add then to the description vector (on the end of actual vector)
	 */
	public void addConstatnt (float n) {
		// cycle over all clusters
		for (int k=0; k<nbSegments; k++) {
			feaures.get(k).add( n );
		}
	}
	
	/**
	 * compute the colour descriptors and for each segment as a mean value 
	 * and add then to the description vector (on the end of actual vector)
	 */
	public void computeColourMeanRGB () {
		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[nbSegments];
		Arrays.fill(nbPixels, 0);
		// sum variable for colours
		int[][] segmColour = new int[segmentation.getMaxLabel()+1][3];
		for(int[] subarray : segmColour) {			Arrays.fill(subarray, 0);		}
		int k; // segment index (local)
		int c[]; // pixel values (local)
		
		// separate to 2D function... 
		
		// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<Width; x++ ) {
			for (int y=0; y<Height; y++ ) {
				k = segmentation.getLabel(x, y);
				c = image.getPixel(x, y); 
				segmColour[k][0] += c[0];
				segmColour[k][1] += c[1];
				segmColour[k][2] += c[2];
				nbPixels[k] ++;
			}
		}
		
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (k=0; k<nbSegments; k++) {
			if (nbPixels[k] == 0) {		continue;	}
			feaures.get(k).add(  (float)segmColour[k][0] / (float)nbPixels[k] );
			feaures.get(k).add(  (float)segmColour[k][1] / (float)nbPixels[k] );
			feaures.get(k).add(  (float)segmColour[k][2] / (float)nbPixels[k] );
		}
		nbFeatures += 3;
	}

	protected void computeColourMean (float[][][] img) {
		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[nbSegments];
		Arrays.fill(nbPixels, 0);
		// sum variable for colours
		int[][] segmColour = new int[segmentation.getMaxLabel()+1][3];
		for(int[] subarray : segmColour) {			Arrays.fill(subarray, 0);		}
		int k; // segment index (local)
		
		// separate to 2D function... 
		
		// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<Width; x++ ) {
			for (int y=0; y<Height; y++ ) {
				k = segmentation.getLabel(x, y);
				segmColour[k][0] += img[x][y][0];
				segmColour[k][1] += img[x][y][1];
				segmColour[k][2] += img[x][y][2];
				nbPixels[k] ++;
			}
		}
		
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (k=0; k<nbSegments; k++) {
			if (nbPixels[k] == 0) {		continue;	}
			feaures.get(k).add(  (float)segmColour[k][0] / (float)nbPixels[k] );
			feaures.get(k).add(  (float)segmColour[k][1] / (float)nbPixels[k] );
			feaures.get(k).add(  (float)segmColour[k][2] / (float)nbPixels[k] );
		}
	}
	

	/**
	 * Computes the texture descriptors as energy using Haar wavelets
	 * 
	 * @param levels specify the number of levels for which will be computed
	 */
	public void computeTextureWaveletsHaar (int levels) {
		// init local variables
		float[][] haar;
		int w = Width;
		int h = Height;
		int scale = 1;
		
		// convert the rgb to simple brightness
		float[][] img = ConvertImage.rgb2bright( image.getProcessor() );
		
		// go over all levels
		for (int i=0; i<levels; i++) {
			// compute Haar
			haar = HaarWavelets.computeHaarForward(img);
						
			// scaling given by haar filter of ration 2
			w = w / 2;
			h = h / 2;
			scale = scale *2;
			
			//ImageProcessor tt = new FloatProcessor(haar);
			//ImagePlus ttt = new ImagePlus();
			//ttt.setProcessor(tt);
			//ttt.show();
			
			// compute energies by given function
			energyWaveletHaar(haar, scale);
			
			// copy low pass image from haar frames
			img = new float[w][h];
			for (int x=0; x<w; x++) {
				for (int y=0; y<h; y++) {
					img[x][y] = haar[x][y];
				}
			}
		}
			
	}
	
	/**
	 * Compute the energies over 3 of 4 Haar frames (namely Low*High, High*Low 
	 * and High*High) belonging to each element of given segmentation
	 * 
	 * @param haarFrame is matrix of all 4 aligned Haar frames
	 * @param scale is int which gives the ration between the haar frames 
	 * and the segmentation defining individual elements 
	 */
	protected void energyWaveletHaar(float[][] haarFrame, int scale) {

		// init temporary variables
		float[] tmp = new float[nbSegments];
		Arrays.fill(tmp, 0);
		int[] count = new int[nbSegments];
		Arrays.fill(count, 0);
		int k;
		float f, fLH, fHH, fHL;
		// offsets, we assume half and half decomposition
		int offsetX = haarFrame.length / 2;
		int offsetY = haarFrame[0].length / 2;
				
		// go over qurter of all positions in the Haar frames 
		for (int x=0; x<offsetX; x++) {
			for (int y=0; y<offsetY; y++) {
				// get labels of given pixel
				k = segmentation.getLabel(x*scale, y*scale);
				// compute the energy as x^2
				// region L*H
				fLH = haarFrame[offsetX +x][y];
				// region H*L
				fHL = haarFrame[x][offsetY +y];
				// region H*H
				fHH = haarFrame[offsetX +x][offsetY +y];
				// increment nb
				tmp[k] += (fLH*fLH) + (fHL*fHL) + (fHH*fHH);
				count[k] ++;
			}
		}
				
		// energy normalization by segment sizes
		for (int i=0; i<nbSegments; i++) {
			// avoid empty spaces
			f = (count[i]>0) ? (float)tmp[i] / (float)count[i] : 0;
			feaures.get(i).add(  f );
		}
		 
		nbFeatures ++;
	}

	
}