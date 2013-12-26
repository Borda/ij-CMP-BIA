/**
 * @file
 */
package sc.fiji.CMP_BIA.tools.converters;

import sc.fiji.CMP_BIA.tools.Logging;
import ij.process.ImageProcessor;

/**
 * @class Image Convertor
 * @version 0.1
 * @date 11/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image conversion
 * 
 * @brief converting an image into different colour spaces
 */
abstract public class ConvertImage {

	/**
	 * Convert whole image from RGB to LAB colour space
	 * 
	 * @param image is a ImageProcessor
	 * @return int[width][height][3]
	 */
	public static int[][][] rgb2cieLAB (final ImageProcessor image) {
		// check if it is RGB image
		if (image.getNChannels() != 3) {
			System.out.println("Image is NOT RGB image, becase it has only "+ Integer.toString(image.getNChannels()) +" channels.");
			return null;
		}
		
		// create pixel buffer
		int[][][] img = new int[image.getWidth()][image.getHeight()][3];
		int c[] = null; // pixel values (local)
		int lab[] = new int[3];
		
		for (int x=0; x<image.getWidth(); x++ ) {
			for (int y=0; y<image.getHeight(); y++ ) {
				c = image.getPixel(x, y, c); 
				// Returns the pixel value at (x,y) as a 4 element array.
				// RGB values are returned in the first 3 elements.
		        ConvertColour.rgb2lab(c[0], c[1], c[2], lab);
		        img[x][y][0] = lab[0];
		        img[x][y][1] = lab[1];
		        img[x][y][2] = lab[2];
			}
		}
		
		return img;
	}

	/**
	 * Convert whole image from RGB to LAB colour space
	 * this fast version save already computed colour so each is computed only once
	 * 
	 * @param image is a ImageProcessor
	 * @return int[width][height][3]
	 */
	public static int[][][] rgb2cieLABfast (final ImageProcessor image) {
		// check if it is RGB image
		if (image.getNChannels() != 3) {
			System.out.println("Image is NOT RGB image, becase it has only "+ Integer.toString(image.getNChannels()) +" channels.");
			return null;
		}
		
		// saving already computed values
		int[][][][] LUT = new int[256][256][256][];
		Logging.logMsg(" -> fast rgb2LAB conversion");
		int count = 0;
		
		// create pixel buffer
		int[][][] img = new int[image.getWidth()][image.getHeight()][3];
		int c[] = null; // pixel values (local)
		//int cRed, cGreed, cBlue;
		int lab[] = new int[3];
		
		for (int x=0; x<image.getWidth(); x++ ) {
			for (int y=0; y<image.getHeight(); y++ ) {
				//cRed = (image.get(x, y) & 0xff0000)>>16;
				//cGreed = (image.get(x, y) & 0x00ff00)>>8;
				//cBlue = (image.get(x, y) & 0x0000ff);
				c = image.getPixel(x, y, c); 
				// Returns the pixel value at (x,y) as a 4 element array.
				// RGB values are returned in the first 3 elements.
				if (LUT[c[0]][c[1]][c[2]] == null) {
					ConvertColour.rgb2lab(c[0], c[1], c[2], lab);
					LUT[c[0]][c[1]][c[2]] = lab.clone();
					count ++;
				} else {
					lab = LUT[c[0]][c[1]][c[2]].clone();
				}
		        img[x][y][0] = lab[0];
		        img[x][y][1] = lab[1];
		        img[x][y][2] = lab[2];
			}
		}
		
		float rate = (float)count / (float)(image.getWidth()*image.getHeight()) * 100;
		Logging.logMsg("   -> computed "+Integer.toString(count)+" colours = "+Float.toString(rate)+"%");
		return img;
	}
	
	/**
	 * Convert whole gray image own colour space
	 * 
	 * @param image is a ImageProcessor
	 * @return int[width][height][3]
	 */
	public static int[][][] gray2cieLAB(final ImageProcessor image) {
		// check if it is RGB image
		if (image.getNChannels() != 1) {
			System.out.println("Image is NOT gray image, becase it has only "+ Integer.toString(image.getNChannels()) +" channels.");
			return null;
		}
		
		// saving already computed values
		int[][] LUT = new int[256][];
		Logging.logMsg(" -> fast rgb2LAB conversion");
				
		// create pixel buffer
		int[][][] img = new int[image.getWidth()][image.getHeight()][3];
		int[] vals = new int[4];
		int c; // pixel values (local)
		//int cRed, cGreed, cBlue;
		int lab[] = new int[3];
		
		image.convertToByte(false);
		
		// over all pixels
		for (int x=0; x<image.getWidth(); x++ ) {
			for (int y=0; y<image.getHeight(); y++ ) {

				c = image.getPixel(x, y, vals)[0];
				
				if (LUT[c] == null) {
					ConvertColour.rgb2lab(c, c, c, lab);
					LUT[c] = lab.clone();
				} else {
					lab = LUT[c].clone();
				}
		        img[x][y][0] = lab[0];
		        img[x][y][1] = lab[1];
		        img[x][y][2] = lab[2];
				
			}
		}
		
		return img;
	}
	
	/**
	 * Convert whole gray image own colour space
	 * 
	 * @param image is a ImageProcessor
	 * @return int[width][height][1]
	 */
	public static int[][][] gray2bright(final ImageProcessor image) {
		// check if it is RGB image
		if (image.getNChannels() != 1) {
			System.out.println("Image is NOT gray image, becase it has only "+ Integer.toString(image.getNChannels()) +" channels.");
			return null;
		}
				
		// create pixel buffer
		int[][][] img = new int[image.getWidth()][image.getHeight()][1];
		int[] vals = new int[4];
		
		image.convertToByte(false);

		// over all pixels
		for (int x=0; x<image.getWidth(); x++ ) {
			for (int y=0; y<image.getHeight(); y++ ) {
				img[x][y][0] = image.getPixel(x, y, vals)[0];
			}
		}
		
		return img;
	}
	
	/**
	 * Convert whole RGB image own colour space by brightness
	 * 
	 * @param image is a ImageProcessor
	 * @return float[width][height]
	 */
	public static float[][] rgb2bright(final ImageProcessor image) {
		// check if it is RGB image
		if (image.getNChannels() != 3) {
			System.out.println("Image is NOT RGB image, becase it has only "+ Integer.toString(image.getNChannels()) +" channels.");
			return null;
		}
		
		// create pixel buffer
		float[][] img = new float[image.getWidth()][image.getHeight()];		
		int c[] = null; // pixel values (local)
		
		// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<image.getWidth(); x++ ) {
			for (int y=0; y<image.getHeight(); y++ ) {
				c = image.getPixel(x, y, c); 
				img[x][y] = ConvertColour.rgb2bright(c[0], c[1], c[2]);
			}
		}
		
		return img;
	}
	
}
