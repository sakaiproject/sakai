package org.sakaiproject.sitestats.impl.report.fop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;


public class LibraryURIResolver implements URIResolver {
	private final static String	LIBRARY_HANDLER	= "library://";
	private String libraryRoot = null;
	
	public LibraryURIResolver() {
		libraryRoot = getLibraryRoot();
	}

	public Source resolve(String href, String base) throws TransformerException {
		if(!href.startsWith(LIBRARY_HANDLER) || libraryRoot == null)
			return null;
		FileInputStream fis = null;
		try{
			String resource = href.substring(LIBRARY_HANDLER.length()); // chop off the library://
			fis = new FileInputStream(libraryRoot + resource);
			return new StreamSource(fis, resource);
		}catch(Exception e){
			throw new TransformerException(e);
		}finally{
			if(fis != null)
				try{
					fis.close();
				}catch(IOException e){
					/* Ignore */
				}
		}
	}

	private String getLibraryRoot() {
		String path = null;
		try{
			// get library folder
			String catalina = System.getProperty("catalina.base");
	        if (catalina == null) {
	        	catalina = System.getProperty("catalina.home");
	        }
	        StringBuilder buff = new StringBuilder(catalina);
	        buff.append(File.separatorChar);
	        buff.append("webapps");
	        buff.append(File.separatorChar);
	        buff.append("library");
	        buff.append(File.separatorChar);
	        path = buff.toString();
		}catch(Exception e) {
			path = null;
		}
		return path;
	}
}
