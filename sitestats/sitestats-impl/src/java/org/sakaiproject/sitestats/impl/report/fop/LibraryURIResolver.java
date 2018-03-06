/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.report.fop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.sitestats.api.StatsManager;

@Slf4j
public class LibraryURIResolver implements URIResolver {
	private final static String	LIBRARY_HANDLER		= "library://";
	private final static String	SITESTATS_HANDLER	= "sitestats://";
	private String				libraryRoot			= null;
	private String				sitestatsRoot		= null;
	
	public LibraryURIResolver() {
		this.libraryRoot = getLibraryRoot();
		this.sitestatsRoot = getSitestatsRoot();
	}

	public Source resolve(String href, String base) throws TransformerException {
		if( (href.startsWith(LIBRARY_HANDLER) && libraryRoot != null) 
				|| (href.startsWith(SITESTATS_HANDLER) && sitestatsRoot != null) ) {
			String resource = null;
			String fullResource = null;
			if(href.startsWith(LIBRARY_HANDLER)) {
				resource = href.substring(LIBRARY_HANDLER.length()); // chop off the library://
				fullResource = libraryRoot + resource;
			}
			if(href.startsWith(SITESTATS_HANDLER)){
				resource = href.substring(SITESTATS_HANDLER.length()); // chop off the sitestats://
				String webappStartDir = StatsManager.SITESTATS_WEBAPP;
				if(resource.startsWith(webappStartDir)) {
					resource = resource.substring(webappStartDir.length());
				}
				fullResource = sitestatsRoot + resource;
			}
			FileInputStream fis = null;
			StreamSource ss = null;
			try{
				fis = new FileInputStream(fullResource);
				ss = new StreamSource(fis, resource);
				return ss;
			}catch(FileNotFoundException e){
				throw new TransformerException(e);
			}finally{
				// If FileInputStream is closed as suggested by FindBugs, code doesn't work!
				/*
				 * if(fis != null) { try{ fis.close(); }catch(IOException e){
				 * log.debug("Unable to read library image: "+href); } }
				 */
			}
		}else{
			return null;
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

	private String getSitestatsRoot() {
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
	        buff.append(StatsManager.SITESTATS_WEBAPP);
	        buff.append(File.separatorChar);
	        path = buff.toString();
		}catch(Exception e) {
			path = null;
		}
		return path;
	}
}
