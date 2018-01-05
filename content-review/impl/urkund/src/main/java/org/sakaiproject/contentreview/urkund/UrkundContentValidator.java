/**********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.urkund;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * This class contains the implementation of 
 * {@link ContentReviewService.isAcceptableContent}.
 *
 */
@Slf4j
public class UrkundContentValidator {
	private int max_file_size;
	private static int DEFAULT_MAX_FILE_SIZE = 20971520; //20Mb
	
	ContentReviewService contentReviewService = null;
	public void setContentReviewService(ContentReviewService contentReviewService) {
		this.contentReviewService = contentReviewService;
	}
	
	ServerConfigurationService serverConfigurationService;
		public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void init() {
		max_file_size = serverConfigurationService.getInt("urkund.maxFileSize", DEFAULT_MAX_FILE_SIZE);
	}
	
	public boolean isAcceptableContent(ContentResource resource) {
		if (resource == null) {
			return false;
		}
		String mime = resource.getContentType();
		log.debug("Got a content type of {}", mime);

		Map<String, SortedSet<String>> acceptableExtensionsToMimeTypes = contentReviewService.getAcceptableExtensionsToMimeTypes();
		Set<String> acceptableMimeTypes = new HashSet<>();
		for (SortedSet<String> mimeTypes : acceptableExtensionsToMimeTypes.values()) {
			acceptableMimeTypes.addAll(mimeTypes);
		}

		Boolean fileTypeOk = false;
		if (acceptableMimeTypes.contains(mime)) {
			fileTypeOk =  true;
			log.debug("FileType matches a known mime");
		} else {
			log.debug("FileType doesn't match a known mime");
		}

		//as mime's can be tricky check the extensions
		if (!fileTypeOk) {
			ResourceProperties resourceProperties = resource.getProperties();
			String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
			if (fileName.indexOf(".") > 0) {

				String extension = fileName.substring(fileName.lastIndexOf("."));
				log.debug("file has an extension of {}", extension);
				Set<String> extensions = acceptableExtensionsToMimeTypes.keySet();
				if (extensions.contains(extension)) {
					fileTypeOk = true;
				}
				else {
					// Neither the mime type nor the file extension are accepted
					fileTypeOk = false;
				}

			} else {
				// No extension is not accepted
				// TODO: Make this configurable
				fileTypeOk = false;
			}
		}

		if(fileTypeOk){
			if (resource.getContentLength() > max_file_size) {
				log.debug("File is too big: {}", resource.getContentLength());
				fileTypeOk = false;
			}

			if (resource.getContentLength() == 0) {
				log.debug("File is empty");
				fileTypeOk = false;
			}

		}
		
		return fileTypeOk;
	}
}
