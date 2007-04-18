/**********************************************************************************
*
* $Id: AssignmentBean.java  $
*
***********************************************************************************
*
* Copyright (c) 2005, 2006, 2007 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.GradebookService;

public class AssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(AssignmentBean.class);

	private Long assignmentId;
    private Assignment assignment;
    private List categoriesSelectList;
    private String assignmentCategory;
    
    private static final String UNASSIGNED_CATEGORY = "unassigned";

	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("init assignment=" + assignment);

		if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new Assignment();
				assignment.setReleased(true);
			}
		}

		Category assignCategory = assignment.getCategory();
		if (assignCategory != null) {
			assignmentCategory = assignCategory.getId().toString();
		}
		else {
			assignmentCategory = getLocalizedString("cat_unassigned");
		}
		
		categoriesSelectList = new ArrayList();

		// The first choice is always "Unassigned"
		categoriesSelectList.add(new SelectItem(UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		List gbCategories = getAvailableCategories();
		if (gbCategories != null && gbCategories.size() > 0)
		{
			Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				Category cat = (Category) catIter.next();
				categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
			}
		}

	}
	
	public String saveNewAssignment() {
		try {
			Category selectedCategory = retrieveSelectedCategory();
			if (selectedCategory != null) {
				getGradebookManager().createAssignmentForCategory(getGradebookId(), selectedCategory.getId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()));
			}
			else {
				getGradebookManager().createAssignment(getGradebookId(),  assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()));
			}
            getGradebookBean().getEventTrackingService().postEvent("gradebook.newItem","/gradebook/"+getGradebookId()+"/"+assignment.getName());
            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));
		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
			return "failure";
		}
		return "overview";
	}

	public String updateAssignment() {
		try {
			Category category = retrieveSelectedCategory();
			assignment.setCategory(category);
			getGradebookManager().updateAssignment(assignment);
			String messageKey = getGradebookManager().isEnteredAssignmentScores(assignmentId) ?
				"edit_assignment_save_scored" :
				"edit_assignment_save";
            FacesUtil.addRedirectSafeMessage(getLocalizedString(messageKey, new String[] {assignment.getName()}));
		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
            return "failure";
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_locking_failure"));
            return "failure";
		}
		return navigateToAssignmentDetails();
	}

	public String cancelToAssignmentDetails() {
		return navigateToAssignmentDetails();
	}

	private String navigateToAssignmentDetails() {
		// Go back to the Assignment Details page for this assignment.
		AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
		assignmentDetailsBean.setAssignmentId(assignmentId);
		return "assignmentDetails";
	}

	/**
	 * View maintenance methods.
	 */
	public Long getAssignmentId() {
		if (logger.isDebugEnabled()) logger.debug("getAssignmentId " + assignmentId);
		return assignmentId;
	}
	public void setAssignmentId(Long assignmentId) {
		if (logger.isDebugEnabled()) logger.debug("setAssignmentId " + assignmentId);
		if (assignmentId != null) {
			this.assignmentId = assignmentId;
		}
	}

    public Assignment getAssignment() {
        if (logger.isDebugEnabled()) logger.debug("getAssignment " + assignment);
        return assignment;
    }
    
    public List getCategoriesSelectList() {
    	return categoriesSelectList;
    }
    
    public String getAssignmentCategory() {
    	return assignmentCategory;
    }
    
    public void setAssignmentCategory(String assignmentCategory) {
    	this.assignmentCategory = assignmentCategory;
    }
    
    /**
     * Returns the Category associated with assignmentCategory
     * If unassigned or not found, returns null
     * @return
     */
    private Category retrieveSelectedCategory() {
    	Long catId = null;
    	Category category = null;
    	
		if (assignmentCategory != null && !assignmentCategory.equals(UNASSIGNED_CATEGORY)) {
			try {
				catId = new Long(assignmentCategory);
			}
			catch (Exception e) {
				catId = null;
			}
			
			if (catId != null)
			{
				// check to make sure there is a corresponding category
				category = getGradebookManager().getCategory(catId);
			}
		}
		
		return category;
    }
}



