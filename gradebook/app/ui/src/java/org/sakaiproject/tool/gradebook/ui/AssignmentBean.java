/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class AssignmentBean extends GradebookDependentBean implements Serializable {
    private Long assignmentId;
    private GradebookAssignment assignment;
    private List categoriesSelectList;
    private String extraCreditCategories;
    private String assignmentCategory;
    // these 2 used to determine whether to zero-out the point value in applyPointsPossibleForDropScoreCategories
    private boolean categoryChanged;
    public boolean gradeEntryTypeChanged;

	private Category selectedCategory;
    private boolean selectedCategoryDropsScores;
    private boolean selectedAssignmentIsOnlyOne;
    private List<Category> categories;
    
    private ScoringAgentData scoringAgentData;

    // added to support bulk gradebook item creation
    public List newBulkItems; 
    public int numTotalItems = 1;
    public List addItemSelectList;
    
    public String gradeEntryType;
    public static final String UNASSIGNED_CATEGORY = "unassigned";
    public static final String GB_ADJUSTMENT_ENTRY = "Adjustment";
    
    private static final String GB_EDIT_ASSIGNMENT_PAGE = "editAssignment";
    
    /** 
     * To add the proper number of blank gradebook item objects for bulk creation 
     */
    private static final int NUM_EXTRA_ASSIGNMENT_ENTRIES = 50;
    
	protected void init() {
		if (log.isDebugEnabled()) log.debug("init assignment=" + assignment);

		if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new GradebookAssignment();
				assignment.setReleased(true);
			}
		}

        // initialization; shouldn't enter here after category drop down changes
		if(selectedCategory == null && !UNASSIGNED_CATEGORY.equals(assignmentCategory)) {
			Category assignCategory = assignment.getCategory();
			if (assignCategory != null) {
				assignmentCategory = assignCategory.getId().toString();
				selectedCategoryDropsScores = assignCategory.isDropScores();
				assignCategory.setAssignmentList(retrieveCategoryAssignmentList(assignCategory));
				selectedAssignmentIsOnlyOne = isAssignmentTheOnlyOne(assignment, assignCategory);
				selectedCategory = assignCategory;
			}
			else {
				assignmentCategory = getLocalizedString("cat_unassigned");
			}
        }
		
		categoriesSelectList = new ArrayList();
		//create comma seperate string representation of the list of EC categories
		List<String> extraCreditCategoriesList = new ArrayList<String>();
		// The first choice is always "Unassigned"
		categoriesSelectList.add(new SelectItem(UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		List gbCategories = getGradebookManager().getCategories(getGradebookId());
		if (gbCategories != null && gbCategories.size() > 0)
		{
			Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				Category cat = (Category) catIter.next();
				categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
				if(cat.isExtraCredit()){
					extraCreditCategoriesList.add(cat.getId().toString());
				}
			}
		}
		extraCreditCategories = StringUtils.join(extraCreditCategoriesList, ",");

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

        
        if(newBulkItems.size() == NUM_EXTRA_ASSIGNMENT_ENTRIES) {
            applyPointsPossibleForDropScoreCategories(newBulkItems);
        } else {
            for (int i = newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {			
			BulkAssignmentDecoratedBean item = getNewAssignment();
			
			if (i == 0) {
				item.setSaveThisItem("true");
			}
			
			newBulkItems.add(item);
		}
        }
        
        if (isScoringAgentEnabled()) {
        	scoringAgentData = initializeScoringAgentData(getGradebookUid(), assignment.getId(), null);
        }
}

	private boolean isAssignmentTheOnlyOne(GradebookAssignment assignment, Category category){
		if(assignment != null && category != null && category.getAssignmentList() != null){
			if(category.getAssignmentList().size() == 1)
				return ((GradebookAssignment)category.getAssignmentList().get(0)).getId().equals(assignment.getId());
			else if(category.getAssignmentList().size() == 0)
				return true;
			else
				return false;
		}else{
			return false;
		}		
	}
	
    /* 
     * sets pointsPossible for items in categories that drop scores
     * and sets the assignment's category, so that the ui can read item.assignment.category.dropScores
     */
    private void applyPointsPossibleForDropScoreCategories(List items) {
        categories = getCategories();
        
        Map<String, Category> categoryCache = new HashMap<String, Category>();
        
        for(int i=0; i<items.size(); i++) {
            BulkAssignmentDecoratedBean bulkAssignment = (BulkAssignmentDecoratedBean)items.get(i);
            
            String assignmentCategory = bulkAssignment.getCategory();
            if(getLocalizedString("cat_unassigned").equalsIgnoreCase(assignmentCategory)) {
                Category unassigned = new Category();
                bulkAssignment.getAssignment().setCategory(unassigned); // set this unassigned category, so that in the ui, item.assignment.category.dropScores will return false
                if(categoryChanged || gradeEntryTypeChanged) {
                	//bulkAssignment.setPointsPossible(null);
                    bulkAssignment.getAssignment().setPointsPossible(null);
                }
            } else {
                for(int j=0; j<categories.size(); j++) {
                    Category category = (Category)categories.get(j);
                    
                    if(assignmentCategory.equals(category.getId().toString())) {
                        if(categoryCache.containsKey(category.getId().toString())){
                        	category = categoryCache.get(category.getId().toString());
                        }else{
                        	category = retrieveSelectedCategory(category.getId().toString(), true);
                        	categoryCache.put(category.getId().toString(), category);
                        }
                    	
                        bulkAssignment.getAssignment().setCategory(category); // set here, because need to read item.assignment.category.dropScores in the ui
                        if(category.getAssignmentList() != null && category.getAssignmentList().size() > 0 
                        		&& category.isDropScores() && !GB_ADJUSTMENT_ENTRY.equals(bulkAssignment.getSelectedGradeEntryValue())) {
                            bulkAssignment.setPointsPossible(category.getItemValue().toString());
                            bulkAssignment.getAssignment().setPointsPossible(category.getItemValue());
                        } else if(categoryChanged || gradeEntryTypeChanged) {
                        	if (category.isDropScores()) {
                        		bulkAssignment.setPointsPossible(null);
                        	}
                            bulkAssignment.getAssignment().setPointsPossible(null);
                        }
                        continue;
                    }
                }
            }
        }            
    }
    
    private Category getCategoryById(String categoryId) {
        if(categoryId == null) {
            return null;
        }
        Category category = null;
        for(int i=0; i<categories.size(); i++) {
            category = (Category)categories.get(i);
            String id = category.getId().toString();
            
            if(id != null && categoryId.trim().equals(id)) {
                break;
            }
        }
        return category;
    }
    
	private BulkAssignmentDecoratedBean getNewAssignment() {
		GradebookAssignment assignment = new GradebookAssignment();
		assignment.setReleased(true);
		if(selectedCategory != null && selectedCategory.isDropScores()) {
		    assignment.setPointsPossible(selectedCategory.getItemValue());
		}
		BulkAssignmentDecoratedBean bulkAssignmentDecoBean = new BulkAssignmentDecoratedBean(assignment, getItemCategoryString(assignment));

		return bulkAssignmentDecoBean;
	}

	private String getItemCategoryString(GradebookAssignment assignment) {
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
		Map<String, Category> categoryCache = new HashMap<String, Category>();
		while (i < numTotalItems && assignIter.hasNext()) {
			BulkAssignmentDecoratedBean bulkAssignDecoBean = (BulkAssignmentDecoratedBean) assignIter.next();
			
			if (bulkAssignDecoBean.getBlnSaveThisItem()) {
				GradebookAssignment bulkAssignment = bulkAssignDecoBean.getAssignment();
			
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
				
                Category selectedCategory;
                if(categoryCache.containsKey(bulkAssignDecoBean.getCategory())){
                	selectedCategory = categoryCache.get(bulkAssignDecoBean.getCategory());
                }else{
                	selectedCategory = retrieveSelectedCategory(bulkAssignDecoBean.getCategory(), true);
                	categoryCache.put(bulkAssignDecoBean.getCategory(), selectedCategory);
                }
				boolean categoryDropsScores = false;
				if(selectedCategory != null && selectedCategory.isDropScores()
						&& selectedCategory.getAssignmentList() != null && selectedCategory.getAssignmentList().size() > 0 ) {
				    categoryDropsScores = true;
                    if(!GB_ADJUSTMENT_ENTRY.equals(bulkAssignDecoBean.getSelectedGradeEntryValue())) {
                        bulkAssignDecoBean.setPointsPossible(selectedCategory.getItemValue().toString()); // if category drops scores and is not adjustment, point value will come from the category level
                    }
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
				        // Check if PointsPossible uses local number format
				        String strPointsPossible = bulkAssignDecoBean.getPointsPossible();
				        NumberFormat nf = NumberFormat.getInstance(new ResourceLoader().getLocale()); 

				        double dblPointsPossible = new Double (nf.parse(strPointsPossible).doubleValue());

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
					bulkAssignDecoBean.getAssignment().setCategory(retrieveSelectedCategory(bulkAssignDecoBean.getCategory(), false));
					// if points possible is still 0 at this point, set it to null to avoid Division By Zero exceptions.  These should never be allowed in the database.
					if (null != bulkAssignDecoBean.getAssignment().getPointsPossible() && bulkAssignDecoBean.getAssignment().getPointsPossible()==0)
						bulkAssignDecoBean.getAssignment().setPointsPossible(null);
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
				
				String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
	            for (Iterator gbItemIter = itemsToSave.iterator(); gbItemIter.hasNext();) {
	            	String itemName = ((GradebookAssignment) gbItemIter.next()).getName();
					FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", 
									new String[] {itemName}));
					getGradebookBean().getEventTrackingService().postEvent("gradebook.newItem","/gradebook/"+getGradebookId()+"/"+itemName+"/"+authzLevel);
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

            if(!GB_ADJUSTMENT_ENTRY.equals(assignment.getSelectedGradeEntryValue()) && category != null && category.isDropScores() && !isAssignmentTheOnlyOne(assignment, category)) {
                assignment.setPointsPossible(category.getItemValue()); // if category drops scores, point value will come from the category level
            }
			
			GradebookAssignment originalAssignment = getGradebookManager().getAssignment(assignmentId);
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
			long dueDateMillis = -1;
			Date dueDate = assignment.getDueDate();
			if (dueDate != null) dueDateMillis = dueDate.getTime();
			getGradebookBean().getEventTrackingService().postEvent("gradebook.updateAssignment","/gradebook/"+getGradebookUid()+"/"+assignment.getName()+"/"+assignment.getPointsPossible()+"/"+dueDateMillis+"/"+assignment.isReleased()+"/"+assignment.isCounted()+"/"+getAuthzLevel());
			
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
			log.error(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
            return "failure";
		} catch (StaleObjectModificationException e) {
            log.error(e.getMessage());
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
		if (log.isDebugEnabled()) log.debug("getAssignmentId " + assignmentId);
		return assignmentId;
	}
	public void setAssignmentId(Long assignmentId) {
		if (log.isDebugEnabled()) log.debug("setAssignmentId " + assignmentId);
		if (assignmentId != null) {
			this.assignmentId = assignmentId;
		}
	}

    public GradebookAssignment getAssignment() {
        if (log.isDebugEnabled()) log.debug("getAssignment " + assignment);
		if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new GradebookAssignment();
				assignment.setReleased(true);
			}
		}

        return assignment;
    }
    
    public List getCategoriesSelectList() {
    	return categoriesSelectList;
    }
    
    public String getExtraCreditCategories(){
    	return extraCreditCategories;
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
     * Generates and returns a List of blank GradebookAssignment objects.
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
    
    public List getCategories() {
        if(categories == null) {
            categories = getGradebookManager().getCategories(getGradebookId());
        }

        return categories;
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
    	return retrieveSelectedCategory(assignmentCategory, true);
    }
    
    private Category retrieveSelectedCategory(String assignmentCategory, boolean includeAssignments) 
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
				if(includeAssignments){
					//populate assignments list
					category.setAssignmentList(retrieveCategoryAssignmentList(category));
				}
			}
		}
		
		return category;
    }    
    
    private List retrieveCategoryAssignmentList(Category cat){
    	List assignmentsToUpdate = new ArrayList();
    	if(cat != null){
    		List assignments = cat.getAssignmentList();
    		if(cat.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
    			assignments = getGradebookManager().getAssignmentsForCategory(cat.getId());

    			// only include assignments which are not adjustments must not update adjustment item pointsPossible
    			for(Object o : assignments) { 
    				if(o instanceof GradebookAssignment) {
    					GradebookAssignment assignment = (GradebookAssignment)o;
    					if(!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
    						assignmentsToUpdate.add(assignment);
    					}
    				}
    			}
    		}
    	}
		return assignmentsToUpdate;
    }

    public boolean isSelectedCategoryDropsScores() {
        return selectedCategoryDropsScores;
    }

    public void setSelectedCategoryDropsScores(boolean selectedCategoryDropsScores) {
        this.selectedCategoryDropsScores = selectedCategoryDropsScores;
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String processCategoryChangeInEditAssignment(ValueChangeEvent vce)
    {
        String changeCategory = (String) vce.getNewValue();
        assignmentCategory = changeCategory;
        if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))  
        {
            if(changeCategory.equals(UNASSIGNED_CATEGORY)) {
                selectedCategoryDropsScores = false;
                selectedAssignmentIsOnlyOne = false;
                selectedCategory = null;
                assignmentCategory = getLocalizedString("cat_unassigned");
            } else {
                List<Category> categories = getGradebookManager().getCategories(getGradebookId());
                if (categories != null && categories.size() > 0)
                {
                    for (Category category : categories) {
                        if(changeCategory.equals(category.getId().toString())) {
                            selectedCategoryDropsScores = category.isDropScores();
                            category.setAssignmentList(retrieveCategoryAssignmentList(category));
                            selectedCategory = category;
                            selectedAssignmentIsOnlyOne = isAssignmentTheOnlyOne(assignment, selectedCategory);
                            assignmentCategory = category.getId().toString();
                            break;
                        }
                    }
                }
            }
        }
        return GB_EDIT_ASSIGNMENT_PAGE;
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

	public boolean isSelectedAssignmentIsOnlyOne() {
		return selectedAssignmentIsOnlyOne;
	}

	public void setSelectedAssignmentIsOnlyOne(boolean selectedAssignmentIsOnlyOne) {
		this.selectedAssignmentIsOnlyOne = selectedAssignmentIsOnlyOne;
	}
	
	public ScoringAgentData getScoringAgentData() {
		return this.scoringAgentData;
	}

}

