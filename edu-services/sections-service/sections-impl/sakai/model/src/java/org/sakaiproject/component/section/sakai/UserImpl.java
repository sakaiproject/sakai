/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.component.section.sakai;

import java.io.Serializable;

import org.sakaiproject.section.api.coursemanagement.User;

public class UserImpl implements User, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String userUid;
	private String displayId;
	private String sortName;
	private String displayName;
	
	public UserImpl(String displayId, String displayName, String sortName, String uid) {
		this.displayId = displayId;
		this.displayName = displayName;
		this.sortName = sortName;
		this.userUid = uid;
	}

	public String getUserUid() {
		return userUid;
	}

	public String getSortName() {
		return sortName;
	}

	public String getDisplayId() {
		return displayId;
	}

	public String getDisplayName() {
		return displayName;
	}
}
