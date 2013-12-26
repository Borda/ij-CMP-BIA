/**
 * 
 */
package sc.fiji.CMP_BIA;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * @author borovji3
 *
 */
public class TestPlugins {

	// an sample image
	public static final String LENA = "http://imagej.net/images/lena.jpg";
	
	/**
	 * 
	 */
	public TestPlugins() {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
        String pluginsDir = System.getProperty("user.dir")+"/target";
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();
	}
	
	/**
	 * 
	 * @param img
	 */
	protected void openImage(String img) {
		// open the Blobs sample
        ImagePlus image = IJ.openImage(img);
        image.show();
	}
	
	/**
	 * 
	 */
	protected void openLena() {
		IJ.run("Lena (68K)");
	}
		
	/**
	 * 
	 */
	public void run_SLIC() {
		// open Lena image from samples
		openLena();
        // RUN THE SLIC
		//IJ.run("SLIC superpixels 2D", "init.=40 regularisation=0.2 overlap=white export show");
		IJ.run("jSLIC superpixels 2D", "init.=60 regularisation=0.15 overlap=blue");
	}
	
	/**
	 * 
	 */
	public void run_Segm() {
	    // RUN THE ...
		String path = System.getProperty("user.dir") + "/src/test/resources/imgs/texture-sample.png";
		openImage(path);
		//openLena();
		IJ.run("Automatic segmentation", "init.=20 number=4");;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// init
		TestPlugins plg = new TestPlugins();
		
		plg.run_SLIC();
		//plg.run_Segm();
		
	}

}
