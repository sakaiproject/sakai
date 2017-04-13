/**********************************************************************************
 *
 * Copyright (c) 2016 Sakai Foundation
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
package org.sakaiproject.contentreview.compilatio;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * This class contains the implementation of 
 * {@link ContentReviewService.isAcceptableContent}.
 *
 */
public class CompilatioContentValidator {
	private static final Log log = LogFactory.getLog(CompilatioContentValidator.class);
	
	ContentReviewService contentReviewService = null;
	public void setContentReviewService(ContentReviewService contentReviewService) {
		this.contentReviewService = contentReviewService;
	}
	
	public void init() {}
	
	public boolean isAcceptableContent(ContentResource resource) {
		if (resource == null) {
			return false;
		}
		String mime = resource.getContentType();
		log.debug("Got a content type of " + mime);

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
				log.debug("file has an extension of " + extension);
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

		// for files like .png we'd like to get a status code from Compilatio so we can display an error message
		// other than "An unknown error occurred"
		return fileTypeOk;
	}
}
