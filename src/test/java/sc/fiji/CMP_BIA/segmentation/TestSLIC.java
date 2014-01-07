package sc.fiji.CMP_BIA.segmentation;

import java.io.File;

import ij.ImagePlus;

import org.junit.Before;
import org.junit.Test;

import sc.fiji.CMP_BIA.segmentation.superpixels.jSLIC;
import sc.fiji.CMP_BIA.segmentation.superpixels.jSLICe;
import sc.fiji.CMP_BIA.tools.Prints;


public class TestSLIC {

	ImagePlus img = null;
	jSLIC sp = null;
	jSLICe sp2 = null;
	String path = System.getProperty("user.dir") + "/src/test/resources/imgs/letter_a.png";
	
	/**
	 * 
	 */
	@Before
	public void loadImage () {
		// init image / load
		if ( (new File(path)).exists() ) {
			img = new ImagePlus( path );
			img.show();
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
			sp.getSegmentation().showLabelling();
		
			// EXTENSION
			sp2 = new jSLICe(img);
			sp2.process(3, 0.2f);
			//sp.getSegmentation().printData();
			sp2.getSegmentation().showLabelling();
		
		} else {
			System.out.println("ERROR: resources image '"+path+"' was not found!");
		}
		
	}

}
