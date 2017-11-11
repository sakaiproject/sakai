/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;


public abstract class AbstractNamedCourseManagementObjectCmImpl
	extends AbstractPersistentCourseManagementObjectCmImpl  implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	protected String title;
	protected String description;
	protected String eid;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}	

	public String toString() {
		return new ToStringBuilder(this)
			.append(getEid())
			.append(getTitle())
			.append(getDescription())
			.toString();
	}

	protected boolean isTitleEmpty() {
		return title == null || title.length() == 0;
	}

	protected boolean isDescriptionEmpty() {
		return description == null || description.length() == 0;
	}
}
