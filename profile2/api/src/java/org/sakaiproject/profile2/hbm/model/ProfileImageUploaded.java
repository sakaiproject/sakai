/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.hbm.model;

import java.io.Serializable;

/**
 * Hibernate model for an uploaded profile image
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileImageUploaded implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String userUuid;
	private String mainResource;
	private String thumbnailResource;
	private boolean current;
	
	/**
	 * Empty constructor
	 */
	public ProfileImageUploaded() {
	}

	/** 
	 * Constructor to create a ProfileImage record in one go
	 */
	public ProfileImageUploaded(String userUuid, String mainResource, String thumbnailResource, boolean current) {
		this.userUuid = userUuid;
		this.mainResource = mainResource;
		this.thumbnailResource = thumbnailResource;
		this.current = current;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getMainResource() {
		return mainResource;
	}

	public void setMainResource(String mainResource) {
		this.mainResource = mainResource;
	}

	public String getThumbnailResource() {
		return thumbnailResource;
	}

	public void setThumbnailResource(String thumbnailResource) {
		this.thumbnailResource = thumbnailResource;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}
	
	
}
