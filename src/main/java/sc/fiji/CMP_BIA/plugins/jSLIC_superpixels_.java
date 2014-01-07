package sc.fiji.CMP_BIA.plugins;
/**
 * @file
 */

import java.awt.Color;

import sc.fiji.CMP_BIA.segmentation.superpixels.jSLICe;
import sc.fiji.CMP_BIA.tools.Logging;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
//import segmentation.structures.Labelling2D;
 
/**
 * @class jSLIC plugin
 * @version 0.1
 * @date 10/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief A ImageJ plugin for jSLIC superpixel clustering
 * 
 */
public class jSLIC_superpixels_  implements PlugInFilter {
	// handler to the image we work with
	protected ImagePlus image;
	// jSLIC superpixel instance
	protected jSLICe sp;

	// segmentation paramters
	protected int gSize = 30;
	protected float regul = 0.2f;
	// visualisation funtions
	protected boolean showROIs = true;
	protected boolean showSegm = true;
	protected boolean saveSegm = false;
	Color clrOverlay = null;
	// set of colours
	private String[] clrStr = {"none", "white", "yellow", "red", "green", "blue", "black"};
	private Color[] clrs = {null, Color.WHITE, Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};

	/**
	 * This method gets called by ImageJ / Fiji to determine whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus img) {
		//ij.IJ.log("Image type verification...");
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		this.image = img;
		// check that we are working with RGB image
		return DOES_RGB | DOES_8G | DOES_16 | DOES_32;
	}
	
	/**
	 * show the main plugin dialog frame
	 * 
	 * @return true if user chose OK
	 */
	protected boolean showConfigDialog() {
		assert (clrStr.length == clrs.length);
		// Create interface window
		GenericDialog gd = new GenericDialog("jSLIC segmentation");
		gd.addNumericField("Init. grid size: ", this.gSize, 0);
		gd.addNumericField("Regularisation: ", this.regul, 2);
		gd.addChoice("Overlap contours - colour:", clrStr, clrStr[0]);
		gd.addCheckbox("Export segments as ROIs.", true);
		gd.addCheckbox("Show final segmentation.", false);
		gd.addCheckbox("Save segmentation into file.", false);
		
		// show the dialog and quit
		gd.showDialog();
		// if the user clicks "cancel"
		if (gd.wasCanceled()) {		return false;		}

		
		// get values from interface window
		gSize = (int) gd.getNextNumber();
		regul = (float) gd.getNextNumber();
		showROIs = gd.getNextBoolean();
		clrOverlay = clrs[ gd.getNextChoiceIndex() ];
		showSegm = gd.getNextBoolean();
		saveSegm = gd.getNextBoolean();
				
		return true;
	}

	
	/**
	 * This method is run when the current image was accepted.
	 *
	 * @param ip is the current slice
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
						
		// if the user clicks "OK"
		if ( showConfigDialog() ) {		
			
			try {
				image.lock();
				process();
				image.unlock();
			} catch (Exception e) {
			} finally {
				image.unlock();
			}
			
		}
	}
	
	/**
	 * the whole processing method
	 */
	protected void process() {
		// measure the processing time
		long startTime, estimTime;
		
		IJ.showProgress(0.);

		printInfo("jSLIC initialisation...");
		startTime = System.currentTimeMillis();
		
		// init jSLIC superpixels
		sp = new jSLICe(image);
		
		IJ.showProgress(20.);

		printInfo("jSLIC processing...");
		
		sp.process(gSize, regul);
		
		estimTime = System.currentTimeMillis() - startTime;
		ij.IJ.log("jSLIC process took " + Float.toString((float)estimTime/1000) + "s");
		IJ.showProgress(90.);
		
		printInfo("jSLIC visualisation...");
		showSegmentation ();
					
		printInfo("jSLIC finished.");
		IJ.showProgress(100.);
	}
	
	/**
	 * used only for presenting the segmentation results
	 */
	protected void showSegmentation() {
		// show the ROI in ROI manager
		if (showROIs) {
			ij.IJ.log(" -> show ROI manager");
			sp.getSegmentation().showOverlapROIs(image);
		}
		// show the general Overlay
		if (clrOverlay != null) {
			ij.IJ.log(" -> show contour overlap");
			sp.getSegmentation().showOverlapContours(image, clrOverlay);
		}
		// show the segments
		if (showSegm) {
			try {
				// FIXME in case of gray images we cannot create colour segmentation mask
				ij.IJ.log(" -> show segmentation");
				sp.getSegmentation().showOverlapLabeling(image, 0.5);
			} catch (Exception e) {
				IJ.error("Your image is not RGB image.");
			}	
		}
		// saving the segmentation into a file you chose
		if (saveSegm) {
			OpenDialog od = new OpenDialog("Save raw segmentation");
			if (od.getPath() != null) {
				ij.IJ.log(" -> export to file: "+od.getPath());
				Logging.logMsg(" -> export to file");
				sp.getSegmentation().exportToFile( od.getPath() );
			}
		}
	}

	/**
	 * content of About frame
	 */
	public void showAbout() {
	    IJ.showMessage("About jSLIC...",
	                   "Simple Linear Iterative Clustering (jSLIC)\n"+
	                   "Implemented: Jiri Borovec <jiri.borovec@fel.cvut.cz>\n"+
	                   "Version: 0.2 (22/12/2013)\n"+
	                   "This is jSLIC superpixel segmentation for 2D images only for RGB and and gray images.\n"+
	                   "Ref: http://fiji.sc/CMP-BIA_tools"
	                   );
	  } 
	
	/**
	 * 
	 * @param s string text
	 */
	private void printInfo(String s) {
		ij.IJ.log(s);
		ij.IJ.showStatus(s);
	}
}
