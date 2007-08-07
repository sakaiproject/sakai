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
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.GradebookService;

public class AssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(AssignmentBean.class);

	private Long assignmentId;
    private Assignment assignment;
    private List categoriesSelectList;
    private String assignmentCategory;

    public static final String UNASSIGNED_CATEGORY = "unassigned";

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
		List gbCategories = getGradebookManager().getCategories(getGradebookId());
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
			
			Assignment originalAssignment = getGradebookManager().getAssignment(assignmentId);
			Double origPointsPossible = originalAssignment.getPointsPossible();
			Double newPointsPossible = assignment.getPointsPossible();
			boolean scoresEnteredForAssignment = getGradebookManager().isEnteredAssignmentScores(assignmentId);
			
			/* If grade entry by percentage or letter and the points possible has changed for this assignment,
			 * we need to convert all of the stored point values to retain the same value
			 */
			if ((getGradeEntryByPercent() || getGradeEntryByLetter()) && scoresEnteredForAssignment) {
				if (!newPointsPossible.equals(origPointsPossible)) {
					List enrollments = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
			        List studentUids = new ArrayList();
			        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
			            studentUids.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
			        }
					getGradebookManager().convertGradePointsForUpdatedTotalPoints(getGradebook(), originalAssignment, assignment.getPointsPossible(), studentUids);
				}
			}
			
			getGradebookManager().updateAssignment(assignment);
			
			if ((!origPointsPossible.equals(newPointsPossible)) && scoresEnteredForAssignment) {
				if (getGradeEntryByPercent() || getGradeEntryByLetter())
					FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_converted", new String[] {assignment.getName()}));
				else
					FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_scored", new String[] {assignment.getName()}));

			} else {
				FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save", new String[] {assignment.getName()}));
			}

		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
            return "failure";
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_locking_failure"));
            return "failure";
		}
		
		return navigateBack();
	}

	public String navigateToAssignmentDetails() {
		return navigateBack();
	}

	/**
	 * Go to assignment details page. InstructorViewBean contains duplicate
	 * of this method, cannot migrate up to GradebookDependentBean since
	 * needs assignmentId, which is defined here.
	 */
	public String navigateBack() {
		String breadcrumbPage = getBreadcrumbPage();
		final Boolean middle = new Boolean((String) SessionManager.getCurrentToolSession().getAttribute("middle"));
		
		if (breadcrumbPage == null || middle) {
			AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
			assignmentDetailsBean.setAssignmentId(assignmentId);
			assignmentDetailsBean.setBreadcrumbPage(breadcrumbPage);
			
			breadcrumbPage = "assignmentDetails";
		}

		// wherever we go, we're not editing, and middle section
		// does not need to be shown.
		setNav(null, "false", null, "false", null);
		
		return breadcrumbPage;
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
	
	public Gradebook getLocalGradebook()
	{
		return getGradebook();
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

