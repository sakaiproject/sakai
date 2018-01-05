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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;
import org.sakaiproject.tool.section.jsf.backingbean.EditManagersBean;
/**
 * Controls the edit students page (where students are assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class EditStudentsBean extends EditManagersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	// For the "View" selectbox
	private String availableSectionUuid;
	private String availableSectionTitle;
	private Integer availableSectionMax;
	private List availableSectionItems;
	
	private Integer sectionMax;

	public void init() {
		SectionDecorator currentSection = initializeFields();
		sectionMax = currentSection.getMaxEnrollments();
		
		// Get the current users
		List enrollments = getSectionManager().getSectionEnrollments(currentSection.getUuid());
		Collections.sort(enrollments, EditManagersBean.sortNameComparator);

		populateSelectedUsers(enrollments);
		
		// Build the list of items for the left-side box
		List available;
		if(StringUtils.trimToNull(availableSectionUuid) == null) {
			available = getSectionManager().getUnsectionedEnrollments(currentSection.getCourse().getUuid(), currentSection.getCategory());
		} 
		else if (this.isFromCategoryReadOnly(availableSectionUuid)) {
			List available1=getSectionManager().getUnsectionedEnrollments(currentSection.getCourse().getUuid(), currentSection.getCategory());
			List available2= getSectionManager().getSectionEnrollments(availableSectionUuid);
			available=new ArrayList();
			
			Collections.sort(available1, EditManagersBean.sortNameComparator);
			Collections.sort(available2, EditManagersBean.sortNameComparator);
			
			int k1 = 0; int k2 = 0;
			while (k1<available1.size() && k2 < available2.size()) {
				ParticipationRecord p1=(ParticipationRecord)available1.get(k1);
				ParticipationRecord p2=(ParticipationRecord)available2.get(k2);
				String a1=p1.getUser().getSortName();
				String a2=p2.getUser().getSortName();
				if (a1.compareTo(a2)==0) {
				   available.add(p2);
				   k1++; k2++;
				} else if (a1.compareTo(a2) < 0) {
				   k1++;
				} else {
				   k2++;
			   }
			}
		}
		else {
			available = getSectionManager().getSectionEnrollments(availableSectionUuid);
		}
		Collections.sort(available, EditManagersBean.sortNameComparator);

		availableUsers = new ArrayList();
		for(Iterator iter = available.iterator(); iter.hasNext();) {
			User student = ((ParticipationRecord)iter.next()).getUser();
			availableUsers.add(new SelectItem(student.getUserUid(), student.getSortName()));
		}
		
		// Build the list of available sections
		List sectionsInCategory = getSectionManager().getSectionsInCategory(getSiteContext(), currentSection.getCategory());
		List<CourseSection> all= getAllSiteSections();
		List sectionsReadOnly= new ArrayList<CourseSection>();
		for (CourseSection section : all){
			String sectionUid=section.getUuid();
			if(this.isFromCategoryReadOnly(sectionUid))
			{
				sectionsReadOnly.add(section);
			}
		}
		Collections.sort(sectionsInCategory);
		Collections.sort(sectionsReadOnly);
		availableSectionItems = new ArrayList();
		availableSectionItems.add(new SelectItem("", JsfUtil.getLocalizedMessage("edit_student_unassigned")));
		for (CourseSection section : ((List<CourseSection>) sectionsReadOnly)){
			
			availableSectionItems.add(new SelectItem(section.getUuid(), section.getTitle()+" ("+JsfUtil.getLocalizedMessage("edit_student_unassigned")+")"));
		}
		for(Iterator iter = sectionsInCategory.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Don't include the current section
			if(section.getUuid().equals(currentSection.getUuid())) {
				continue;
			}
			if(section.getUuid().equals(availableSectionUuid)) {
				availableSectionTitle = section.getTitle();
				availableSectionMax = section.getMaxEnrollments();
			}
			availableSectionItems.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
	}

	public void processChangeSection(ValueChangeEvent event) {
		// Reset all lists
		init();
	}
	public String update(){
		//false as default
		boolean overrideSections = ServerConfigurationService.getBoolean("sections.override", true);
		if (overrideSections)
			return update_with_override();
		else
			return update_without_override();
	}
	
	private String update_with_override(){
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		// The section might have been deleted
		if(section == null) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("error_section_deleted"));
			return "overview";
		}

		
		Set selectedUserUuids = getHighlightedUsers("memberForm:selectedUsers");
		try {
			getSectionManager().setSectionMemberships(selectedUserUuids, Role.STUDENT, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			return null;
		}
		
		// If the "available" box is a section, update that section's members as well
		Set availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
		if(StringUtils.trimToNull(availableSectionUuid) != null) {
			availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
			try {
				if(!(this.isFromCategoryReadOnly(availableSectionUuid))) {
					getSectionManager().setSectionMemberships(availableUserUuids, Role.STUDENT, availableSectionUuid);
				}
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
				return null;
			}
		}
		StringBuilder titles = new StringBuilder();
		titles.append(sectionTitle);
		if(StringUtils.trimToNull(availableSectionUuid) != null) {
			titles.append(" ");
			titles.append(JsfUtil.getLocalizedMessage("and"));
			titles.append(" ");
			titles.append(availableSectionTitle);
		}
		
		// Add the success message first, before any caveats (see below)
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"edit_student_successful", new String[] {titles.toString()}));

		// If the selected section is now overenrolled, let the user know
		if(sectionMax != null && selectedUserUuids.size() > sectionMax.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							sectionTitle,
							Integer.toString(selectedUserUuids.size()),
							Integer.toString(selectedUserUuids.size() - sectionMax.intValue()) }));
		}

		// If the available section is now overenrolled, let the user know
		if(availableSectionMax != null && availableUserUuids.size() > availableSectionMax.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							availableSectionTitle,
							Integer.toString(availableUserUuids.size()),
							Integer.toString(availableUserUuids.size() - availableSectionMax.intValue()) }));
		}
		return "overview";
	}
	
	
	private String update_without_override() {
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		// The section might have been deleted
		if(section == null) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("error_section_deleted"));
			return "overview";
		}else{
			Set selectedUserUuids = getHighlightedUsers("memberForm:selectedUsers");
			int totalEnrollments = selectedUserUuids.size();
			Integer sectionMaxEnrollments = section.getMaxEnrollments();
			int maxEnrollments = Integer.MAX_VALUE;

			if (sectionMaxEnrollments!=null){
				maxEnrollments = sectionMaxEnrollments.intValue(); 
			}
			
			try {
				if(totalEnrollments<=maxEnrollments)
					getSectionManager().setSectionMemberships(selectedUserUuids, Role.STUDENT, sectionUuid);
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
				return null;
			}
			
			// If the "available" box is a section, update that section's members as well
			Set availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
			if(StringUtils.trimToNull(availableSectionUuid) != null) {
				availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
				if (totalEnrollments <= maxEnrollments) {			
					try {
						getSectionManager().setSectionMemberships(availableUserUuids, Role.STUDENT, availableSectionUuid);					
					} catch (RoleConfigurationException rce) {
						JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
						return null;
					}
				}
			}
			
			if (totalEnrollments<=maxEnrollments){
				StringBuffer titles = new StringBuffer();
				titles.append(sectionTitle);
				if(StringUtils.trimToNull(availableSectionUuid) != null) {
					titles.append(" ");
					titles.append(JsfUtil.getLocalizedMessage("and"));
					titles.append(" ");
					titles.append(availableSectionTitle);
				}

				// Add the success message first, before any caveats (see below)
				JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
						"edit_student_successful", new String[] {titles.toString()}));				
			}

	
			// If the selected section is now overenrolled, let the user know
			if(sectionMax != null && selectedUserUuids.size() > sectionMax.intValue()) {
				JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
						"edit_student_over_max_warning_try", new String[] {
								Integer.toString(totalEnrollments),
								sectionTitle,
								Integer.toString(totalEnrollments - sectionMax.intValue()) }));
			}
	
			
			// If the available section is now overenrolled, let the user know
			if(availableSectionMax != null && availableUserUuids.size() > availableSectionMax.intValue()) {
				JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
						"edit_student_over_max_warning_try", new String[] {
								Integer.toString(availableUserUuids.size()),
								availableSectionTitle,
								Integer.toString(availableUserUuids.size() - availableSectionMax.intValue()) }));
			}
		}		
		return "overview";
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

	public Integer getSectionMax() {
		return sectionMax;
	}
	//SAK-32515
	//We only need sections that belong to "readonly" categories from property 
	//called "section.info.readonly.section.categories" in sakai.properties.
	public boolean isFromCategoryReadOnly(String secUuid) {
		CourseSection section=getSectionManager().getSection(secUuid);
		SectionDecorator sc=new SectionDecorator(section, true);
		return sc.isReadOnly() && !section.isLocked();
    }
}
