/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit managers page (where TAs are assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class EditManagersBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	// For the right-side list box
	protected List<SelectItem> selectedUsers;
	
	// For the left-side list box
	protected List<SelectItem> availableUsers;
	
	protected String sectionUuid;
	protected String sectionTitle;
	protected String sectionDescription;
	
	protected boolean externallyManaged;

	/**
	 * Compares ParticipationRecords by users' sortNames.
	 */
	static Comparator sortNameComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			ParticipationRecord manager1 = (ParticipationRecord)o1;
			ParticipationRecord manager2 = (ParticipationRecord)o2;
			return manager1.getUser().getSortName().compareTo(manager2.getUser().getSortName());
		}
	};

	protected SectionDecorator initializeFields() {
		// Determine whether this course is externally managed
		externallyManaged = getSectionManager().isExternallyManaged(getCourse().getUuid());
		
		// Get the section to edit
		String sectionUuidFromParam = (String)FacesContext.getCurrentInstance()
			.getExternalContext().getRequestParameterMap().get("sectionUuid");
		if(sectionUuidFromParam != null) {
			sectionUuid = sectionUuidFromParam;
		}
		SectionDecorator currentSection = new SectionDecorator(getSectionManager().getSection(sectionUuid), true);

		sectionTitle = currentSection.getTitle();
		
		// Generate the description
//		String sectionMeetingTimes = currentSection.getMeetings();
//		if(StringUtils.trimToNull(sectionMeetingTimes) == null) {
		sectionDescription = sectionTitle;
//		} else {
//			sectionDescription = JsfUtil.getLocalizedMessage("section_description",
//				new String[] {sectionTitle, sectionMeetingTimes});
//		}
		
		return currentSection;
	}

	protected void populateSelectedUsers(List participationRecords) {
		// Build the list of items for the right-side list box
		selectedUsers = new ArrayList<SelectItem>();
		for(Iterator iter =participationRecords.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			SelectItem item = new SelectItem(record.getUser().getUserUid(),
					record.getUser().getSortName());
			selectedUsers.add(item);
		}
	}

	public void init() {
		initializeFields();

		// Get the current users in the manager role for this section
		List<ParticipationRecord> selectedManagers = getSectionManager().getSectionTeachingAssistants(sectionUuid);
		Collections.sort(selectedManagers, sortNameComparator);
		
		populateSelectedUsers(selectedManagers);

		// Build the list of items for the left-side box.  Since the selected (right-side)
		// participation records are linked to a section, while the available records
		// are linked to the course, we can not use collection manipulation on these
		// objects.  So, generate a set of user uuids to filter out the currently
		// selected users from the available (left side) list.
		Set<String> selectedUserUuids = new HashSet<String>();
		for(Iterator<ParticipationRecord> iter = selectedManagers.iterator(); iter.hasNext();) {
			ParticipationRecord manager = iter.next();
			selectedUserUuids.add(manager.getUser().getUserUid());
		}

		List availableManagers = getSectionManager().getSiteTeachingAssistants(getSiteContext());
		Collections.sort(availableManagers, sortNameComparator);
		
		availableUsers = new ArrayList<SelectItem>();
		for(Iterator iter = availableManagers.iterator(); iter.hasNext();) {
			User manager = ((ParticipationRecord)iter.next()).getUser();
			if( ! selectedUserUuids.contains(manager.getUserUid())) {
				availableUsers.add(new SelectItem(manager.getUserUid(), manager.getSortName()));
			}
		}
	}
	
	public String update() {
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		// The section might have been deleted
		if(section == null) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("error_section_deleted"));
			return "overview";
		}

		Set userUids = getHighlightedUsers("memberForm:selectedUsers");
		try {
			getSectionManager().setSectionMemberships(userUids, Role.TA, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			return null;
		}
		
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"edit_manager_successful", new String[] {sectionTitle}));
		
		return "overview";
	}

	public String cancel() {
		return "overview";
	}

	protected Set<String> getHighlightedUsers(String componentId) {
		Set<String> userUids = new HashSet<String>();
		
		String[] highlighted = (String[])FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterValuesMap().get(componentId);

		if(highlighted != null) {
			for(int i=0; i < highlighted.length; i++) {
				userUids.add(highlighted[i]);
			}
		}
		return userUids;
	}
	
	public List<SelectItem> getAvailableUsers() {
		return availableUsers;
	}

	public void setAvailableUsers(List<SelectItem> availableUsers) {
		this.availableUsers = availableUsers;
	}

	public List<SelectItem> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<SelectItem> selectedUsers) {
		this.selectedUsers = selectedUsers;
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

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public String getSectionDescription() {
		return sectionDescription;
	}

	public String getAbbreviatedSectionTitle() {
		return StringUtils.abbreviate(sectionTitle, 15);
	}
}
