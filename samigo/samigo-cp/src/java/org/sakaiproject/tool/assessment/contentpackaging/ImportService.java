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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

/**
 * <p>Copyright: Copyright (c) 2007 Sakai</p>
 * @version $Id$
 */

@Slf4j
public class ImportService {
	private String qtiFilename;

	public String unzipImportFile(String filename) {

		ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		String repositoryPath = serverConfigurationService.getString("samigo.answerUploadRepositoryPath", "${sakai.home}/samigo/answerUploadRepositoryPath/");
		StringBuilder unzipLocation = new StringBuilder(repositoryPath);
		log.debug("**** {}", unzipLocation);
		unzipLocation.append("/jsf/upload_tmp/qti_imports/");
		unzipLocation.append(AgentFacade.getAgentString());
		unzipLocation.append("/unzip_files/");
		unzipLocation.append(Instant.now().toEpochMilli());

	    try (FileInputStream fileInputStream = new FileInputStream(new File(filename))) {
	    	byte[] data = new byte[fileInputStream.available()];
	    	fileInputStream.read(data, 0, fileInputStream.available());

	    	File dir = new File(unzipLocation.toString()); // directory where file would be saved
	    	if (!dir.exists()) {
	    		if (!dir.mkdirs()) {
                    log.warn("Unable to mkdir {}", dir.getPath());
	    		}
	    	}

	    	Set<String> dirsMade = new TreeSet<>();
	    	try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(data))) {
	    		ZipEntry entry = (ZipEntry) zipStream.getNextEntry();
	    		// Get the name of the imported zip file name. The value of "filename" has timestamp append to it.
	    		String tmpName = filename.substring(filename.lastIndexOf("/") + 1);
	    		qtiFilename = "exportAssessment.xml";
	    		List<String> xmlFilenames = new ArrayList<>();
	    		while (entry != null) {
	    			String entryName = entry.getName();
	    			String entryNameTrimmed = entryName.trim();
	    			int ix = entryName.lastIndexOf('/');
	    			if (ix > 0) {
	    				String dirName = entryName.substring(0, ix);
	    				if (!dirsMade.contains(dirName)) {
	    					File d = new File(dir.getPath() + "/" + dirName);
	    					// If it already exists as a dir, don't do anything
	    					if (!(d.exists() && d.isDirectory())) {
	    						// Try to create the directory, warn if it fails
	    						if (!d.mkdirs()) {
	    							log.warn("unable to mkdir {}/{}", dir.getPath(), dirName);
	    						}
	    						dirsMade.add(dirName);
	    					}
	    				}
	    			}

                    File zipEntryFile = new File(dir.getPath() + "/" + entryName);
                    if (!zipEntryFile.isDirectory()) {
	    				try (FileOutputStream ofile = new FileOutputStream(zipEntryFile)) {
	    					byte[] buffer = new byte[1024 * 10];
	    					int bytesRead;
	    					while ((bytesRead = zipStream.read(buffer)) != -1) {
	    						ofile.write(buffer, 0, bytesRead);
	    					}
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
	    					NamedNodeMap namedNodeMap = fstNode.getAttributes();
	    					qtiFilename = namedNodeMap.getNamedItem("href").getNodeValue();
	    				} catch (Exception e) {
	    					log.warn("Could not parse imsmanifest.xml: {}", e.toString());
	    				}
	    			} else if (entryNameTrimmed.endsWith(".xml")) {
	    				xmlFilenames.add(entryNameTrimmed);
	    				// If the QTI file doesn't exist in the zip,
	    				// we guess the name might be either exportAssessment.xml or the same as the zip or other
	    				// file name
	    				if (!xmlFilenames.contains(qtiFilename.trim())) {
	    					if (xmlFilenames.contains("exportAssessment.xml")) {
	    						qtiFilename = "exportAssessment.xml";
	    					} else if (xmlFilenames.contains(tmpName.substring(0, tmpName.lastIndexOf("_")) + ".xml")) {
	    						qtiFilename = tmpName.substring(0, tmpName.lastIndexOf("_")) + ".xml";
	    					} else {
	    						qtiFilename = entryNameTrimmed;
	    					}
	    				}
	    			}

	    			zipStream.closeEntry();
	    			entry = zipStream.getNextEntry();
	    		}
	    	}
	    } catch (IOException e) {
	    	log.warn(e.toString());
	    }

		return unzipLocation.toString();
	}

	public String getQtiFilename() {
		return qtiFilename;
	}

	public void setQtiFilename(String qtiFilename) {
		this.qtiFilename = qtiFilename;
	}
	
}
