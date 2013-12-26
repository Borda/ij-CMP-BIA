/**
 * @file
 */
package sc.fiji.CMP_BIA.segmentation.tools;

import java.util.ArrayList;
import java.util.InputMismatchException;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.converters.ConvertStructure;
import ij.ImagePlus;

/**
 * @class General Descriptor class
 * @version 0.1
 * @date 10/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief General abstract class for computing/extraction some decriptors/features 
 * on given image and its related segmentation. Meaning the partial descriptors 
 * are computed on each element of multi-class segmentation. Basically we can use 
 * a superpixel clustering and then compute the descriptors for each superpixel.  
 * 
 * @see Labelling
 */
abstract public class Descriptors<T extends Labelling> {
	// original image
	protected ImagePlus image = null;
	// number of labels in segmentation
	protected T segmentation = null;
	// vector of descriptors of size nbLabels x nbDesc
	protected ArrayList<ArrayList<Float>> feaures;
	
	/**
	 * Constructor which asked for a image and its reliable segmentation  
	 * 
	 * @param img is a ImagePlus on which the descriptor will be computed
	 * @param segm is a Labelling for given image 
	 */
	public Descriptors(ImagePlus img, T segm) {
		if (! checkDimensions(img, segm)) {
			ij.IJ.log("The image and segmentation dimesion are not the same!");
			throw new InputMismatchException();
		}
		image = img;
		segmentation = segm;
		
		// construct the description vector
		initVariables();
	}
	
	abstract protected boolean checkDimensions(ImagePlus img, T lab);
	
	/**
	 * Gives the vector (matrix) of descriptors for each segment
	 * 
	 * @return float[nbSegments][nbDesc] descriptors for each segment
	 */
	public float[][] getDescMatrix() {
		float[][] res = ConvertStructure.arrayLists2floatMatrix(feaures);
		return res;
	}

	/**
	 * Returns all computed descriptors for each element of segmentation
	 * 
	 * @return ArrayList<float[nbFeatures]> is array of features per element
	 */
	public ArrayList<float[]> getDescList() {
		ArrayList<float[]> res = ConvertStructure.arrayLists2floatList(feaures);
		return res;
	}
	
	/**
	 * Initialise all internal variables needed for computing
	 */
	protected void initVariables() {
		// construct the description vector
		feaures = new ArrayList<ArrayList<Float>>(segmentation.getMaxLabel()+1);
		for (int i=0; i<(segmentation.getMaxLabel()+1); i++) {
			feaures.add( new ArrayList<Float>() );
		}
	}
	
	/**
	 * Print all actually computed variables features on chosen Log stream
	 */
	public void show() {
		for(int i=0; i<feaures.size(); i++) {
			String str = new String("label " + Integer.toString(i) + " contains features : ");
			for (int j=0; j<feaures.get(i).size(); j++) {
				str = str + Float.toString(feaures.get(i).get(j)) + ", ";
			}
			Logging.logMsg(str);
		}
	}
		
}

