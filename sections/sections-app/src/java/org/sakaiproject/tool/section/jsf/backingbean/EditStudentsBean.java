/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.component.section.facade.impl.sakai.RoleImpl;

public class EditStudentsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(EditStudentsBean.class);
	
	// For the right-side list box
	private List selectedUsers;
	
	// For the left-side list box
	private List availableUsers;
	
	// For the "View" selectbox
	private String availableSectionUuid;
	private List availableSectionItems;
	
	private String sectionUuid;
	private String sectionTitle;
	
	public void init() {
		// Get the section to edit
		String sectionUuidFromParam = (String)FacesContext.getCurrentInstance()
			.getExternalContext().getRequestParameterMap().get("sectionUuid");
		if(sectionUuidFromParam != null) {
			sectionUuid = sectionUuidFromParam;
		}
		CourseSection currentSection = getSectionAwareness().getSection(sectionUuid);
		sectionTitle = currentSection.getTitle();
		
		// Get the current users
		List enrollments = getSectionAwareness().getSectionMembersInRole(currentSection.getUuid(), RoleImpl.STUDENT);
		
		// Build the list of items for the right-side list box
		selectedUsers = new ArrayList();
		for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
			ParticipationRecord enrollment = (ParticipationRecord)iter.next();
			SelectItem item = new SelectItem(enrollment.getUser().getUserUuid(),
					enrollment.getUser().getDisplayName());
			selectedUsers.add(item);
		}

		// Build the list of items for the left-side box
		List available;
		if(StringUtils.trimToNull(availableSectionUuid) == null) {
			available = getSectionManager().getUnsectionedEnrollments(currentSection.getCourse().getUuid(), currentSection.getCategory());
		} else {
			available = getSectionAwareness().getSectionMembersInRole(availableSectionUuid, RoleImpl.STUDENT);
		}
		availableUsers = new ArrayList();
		for(Iterator iter = available.iterator(); iter.hasNext();) {
			User student = ((ParticipationRecord)iter.next()).getUser();
			availableUsers.add(new SelectItem(student.getUserUuid(), student.getDisplayName()));
		}
		
		// Build the list of available sections
		List sectionsInCategory = getSectionAwareness().getSectionsInCategory(getSiteContext(), currentSection.getCategory());
		availableSectionItems = new ArrayList();
		availableSectionItems.add(new SelectItem("", getLocalizedMessage("edit_student_unassigned")));
		for(Iterator iter = sectionsInCategory.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Don't include the current section
			if(section.equals(currentSection)) {
				continue;
			}
			availableSectionItems.add(new SelectItem(section.getUuid(), section.getTitle()));
		}

	}

	public void processChangeSection(ValueChangeEvent event) {
		// Reset all lists
		init();
	}
	
	public String update() {
		Set userUuids = getHighlightedUsers("memberForm:selectedUsers");
		getSectionManager().setSectionMemberships(userUuids, RoleImpl.STUDENT, sectionUuid);
		
		// If the "available" box is a section, update that section's members as well
		if(StringUtils.trimToNull(availableSectionUuid) != null) {
			userUuids = getHighlightedUsers("memberForm:availableUsers");
			getSectionManager().setSectionMemberships(userUuids, RoleImpl.STUDENT, availableSectionUuid);
		}
		
		return null;
		
		//return "overview";
	}
	
	private Set getHighlightedUsers(String componentId) {
		Set userUuids = new HashSet();
		
		String[] highlighted = (String[])FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterValuesMap().get(componentId);

		if(highlighted != null) {
			for(int i=0; i < highlighted.length; i++) {
				userUuids.add(highlighted[i]);
			}
		}
		return userUuids;
	}
	
	public List getAvailableUsers() {
		return availableUsers;
	}

	public void setAvailableUsers(List availableUsers) {
		this.availableUsers = availableUsers;
	}

	public List getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public String getAvailableSectionUuid() {
		return availableSectionUuid;
	}

	public void setAvailableSectionUuid(String availableSectionUuid) {
		this.availableSectionUuid = availableSectionUuid;
	}

	public List getAvailableSectionItems() {
		return availableSectionItems;
	}

	public String getSectionUuid() {
		return sectionUuid;
	}

	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
