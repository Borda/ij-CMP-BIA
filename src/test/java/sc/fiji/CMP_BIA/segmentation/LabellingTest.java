/**
 * 
 */
package sc.fiji.CMP_BIA.segmentation;

import java.awt.Color;

import ij.ImagePlus;
import ij.process.ShortProcessor;

import org.junit.Test;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.tools.Connectivity2D;
import sc.fiji.CMP_BIA.tools.Prints;

/**
 * @author borovji3
 *
 */
public class LabellingTest {

	static final int[][] segmMedium = new int[][]{ 
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3},
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3},
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3},
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3},
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3},
			{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3},
			{4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6},
			{4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6},
			{4,4,4,4,4,4,4,4,5,5,5,0,0,0,5,5,5,5,6,6,6,6,6,6,6},
			{4,4,4,4,4,4,4,5,5,5,5,0,0,0,5,5,5,5,6,6,6,6,6,6,6},
			{4,4,4,4,4,4,4,5,5,5,5,0,0,0,5,5,5,5,6,6,6,6,6,6,6},
			{4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6} };

	static final int[][] segmSmall = new int[][]{ 
			{1,1,1,2,2,2,2},
			{1,1,1,2,2,2,2},
			{1,1,1,2,2,2,2},
			{4,4,4,5,5,5,5},
			{4,4,4,4,5,5,5},
			{4,4,4,4,5,5,5} };
	
	static final int[][] segmSmall2 = new int[][]{ 
			{1,1,2,2,2,2,2},
			{1,1,1,1,1,2,2},
			{1,1,2,2,2,2,2},
			{4,4,4,5,5,5,5},
			{4,4,4,4,4,5,5},
			{4,4,4,4,5,5,5} };
	
	@Test
	public void test_labelling2D() {
		Prints.printTitle("Labelling 2D");
		final int[][] data = segmMedium;
		Labelling2D lb = new Labelling2D(data);
		lb.printData();

		System.out.println("\n Maximal label: ");
		System.out.println( lb.getMaxLabel() );

		lb.printHistogram();

		System.out.println("\n All Elements Boundaries: 4");
		Prints.printListListArray( lb.findElementsBoundaries(Connectivity2D.CONNECT4) );

		System.out.println("\n Contunuous Elements Boundaries: 8");
		Prints.printListListArray( lb.findElementsBoundariesPolygon() );
		
		System.out.println("\n Find Segments Connectivity: 4");
		Prints.printMatrix( lb.findSegmentsConnectivity(Connectivity2D.CONNECT4) );
		
		System.out.println("\n Find Segments Connectivity: 8");
		Prints.printMatrix( lb.findSegmentsConnectivity(Connectivity2D.CONNECT8) );
		
		System.out.println("\n Relabel: ");
		// int[] lut = new int[]{5,4,3,2,1,0};
		int[] lut = new int[]{7,6,5,4,3,2,1};
		Prints.printArray(lut);
		lb.reLabel(lut);
		lb.printData();

		lb.printHistogram();
		
		Labelling2D lb2 = new Labelling2D(data);
		lb2.printHistogram();
		
		System.out.println("\n Find Multi-Class Boundary Points: ");
		Prints.printMatrix( lb.findMultiClassBoundaryPoints(Connectivity2D.CONNECT4) );


		System.out.println("\n Overlaps: ");
		int[] shift = new int[]{0,0};
		Prints.printArray(shift);
		Prints.printMatrix( lb.overlaps(lb2, shift) );

		System.out.println("\n Overlaps: ");
		shift = new int[]{1,-1};
		//shift = new int[]{3,-4};
		Prints.printArray(shift);
		Prints.printMatrix( lb.overlaps(lb2, shift) );
		
		String path = System.getProperty("user.dir") + "/temp/exportLabelling.txt";
		System.out.println("\n Export to a file: " +path);
		lb.exportToFile(path);
		
		System.out.println("\n Show RIO: ");
		ImagePlus im = new ImagePlus("test", new ShortProcessor(lb.getDims()[0], lb.getDims()[1]));
		im.show();
		lb.showOverlapROIs(im);	
		lb.showOverlapContours(im, Color.RED);
		lb.showOverlapLabeling(im, 0.5);
		
		//GeneralTest.wait(10);
						
	}

}
