/**********************************************************************************
*
* $Id: AssignmentBean.java  $
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

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
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.GradebookService;

public class AssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(AssignmentBean.class);

	private Long assignmentId;
    private Assignment assignment;
    private List categoriesSelectList;
    private String assignmentCategory;

    // added to support bulk gradebook item creation
    public List newBulkItems; 
    public int numTotalItems = 1;
    public List addItemSelectList;
    
    public static final String UNASSIGNED_CATEGORY = "unassigned";

    /** 
     * To add the proper number of blank gradebook item objects for bulk creation 
     */
    private static final int NUM_EXTRA_ASSIGNMENT_ENTRIES = 50;
    
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

		// To support bulk creation of assignments
		if (newBulkItems == null) {
			newBulkItems = new ArrayList();
		}

		// initialize the number of items to add dropdown
		addItemSelectList = new ArrayList();
		addItemSelectList.add(new SelectItem("0", ""));
		for (int i = 1; i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			addItemSelectList.add(new SelectItem(new Integer(i).toString(), new Integer(i).toString()));
		}

		for (int i = newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {			
			BulkAssignmentDecoratedBean item = getNewAssignment();
			
			if (i == 0) {
				item.setSaveThisItem("true");
			}
			
			newBulkItems.add(item);
		}
}

	private BulkAssignmentDecoratedBean getNewAssignment() {
		Assignment assignment = new Assignment();
		assignment.setReleased(true);
		
		BulkAssignmentDecoratedBean bulkAssignmentDecoBean = new BulkAssignmentDecoratedBean(assignment, getItemCategoryString(assignment));

		return bulkAssignmentDecoBean;
	}

	private String getItemCategoryString(Assignment assignment) {
		String assignmentCategory;
		Category assignCategory = assignment.getCategory();
		if (assignCategory != null) {
			assignmentCategory = assignCategory.getId().toString();
		}
		else {
			assignmentCategory = getLocalizedString("cat_unassigned");
		}
		
		return assignmentCategory;
	}
	
	/**
	 * Used to check if all gradebook items are valid before saving. Due to the way JSF works, had to turn off
	 * validators for bulk assignments so had to perform checks here.
	 * This is an all-or-nothing save, ie, either all items are OK and we save them all, or return to add page
	 * and highlight errors.
	 */
	public String saveNewAssignment() {
		String resultString = "overview";
		boolean saveAll = true;

		// keep list of new assignment names just in case
		// duplicates entered on screen
		List newAssignmentNameList = new ArrayList();
		
		// used to hold assignments that are OK since we 
		// need to determine if all are correct before saving
		List itemsToSave = new ArrayList();

		Iterator assignIter = newBulkItems.iterator();
		int i = 0;
		while (i < numTotalItems && assignIter.hasNext()) {
			BulkAssignmentDecoratedBean bulkAssignDecoBean = (BulkAssignmentDecoratedBean) assignIter.next();
			
			if (bulkAssignDecoBean.getBlnSaveThisItem()) {
				Assignment bulkAssignment = bulkAssignDecoBean.getAssignment();
			
				// Check for blank entry else check if duplicate within items to be
				// added or with item currently in gradebook.
				if ("".equals(bulkAssignment.getName().toString().trim())) {
					bulkAssignDecoBean.setBulkNoTitleError("blank");
					saveAll = false;
					resultString = "failure";
				}
				else if (newAssignmentNameList.contains(bulkAssignment.getName().trim()) ||
						 ! getGradebookManager().checkValidName(getGradebookId(), bulkAssignment)){
					bulkAssignDecoBean.setBulkNoTitleError("dup");
					saveAll = false;
					resultString = "failure";
				}
				else {
					bulkAssignDecoBean.setBulkNoTitleError("OK");
					newAssignmentNameList.add(bulkAssignment.getName().trim());
				}

				// Check if points possible is blank else convert to double. Exception at else point
				// means non-numeric value entered.
				if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
					bulkAssignDecoBean.setBulkNoPointsError("blank");
					saveAll = false;
					resultString = "failure";
				}
				else {
					try {
						double dblPointsPossible = new Double(bulkAssignDecoBean.getPointsPossible()).doubleValue();

						// Added per SAK-13459: did not validate if point value was valid (> zero)
						if (dblPointsPossible > 0) {
							// No more than 2 decimal places can be entered.
							BigDecimal bd = new BigDecimal(dblPointsPossible);
							bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
							double roundedVal = bd.doubleValue();
							double diff = dblPointsPossible - roundedVal;
							if(diff != 0) {
								saveAll = false;
								resultString = "failure";
								bulkAssignDecoBean.setBulkNoPointsError("precision");
							}
							else {
								bulkAssignDecoBean.setBulkNoPointsError("OK");
								bulkAssignDecoBean.getAssignment().setPointsPossible(new Double(bulkAssignDecoBean.getPointsPossible()));
							}
						}
						else {
							saveAll = false;
							resultString = "failure";
							bulkAssignDecoBean.setBulkNoPointsError("invalid");
						}
					}
					catch (Exception e) {
						bulkAssignDecoBean.setBulkNoPointsError("NaN");
						saveAll = false;
						resultString = "failure";
					}
				}
			
				if (saveAll) {
					bulkAssignDecoBean.getAssignment().setCategory(retrieveSelectedCategory(bulkAssignDecoBean.getCategory()));
			    	itemsToSave.add(bulkAssignDecoBean.getAssignment());
				}

				// Even if errors increment since we need to go back to add page
		    	i++;
			}
		}

		// Now ready to save, the only problem is due to duplicate names.
		if (saveAll) {
			try {
				getGradebookManager().createAssignments(getGradebookId(), itemsToSave);
				
				for (Iterator gbItemIter = itemsToSave.iterator(); gbItemIter.hasNext();) {
					FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", 
									new String[] {((Assignment) gbItemIter.next()).getName()}));
				}
			}
			catch (MultipleAssignmentSavingException e) {
				FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
				resultString = "failure";
			}
		}
		else {
			// There are errors so need to put an error message at top
			FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
		}
		
		return resultString;
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
				if (getGradeEntryByPercent())
					FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_percentage", new String[] {assignment.getName()}));
				else if (getGradeEntryByLetter())
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

        return assignment;
    }
    
    public List getCategoriesSelectList() {
    	return categoriesSelectList;
    }
    
    public List getAddItemSelectList() {
		return addItemSelectList;
	}

	public String getAssignmentCategory() {
    	return assignmentCategory;
    }
    
    public void setAssignmentCategory(String assignmentCategory) {
    	this.assignmentCategory = assignmentCategory;
    }
	
    /**
     * getNewBulkItems
     * 
     * Generates and returns a List of blank Assignment objects.
     * Used to support bulk gradebook item creation.
     */
	public List getNewBulkItems() {
		if (newBulkItems == null) {
			newBulkItems = new ArrayList();
		}

		for (int i = newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			newBulkItems.add(getNewAssignment());
		}
		
		return newBulkItems;
	}

	public void setNewBulkItems(List newBulkItems) {
		this.newBulkItems = newBulkItems;
	}

	public int getNumTotalItems() {
		return numTotalItems;
	}

	public void setNumTotalItems(int numTotalItems) {
		this.numTotalItems = numTotalItems;
	}

	public Gradebook getLocalGradebook()
	{
		return getGradebook();
	}
    
    /**
     * Returns the Category associated with assignmentCategory
     * If unassigned or not found, returns null
     * 
     * added parameterized version to support bulk gradebook item creation
     */
    private Category retrieveSelectedCategory() 
    {
    	return retrieveSelectedCategory(assignmentCategory);
    }
       
    private Category retrieveSelectedCategory(String assignmentCategory) 
    {
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

    /**
     * For bulk assignments, need to set the proper classes as a string
     */
	public String getRowClasses()
	{
		StringBuffer rowClasses = new StringBuffer();
		
		if (newBulkItems == null) {
			newBulkItems = getNewBulkItems();
		}

		//if shown in UI, set class to 'bogus show' otherwise 'bogus hide'
		for (int i=0; i< newBulkItems.size(); i++){
			Object obj = newBulkItems.get(i);
			if(obj instanceof BulkAssignmentDecoratedBean){
				BulkAssignmentDecoratedBean assignment = (BulkAssignmentDecoratedBean) newBulkItems.get(i);
			
				if (i != 0) rowClasses.append(",");

				if (assignment.getBlnSaveThisItem() || i == 0) {
					rowClasses.append("show bogus");
				}
				else {
					rowClasses.append("hide bogus");					
				}
			}		
		}
		
		return rowClasses.toString();
	}
}

