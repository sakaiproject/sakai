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
package org.sakaiproject.roster.api;

import java.util.List;

/**
 * Container for a site.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterSite {

	private String id;
	private String title;
	private List<String> userRoles;
	private List<RosterGroup> siteGroups;
	private List<RosterEnrollment> siteEnrollmentSets;
	private List<String> enrollmentStatusDescriptions;
	
	public RosterSite() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<String> getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}
	public List<RosterGroup> getSiteGroups() {
		return siteGroups;
	}
	public void setSiteGroups(List<RosterGroup> siteGroups) {
		this.siteGroups = siteGroups;
	}

	public List<RosterEnrollment> getSiteEnrollmentSets() {
		return siteEnrollmentSets;
	}

	public void setSiteEnrollmentSets(List<RosterEnrollment> siteEnrollmentSets) {
		this.siteEnrollmentSets = siteEnrollmentSets;
	}
	
	public List<String> getEnrollmentStatusDescriptions() {
		return enrollmentStatusDescriptions;
	}
	
	public void setEnrollmentStatusDescriptions(List<String> enrollmentStatusDescriptions) {
		this.enrollmentStatusDescriptions = enrollmentStatusDescriptions;
	}
}
