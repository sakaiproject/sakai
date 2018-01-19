/**
 * Copyright (c) 2003 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.contentreview.turnitin;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * This class contains the implementation of 
 * {@link ContentReviewService.isAcceptableContent}. This includes other
 * utility logic for checking the length and correctness of Word Documents and
 * other things.
 * 
 * @author sgithens
 *
 */
@Slf4j
public class TurnitinContentValidator {

	private int tii_Max_Fil_Size;
	/**
	 * Default max allowed filesize - should match turnitins own setting (surrently 20Mb)
	 */
	private static int TII_DEFAULT_MAX_FILE_SIZE = 20971520;
	
	private ServerConfigurationService serverConfigurationService; 
	public void setServerConfigurationService (ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void init() {
		tii_Max_Fil_Size = serverConfigurationService.getInt("turnitin.maxFileSize", TII_DEFAULT_MAX_FILE_SIZE);
	}
	
	private boolean isMsWordDoc(ContentResource resource) {
		String mime = resource.getContentType();
		log.debug("Got a content type of " + mime);


		if (mime.equals("application/msword")) {
			log.debug("FileType matches a known mime");
			return true;
		}

		ResourceProperties resourceProperties = resource.getProperties();
		String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
		if (fileName.contains(".")) {
			String extension = fileName.substring(fileName.lastIndexOf("."));
			log.debug("file has an extension of " + extension);
			if (extension.equalsIgnoreCase(".doc") || extension.equalsIgnoreCase(".docx") || ".rtf".equalsIgnoreCase(extension)) {
				return true;
			}
		} else {
			//if the file has no extension we assume its a doc
			return true;
		}

		return false;
	}
	
	private int wordDocLength(ContentResource resource) {
		if (!serverConfigurationService.getBoolean("tii.checkWordLength", false))
			return 100;

		try {
			POIFSFileSystem pfs = new POIFSFileSystem(resource.streamContent());
			HWPFDocument doc = new HWPFDocument(pfs);
			SummaryInformation dsi = doc.getSummaryInformation();
			int count = dsi.getWordCount();
			log.debug("got a count of " + count);
			//if this == 0 then its likely that something went wrong -poi couldn't read it
			if (count == 0)
				return 100;
			return count;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (ServerOverloadException e) {
			log.error(e.getMessage(), e);
		}
		//in case we can't read this lets err on the side of caution
		return 100;
	}
	
	public boolean isAcceptableContent(ContentResource resource) {
		//for now we accept all content
		// TODO: Check against content types accepted by Turnitin
		/*
		 * Turnitin currently accepts the following file types for submission: MS Word (.doc), WordPerfect (.wpd), PostScript (.eps), Portable Document Format (.pdf), HTML (.htm), Rich Text (.rtf) and Plain Text (.txt)
		 * text/plain
		 * text/html
		 * application/msword
		 * application/msword
		 * application/postscript
		 */

		String mime = resource.getContentType();
		log.debug("Got a content type of " + mime);

		Boolean fileTypeOk = false;
		if ((mime.equals("text/plain") || mime.equals("text/html") || mime.equals("application/msword") || 
				mime.equals("application/postscript") || mime.equals("application/pdf") || mime.equals("text/rtf")) ) {
			fileTypeOk =  true;
			log.debug("FileType matches a known mime");
		}

		//as mime's can be tricky check the extensions
		if (!fileTypeOk) {
			ResourceProperties resourceProperties = resource.getProperties();
			String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
			if (fileName.indexOf(".")>0) {

				String extension = fileName.substring(fileName.lastIndexOf("."));
				log.debug("file has an extension of " + extension);
				if (extension.equals(".doc") || extension.equals(".wpd") || extension.equals(".eps") 
						||  extension.equals(".txt") || extension.equals(".htm") || extension.equals(".html") 
						|| extension.equals(".pdf") || extension.equals(".docx") || ".rtf".equals(extension))
					fileTypeOk = true;

			} else {
				//we don't know what this is so lets submit it anyway
				fileTypeOk = true;
			}
		}

        // for files like .png we'd like to get a status code from TII so we can display an error message
        // other than "An unknown error occurred"
//      if (!fileTypeOk) {
//          return false;
//      }

		//TODO: if file is too big reject here 10.48576 MB

		if (resource.getContentLength() > tii_Max_Fil_Size) {
			log.debug("File is too big: " + resource.getContentLength());
			return false;
		}

		//TII-93 content length must be > o
		if (resource.getContentLength() == 0) {
			log.debug("File is Ob");
			return false;
		}
		
		//if this is a msword type file we can check the legth
		if (isMsWordDoc(resource)) {
			if (wordDocLength(resource) < 20) {
				return false;
			}
		}


		return true;
	}
}
