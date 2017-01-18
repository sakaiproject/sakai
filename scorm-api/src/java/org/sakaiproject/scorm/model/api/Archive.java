/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public class Archive implements Serializable {
	private static final long serialVersionUID = 1L;

	private String resourceId;

	private String title;

	private boolean isHidden;

	private boolean isValidated;

	private String mimeType;

	private String path;

	public Archive(String resourceId, String title) {
		this.resourceId = resourceId;
		this.title = title;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getPath() {
		return path;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getTitle() {
		return title;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public boolean isValidated() {
		return isValidated;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setValidated(boolean isValidated) {
		this.isValidated = isValidated;
	}

}
