/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/qti/QTIService.java $
 * $Id: QTIService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

/**
 * <p>Copyright: Copyright (c) 2007 Sakai</p>
 * @version $Id: QTIService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 */

public class ExportService {
	private static Log log = LogFactory.getLog(ExportService.class);
	private String qtiFilename;

	public String zipImportFile(String filename) {
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
				dir.mkdirs();
			}

			Set dirsMade = new TreeSet();
			zipStream = new ZipInputStream(new ByteArrayInputStream(data));
			entry = (ZipEntry) zipStream.getNextEntry();
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
				else {
					if(!"imsmanifest.xml".equals(zipName)) {
						qtiFilename = zipName;
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
				zipStream.closeEntry();
				entry = zipStream.getNextEntry();
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
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

	public String getQTIFilename() {
		return qtiFilename;
	}
}
