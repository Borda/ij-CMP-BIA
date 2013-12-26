package sc.fiji.CMP_BIA.segmentation.superpixels;

import java.io.*;
import java.util.*;

import sc.fiji.CMP_BIA.tools.Logging;

/**
 * @class SLIC
 * @version 1.0
 * @date 30/06/2013
 * @author Atin Mathur <mathuratin007@gmail.com>
 * @author Ardhendu Shekhar Tripathi <ast.lnmiit@gmail.com>
 * @category image segmentation
 * 
 * @brief This is SLIC superpixel segmentation java implementation for 2D images only. 
 * This is a transcription of the original EPFL code for RGB and Gray images.
 * @see http://ivrg.epfl.ch/research/superpixels
 * 
 * @details Superpixels are becoming increasingly popular for use in the computer
 * vision applications. However, there are few algorithms that output a desired number
 * of regular, compact superpixels with a low computational overhead. Achanta et al.[1] 
 * introduced a novel algorithm called SLIC (Simple Linear Iterative Clustering) that 
 * clusters pixels in the combined five-dimensional color and image plane space to 
 * efficiently generate compact, nearly uniform superpixels. The simplicity of our 
 * approach makes it extremely easy to use - a lone parameter specifies the number of 
 * superpixels and the efficiency of the algorithm makes it very practical. Experiments 
 * show that our approach produces superpixels at a lower computational cost while achieving 
 * a segmentation quality equal to or greater than other state-of-the-art methods, as measured 
 * by boundary recall and under-segmentation error.
 * 
 * References:
 *
 * [1] Achanta, Radhakrishna, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine S??sstrunk. 
 * "Slic superpixels." ??cole Polytechnique F??d??ral de Lausssanne (EPFL), Tech. Rep 149300 (2010).
 */
class SLIC {

	/* class variables */
    private int[] dx4 = {-1, 0, 1, 0};
    private int[] dy4 = {0, -1, 0, 1};
    private int m_width;
    private int m_height;
    private double[] m_lvec = null;
    private double[] m_avec = null;
    private double[] m_bvec = null;

	//==============================================================================
	///	RGB2XYZ
	///
	/// sRGB (D65 illuninant assumption) to XYZ conversion
	//==============================================================================
    void RGB2XYZ(int sR, int sG, int sB, double[] XYZ) {
        double R = sR/255.0;
        double G = sG/255.0;
        double B = sB/255.0;

        double r, g, b;
        if (R<=0.04045) {
            r=R/12.92;
        } else {
            r= Math.pow((R+0.055)/1.055, 2.4);
        }
        if (G<=0.04045) {
            g=G/12.92;
        } else {
            g=Math.pow((G+0.055)/1.055, 2.4);
        }
        if (B<=0.04045) {
            b=B/12.92;
        } else {
            b=Math.pow((B+0.055)/1.055, 2.4);
        }
        XYZ[0] = r*0.4124564 + g*0.3575761 + b*0.1804375;
        XYZ[1] = r*0.2126729 + g*0.7151522 + b*0.0721750;
        XYZ[2] = r*0.0193339 + g*0.1191920 + b*0.9503041;
    }
    
    //===========================================================================
    ///	RGB2LAB
    //===========================================================================
    void RGB2LAB(int sR, int sG, int sB, double[] labVal) {
        //------------------------
        // sRGB to XYZ conversion
        //------------------------
    	double[] XYZ = new double[3]; 
        RGB2XYZ(sR, sG, sB, XYZ);
        
        //------------------------
        // XYZ to LAB conversion
        //------------------------
        double epsilon =  0.008856;	//actual CIE standard
        double kappa = 903.3;		//actual CIE standard
        
        double Xr = 0.950456;	//reference white
        double Yr = 1.0;        //reference white
        double Zr = 1.088754;	//reference white
        
        double xr = XYZ[0]/Xr;
        double yr = XYZ[1]/Yr;
        double zr = XYZ[2]/Zr;

        double fx, fy, fz;
        if (xr > epsilon) {
            fx = Math.pow(xr, 1.0/3.0);
        } else {
            fx = (kappa*xr+16.0)/116.0;
        }
        if (yr > epsilon) {
            fy = Math.pow(yr, 1.0/3.0);
        } else {
            fy = (kappa*yr+16.0)/116.0;
        }
        if (zr > epsilon) {
            fz = Math.pow(zr, 1.0/3.0);
        } else {	
            fz = (kappa*zr+16.0)/116.0;
        }

        labVal[0] = 116.0*fx-16.0;
        labVal[1] = 500.0*(fx-fy);
        labVal[2] = 200.0*(fy-fz);
    }

	//===========================================================================
	///	DoRGBtoLABConversion
	///
	///	For whole image: overlaoded floating point version
	//===========================================================================
    void DoRGBtoLABConversion(int[] ubuff, double[] lvec, double[] avec, double[] bvec) {
        int sz = m_width * m_height;

        double[]labVal = new double[3];
        
        int i=0, r, g, b;
        for (int j=0; j<sz; j++) {
            if (10*j/sz > i) {
            i++;
            }
            r = (ubuff[j] >> 16) & 0xFF;
            g = (ubuff[j] >>  8) & 0xFF;
            b = (ubuff[j]      ) & 0xFF;

            RGB2LAB(r, g, b, labVal);
            
            lvec[j] = labVal[0];
            avec[j] = labVal[1];
            bvec[j] = labVal[2];
        }
    }
    
	//=================================================================================
	/// DrawContoursAroundSegments
	///
	/// Internal contour drawing option exists. One only needs to comment the if
	/// statement inside the loop that looks at neighbourhood.
	//=================================================================================
    void DrawContoursAroundSegments(int[] ubuff, int[] labels, int width, int height, @SuppressWarnings("unused") int color) {
        int[] dx8 = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy8 = {0, -1, -1, -1, 0, 1, 1, 1};
        
        int sz = width * height;

        boolean[] istaken = new boolean[sz];
        int[] contourx = new int[sz];
        int[] contoury = new int[sz];
        
        for (int i = 0; i < sz; i++) {
            istaken[i] = false;
        }

        int mainindex=0, cind=0;
        for (int j=0; j<height; j++) {
            for (int k=0; k<width; k++) {
                    int np = 0;
                    for (int i=0; i<8; i++) {
                        int x = k + dx8[i];
                        int y = j + dy8[i];

                        if ((x>=0 && x<width) && (y>=0 && y<height)) {
                            int index = y*width + x;
                             //if (false == istaken[index])//comment this to obtain internal contours
                               {   if (labels[mainindex] != labels[index]) {
                                   np++;
                            	   }
                               }
                        }
                    }
                    if (np > 1) {
        				contourx[cind] = k;
        				contoury[cind] = j;
        				istaken[mainindex] = true;
        				//img[mainindex] = color;
        				cind++;
        			}
        			mainindex++;
        	}
        }

    	int numboundpix = cind;//int(contourx.size());
    	for( int j = 0; j < numboundpix; j++ ) {
    		int ii = contoury[j]*width + contourx[j];
    		ubuff[ii] = 0xffffff;
    		for( int n = 0; n < 8; n++ ) {
    			int x = contourx[j] + dx8[n];
    			int y = contoury[j] + dy8[n];
    			if( (x >= 0 && x < width) && (y >= 0 && y < height) ) {
    				int ind = y*width + x;
    				if(!istaken[ind]) ubuff[ind] = 0;
    			}
    		}
    	}
    }

	//==============================================================================
	///	DetectLabEdges
	//==============================================================================
    void DetectLabEdges(
            double[] lvec,
            double[] avec,
            double[] bvec,
            int width,
            int height,
            double[] edges) {
        
        for (int j=1; j<height-1; j++) {
            for (int k=1; k<width-1; k++) {
                int i = j*width + k;

                double dx = (lvec[i - 1] - lvec[i + 1]) * (lvec[i - 1] - lvec[i + 1])
                          + (avec[i - 1] - avec[i + 1]) * (avec[i - 1] - avec[i + 1])
                          + (bvec[i - 1] - bvec[i + 1]) * (bvec[i - 1] - bvec[i + 1]);

                double dy = (lvec[i - width] - lvec[i + width]) * (lvec[i - width] - lvec[i + width])
                          + (avec[i - width] - avec[i + width]) * (avec[i - width] - avec[i + width])
                          + (bvec[i - width] - bvec[i + width]) * (bvec[i - width] - bvec[i + width]);

                 edges[i] = (dx*dx + dy*dy);
            }
        }
    }

	//===========================================================================
	///	PerturbSeeds
	//===========================================================================
    void PerturbSeeds(
            double[] kseedsl,
            double[] kseedsa,
            double[] kseedsb,
            double[] kseedsx,
            double[] kseedsy,
            double[] edges) {
        int[] dx8 = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dy8 = {0, -1, -1, -1, 0, 1, 1, 1};

        int numseeds = kseedsl.length;

        for (int n=0; n<numseeds; n++) {
            int ox = (int) kseedsx[n];//original x
            int oy = (int) kseedsy[n];//original y
            int oind = oy*m_width + ox;

            int storeind = oind;
            for (int i=0; i<8; i++) {
                int nx = ox+dx8[i];//new x
                int ny = oy+dy8[i];//new y

                if (nx >= 0 && nx < m_width && ny >= 0 && ny < m_height) {
                    int nind = ny * m_width + nx;
                    if (edges[nind] < edges[storeind]) {
                        storeind = nind;
                    }
                }
            }
            if (storeind != oind) {
                kseedsx[n] = storeind%m_width;
                kseedsy[n] = storeind/m_width;
                kseedsl[n] = m_lvec[storeind];
                kseedsa[n] = m_avec[storeind];
                kseedsb[n] = m_bvec[storeind];
            }
        }
    }
    
    @SuppressWarnings("unused")
	int countSeedSize(int K) {
        int sz = m_width * m_height;
        double step = Math.sqrt((double) sz / (double) K);
        int T = (int) step;
        int xoff = (int) (step / 2);
        int yoff = (int) (step / 2);

        int n = 0;
        for (int y = 0; y < m_height; y++) {
            int Y = (int) (y * step + yoff);
            if (Y > m_height - 1) {
                break;
            }

            for (int x = 0; x < m_width; x++) {
                int X = (int) (x * step + xoff);
                if (X > m_width - 1) {
                    break;
                }
                n++;
            }
        }
        return (n);
    }

	//===========================================================================
	///	GetLABXYSeeds_ForGivenStepSize
	///
	/// The k seed values are taken as uniform spatial pixel samples.
	//===========================================================================
    @SuppressWarnings("unused")
	void GetLABXYSeeds_ForGivenStepSize(
            double[] kseedsl,
            double[] kseedsa,
            double[] kseedsb,
            double[] kseedsx,
            double[] kseedsy,
            int STEP,
            boolean perturbseeds,
            double[] edgemag) {
        int sz = m_width * m_height;
        double step = Math.sqrt((double) sz / (double) STEP);
        int T = (int) step;
        int xoff = (int) (step / 2);
        int yoff = (int) (step / 2);
        int xevenoff = (int) (step / 2);
        int xoddoff = 0;

        int n = 0;
        for (int y = 0; y < m_height; y++) {
            int Y = (int) (y * step + yoff);
            if (Y > m_height - 1) {
                break;
            }

            for (int x = 0; x < m_width; x++) {
                int X = (int) (x * step + xoff);
                if (y%2==0) {
                    X += xevenoff;
                } else {
                    X += xoddoff;
                }
                if (X > m_width - 1) {
                    break;
                }

                int i = Y * m_width + X;

                kseedsl[n] = m_lvec[i];
                kseedsa[n] = m_avec[i];
                kseedsb[n] = m_bvec[i];
                kseedsx[n] = X;
                kseedsy[n] = Y;
                n++;
            }
        }

        if (perturbseeds) {
            PerturbSeeds(kseedsl, kseedsa, kseedsb, kseedsx, kseedsy, edgemag);
        }
    }

	//===========================================================================
	///	PerformSuperpixelSLIC
	///
	///	Performs k mean segmentation. It is fast because it looks locally, not
	/// over the entire image.
	//===========================================================================
    void PerformSuperpixelSLIC(
            double[] kseedsl,
            double[] kseedsa,
            double[] kseedsb,
            double[] kseedsx,
            double[] kseedsy,
            int[] klabels,
            int STEP,
            @SuppressWarnings("unused") double[] edgemag,
            double M) {
        int sz = m_width * m_height;
        int numk = kseedsl.length;
        //----------------
        int offset = STEP;
        //----------------

        double[] clustersize = new double[numk];
        double[] inv = new double[numk];//to store 1/clustersize[k] values

        double[] sigmal = new double[numk];
        double[] sigmaa = new double[numk];
        double[] sigmab = new double[numk];
        double[] sigmax = new double[numk];
        double[] sigmay = new double[numk];
        double[] distvec = new double[sz];

        for (int i_ = 0; i_ < numk; i_++) {
            clustersize[i_] = 0;
            inv[i_] = 0;
            sigmal[i_] = 0;
            sigmaa[i_] = 0;
            sigmab[i_] = 0;
            sigmax[i_] = 0;
            sigmay[i_] = 0;

        }
        for (int i_ = 0; i_ < distvec.length; i_++) {
            distvec[i_] = Double.MAX_VALUE;
        }

        double invwt = 1.0 / ((STEP / M) * (STEP / M));

        int x1, y1, x2, y2;
        double l, a, b;
        double dist;
        double distxy;
        for (int itr=0; itr<10; itr++) {
            //distvec.assign(sz, DBL_MAX);
            for (int i_ = 0; i_ < distvec.length; i_++) {
                distvec[i_] = Double.MAX_VALUE;
            }
            for (int n=0; n<numk; n++) {
                y1 = (int) Math.max(0, kseedsy[n] - offset);
                y2 = (int) Math.min(m_height, kseedsy[n] + offset);
                x1 = (int) Math.max(0, kseedsx[n] - offset);
                x2 = (int) Math.min(m_width, kseedsx[n] + offset);

                for (int y = y1; y < y2; y++) {
                    for (int x = x1; x < x2; x++) {                
                        int i = y * m_width + x;
                        
                        l = m_lvec[i];
                        a = m_avec[i];
                        b = m_bvec[i];

                        dist =    (l - kseedsl[n]) * (l - kseedsl[n])
                                + (a - kseedsa[n]) * (a - kseedsa[n])
                                + (b - kseedsb[n]) * (b - kseedsb[n]);

                        distxy =  (x - kseedsx[n]) * (x - kseedsx[n])
                                + (y - kseedsy[n]) * (y - kseedsy[n]);

                        //------------------------------------------------------------------------
                        dist += distxy * invwt;//dist = sqrt(dist) + sqrt(distxy*invwt);//this is more exact
                        //------------------------------------------------------------------------

                        if (dist < distvec[i]) {
                            distvec[i] = dist;
                            klabels[i] = n;
                        }
                    }
                }
            }
            //-----------------------------------------------------------------
            // Recalculate the centroid and store in the seed values
            //-----------------------------------------------------------------
            //instead of reassigning memory on each iteration, just reset.

            for (int i_ = 0; i_ < numk; i_++) {
                clustersize[i_] = 0;
                sigmal[i_] = 0;
                sigmaa[i_] = 0;
                sigmab[i_] = 0;
                sigmax[i_] = 0;
                sigmay[i_] = 0;
            }

            //------------------------------------
            //edgesum.assign(numk, 0);
            //------------------------------------
            
            int ind = 0;
            for (int r = 0; r < m_height; r++) {
                for (int c = 0; c < m_width; c++) {
                    sigmal[klabels[ind]] += m_lvec[ind];
                    sigmaa[klabels[ind]] += m_avec[ind];
                    sigmab[klabels[ind]] += m_bvec[ind];
                    sigmax[klabels[ind]] += c;
                    sigmay[klabels[ind]] += r;
                    //------------------------------------
                    //edgesum[klabels[ind]] += edgemag[ind];
                    //------------------------------------
                    clustersize[klabels[ind]] += 1.0;
                    ind++;
                }
            }            

            for (int k = 0; k < numk; k++) {
                if (clustersize[k] <= 0) {
                    clustersize[k] = 1;
                }
                inv[k] = 1.0 / clustersize[k];//computing inverse now to multiply, than divide later
            }
            
            for (int k = 0; k < numk; k++) {
                kseedsl[k] = sigmal[k] * inv[k];
                kseedsa[k] = sigmaa[k] * inv[k];
                kseedsb[k] = sigmab[k] * inv[k];
                kseedsx[k] = sigmax[k] * inv[k];
                kseedsy[k] = sigmay[k] * inv[k];
                //------------------------------------
                //edgesum[k] *= inv[k];
                //------------------------------------
            }            
        }
    }
    
	//===========================================================================
	///	SaveSuperpixelLabels
	///
	///	Save labels in raster scan order.
	//===========================================================================
	void SaveSuperpixelLabels(
	    int[]					labels,
	    int					width,
	    int					height,
	    String				filename,
	    String				path) {
	    int sz = width*height;
	    
	    try {
	        String fname = path + filename;
	        OutputStream outfile = new FileOutputStream(fname);
	        PrintStream myOutfile = new PrintStream(outfile);
	        for( int i = 0; i < sz; i++ ) {
	            myOutfile.println(labels[i]);
	        }
	        outfile.close();
	    }  catch (Exception E)
	    {
	        Logging.logMsg("Error!");
	    }
	}
 
	//===========================================================================
	///	EnforceLabelConnectivity
	///
	///	Some superpixels may be unconnected, Relabel them. Recursive algorithm
	/// used here, can crash if stack overflows. This will only happen if the
	/// superpixels are very large, otherwise safe.
	///		STEPS:
	///		1. finding an adjacent label for each new component at the start
	///		2. if a certain component is too small, assigning the previously found
	///		    adjacent label to this component, and not incrementing the label.
	//===========================================================================
    private int EnforceLabelConnectivity(
            int[] labels, // input label that needs to be corrected to remove stray labels
            int width,
            int height,
            int[] nlabels,// new labels
            int numlabels,// the number of labels changes in the end if segments are removed
            int K) // The number of superpixel desired by the user 
    	{
        int sz = width * height;
        for (int i=0; i<sz; i++) {
           nlabels[i] = -1;
        }

        int SUPSZ = sz / K;
        //------------------
        // labeling
        //------------------
        int lab = 0;
        int i = 0;
        int adjlabel = 0;//adjacent label
        int[] xvec = new int[sz];//worst case size
        int[] yvec = new int[sz];//worst case size
        int[] count = new int[1];
        {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    if (nlabels[i] < 0) {
                        nlabels[i] = lab;
                        //-------------------------------------------------------
                        // Quickly find an adjacent label for use later if needed
                        //-------------------------------------------------------
                        {
                            for (int n = 0; n < 4; n++) {
                                int x = w + dx4[n];
                                int y = h + dy4[n];
                                if ((x >= 0 && x < width) && (y >= 0 && y < height)) {
                                    int nindex = y * width + x;
                                    if (nlabels[nindex] >= 0) {
                                        adjlabel = nlabels[nindex];
                                    }
                                }
                            }
                        }
                        xvec[0] = w;
                        yvec[0] = h;

                        count[0] = 1;
                        
                        List<Integer> h_stack = new ArrayList<Integer>();
                        List<Integer> w_stack = new ArrayList<Integer>();
                        h_stack.add(h);
                        w_stack.add(w);

                        int s, x, y, h1, w1, ind;
                        while(!h_stack.isEmpty()) {
                            s = h_stack.size()-1;
                            h1 = h_stack.remove(s);
                            w1 = w_stack.remove(s);
                            for (int i1=0; i1<4; i1++) {
                                y = h1 + dy4[i1];
                                x = w1 + dx4[i1];
                                if ((y < height && y >= 0) && (x < width && x >= 0)) {
                                    ind = y * width + x;
                                    if (nlabels[ind] < 0 && labels[ind] == labels[h1 * width + w1]) {
                                        xvec[count[0]] = x;
                                        yvec[count[0]] = y;
                                        count[0]++;
                                        nlabels[ind] = lab;
                                        h_stack.add(y);
                                        w_stack.add(x);
                                    }
                                }
                            }
                        }

                        //-------------------------------------------------------
                        // If segment size is less then a limit, assign an
                        // adjacent label found before, and decrement label count.
                        //-------------------------------------------------------
                        if (count[0] <= (SUPSZ >> 2)) {
                            for (int c = 0; c < count[0]; c++) {
                                int ind1 = yvec[c] * width + xvec[c];
                                nlabels[ind1] = adjlabel;
                            }
                            lab--;
                        }
                        lab++;
                    }
                    i++;
                }
            }
        }
        numlabels = lab;
        return numlabels;
    }

	//===========================================================================
	///	DoSuperpixelSegmentation_ForGivenStepSize
	///
	/// Originally called DoSegmentation_LABXY
	/// There is option to save the labels if needed. However the filename and
	/// path need to be provided.
	//===========================================================================
    int DoSuperpixelSegmentation_ForGivenK(
            int[] ubuff,
            int width,
            int height,
            int[] klabels,
            int numlabels,
            int K,//required number of superpixels
            double m,//weight given to spatial distance
            boolean LAB_space) {

        //--------------------------------------------------
        m_width = width;
        m_height = height;
        int sz = m_width * m_height;
        //--------------------------------------------------
        //if(0 == klabels) klabels = new int[sz];
        for (int s = 0; s < sz; s++) {
            klabels[s] = -1;
         }
        //--------------------------------------------------
        
        if (LAB_space)//LAB
        {
            m_lvec = new double[sz];
            m_avec = new double[sz];
            m_bvec = new double[sz];
            DoRGBtoLABConversion(ubuff, m_lvec, m_avec, m_bvec);
        } else//RGB
        {
            m_lvec = new double[sz];
            m_avec = new double[sz];
            m_bvec = new double[sz];
            for (int i = 0; i < sz; i++) {
                m_lvec[i] = ubuff[i] >> 16 & 0xff;
                m_avec[i] = ubuff[i] >>  8 & 0xff;
                m_bvec[i] = ubuff[i]       & 0xff;
            }
        }
        //--------------------------------------------------

        boolean perturbseeds = true;
        double[] edgemag = new double[sz];
        for (int i = 0; i < sz; i++) {
            edgemag[i] = 0;
        }
        if (perturbseeds) {
            DetectLabEdges(m_lvec, m_avec, m_bvec, m_width, m_height, edgemag);
        }

        int css = countSeedSize(K);
        double[] kseedsl = new double[css];
        double[] kseedsa = new double[css];
        double[] kseedsb = new double[css];
        double[] kseedsx = new double[css];
        double[] kseedsy = new double[css];

       GetLABXYSeeds_ForGivenStepSize(kseedsl, kseedsa, kseedsb, kseedsx, kseedsy, K, perturbseeds, edgemag);

       int STEP = (int) (Math.sqrt((double) (sz) / (double) (K)) + 2.0);//adding a small value in the even the STEP size is too small.
       PerformSuperpixelSLIC(kseedsl, kseedsa, kseedsb, kseedsx, kseedsy, klabels, STEP, edgemag, m);
            
       numlabels = kseedsl.length;

       int[] nlabels = new int[sz];
       for (int s = 0; s < sz; s++) {
           nlabels[s] = -1;
        }
      
       EnforceLabelConnectivity(klabels, m_width, m_height, nlabels, numlabels, K);
       System.arraycopy(nlabels, 0, klabels, 0, sz);
        
       return (numlabels);
    }
}
