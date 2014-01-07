/**
 * @file
 */
package sc.fiji.CMP_BIA.transform.wavelets;

/**
 * @class HaarWavelets
 * @version 0.1
 * @date 12/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category wavelets
 * 
 * @brief Computing Haar wavelets on a 2D array of float numbers
 * 
 * @see http://en.wikipedia.org/wiki/Haar_wavelet
 * @see https://www.ceremade.dauphine.fr/~peyre/numerical-tour/tours/wavelet_2_haar2d/
 */
public class HaarWavelets {

	/**
	 * Default constructor
	 */
	public HaarWavelets() {}
	
	/**
	 * Compute the forward Haar wavelet transform which consists of 4 aligned 
	 * frames each is downsampled by 2 (has half size of the original image). 
	 * The frames are Low*Low, Low*High, High*Low and High*High
	 * 
	 * @param image a matrix float[width][height] to be applied the Haar wavelets
	 * @return float[width][height] is matrix with all 4 frames
	 */
	public static float[][] computeHaarForward(final float[][] image) {

		int width = image.length; 
		int height = image[0].length; 
		float norm = (float) Math.sqrt(2.); 		
		
		// init wavelet array
		float[][] haarImg = new float[width][height];
		
		// Haar over the first dimension
		for (int i=0; i<width; i++) {
			for (int j=0; j<(height/2); j++) {
				// low pass filter
				haarImg[i][j] = ( image[i][2*j] + image[i][2*j+1] ) / norm; 
				//hight pass filter
				haarImg[i][j+(height/2)] = ( image[i][2*j] - image[i][2*j+1] ) / norm; 
			}
		}

		float[][] haar = new float[width][height];
		
		// Haar over the second dimension
		for (int i=0; i<(width/2); i++) {
			for (int j=0; j<height; j++) {
				// low pass filter
				haar[i][j] = ( haarImg[2*i][j] + haarImg[2*i+1][j] ) / norm; 
				//hight pass filter
				haar[i+(width/2)][j] = ( haarImg[2*i][j] - haarImg[2*i+1][j] ) / norm; 
			}
		}
		
		return haar;
	}
	
}
