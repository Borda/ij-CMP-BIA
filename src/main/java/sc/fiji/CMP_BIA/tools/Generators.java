package sc.fiji.CMP_BIA.tools;

import java.util.ArrayList;
import java.util.Random;

/**
 * @class Generators
 * @version 0.1
 * @date 13/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * 
 * @brief Simple collection of a few method used for generation systematic 
 * or random sequences of numbers 
 */
public class Generators {


	/**
	 * Generates a random permutation of vector from 0 to nb-1
	 * 
	 * @param nb length of the permutation vector (0; nb-1)
	 * @return
	 */
	public static int[] gPermutation(int nb) {
		int[] vec = new int[nb];
		// generate all possible elements
		for (int i=0; i<nb; i++) {
			vec[i] = i;
		}
		return gPermutation(vec);
	}
	
	/**
	 * Generates a random permutation of given vector numbers, basically 
	 * it keeps all numbers and just change their order randomly
	 * 
	 * @param vec is a vector of integer numbers to be changed
	 * @return returns vector of the same numbers with changed order
	 */
	public static int[] gPermutation(int[] vec) {
		// init
		ArrayList<Integer> list = new ArrayList<Integer>(vec.length);
		for (int i=0; i<vec.length; i++) {
			list.add(vec[i]);
		}
		int[] perm = new int[vec.length];
		Random randomGenerator = new Random();
		int n, count = vec.length;
		
		// resoting array such that it takes random element from the rest
		for (int i=0; count>0; i++) {
			n = randomGenerator.nextInt(count);
			perm[i] = (int)list.get(n);
			list.remove(n);
			count --;
		}
		
		return perm;
	}
	
	/**
	 * It assumes a continuous set of indexes from 0 to (nbTotal-1) and take 
	 * a random subset of these indexes.
	 * In case you ask for more chosen then is the total number you get just 
	 * permutation of the existing indexes
	 * 
	 * @param nbChosen size of a subset of choasen indexes
	 * @param nbTotal is number of all indexes (basically the max index) 
	 * @return vector of chosen indexes
	 */
	public static int[] gUniqueRandomIndexes(int nbChosen, int nbTotal) {
		// init new array of indexes
		int[] idx = new int[nbChosen];
		Random randomGenerator = new Random();
		int n;
		boolean found;
		
		// if more indexes are asked the is the maximum
		if (nbChosen >= nbTotal) {
			idx = gPermutation(nbTotal);
		// if the number is much smaller then the maximum
		} else if ( (nbTotal/nbChosen) > 2 ) {
			int i = 0;
			while ( i < nbChosen ) {
				// random integers in range 0..max-1
				n = randomGenerator.nextInt(nbTotal);
				found = false;
				// check if the a element already exists 
				for (int j=0; j<i; j++) {
					if (idx[j] == n) {
						found = true;
						break;
					}
				}
				// if not in array add it
				if (!found) {
					idx[i] = n;
					i ++;
				}
			}			
		// if the number is larger then half of the maximum
		} else {
			// transform the permutation so we can remove elements
			int[] vec = gPermutation(nbTotal);
			// transcription only nb elements
			for (int i=0; i<nbChosen; i++) {
				idx[i] = vec[i];
			}
		}
		
		return idx;
	}
	
}
