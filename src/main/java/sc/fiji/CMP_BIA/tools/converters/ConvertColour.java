/**
 * @file
 */
package sc.fiji.CMP_BIA.tools.converters;

/**
 * @class Colour Converter
 * @version 0.1
 * @date 11/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image conversion
 * 
 * @brief Conversion of a single pixel from one colour space to another
 */
abstract public class ConvertColour {

	/**
	 * conversion RGB to XYZ by pixel
	 * 
	 * @param R float value of red channel (0..1)
	 * @param G float value of green channel (0..1)
	 * @param B float value of blue channel (0..1)
	 * @param xyz[3] is the array of values for XYZ
	 */
	static public void rgb2xyz(float r, float g, float b, float[] xyz) {
		//http://www.brucelindbloom.com
		  
		// assuming sRGB (D65)
		if (r <= 0.04045) {
			r = r/12;
		} else {
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		}
		if (g <= 0.04045) {
			g = g/12;
		} else {
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		}
		if (b <= 0.04045) {
			b = b/12;
		} else{
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		}
		
		xyz[0] =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		xyz[1] =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		xyz[2] =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
	}

	/**
	 * conversion RGB to LAB by pixel
	 * 
	 * @param R float value of red channel (0..1)
	 * @param G float value of green channel (0..1)
	 * @param B float value of blue channel (0..1)
	 * @param lab[3] is the array of values for LAB
	 */
	static public void rgb2lab(float r, float g, float b, int[] lab) {
		//http://www.brucelindbloom.com
		  
		float fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		   
		float[] refXYZ = new float[]{0.964221f, 1.0f, 0.825211f};
		
		float[] xyz = new float[3];
		rgb2xyz(r, g, b, xyz);
		
		// XYZ to Lab
		xr = xyz[0]/refXYZ[0];
		yr = xyz[1]/refXYZ[1];
		zr = xyz[2]/refXYZ[2];
				
		if ( xr > eps ) {
			fx =  (float) Math.pow(xr, 1/3.);
		} else {
			fx = (k * xr + 16.f) / 116.f;
		}
		if ( yr > eps ) {
			fy =  (float) Math.pow(yr, 1/3.);
		} else {
			fy = (k * yr + 16.f) / 116.f;
		}
		if ( zr > eps ) {
			fz =  (float) Math.pow(zr, 1/3.);
		} else {
			fz = (k * zr + 16.f) / 116f;
		}
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		lab[0] = (int) (2.55f * Ls + 0.5f);
		lab[1] = (int) (as + 0.5f); 
		lab[2] = (int) (bs + 0.5f);       
	} 
	
	/**
	 * conversion RGB to LAB by pixel
	 * 
	 * @param R int value of red channel (0..255)
	 * @param G int value of green channel (0..255)
	 * @param B int value of blue channel (0..255)
	 * @param lab[3] is the array of values for LAB
	 */
	static public void rgb2lab(int R, int G, int B, int[] lab) {
		rgb2lab(R/255f, G/255f, B/255f, lab);
	} 

	/**
	 * conversion RGB to brightness by pixel
	 * 
	 * @param R float value of red channel (0..1)
	 * @param G float value of green channel (0..1)
	 * @param B float value of blue channel (0..1)
	 * @return float value in range (0,1)
	 */
	static public float rgb2bright(float R, float G, float B) {
		return (float) Math.sqrt( (R * R * 0.241) + (G * G * 0.691) + (B * B * 0.068) );
	}
	
	/**
	 * conversion RGB to brightness by pixel
	 * 
	 * @param R int value of red channel (0..255)
	 * @param G int value of green channel (0..255)
	 * @param B int value of blue channel (0..255)
	 * @return int value in range (0,255)
	 */
	static public int rgb2bright(int R, int G, int B) {
		return (int) Math.sqrt( (R * R * 0.241) + (G * G * 0.691) + (B * B * 0.068) );
	}
	
}
