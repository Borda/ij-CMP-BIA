/**
 * 
 */
package sc.fiji.CMP_BIA.tools;

/**
 * @class Logging
 * @version 0.1
 * @date 29/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * 
 * @brief Simple class which redirect the stream of the partial messages
 * The decision of the output stream (termina / ij.log) has to be made before compilation
 */
abstract public class Logging {

	static public void logMsg(String str) {
		System.out.println(str);
		// ij.IJ.log(str);
	}
	
}
