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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.component.UIViewRoot;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.exception.RoleConfigurationException;
import org.sakaiproject.section.api.exception.SectionFullException;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.decorator.StudentSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the student view page.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class StudentViewBean extends EditStudentSectionsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean externallyManaged;
	private boolean joinAllowed;
	private boolean switchAllowed;

	private String instructions;
	private String filter;

	// Students don't need a full preferences bean, so we just store the sorting as fields
	private String sortColumn;
	private boolean sortAscending;

	public StudentViewBean() {
		super();
		showNegativeSpots = false;
		sortColumn = "title";
		sortAscending = true;
	}

	public void init() {
		// Initialize the sections using the current user's uid
		studentUid = getUserUid();
		super.init();

		// Determine whether this course is externally managed
		externallyManaged = getSectionManager().isExternallyManaged(getCourse().getUuid());

		// Determine whether the sections are joinable and/or switchable
		joinAllowed = getSectionManager().isSelfRegistrationAllowed(getCourse().getUuid());
		switchAllowed = getSectionManager().isSelfSwitchingAllowed(getCourse().getUuid());

		// Keep track of whether there are joinable Sections
		boolean joinableSectionsExist = false;

		// Keep track of whether there are switchable Sections
		boolean switchableSectionsExist = false;

		List<SectionDecorator> sectionCopy = new ArrayList<SectionDecorator>(sections);
		for(Iterator<SectionDecorator> iter = sectionCopy.iterator(); iter.hasNext();) {
			StudentSectionDecorator decoratedSection = (StudentSectionDecorator)iter.next();

			// Filter sections
			if(StringUtils.trimToNull(filter) != null) {
				if("MY".equals(filter) && ! decoratedSection.isMember()) {
					sections.remove(decoratedSection);
				}
				if(! "MY".equals(filter) && ! decoratedSection.getCategory().equals(filter)) {
					sections.remove(decoratedSection);
				}
			}

			if(decoratedSection.isJoinable()) {
				joinableSectionsExist = true;
			} else if (decoratedSection.isSwitchable()) {
				switchableSectionsExist = true;
			}
		}
		Collections.sort(sections, getComparator());

		instructions = generateInstructions(joinableSectionsExist, switchableSectionsExist);
	}

	private String generateInstructions(boolean joinableSectionsExist, boolean switchableSectionsExist) {

		//This site is externally managed, or joining and switching are both disallowed
		if(!joinAllowed && ! switchAllowed) {
			return JsfUtil.getLocalizedMessage("student_view_instructions_no_join_or_switch");
		}

		// Joining and switching are both possible
		if(switchAllowed && switchableSectionsExist && joinAllowed && joinableSectionsExist) {
			return JsfUtil.getLocalizedMessage("student_view_instructions_join_or_switch");
		}

		// Joining is possible, but switching is not
		if(joinAllowed && joinableSectionsExist && !(switchAllowed && switchableSectionsExist)) {
			return JsfUtil.getLocalizedMessage("student_view_instructions_join");
		}

		// Switching is possible, but joining is not
		if(switchAllowed && switchableSectionsExist && !(joinAllowed && joinableSectionsExist)) {
			return JsfUtil.getLocalizedMessage("student_view_instructions_switch");
		}

		// No sections can be joined or switched into
		return JsfUtil.getLocalizedMessage("student_view_instructions_no_sections");
	}

	public void processJoinSection(ActionEvent event) {

        String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");
		//is this section still joinable?
		CourseSection section = getSectionManager().getSection(sectionUuid);

		// The section might have been deleted
		if(section == null) {
			// There's nothing we can do in the UI, really.
		    JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_no_sections_not_available"));
			return;
		}
        refresh();

        // Check that there are still places available
		int maxEnrollments = Integer.MAX_VALUE;
		if(section.getMaxEnrollments() != null) {
			maxEnrollments = section.getMaxEnrollments().intValue();
		}

		if (maxEnrollments == Integer.MAX_VALUE) {
			// No maximum size is set for this section
			try {				
				if(getSectionManager().joinSection(sectionUuid) == null) {
					// This operation failed
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_already_member_in_category"));
				}
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			} 
			
		} else {

			// Enforce a maximum size
			Map roleMap = getSectionManager().getTotalEnrollmentsMap(sectionUuid);

			if (roleMap.size() < 3) {
				log.warn("Cannot get section enrollment information for section " + sectionUuid);
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
				return;
			}
			int studentsEnrolled = ((Integer) roleMap.get(Role.STUDENT)).intValue();
			int otherMembers = ((Integer) roleMap.get(Role.TA)).intValue() + 
								((Integer) roleMap.get(Role.INSTRUCTOR)).intValue();
			
			if (studentsEnrolled >= maxEnrollments) {
				if(log.isDebugEnabled()) log.debug("Attempted to join a section with no spaces");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_membership_full", new String[] {section.getTitle()}));
				return;
			}

			try {
				if(getSectionManager().joinSection(sectionUuid, maxEnrollments + otherMembers) == null) {
					// This operation failed
					JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_already_member_in_category"));
				}
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			} catch (SectionFullException sfe) {
				if(log.isDebugEnabled()) log.debug("Attempted to join a section with no spaces");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_membership_full", new String[] {section.getTitle()}));
			}

		}
	
		return;
	}

	public void processSwitchSection(ActionEvent event) {
		String sectionUuid = (String)FacesContext.getCurrentInstance().getExternalContext()
		.getRequestParameterMap().get("sectionUuid");

		// Does the section still exist, and is it still joinable?
		CourseSection section = getSectionManager().getSection(sectionUuid);
		
		if(section == null) {
			// There's nothing we can do in the UI, really.
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_no_sections_not_available"));
			return;
		}
		refresh();
		
		// Check that there are still places available
		
		int maxEnrollments = Integer.MAX_VALUE;
		if(section.getMaxEnrollments() != null) {
			maxEnrollments = section.getMaxEnrollments().intValue();
		}
		
		if (maxEnrollments == Integer.MAX_VALUE) {
			// No maximum size is set for this section
			try {
				getSectionManager().switchSection(sectionUuid);
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			} 
		} else {
			// Enforce a maximum size
			Map roleMap = getSectionManager().getTotalEnrollmentsMap(sectionUuid);

			if (roleMap.size() < 3) {
				log.warn("Cannot get section enrollment information for section " + sectionUuid);
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
				return;
			}
			int studentsEnrolled = ((Integer) roleMap.get(Role.STUDENT)).intValue();
			int otherMembers = ((Integer) roleMap.get(Role.TA)).intValue() + 
								((Integer) roleMap.get(Role.INSTRUCTOR)).intValue();
			
			if (studentsEnrolled >= maxEnrollments) {
				if(log.isDebugEnabled()) log.debug("Attempted to join a section with no spaces");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_membership_full", new String[] {section.getTitle()}));
				return;
			}
			try {
				getSectionManager().switchSection(sectionUuid, maxEnrollments + otherMembers);
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			} catch (SectionFullException sfe) {
				if(log.isDebugEnabled()) log.debug("Attempted to join a section with no spaces");
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("student_view_membership_full", new String[] {section.getTitle()}));
			}
			
		}
		
		return;
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}
	public boolean isJoinAllowed() {
		return joinAllowed;
	}
	public boolean isSwitchAllowed() {
		return switchAllowed;
	}
	public String getInstructions() {
		return instructions;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void refresh() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, context
                .getViewRoot().getViewId());
        context.setViewRoot(viewRoot);
        context.renderResponse(); //Optional
    }
}
