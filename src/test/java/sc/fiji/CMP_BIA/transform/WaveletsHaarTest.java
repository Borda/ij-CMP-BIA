package sc.fiji.CMP_BIA.transform;

import org.junit.Test;

import sc.fiji.CMP_BIA.tools.Prints;
import sc.fiji.CMP_BIA.transform.wavelets.HaarWavelets;

/**
 * @class Generators
 * @version 0.1
 * @date 1#/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * 
 * @brief ...
 * 
 */
public class WaveletsHaarTest {

	@Test
	public void test_Haar() {
		Prints.printTitle("Haar wavelets");
		
		float[][] image = new float[][]{
				{0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f}, 
				{0.f, 0.56f, 0.36f, 0.62f, 0.65f, 0.48f, 0.f, 0.f}, 
				{0.f, 0.42f, 0.52f, 0.f, 0.f, 0.59f, 0.f, 0.f}, 
				{0.f, 0.48f, 0.54f, 0.f, 0.f, 0.67f, 0.f, 0.f}, 
				{0.f, 0.67f, 0.52f, 0.f, 0.f, 0.76f, 0.f, 0.f}, 
				{0.f, 0.56f, 0.49f, 0.49f, 0.39f, 0.81f, 0.f, 0.f}, 
				{0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f}};
		System.out.println("Image: ");	
		Prints.printMatrix(image);
		
		System.out.println("Haar: ");	
		Prints.printMatrix( HaarWavelets.computeHaarForward(image) );		
	}

}
