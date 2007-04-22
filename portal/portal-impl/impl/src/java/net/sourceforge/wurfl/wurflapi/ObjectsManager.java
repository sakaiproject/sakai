/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/ObjectsManager.java,v 1.3 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.io.*;
import java.util.*;
//import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <b>Luca Passani</b>, passani at eunet dot no
 * <br><br>
 * Rather than initializing a new CapabilityMatrix and UAManager
 * each time you need one, you should request ObjectsManager to give 
 * you the instance of an existing one.<br>
 * The advantage of this approach are huge in terms of
 * performance and memory usage, particularly in the contaxt of
 * web applications.
 * 
 */
public class ObjectsManager {
    
    private static Object lock = new Object();
    private static Wurfl wurflInstance = null;
    private static CapabilityMatrix capabilityMatrixInstance = null;
    private static UAManager UAManagerInstance = null;
    private static ListManager ListManagerInstance = null;
    private static Log log = LogFactory.getLog(ObjectsManager.class);
    
    /**
     * You are not allowed to manipulate a Wurfl object directly through
     * the published API. This method is left public because it gives you a chance
     * to initialize the WURFL with a <code>wurfl.xml</code> file located in
     * a place where the library does not look at by default.
     * 
     */

    public static Wurfl getWurflInstance(String parameter, String patch) {
	synchronized (lock) {
	    if (wurflInstance==null)
            	wurflInstance = new Wurfl (parameter,patch);
	    return wurflInstance;
	}
    }

    public static Wurfl getWurflInstance(String parameter) {
	synchronized (lock) {
	    if (wurflInstance==null)
            	wurflInstance = new Wurfl (parameter);
	    return wurflInstance;
	}
    }
    
    
    static Wurfl getWurflInstance() {
	synchronized (lock) {
	    if (wurflInstance==null) {
		String param = getWurflFileLocation();	
		wurflInstance = new Wurfl (param);
	    }
            return wurflInstance;
	}
    }
    /*
     LUCA: NOBODY REALLY USED THIS. IT'S CONFUSING TOO. REMOVED
    
     * <b>Experimental!!!</b>
     * This method tells the library to fetch the WURFL off the wurfl
     * website (http://wurfl.sourceforge.net/wurfl.xml).<br>
     * wurfl.xml is a few hundreds kb, so this is not going to be snappy
     * and you are much better off saving a copy of wurfl.xml on
     * your local file system.
     *
    
    public static void wurflWebInit() {
	synchronized (lock) {
	    if (wurflInstance==null)
            	wurflInstance = new Wurfl ("http://wurfl.sourceforge.net/wurfl.xml");
	}
    }

    */
    
    private static String getWurflFileLocation() {
	String param,param2;
	FileInputStream fis;
	
	//have a look at the wurfl.properties file. It may exist
	Properties locations = new Properties(); 
	if (fileExists("wurfl.properties")) {
	    log.info("wurfl.properties file found. Lemme have a look...");
	    try {
		fis = new FileInputStream("wurfl.properties"); 
		locations.load(fis);
		fis.close();
	    } 
	    catch (IOException ioe) {
		System.err.println("problems with wurfl.properties");
		ioe.printStackTrace();
		throw new WurflException("Problems with wurfl.properties");
	    } 
	    
	    param = locations.getProperty("wurflpath");
	    if (param == null) {
		log.info("Expected wurflpath property not found in wurfl.properties file");
		param = "";
	    }
	    if (param.indexOf("file://") != -1) {
		param2 = param.substring(7,param.length());
	    } else {
		param2 = param;	
	    }
	    //param 2 is used to get rid of 'file://'
	    if (fileExists(param) || fileExists(param2)) {
		log.info("using "+param+" file found in wurfl.properties");
		return param;
	    } else {
		log.info("file '"+param + "' (found in wurfl.properties)  does not exist");	
	    }
	}
	
	
	log.info("Last try. Looking for wurfl.xml in temp directory");
	if (System.getProperty("os.name").indexOf("Windows") != -1) {
	    param = "C:\\temp\\wurfl.xml";
	    if (fileExists(param)) {
		log.info(param+" found! I'll use this");
		return param;
	    }
	} else {
	    param = "/tmp/wurfl.xml";	
	    if (fileExists(param)) {
		log.info(param+" found! I'll use this");
                return param;
	    }
	}

	String sys_prop = System.getProperty("wurflpath");
	if (sys_prop != null) {
		log.info(sys_prop+" found! I'll use this");	    
		return sys_prop;
	}
	
	//no wurfl found!
	log.info("WURFL not found anywhere");
	log.info("You have 3 possibilities:");
	log.info("- define wurfl.properties in the same directory");
	log.info("  as your application and provide the wurfl.xml path");
	log.info("  ex: wurflpath = file://C:\\projects\\wurfl\\resources\\wurfl.xml");
	log.info("");
	log.info("- place wurfl.xml in either C:\\temp (Windows) or /tmp (Unix)");
	log.info("");
	log.info("In a servlet environment, initFromWebApplication() can be used to initialize");
	log.info("using the wurfl at /WEB-INF/wurfl.xml");
	log.info("");

	log.info("- the API will also look at the 'wurflpath' System property");
	log.info("");
	
	throw new WurflException("Cannot find WURFL file (wurfl.xml)");
    }
    
    //tiny utility to see if file exists 
    private static boolean fileExists(String path) {
	
	File file = new File(path);
	return file.exists ();
    }
    
    /** 
     * Use this method to retrieve the existing instance of the CapabilityMatrix
     * (or get one initialized for you). Similar to a Singleton in a way.
     */
    
    public static CapabilityMatrix getCapabilityMatrixInstance() {
	synchronized (lock) {
	    if (wurflInstance==null)
            	wurflInstance = new Wurfl (getWurflFileLocation());
	    if (capabilityMatrixInstance==null)
		capabilityMatrixInstance = new CapabilityMatrix(wurflInstance);
	    return capabilityMatrixInstance;
	}
    }
    
    /** 
     * Use this method to retrieve the existing instance of the UAManager
     * (or get one initialized for you). 
     */
    
    public static UAManager getUAManagerInstance() {
	synchronized (lock) {
	    	if (wurflInstance==null)
		    wurflInstance = new Wurfl (getWurflFileLocation());
		if (UAManagerInstance==null)
		    UAManagerInstance = new UAManager(wurflInstance);
            	return UAManagerInstance;
	}
    }

    
    /** 
     * Use this method to retrieve the existing instance of the ListManager
     * (or get one initialized for you). 
     */
    
    public static ListManager getListManagerInstance() {
	synchronized (lock) {
	    if (wurflInstance==null)
		wurflInstance = new Wurfl (getWurflFileLocation());
	    if (ListManagerInstance==null)
		ListManagerInstance = new ListManager(wurflInstance);
	    return ListManagerInstance;
	}
    }
    

    /** 
     * Use this method to understand if the WURFL is already initialized
     * or not
     */
    
    public static boolean isWurflInitialized() {
	synchronized (lock) {
	    if (wurflInstance==null) {
		return false;
	    } else {
		return true;
	    }
	}
    }
  
    /** 
     * Get an XMLized version of the WURFL (WURFL+patch Object Model turned into an XML file)
     */
    
    public static String getWURFLAsXML() {
	synchronized (lock) {
	    if (wurflInstance==null) {
		return "";
	    } else {
		return wurflInstance.toXML();
	    }
	}
    }
  

    /** 
     * Use this method to initialize from inside a web application
     */
    
    public static void initFromWebApplication(ServletContext sc) {
	
	synchronized (lock) {
	    if (wurflInstance==null) {
		String warpath = "/WEB-INF/wurfl.xml";
		String warpathpatch = "/WEB-INF/wurfl_patch.xml";
		InputStream in1 = sc.getResourceAsStream(warpath);
		InputStream in2 = sc.getResourceAsStream(warpathpatch);
		if (in1 != null) {
		    log.info("Initializing web-app from stream with "+warpath);
		    wurflInstance = new Wurfl(in1,in2);			
		} else {
			log.info("initFromWebApplication(ServletContext): "+
					   "\nCannot initialize Wurfl. no "+warpath+" found!");
		}
		/*
		String path = sc.getRealPath("/WEB-INF/wurfl.xml");
		File file = new File(path);
		if (file.exists()) {
		    log.info(path+" file exists");
		    String path2 = sc.getRealPath("/WEB-INF/wurfl_patch.xml");
		    File file2 = new File(path2);
		    if (file2.exists()) {
			log.info("Initializing web-app with "+file+" and "+file2);
			wurflInstance = new Wurfl(path,path2);
		    } else {
			log.info("Initializing web-app with "+file);
			wurflInstance = new Wurfl(path);
		    }
		} else {
		    log.info(path+" file DOES NOT exist");
		    //could be a WAR file. Let's give it a try with getResourceAsStream(). No patch file
		    String warpath = "/WEB-INF/wurfl.xml";
		    String warpath_patch = "/WEB-INF/wurfl_patch.xml";
		    InputStream in = sc.getResourceAsStream(warpath);
		    InputStream in_patch = sc.getResourceAsStream(warpath_patch);
		    if (in != null) {
			log.info("Initializing web-app from stream with "+warpath);
			wurflInstance = new Wurfl(in,in_patch);			
		    }
		}
                */
	    }
	}
    }



    /** 
     * Use this method to initialize if you have your WURFL in unusual places
     */
    
    public static void initFromWebApplication(String path) {
	
	synchronized (lock) {
	    if (wurflInstance==null) {
		File file = new File(path);
		if (file.exists()) {
		    wurflInstance = new Wurfl(path);
		    log.info("Initializing web-app with "+path);
		} else {
		    log.info("WARNING: initialization failed. Could not find a "+
				       "WURFL file at "+path);
		}
	    }
	}
    }

    /** 
     * Use this method to initialize if you have your WURFL in unusual places
     */
    
    public static void initFromWebApplication(String path, String pathToPatch) {
	
	synchronized (lock) {
	    if (wurflInstance==null) {
		File file = new File(path);
		if (file.exists()) {
		    File file2 = new File(pathToPatch);
		    if (file2.exists()) {
			wurflInstance = new Wurfl(path,pathToPatch);
			log.info("Initializing web-app with "+ path +
					   " and "+ pathToPatch);
		    } else{ 
			wurflInstance = new Wurfl(path);
			log.info("Initializing web-app with "+path);
			//log.info("WARNING: patch file not found at "+pathToPatch);
		    }
		} else {
		    log.info("WARNING: initialization failed. Could not find a "+
				       "WURFL file at "+path);
		}
	    }
	}
    }


    /** 
     * This method lets you initialize the WURFL by providing
     * an object which knows how to get to the input streams
     */
    
    public static void initMyWay(WurflSource ws) {
	
	synchronized (lock) {
	    if (wurflInstance==null) {

		InputStream in1 = ws.getWurflInputStream();
		InputStream in2 = ws.getWurflPatchInputStream();
		if (in1 != null) {
		    log.info("Initializing web-app from stream with InputStream.");
		    wurflInstance = new Wurfl(in1,in2);			
		} else {
			log.info("initMyWay(WurflSource): "+
					   "\nCannot initialize Wurfl. InputStream is empty!");
		}
		
	    } else {
		log.info("WARNING: initMyWay() failed. Wurfl was already initialized ");
	    }
	}
    }

    public static void getFilteredWurfl(HashSet capaList, OutputStream out) {

	    if (wurflInstance==null) {
		return;
	    } else {
		wurflInstance.filterCapabilities(capaList, out);
	    }
    }

    /** 
     * Use this method to force the library to reload the WURFL again
     */
    
    public static void resetWurfl() {
	synchronized (lock) {
	    wurflInstance = null; 
	    capabilityMatrixInstance = null;
	    UAManagerInstance = null;
	    ListManagerInstance = null;
	}
	System.gc();
    }

      
        

}
