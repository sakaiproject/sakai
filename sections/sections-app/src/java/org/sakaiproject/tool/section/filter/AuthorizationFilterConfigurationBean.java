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
package org.sakaiproject.tool.section.filter;

import java.util.*;

/**
 * Singleton bean to set up URL filtering by current user's role.
 */
public class AuthorizationFilterConfigurationBean {
	private List manageEnrollments;
	private List manageTeachingAssistants;
	private List manageAllSections;
	private List viewAllSections;
	private List viewOwnSections;
	
	public List getManageEnrollments() {
		return manageEnrollments;
	}
	public void setManageEnrollments(List manageEnrollments) {
		this.manageEnrollments = manageEnrollments;
	}
	public List getManageAllSections() {
		return manageAllSections;
	}
	public void setManageAllSections(List manageAllSections) {
		this.manageAllSections = manageAllSections;
	}
	public List getManageTeachingAssistants() {
		return manageTeachingAssistants;
	}
	public void setManageTeachingAssistants(List manageTeachingAssistants) {
		this.manageTeachingAssistants = manageTeachingAssistants;
	}
	public List getViewOwnSections() {
		return viewOwnSections;
	}
	public void setViewOwnSections(List viewOwnSections) {
		this.viewOwnSections = viewOwnSections;
	}
	public List getViewAllSections() {
		return viewAllSections;
	}
	public void setViewAllSections(List viewAllSections) {
		this.viewAllSections = viewAllSections;
	}

}
