package sc.fiji.CMP_BIA.segmentation;

import java.awt.GraphicsEnvironment;
import java.io.File;

import ij.ImagePlus;

import org.junit.Before;
import org.junit.Test;

import sc.fiji.CMP_BIA.segmentation.superpixels.jSLIC;
import sc.fiji.CMP_BIA.segmentation.superpixels.jSLICp2D;
import sc.fiji.CMP_BIA.tools.Prints;


public class TestSLIC {

	ImagePlus img = null;
	jSLIC sp = null;
	jSLICp2D sp2 = null;
	String path = System.getProperty("user.dir") + "/src/test/resources/imgs/letter_a.png";

	/**
	 * 
	 */
	@Before
	public void loadImage () {
		// init image / load
		if ( (new File(path)).exists() ) {
			img = new ImagePlus( path );
			// Only show image if not in headless environment
			if (!GraphicsEnvironment.isHeadless()) {
				img.show();
			}
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void test_jSLIC() {
		Prints.printTitle("SLIC superpixels");

		if (img != null) {
		
			// ORIGINAL
			sp = new jSLIC(img);
			sp.process(3, 0.2f);
			//sp.getSegmentation().printData();
			// Only show segmentation if not in headless environment
			if (!GraphicsEnvironment.isHeadless()) {
				sp.getSegmentation().showLabelling();
			}

			// EXTENSION
			sp2 = new jSLICp2D(img);
			sp2.process(3, 0.2f);
			//sp.getSegmentation().printData();
			// Only show segmentation if not in headless environment
			if (!GraphicsEnvironment.isHeadless()) {
				sp2.getSegmentation().showLabelling();
			}
		
		} else {
			System.out.println("ERROR: resources image '"+path+"' was not found!");
		}
		
	}

}
