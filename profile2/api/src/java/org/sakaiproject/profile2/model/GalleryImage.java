/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gallery image container and hibernate model.
 */

@Data
@NoArgsConstructor
public class GalleryImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String userUuid;
	private String mainResource;
	private String thumbnailResource;
	private String displayName;
	
	/**
	 * Additional constructor to create a new instance of GalleryImage.
	 */
	public GalleryImage(String userUuid, String mainResource,
			String thumbnailResource, String displayName) {
		
		this.userUuid = userUuid;
		this.mainResource = mainResource;
		this.thumbnailResource = thumbnailResource;
		this.displayName = displayName;
	}

}
