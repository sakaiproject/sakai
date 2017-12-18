/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
public class AttachmentHelper {

	public ContentResource createContentResource(String fullFilePath, String filename, String mimeType) {
		ContentResource contentResource = null;
		int BUFFER_SIZE = 2048;
		byte tempContent[] = new byte[BUFFER_SIZE];
		File file = null;
		FileInputStream fileInputStream = null;
		BufferedInputStream bufInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		byte content[];
		int count = 0;
			
		try {
			try{
				fullFilePath = URLDecoder.decode(fullFilePath, "UTF-8");
				filename = URLDecoder.decode(filename, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
			
			if (mimeType.equalsIgnoreCase("text/url")) {
				content = filename.getBytes();
				filename = filename.replaceAll("http://","http:__");
			}
			else {
				file = new File(fullFilePath);
				fileInputStream = new FileInputStream(file);
				bufInputStream = new BufferedInputStream(fileInputStream);
				byteArrayOutputStream = new ByteArrayOutputStream();
				while ((count = bufInputStream.read(tempContent, 0, BUFFER_SIZE)) != -1) {
					byteArrayOutputStream.write(tempContent, 0, count);
				}
				content = byteArrayOutputStream.toByteArray();
			}
			
			ResourcePropertiesEdit props = AssessmentService.getContentHostingService().newResourceProperties();
			// Maybe we need to put in some properties?
			// props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
			// props.addProperty(ResourceProperties.PROP_DESCRIPTION, name);

			contentResource = AssessmentService.getContentHostingService().addAttachmentResource(
						filename, ToolManager.getCurrentPlacement().getContext(), 
						ToolManager.getTool("sakai.samigo").getTitle(), mimeType, content, props);
		} catch (IdInvalidException e) {
			log.error("IdInvalidException:" + e.getMessage());
		} catch (PermissionException e) {
			log.error("PermissionException:" + e.getMessage());
		} catch (InconsistentException e) {
			log.error("InconsistentException:" + e.getMessage());
		} catch (IdUsedException e) {
			log.error("IdUsedException:" + e.getMessage());
		} catch (OverQuotaException e) {
			log.error("OverQuotaException:" + e.getMessage());
		} catch (ServerOverloadException e) {
			log.error("ServerOverloadException:" + e.getMessage());
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException:" + e.getMessage());
		} catch (IOException e) {
			log.error("IOException:" + e.getMessage());
		}
		finally {
			if (bufInputStream != null) {
				try {
					bufInputStream.close();
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
			if (byteArrayOutputStream != null) {
				try {
					byteArrayOutputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}

		return contentResource;
	}
}
