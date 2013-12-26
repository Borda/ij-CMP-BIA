/**
 * @file
 */
package sc.fiji.CMP_BIA.tools;

import java.util.Arrays;

/** 
 * @class MatrixTools
 * @version 0.1
 * @date 24/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category tools
 * 
 * @brief implementation of a few basic matrix operations
 */
public class MatrixTools {

	/**
	 * Finds sums per each row or column in given matrix depending on 
	 * chosen dimension by user 
	 * 
	 * @param m is matrix of type double[M][N]
	 * @param axis specify if we search per rows (0) or columns (1)
	 * @return int[M] or int[N] maximal values is each row or column
	 */
	public static float[] matrixSumInDim(float[][] m, int axis) {
		if (axis == 0) {
			return matrixSumInRows(m);
		} else {
			return matrixSumInCols(m);
		}
	}
	
	/**
	 * Finds sums per each column in given matrix 
	 * 
	 * @param m is matrix of type double[M][N]
	 * @return int[N] maximal values is each row
	 */
	public static float[] matrixSumInCols(float[][] m) {
		// array of indexes
		float[] sum = new float[m[0].length];
		Arrays.fill(sum, 0);
		//
		for (int i=0; i<m[0].length; i++) {
			for (int j=0; j<m.length; j++) {
				sum[i] += m[j][i];
			}
		}
				
		return sum;
	}

	/**
	 * Finds sums per each row in given matrix
	 * 
	 * @param m is matrix of type double[M][N]
	 * @return int[M] maximal values is each column
	 */
	public static float[] matrixSumInRows(float[][] m) {
		// array of indexes
		float[] sum = new float[m.length];
		Arrays.fill(sum, 0);
		//
		for (int i=0; i<m.length; i++) {
			for (int j=0; j<m[0].length; j++) {
				sum[i] += m[i][j];
			}
		}
				
		return sum;
	}


	/**
	 * Finds all maximas per each row or column in given matrix depending on 
	 * chosen dimension by user 
	 * 
	 * @param m is matrix of type double[M][N]
	 * @param axis specify if we search per rows (0) or columns (1)
	 * @return int[M] or int[N] maximal values is each row or column
	 */
	public static int[] matrixMaxInDim(float[][] m, int axis) {
		if (axis == 0) {
			return matrixMaxInRows(m);
		} else {
			return matrixMaxInCols(m);
		}
	}
	
	/**
	 * Finds all maxims per each column in given matrix 
	 * 
	 * @param m is matrix of type double[M][N]
	 * @return int[N] maximal values is each row
	 */
	public static int[] matrixMaxInCols(float[][] m) {
		// array of indexes
		int[] indexes = new int[m[0].length];
		Arrays.fill(indexes, 0);
		for (int i=0; i<m[0].length; i++) {
			for (int j=0; j<m.length; j++) {
				if (m[j][i] > m[indexes[i]][i]) {
					indexes[i] = j;
				}
			}
		}
				
		return indexes;
	}
	
	/**
	 * Finds all maxims per each row in given matrix
	 * 
	 * @param m is matrix of type double[M][N]
	 * @return int[M] maximal values is each column
	 */
	public static int[] matrixMaxInRows(float[][] m) {
		// array of indexes
		int[] indexes = new int[m.length];
		Arrays.fill(indexes, 0);
		for (int i=0; i<m.length; i++) {
			for (int j=0; j<m[0].length; j++) {
				if (m[i][j] > m[i][indexes[i]]) {
					indexes[i] = j;
				}
			}
		}
				
		return indexes;
	}

	/**
	 * Returns position of minimum in given matrix
	 * 
	 * @param m 
	 * @return int[] position of minimal value {row, col} 
	 */
	public static int[] matrixMin(float[][] m){
		// init search
		int[] index = new int[] {0, 0};
		double min = m[0][0];
		
		// over all matrix elements
		for(int i=0; i < m.length; i++){		
			for(int j=0; j < m[0].length; j++){				
				if(m[i][j] < min){				
					index[0] = i;				
					index[1] = j;
					min = m[i][j];
				}				
			}		
		}
		
		return index;
	}

	/**
	 * Returns position of maximum in given matrix
	 * 
	 * @param m 
	 * @return int[] position of minimal value {row, col}
	 */
	public static int[] matrixMax(float[][] m){
		// init search
		int[] index = new int[] {0, 0};
		double max = m[0][0];
		
		// over all matrix elements
		for(int i=0; i < m.length; i++){		
			for(int j=0; j < m[0].length; j++){				
				if(m[i][j] > max){				
					index[0] = i;				
					index[1] = j;
					max = m[i][j];
				}				
			}		
		}
		
		return index;
	}
	
	/**
	 * Reshape the matrix such that it keep order of elements
	 *  
	 * @param A - input matrix
	 * @param m - new row count
	 * @param n - new column count
	 * @return matrix B with order m x n
	 */
	public static float[][] reshape(float[][] A, int m, int n) throws Exception {
		// check dimensions compatibility
		if(A.length*A[0].length != m*n) {
			throw new Exception("New matrix must be of same area as matix A");
		}
		float[][] B = new float[m][n];
		float[] A1D = matrix2array(A);
		int index = 0;
		// copy elements
		for(int i=0; i<n; i++){
			for(int j=0; j<m; j++){
				B[j][i] = A1D[index++];
			}
		}
			
		return B;
	}
   
	/**
	 * Transforms 2D matrix to 1D by concatenation
	 * 
	 * @param A
	 * @return
	 */
	public static float[] matrix2array(float[][] A){
		// init matrix
		float[] B = new float[A.length * A[0].length];
		int index = 0;
		// copy
		for(int i=0; i<A.length; i++){
			for(int j=0; j<A[0].length; j++){
				B[index++] = A[i][j];
			}
		}
		
		return B;
	}
	
	/**
	 * Find a minimum of a vector
	 * 
	 * @param v is a vector of double values
	 * @return
	 */
	public static double min(double[] v) {
		double r = v[0];
		for(int i=1; i<v.length; i++) {
			if(r > v[i]) {
				r = v[i];
			}
		}
		return r;
	}
	

	/**
	 * Matrix Vector Multiplication
	 * 
	 * @param a = coefA in main()
	 * @param b = v in main()
	 * @return double[] a vector multiplication of a[][] and b[]
	 */
	public static double[] dot(double[][] a, double[] b) {
		// To check if columns of a[][] are same as rows in b[] 
		assert (b.length == a[0].length);
		// sum of products of columns of a[][] with b[].
		// assigns r[i] with sum and reinitialized to 0.0
		double[] r = new double[a.length];
		Arrays.fill(r, 0);
		
		for(int i=0;i<a.length;i++) {
			for(int j=0;j<a[0].length;j++) {
				r[i] += a[i][j]*b[j];
			}
		}
		return r;
	}


	
	/**
	 * normalise the a vector by sum of all elements
	 * 
	 * @param v is a vector of double values
	 * @return double[] normed values
	 */
	public static double[] normVector(double[] v, double d) {
		// new vector
		double[] z = new double[v.length];
		// normalise
		for(int j=0;j<v.length; j++) {
			z[j]=v[j] / d;
		}
		return z;
	}
	
	/**
	 * normalise the a vector by sum of all elements
	 * 
	 * @param v is a vector of double values
	 * @return double[] normed values
	 */
	public static double[] normVector(double[] v) {
		double sum = vectorSum(v);
		return normVector(v, sum);
	}

	/**
	 * 
	 * @return
	 */
	public static float matrixSum(float[][] m) {
		float sum=0;
		// counting the sum
		for(int i=0; i<m.length; i++) {
			for(int j=0; j<m[0].length; j++) {
				sum+=m[i][j];
			}
		}
		return sum;
	}

	/**
	 * 
	 * @return
	 */
	public static float[][] matrixAdd(float[][] m, float d) {
		float[][] r = new float[m.length][m[0].length];
		// incrementing
		for(int i=0; i<m.length; i++) {
			for(int j=0; j<m[0].length; j++) {
				r[i][j] = m[i][j] + d;
			}
		}
		return r;
	}

	/**
	 * 
	 * @return
	 */
	public static double vectorSum(double[] v) {
		double sum=0.;
		// counting the sum
		for(int i=0; i<v.length; i++) {
			sum+=v[i];
		}
		return sum;
	}
	
	/**
	 * normalise the a matrix by sum of all elements
	 * 
	 * @param m is a matrix of double values
	 * @return double[][] normed values
	 */
	public static float[][] normMatrix(float[][] m) {
		float sum = matrixSum(m);
		return normMatrix(m, sum);
	}
	
	/**
	 * normalise the a matrix by sum of all elements
	 * 
	 * @param m is a matrix of double values
	 * @param d is s double number for division
	 * @return double[][] normed values
	 */
	public static float[][] normMatrix(float[][] m, float d) {
		// new vector
		float[][] r = new float[m.length][m[0].length];
		d = (d==0.) ? Float.MIN_VALUE : d;
		// normalise
		for(int i=0; i<m.length; i++) {
			for(int j=0; j<m[0].length; j++) {
				r[i][j] = m[i][j] / d;
			}
		}
		return r;
	}
	
	/**
	 * normalise the a matrix by a vector relented to the first dimension
	 * 
	 * @param m is a matrix of double values
	 * @param d is s double number for division
	 * @return double[][] normed values
	 */
	public static float[][] normMatrix(float[][] m, float[] v) {
		assert (m.length == v.length);
		// new vector
		float[][] r = new float[m.length][m[0].length];
		float f;
		// normalise
		for(int i=0; i<m.length; i++) {
			f = (v[i]==0.) ? Float.MIN_VALUE : v[i];
			for(int j=0; j<m[0].length; j++) {
				r[i][j] = m[i][j] / f;
			}
		}
		return r;
	}
	
	/**
	 * Compute the outer product of two vectors.
	 * 
	 * @return double[][] matrix of out[i, j] = a[i] * b[j]
	 */
	public static float[][] vectorOuter(float[] a, float[] b) {
		// create the matrix
		float[][] r = new float[a.length][b.length];
		// multiplication
		for(int i=0; i<a.length; i++) {
			for(int j=0; j<b.length; j++) {
				r[i][j] = a[i] * b[j];
			}
		}
		return r;
	} 
	
	/**
	 * Matrix division by elements of two matrixes with the same size
	 * 
	 * @param p
	 * @param fs
	 * @return
	 */
	public static float[][] matrixDiv(float[][] p, float[][] fs) {
		assert (p.length == fs.length);
		assert (p[0].length == fs[0].length);
		//
		float[][] r = new float[p.length][p[0].length];
		//
		for(int i=0; i<p.length; i++) {
			for(int j=0; j<p[i].length; j++) {
				r[i][j] = p[i][j] / fs[i][j];
			}
		}
		return r;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static float vectorDotProduct(float[] a, float[] b) {
		assert (a.length == b.length);
		float r = 0;
		// summing
		for(int i=0; i<a.length; i++) {
				r += a[i] * b[i];
		}
		return r;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static float matrixTensorDot(float[][] a, float[][] b) {
		assert (a.length == b.length);
		assert (a[0].length == b[0].length);
		//
		float r = 0;
		//
		for(int i=0; i<a.length; i++) {
			for(int j=0; j<a[i].length; j++) {
				r += a[i][j] * b[i][j];
			}
		}
		return r;
	}
	
	/**
	 * apply the exp function to each element and reduce all infinities and NAN
	 * 
	 * @param v is a vector of double values
	 * @return double[] exposed values
	 */
	public static float[] vectorExp(float[] v) {
		float[] e = new float[v.length];
		for(int i=0; i<v.length; i++) {
			e[i] = (float) Math.exp(v[i]);
			// if not valid value
			if(e[i]==Float.NaN) {
				e[i]=0;
			} else if(e[i]==Double.NEGATIVE_INFINITY) {
				e[i]=Float.MIN_VALUE;
			} else if(e[i]==Double.POSITIVE_INFINITY) {
				e[i]=Float.MAX_VALUE;
			}
		}
		return e;
	}
	
	/**
	 * apply the log function to each element and reduce all infinities and NAN
	 * 
	 * @param m is a matrix of double values
	 * @return double[][] log values
	 */
	public static float[][] matrixLog(float[][] m) {
		float[][] r = new float[m.length][m[0].length];
		for(int i=0; i<m.length; i++) {
			for(int j=0; j<m[0].length; j++) {
				r[i][j] = (float) Math.log(m[i][j]);
				// if not valid value
				if(r[i][j]==Float.NaN) {
					r[i][j]=0;
				} else if(r[i][j]==Double.NEGATIVE_INFINITY) {
					r[i][j]=Float.MIN_VALUE;
				} else if(r[i][j]==Double.POSITIVE_INFINITY) {
					r[i][j]=Float.MAX_VALUE;
				}
			}
		}
		return r;
	}
	
}
