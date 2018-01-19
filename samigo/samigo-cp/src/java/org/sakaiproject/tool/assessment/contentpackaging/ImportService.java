/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.contentpackaging;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Copyright: Copyright (c) 2007 Sakai</p>
 * @version $Id$
 */

@Slf4j
public class ImportService {
	private String qtiFilename;

	public String unzipImportFile(String filename) {
		FileInputStream fileInputStream = null;
		FileOutputStream ofile = null;
		ZipInputStream zipStream = null;
		ZipEntry entry = null;

		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
		StringBuilder unzipLocation = new StringBuilder((String)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_REPOSITORY_PATH"));
	    log.debug("****"+unzipLocation);
	    unzipLocation.append("/jsf/upload_tmp/qti_imports/");
	    unzipLocation.append(AgentFacade.getAgentString());
	    unzipLocation.append("/unzip_files/");
	    unzipLocation.append(Long.toString(new java.util.Date().getTime()));
	    
	    try {
	    	fileInputStream = new FileInputStream(new File(filename));
	    	byte[] data = new byte[fileInputStream.available()];
	    	fileInputStream.read(data, 0, fileInputStream.available());

	    	File dir = new File(unzipLocation.toString()); // directory where file would be saved
	    	if (!dir.exists()) {
	    		if (!dir.mkdirs()) {
	    			log.error("unable to mkdir " + dir.getPath());
	    		}
	    	}

	    	Set dirsMade = new TreeSet();
	    	zipStream = new ZipInputStream(new ByteArrayInputStream(data));
	    	entry = (ZipEntry) zipStream.getNextEntry();
	    	// Get the name of the imported zip file name. The value of "filename" has timestamp append to it.
	    	String tmpName = filename.substring(filename.lastIndexOf("/") + 1);
	    	qtiFilename = "exportAssessment.xml";
	    	ArrayList xmlFilenames = new ArrayList();
	    	while (entry != null) {
	    		String zipName = entry.getName();
	    		int ix = zipName.lastIndexOf('/');
	    		if (ix > 0) {
	    			String dirName = zipName.substring(0, ix);
	    			if (!dirsMade.contains(dirName)) {
	    				File d = new File(dir.getPath() + "/" + dirName);
	    				// If it already exists as a dir, don't do anything
	    				if (!(d.exists() && d.isDirectory())) {
	    					// Try to create the directory, warn if it fails
	    					if (!d.mkdirs()) {
	    						log.error("unable to mkdir " + dir.getPath() + "/" + dirName);
	    					}
	    					dirsMade.add(dirName);
	    				}
	    			}
	    		}

	    		File zipEntryFile = new File(dir.getPath() + "/" + entry.getName());
	    		if (!zipEntryFile.isDirectory()) {
	    			ofile = new FileOutputStream(zipEntryFile);
	    			byte[] buffer = new byte[1024 * 10];
	    			int bytesRead;
	    			while ((bytesRead = zipStream.read(buffer)) != -1) {
	    				ofile.write(buffer, 0, bytesRead);
	    			}
	    		}

	    		// Now try to get the QTI xml file name from the imsmanifest.xml
	    		if ("imsmanifest.xml".equals(entry.getName())) {
	    			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    			try {
	    				DocumentBuilder db = dbf.newDocumentBuilder();
	    				Document doc = db.parse(zipEntryFile);
	    				doc.getDocumentElement().normalize();
	    				NodeList nodeLst = doc.getElementsByTagName("resource");
	    				Node fstNode = nodeLst.item(0);
	    				NamedNodeMap namedNodeMap= fstNode.getAttributes();
	    				qtiFilename = namedNodeMap.getNamedItem("href").getNodeValue();
	    			}
	    			catch (Exception e) {
	    				log.error("error parsing imsmanifest.xml");
	    			}
	    		}
	    		else if (entry.getName() != null && entry.getName().trim().endsWith(".xml")) {
	    			xmlFilenames.add(entry.getName().trim());
	    		}
	    	
	    		zipStream.closeEntry();
	    		entry = zipStream.getNextEntry();
	    	}
	    	// If the QTI file doesn't exist in the zip, 
	    	// we guess the name might be either exportAssessment.xml or the same as the zip file name
	    	if (!xmlFilenames.contains(qtiFilename.trim())) {
	    		if (xmlFilenames.contains("exportAssessment.xml")) {
	    			qtiFilename = "exportAssessment.xml";
	    		}
	    		else {
	    			qtiFilename = tmpName.substring(0, tmpName.lastIndexOf("_")) + ".xml";
	    		}
	    	}
	    } catch (FileNotFoundException e) {
	    	log.error(e.getMessage());
	    } catch (IOException e) {
	    	log.error(e.getMessage());
	    } finally {
	    	if (ofile != null) {
	    		try {
	    			ofile.close();
	    		} catch (IOException e) {
	    			log.error(e.getMessage());
	    		}
	    	}
	    	if (fileInputStream != null) {
	    		try {
	    			fileInputStream.close();
	    		} catch (IOException e) {
	    			log.error(e.getMessage());
	    		}
	    	}
	    	if (zipStream != null) {
	    		try {
	    			zipStream.close();
	    		} catch (IOException e) {
	    			log.error(e.getMessage());
	    		}
	    	}
	    }
	    return unzipLocation.toString();
	}

	public String setQTIFilename() {
		return qtiFilename;
	}
	
	public String getQTIFilename() {
		return qtiFilename;
	}
}
