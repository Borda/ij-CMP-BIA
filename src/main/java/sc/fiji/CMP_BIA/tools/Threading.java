package sc.fiji.CMP_BIA.tools;

import ij.Prefs;

public class Threading {


	/** 
	 * Create a Thread[] array as large as the number of processors available. 
     * From Stephan Preibisch's Multithreading.java class. See: 
     * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
     */  
    public static int nbAvailableThread() {  
    	return Prefs.getThreads();
        //return Runtime.getRuntime().availableProcessors();   
    } 
    
    /** 
     * Start all given threads and wait on each of them until all are done. 
     * From Stephan Preibisch's Multithreading.java class. See: 
     * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD 
     */  
    public static void startAndJoin(Thread[] threads) {  
        for (int ithread = 0; ithread < threads.length; ++ithread) {  
            threads[ithread].setPriority(Thread.NORM_PRIORITY);  
            threads[ithread].start();  
        }  
  
        try  {     
            for (int ithread = 0; ithread < threads.length; ++ithread)  
                threads[ithread].join();  
        } catch (InterruptedException ie) {  
            throw new RuntimeException(ie);  
        }  
    } 
	
}