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
import java.util.Properties;

public class Learner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String displayName;

	private String displayId;

	private String sortName;

	private Properties properties;

	public Learner(String id) {
		this.id = id;
	}

	public Learner(String id, String displayName, String displayId) {
		this.id = id;
		this.displayName = displayName;
		this.displayId = displayId;
	}

	public String getDisplayId() {
		return displayId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getId() {
		return id;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getSortName() {
		return sortName;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

}
