/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
