/**
 * 
 */
package sc.fiji.CMP_BIA.segmentation;


import java.io.File;

import ij.ImagePlus;

import org.junit.Before;
import org.junit.Test;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.tools.Descriptors2D;
import sc.fiji.CMP_BIA.tools.Prints;

/**
 * @version 1.0
 * @date 24/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category tests
 * 
 * @brief 
 */
public class DescriptorsTest {

	ImagePlus img = null;
	Labelling2D segm = null;
	Descriptors2D desc = null;
	String path = System.getProperty("user.dir") + "/src/test/resources/imgs/texture-sample.png";
	
	/**
	 * 
	 */
	@Before
	public void loadImage () {
		// init image / load		
		
		if ( (new File(path)).exists() ) {
			img = new ImagePlus( path );
			
			// number of samples
			int nbSplRow = 4;
			int nbSplCol = 4;
			int sizeSplRow = img.getWidth() / nbSplRow;
			int sizeSplCol = img.getHeight() / nbSplCol;
			int lb;
			
			// init segmentation		
			segm = new Labelling2D(img.getWidth(), img.getHeight());
			for (int i=0; i<img.getWidth(); i++) {
				for (int j=0; j<img.getHeight(); j++) {
					lb = (i/sizeSplRow)*nbSplRow + (j/sizeSplCol);
					segm.setLabel(i, j, lb );
				}
			}
		}	
	}
	
	/**
	 * 
	 */
	@Test
	public void test_Texture() {
		Prints.printTitle("Texture Descriptors");
		
		if (img != null) {
			assert (segm != null);
			
	//		img.show();
	//		segm.showLabelling();		
	//		new java.util.Scanner(System.in).nextLine();
			
	//		segm.computeHistogram();
	//		segm.printHistogram();
			
			desc = new Descriptors2D(img, segm);
			desc.computeTextureWaveletsHaar(3);
	
			desc.show();
		} else {
			System.out.println("ERROR: resources image '"+path+"' was not found!");
		}
	}
	
}
