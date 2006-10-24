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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseGroup;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the add sections page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class AddGroupsBean extends CourseDependentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(AddGroupsBean.class);
	
	private int numToAdd;
	private List groups;
	private  transient boolean groupsChanged;
	
	/**
	 * @inheritDoc
	 */
	public void init() {
		// initialize the number of groups to add
		if(numToAdd == 0) numToAdd++;
		
		if(groups == null || groupsChanged) {
			groups = new ArrayList();
			if(log.isDebugEnabled()) log.debug("initializing add groups bean");
			for(int i=1; i<=numToAdd; i++) {
				groups.add(new LocalGroupModel());
			}
		}
	}
	
	/**
	 * Responds to a change in the groups selector in the UI.
	 * 
	 * @param event
	 */
	public void processChangeGroups(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");
		groupsChanged = true;
	}
	
	
	/**
	 * Checks whether a string is currently being used as a title for another section.
	 * 
	 * @param title
	 * @param existingSections
	 * @return
	 */
	private boolean isDuplicateGroupTitle(String title, Collection existingGroups) {
		for(Iterator iter = existingGroups.iterator(); iter.hasNext();) {
			CourseGroup group = (CourseGroup)iter.next();
			if(group.getTitle().equals(title)) {
				if(log.isDebugEnabled()) log.debug("Conflicting group name found: " + title);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the sections, or generates validation messages for bad inputs.
	 * 
	 * @return
	 */
	public String addGroups() {
		if(validationFails()) {
			setNotValidated(true);
			return "failure";
		}

		// Validation passed, so save the new groups
		String courseUuid = getCourse().getUuid();
		StringBuffer titles = new StringBuffer();
		String sepChar = JsfUtil.getLocalizedMessage("section_separator");
		
		for(Iterator iter = groups.iterator(); iter.hasNext();) {
			LocalGroupModel groupModel = (LocalGroupModel)iter.next();
			titles.append(groupModel.getTitle());
			if(iter.hasNext()) {
				titles.append(sepChar);
				titles.append(" ");
			}

			getSectionManager().addCourseGroup(courseUuid, groupModel.getTitle(), groupModel.getDescription());
		}
		String[] params = new String[3];
		params[0] = titles.toString();
		if(groups.size() == 1) {
			params[1] = JsfUtil.getLocalizedMessage("add_group_successful_singular");
			params[2] = JsfUtil.getLocalizedMessage("group_singular");
		} else {
			params[1] = JsfUtil.getLocalizedMessage("add_group_successful_plural");
			params[2] = JsfUtil.getLocalizedMessage("group_plural");
		}
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("add_group_successful", params));
		return "overview";
	}
	
	
	/**
	 * Since the validation and conversion rules rely on the *relative*
	 * values of one component to another, we can't use JSF validators and
	 * converters.  So we check everything here.
	 * 
	 * @return
	 */
	protected boolean validationFails() {
		Collection existingGroups = getAllSiteGroups();

		// Keep track of whether a validation failure occurs
		boolean validationFailure = false;
		
		int index = 0;
		for(Iterator iter = groups.iterator(); iter.hasNext(); index++) {
			LocalGroupModel groupModel = (LocalGroupModel)iter.next();
			
			// Ensure that this title isn't being used by another group
			if(isDuplicateGroupTitle(groupModel.getTitle(), existingGroups)) {
				if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + groupModel.getTitle());
				String componentId = "addSectionsForm:sectionTable_" + index + ":titleInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_add_failure_duplicate_title", new String[] {groupModel.getTitle()}), componentId);
				validationFailure = true;
			}
		}
		return validationFailure;
	}
	
	public int getNumToAdd() {
		return numToAdd;
	}

	public void setNumToAdd(int numToAdd) {
		this.numToAdd = numToAdd;
	}
	
	public List getGroups() {
		return groups;
	}

	public class LocalGroupModel implements Serializable {
		private static final long serialVersionUID = 1L;

		public LocalGroupModel() {}
		public LocalGroupModel(String title, String description) {this.title = title; this.description = description;}
		
		private String title, description;

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
	}
}
