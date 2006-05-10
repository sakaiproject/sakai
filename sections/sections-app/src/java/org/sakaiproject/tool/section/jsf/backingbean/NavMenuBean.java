/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf.backingbean;

/**
 * Caches whether the instructor features are enabled for the current user in
 * the current request.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class NavMenuBean extends CourseDependentBean {
	private static final long serialVersionUID = 1L;
	
	private boolean sectionTaManagementEnabled;
	private boolean sectionEnrollmentMangementEnabled;
	private boolean sectionOptionsManagementEnabled;
	private boolean sectionManagementEnabled;

	public NavMenuBean() {
		this.sectionManagementEnabled = super.isSectionManagementEnabled();
		this.sectionOptionsManagementEnabled = super.isSectionOptionsManagementEnabled();
		this.sectionEnrollmentMangementEnabled = super.isSectionEnrollmentMangementEnabled();
		this.sectionTaManagementEnabled = super.isSectionTaManagementEnabled();
	}

	public boolean isSectionEnrollmentMangementEnabled() {
		return sectionEnrollmentMangementEnabled;
	}

	public void setSectionEnrollmentMangementEnabled(
			boolean sectionEnrollmentMangementEnabled) {
		this.sectionEnrollmentMangementEnabled = sectionEnrollmentMangementEnabled;
	}

	public boolean isSectionManagementEnabled() {
		return sectionManagementEnabled;
	}

	public void setSectionManagementEnabled(boolean sectionManagementEnabled) {
		this.sectionManagementEnabled = sectionManagementEnabled;
	}

	public boolean isSectionOptionsManagementEnabled() {
		return sectionOptionsManagementEnabled;
	}

	public void setSectionOptionsManagementEnabled(
			boolean sectionOptionsManagementEnabled) {
		this.sectionOptionsManagementEnabled = sectionOptionsManagementEnabled;
	}

	public boolean isSectionTaManagementEnabled() {
		return sectionTaManagementEnabled;
	}

	public void setSectionTaManagementEnabled(boolean sectionTaManagementEnabled) {
		this.sectionTaManagementEnabled = sectionTaManagementEnabled;
	}

}
