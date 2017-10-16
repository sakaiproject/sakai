/**********************************************************************************
 *
 * $Id: GradebookSetupBean.java 20001 2007-04-01 19:41:33Z wagnermr@iupui.edu $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.component.cover.ServerConfigurationService;

public class GradebookSetupBean extends GradebookDependentBean implements Serializable
{
	private static final Logger logger = LoggerFactory.getLogger(GradebookSetupBean.class);

	private String gradeEntryMethod;
	private String categorySetting;
    private boolean showDropHighestDisplayed;
    private boolean showDropLowestDisplayed;
    private boolean showKeepHighestDisplayed;
    private boolean anyCategoriesWithDrops;
	private List categories;
	private Gradebook localGradebook;
	private List categoriesToRemove;
	private double regularTotal;
	private double neededTotal;
	private double adjustmentTotal;
	private double grandTotal;
	private String pageName;
	private List letterGradeRows;
	private List letterGradesList;
	private LetterGradePercentMapping lgpm;
	private LetterGradePercentMapping defaultLGPM;
	private boolean enableLetterGrade = false;
  private boolean isValidWithCourseGrade = true;
	
	private boolean isLetterGrade = false;
    private boolean isPointGrade = false;
    private boolean isPercentageGrade = false;

    private static final int NUM_EXTRA_CAT_ENTRIES = 50;
	private static final String ENTRY_OPT_POINTS = "points";
	private static final String ENTRY_OPT_PERCENT = "percent";
	private static final String ENTRY_OPT_LETTER = "letterGrade";
	private static final String CATEGORY_OPT_NONE = "noCategories";
	private static final String CATEGORY_OPT_CAT_ONLY = "onlyCategories";
	private static final String CATEGORY_OPT_CAT_AND_WEIGHT = "categoriesAndWeighting";
    private static final String DROP_OPT_HIDE = "hideDrop";
    private static final String DROP_OPT_SHOW = "showDrop";

	private static final String GB_SETUP_PAGE = "gradebookSetup";
	private static final String GB_OVERVIEW_PAGE = "overview";

	private static final String ROW_INDEX_PARAM = "rowIndex";

	protected void init() 
	{
		if (localGradebook == null)
		{
			localGradebook = getGradebook();
			categories = getGradebookManager().getCategoriesWithStats(getGradebookId(),
                    GradebookAssignment.DEFAULT_SORT, true, Category.SORT_BY_NAME, true);
            populateCategoryAssignments(categories);
			convertWeightsFromDecimalsToPercentages();
			intializeGradeEntryAndCategorySettings();
			categoriesToRemove = new ArrayList();
		}
		
		calculateRunningTotal();
		
		defaultLGPM = getGradebookManager().getDefaultLetterGradePercentMapping();
		letterGradesList = new ArrayList(defaultLGPM.getGradeMap().keySet());
		Collections.sort(letterGradesList, GradebookService.lettergradeComparator);
		
		lgpm = getGradebookManager().getLetterGradePercentMapping(localGradebook);
		if (lgpm != null && lgpm.getGradeMap().size() > 0) {	
			initLetterGradeRows();
		}

        if(getAnyCategoriesWithDropHighest()) {
            showDropHighestDisplayed = true;
        } else {
            showDropHighestDisplayed = false;
        }

        if(getAnyCategoriesWithDropLowest()) {
            showDropLowestDisplayed = true;
        } else {
            showDropLowestDisplayed = false;
        }

        if(getAnyCategoriesWithKeepHighest()) {
            showKeepHighestDisplayed = true;
        } else {
            showKeepHighestDisplayed = false;
        }
	}
    
    /*
     * For category requests to drop scores, need their assignments populated
     * so that system can determine eligibility of category to drop scores
     * if assignments have unequal pointsPossible, then they cannot drop scores
     */
    private void populateCategoryAssignments(List categories) {
        if(categories != null) {
            for(Object obj : categories) {
                if(obj instanceof Category) {
                    Category category = (Category)obj;
                    List assignments = category.getAssignmentList();
                    if(category.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
                        assignments = getGradebookManager().getAssignmentsForCategory(category.getId());
                        List assignmentsToUpdate = new ArrayList();
                     // only include assignments which are not adjustments must not update adjustment item pointsPossible
                        for(Object o : assignments) { 
                            if(o instanceof GradebookAssignment) {
                                GradebookAssignment assignment = (GradebookAssignment)o;
                                if(!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
                                    assignmentsToUpdate.add(assignment);
                                }
                            }
                        }
                        if(assignments != null && assignments.size() > 0) {
                            category.setAssignmentList(assignmentsToUpdate);
                        }
                    }
                }
            }
        }
    }
    
//    private void removeDropHighestFromCategories() {
//        if(categories != null) {
//            for(Object obj : categories) {
//                if(obj instanceof Category) {
//                    Category category = (Category)obj;
//                    category.setDropHighest(0);
//                    if(category.getDrop_lowest() == 0 && category.getKeepHighest() == 0) {
//                        category.setItemValue(0.0);
//                    }
//                }
//            }
//        }
//    }
//    
//    private void removeDropLowestFromCategories() {
//        if(categories != null) {
//            for(Object obj : categories) {
//                if(obj instanceof Category) {
//                    Category category = (Category)obj;
//                    category.setDrop_lowest(0);
//                    if(category.getDropHighest() == 0 && category.getKeepHighest() == 0) {
//                        category.setItemValue(0.0);
//                    }
//                }
//            }
//        }
//    }
//    
//    private void removeKeepHighestFromCategories() {
//        if(categories != null) {
//            for(Object obj : categories) {
//                if(obj instanceof Category) {
//                    Category category = (Category)obj;
//                    category.setKeepHighest(0);
//                    if(category.getDrop_lowest() == 0 && category.getDropHighest() == 0) {
//                        category.setItemValue(0.0);
//                    }
//                }
//            }
//        }
//    }
	
	private void initLetterGradeRows() {
		letterGradeRows = new ArrayList();
		
		for (Iterator iter = letterGradesList.iterator(); iter.hasNext(); ) {
			String grade = (String)iter.next();
			
			// Bottom grades (with a lower bound of 0%)
			Double d = defaultLGPM.getValue(grade);
			boolean editable = ((d != null) && (d.doubleValue() > 0.0));
			letterGradeRows.add(new LetterGradeRow(lgpm, grade, editable));
		}
	}

	private void reset()
	{
		localGradebook = null;
		categories = null;
		categorySetting = null;
		gradeEntryMethod = null;
		isValidWithCourseGrade = true;
	}

	public Gradebook getLocalGradebook()
	{
		return localGradebook;
	}

	public String getGradeEntryMethod()
	{		
		return gradeEntryMethod;
	}

	public void setGradeEntryMethod(String gradeEntryMethod)
	{
		this.gradeEntryMethod = gradeEntryMethod;
	} 

	public String getCategorySetting()
	{	
		return categorySetting;
	}

	public void setCategorySetting(String categorySetting)
	{
		this.categorySetting = categorySetting;
	} 
	
	public boolean getShowDropHighestDisplayed() {
        return showDropHighestDisplayed;
    }

    public void setShowDropHighestDisplayed(boolean showDropHighestDisplayed) {
        this.showDropHighestDisplayed = showDropHighestDisplayed;
    }

    public boolean getShowDropLowestDisplayed() {
        return showDropLowestDisplayed;
    }

    public void setShowDropLowestDisplayed(boolean showDropLowestDisplayed) {
        this.showDropLowestDisplayed = showDropLowestDisplayed;
    }

    public boolean getShowKeepHighestDisplayed() {
        return showKeepHighestDisplayed;
    }

    public void setShowKeepHighestDisplayed(boolean showKeepHighestDisplayed) {
        this.showKeepHighestDisplayed = showKeepHighestDisplayed;
    }

    public void setAnyCategoriesWithDropHighest(boolean anyCategoriesWithDropHighest) {
    }

    public void setAnyCategoriesWithDropLowest(boolean anyCategoriesWithDropLowest) {
    }

    public void setAnyCategoriesWithKeepHighest(boolean anyCategoriesWithKeepHighest) {
    }
    
    public boolean getAnyCategoriesWithDropHighest() {
        boolean anyDrops = false;
        if(categories != null) {
            for(Object obj : categories) {
                if(obj instanceof Category) {
                    Category category = (Category)obj;
                    anyDrops = category.getDropHighest() > 0;
                    setShowDropHighestDisplayed(anyDrops);
                    if(anyDrops)
                        break;
                }
            }
        }
        return anyDrops;
    }
    
    public boolean getAnyCategoriesWithDropLowest() {
        boolean anyDrops = false;
        if(categories != null) {
            for(Object obj : categories) {
                if(obj instanceof Category) {
                    Category category = (Category)obj;
                    anyDrops = category.getDrop_lowest() > 0;
                    setShowDropLowestDisplayed(anyDrops);
                    if(anyDrops)
                        break;
                }
            }
        }
        return anyDrops;
    }
    
    public boolean getAnyCategoriesWithKeepHighest() {
        boolean anyDrops = false;
        if(categories != null) {
            for(Object obj : categories) {
                if(obj instanceof Category) {
                    Category category = (Category)obj;
                    anyDrops = category.getKeepHighest() > 0;
                    setShowKeepHighestDisplayed(anyDrops);
                    if(anyDrops)
                        break;
                }
            }
        }
        return anyDrops;
    }

	/**
	 * 
	 * @return String value of display:none or display:block for initial display
	 * of grade entry scale
	 */
	public String getDisplayGradeEntryScaleStyle()
	{
		if (gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_LETTER))
			return "display:block;";
		else
			return "display:none;";
	}

	/**
	 * Returns true if categories are used in gb
	 * @return
	 */
	public boolean isDisplayCategories()
	{
		return !categorySetting.equals(CATEGORY_OPT_NONE);
	}

	/**
	 * Returns true if weighting is used in gb
	 * @return
	 */
	public boolean isDisplayWeighting()
	{
		return categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT);
	}

	/**
	 * Save gradebook settings (including categories and weighting)
	 * @return
	 */
	public String processSaveGradebookSetup()
	{
		if (gradeEntryMethod == null || (!gradeEntryMethod.equals(ENTRY_OPT_POINTS) && 
				!gradeEntryMethod.equals(ENTRY_OPT_PERCENT) && !gradeEntryMethod.equals(ENTRY_OPT_LETTER)))
		{
			FacesUtil.addErrorMessage(getLocalizedString("grade_entry_invalid"));
			return "failure";
		}
    if(!isConflictWithCourseGrade())
    {
    	isValidWithCourseGrade = false;
    	return null;
    }
    else
    	isValidWithCourseGrade = true;
		
		int origialGradeType = localGradebook.getGrade_type();

		if (gradeEntryMethod.equals(ENTRY_OPT_PERCENT))
		{
			localGradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		}
		else if (gradeEntryMethod.equals(ENTRY_OPT_LETTER))
		{
			localGradebook.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		}
		else
		{
			localGradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		}
		// SAK-10879 - comment out ability to customize lgpm
		/*
		if (lgpm != null) {
			if (!isMappingValid(lgpm)) {
				return "failure";
			}
			
			LetterGradePercentMapping originalLgpm = getGradebookManager().getLetterGradePercentMapping(localGradebook);
			boolean lgpmUpdate = false;
			for (Iterator iter = letterGradesList.iterator(); iter.hasNext(); ) {
				String grade = (String) iter.next();
				Double originalPercent = null;
				if (originalLgpm != null)
					originalPercent = (Double) originalLgpm.getValue(grade);
				Double currentPercent = (Double) lgpm.getValue(grade);
				if (!originalPercent.equals(currentPercent)) {
					lgpmUpdate = true;
					break;
				}
			}
			
			if (lgpmUpdate) {
				getGradebookManager().saveOrUpdateLetterGradePercentMapping(lgpm.getGradeMap(), localGradebook);
			}
		}*/
		
		if (categorySetting == null || (!categorySetting.equals(CATEGORY_OPT_CAT_ONLY) && 
				!categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT) && !categorySetting.equals(CATEGORY_OPT_NONE)))
		{
			FacesUtil.addErrorMessage(getLocalizedString("cat_setting_invalid"));
			return "failure";
		}
		int origCategorySetting = localGradebook.getCategory_type();
		if (categorySetting.equals(CATEGORY_OPT_NONE))
		{
			localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
			// remove current categories
			List gbCategories = getGradebookManager().getCategories(localGradebook.getId());
			if (gbCategories != null && !gbCategories.isEmpty())
			{
				Iterator removeIter = gbCategories.iterator();
				while (removeIter.hasNext())
				{
					Category removeCat = (Category) removeIter.next();
					getGradebookManager().removeCategory(removeCat.getId());
				}
			}
			
			// check to see if any permissions need to be removed
			List sections = getAllSections();
			List gbPermissions = getGradebookManager().getPermissionsForGB(localGradebook.getId());
			if (gbPermissions != null) {
				for (Iterator permIter = gbPermissions.iterator(); permIter.hasNext();) {
					Permission perm = (Permission) permIter.next();
					// if there is a specific category associated with this permission or if
					// there are no sections defined in the site, we need to delete this permission
					if (perm.getCategoryId() != null || sections == null || sections.size() == 0) {
						logger.debug("Permission " + perm.getId() + " was deleted b/c gb changed to no categories");
						getGradebookManager().deletePermission(perm);
					}
				}
			}

			getGradebookManager().updateGradebook(localGradebook);
			reset();

			FacesUtil.addRedirectSafeMessage(getLocalizedString("gb_save_msg"));

			return null;
		}
		
		// if we are going from no categories to having categories, we need to set
		// counted = false for all existing assignments b/c the category will
		// now be "unassigned"
		/*if (localGradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY &&
				(categorySetting.equals(CATEGORY_OPT_CAT_ONLY) || categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT))) {
			List assignmentsInGb = getGradebookManager().getAssignments(getGradebookId(), GradebookAssignment.DEFAULT_SORT, true);
			if (assignmentsInGb != null && !assignmentsInGb.isEmpty()) {
				Iterator assignIter = assignmentsInGb.iterator();
				while (assignIter.hasNext()) {
					GradebookAssignment assignment = (GradebookAssignment) assignIter.next();
					assignment.setCounted(false);
					getGradebookManager().updateAssignment(assignment);
				}
			}
		} */

		if (categorySetting.equals(CATEGORY_OPT_CAT_ONLY))
		{
			localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);

			// set all weighting to 0 for existing categories
			Iterator unweightIter = categories.iterator();
			while (unweightIter.hasNext())
			{
				Category unweightCat = (Category) unweightIter.next();
				unweightCat.setWeight(new Double(0));
			}
		}
		else if (categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT))
		{
			// we need to make sure all of the weights add up to 100
			calculateRunningTotal();
			if (neededTotal != 0)
			{
				FacesUtil.addErrorMessage(getLocalizedString("cat_weight_total_not_100"));
				return "failure";
			}
			localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		}
        
//        if(getShowDropHighestDisplayed() == false
//                || localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_LETTER // when Grade Entry change from Points/Percentage to Letter Grades also remove drops from categories
//                 && (origialGradeType==GradebookService.GRADE_TYPE_POINTS 
//                         || origialGradeType==GradebookService.GRADE_TYPE_PERCENTAGE)) { // handles the case when user switches grade entry method
//            removeDropHighestFromCategories();
//        }
//        if(getShowDropLowestDisplayed() == false
//                || localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_LETTER // when Grade Entry change from Points/Percentage to Letter Grades also remove drops from categories
//                 && (origialGradeType==GradebookService.GRADE_TYPE_POINTS 
//                         || origialGradeType==GradebookService.GRADE_TYPE_PERCENTAGE)) { // handles the case when user switches grade entry method
//            removeDropLowestFromCategories();
//        }
//        if(getShowKeepHighestDisplayed() == false
//                || localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_LETTER // when Grade Entry change from Points/Percentage to Letter Grades also remove drops from categories
//                 && (origialGradeType==GradebookService.GRADE_TYPE_POINTS 
//                         || origialGradeType==GradebookService.GRADE_TYPE_PERCENTAGE)) { // handles the case when user switches grade entry method
//            removeKeepHighestFromCategories();
//        }
        
        // do drop scores validation before on all categories before the database transactions begins
        Iterator itr = categories.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if(!(obj instanceof Category)) {
                continue;
            }

            Category uiCategory = (Category) obj;
            Long categoryId = uiCategory.getId();
            String categoryName = uiCategory.getName();

            // do cross validation 
            if((uiCategory.getDrop_lowest() > 0 || uiCategory.getDropHighest() > 0) && uiCategory.getKeepHighest() > 0) {
               FacesUtil.addErrorMessage(getLocalizedString("cat_keep_and_drop_mutually_exclusive"));
               return "failure";
            }
            if(uiCategory.getItemValue() < 0 && (uiCategory.getDrop_lowest() > 0 || uiCategory.getDropHighest() > 0 || uiCategory.getKeepHighest() > 0)) {
               FacesUtil.addErrorMessage(getLocalizedString("cat_pointvalue_not_valid"));
               return "failure";
            }

            if(uiCategory.isDropScores()) {
               if (!uiCategory.isAssignmentsEqual()) {
                   if(gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_POINTS)) {
                       FacesUtil.addErrorMessage(getLocalizedString("cat_pointvalue_not_valid"));
                   } else if(gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_PERCENT)) {
                       FacesUtil.addErrorMessage(getLocalizedString("cat_relativeweight_not_valid"));
                   }
                   return "failure";
               }
            }

            // we will be updating an existing category
            if (categoryId != null) {
               Category updatedCategory = getGradebookManager().getCategory(categoryId);
               if(updatedCategory.isDropScores()) {
                   if(!updatedCategory.isAssignmentsEqual()) {
                       if (gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_POINTS)) {
                           FacesUtil.addErrorMessage(getLocalizedString("cat_point_values_unequal"));
                       }
                       if (gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_PERCENT)) {
                           FacesUtil.addErrorMessage(getLocalizedString("cat_rel_weights_unequal"));
                       }
                       return "failure";
                   }
               }
            }
        }        

		
		/* now we need to iterate through the categories and
		 	1) remove categories
		 	2) add any new categories
		 	3) update existing categories */

		Iterator catIter = categories.iterator();
		while (catIter.hasNext())
		{
			try {
				Object obj = catIter.next();
				if(!(obj instanceof Category)){
					continue;
				}
				
				Category uiCategory = (Category) obj;
				Long categoryId = uiCategory.getId();
				String categoryName = uiCategory.getName();

				if ((categoryName == null || categoryName.trim().length() < 1) && categoryId != null)
				{
					categoriesToRemove.add(categoryId);
				}

				if (categoryName != null && categoryName.length() > 0)
				{
					// treat blank weight fields as 0
					if (localGradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY &&
							uiCategory.getWeight() == null) {
						uiCategory.setWeight(new Double(0));
					}
					
					if ((localGradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY 
							&& localGradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
							|| uiCategory.isExtraCredit() == null)
					{
						uiCategory.setExtraCredit(false);
					}

					if (categoryId == null) {
						// must be a new or blank category
						if (uiCategory.getWeight() != null && uiCategory.getWeight().doubleValue() > 0) {
							getGradebookManager().createCategory(localGradebook.getId(), categoryName.trim(), new Double(uiCategory.getWeight().doubleValue()/100), uiCategory.getDrop_lowest(), uiCategory.getDropHighest(), uiCategory.getKeepHighest(), uiCategory.isExtraCredit());
						} else {
							getGradebookManager().createCategory(localGradebook.getId(), categoryName.trim(), uiCategory.getWeight(), uiCategory.getDrop_lowest(), uiCategory.getDropHighest(), uiCategory.getKeepHighest(), uiCategory.isExtraCredit());
						}
					}
					else {
						// we are updating an existing category
						Category updatedCategory = getGradebookManager().getCategory(categoryId);
						updatedCategory.setName(categoryName.trim());
						if (uiCategory.getWeight() != null && uiCategory.getWeight().doubleValue() > 0) {
							updatedCategory.setWeight(new Double (uiCategory.getWeight().doubleValue()/100));
						} else {
							updatedCategory.setWeight(uiCategory.getWeight());
						}
						if (uiCategory.isExtraCredit()!=null)
						{
							updatedCategory.setExtraCredit(uiCategory.isExtraCredit());
						}
						
						updatedCategory.setDrop_lowest(uiCategory.getDrop_lowest());
                        updatedCategory.setDropHighest(uiCategory.getDropHighest());
                        updatedCategory.setKeepHighest(uiCategory.getKeepHighest());
//                        if(uiCategory.getItemValue() != null && uiCategory.getItemValue().doubleValue() > 0) {
//                            updatedCategory.setItemValue(uiCategory.getItemValue());
//                        } else {
//                            updatedCategory.setItemValue(0.0);
//                        }

                        if(updatedCategory.isDropScores() && updatedCategory.isAssignmentsEqual()) {
                            if((updatedCategory.getAssignmentList() == null || updatedCategory.getAssignmentList().size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
                                List assignments = getGradebookManager().getAssignmentsForCategory(updatedCategory.getId());
                                List assignmentsToUpdate = new ArrayList();
                                for(Object o : assignments) { // must not update adjustment item pointsPossible
                                    if(o instanceof GradebookAssignment) {
                                        GradebookAssignment assignment = (GradebookAssignment)o;
                                        if(!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
                                            assignmentsToUpdate.add(assignment);
                                        }
                                    }
                                }
                                updatedCategory.setAssignmentList(assignmentsToUpdate);
                            }
                            // now update the pointsPossible of any assignments within the category that drop scores
                            getGradebookManager().updateCategoryAndAssignmentsPointsPossible(localGradebook.getId(), updatedCategory);
                        } else {
                            getGradebookManager().updateCategory(updatedCategory);
                        }
					}
				}
			}
			catch (ConflictingCategoryNameException cne) {
				FacesUtil.addErrorMessage(getLocalizedString("cat_same_name_error"));
				return "failure";
			}
			catch (StaleObjectModificationException e) {
				logger.error(e.getMessage());
				FacesUtil.addErrorMessage(getLocalizedString("cat_locking_failure"));
				return "failure";
			}
		}

		// remove any categories marked to remove
		if (categoriesToRemove != null && categoriesToRemove.size() > 0) {
			Iterator removeIter = categoriesToRemove.iterator();
			while (removeIter.hasNext()) {
				Long removeId = (Long) removeIter.next();
				getGradebookManager().removeCategory(removeId);
			}
			
			List permsToRemove = getGradebookManager().getPermissionsForGBForCategoryIds(localGradebook.getId(), categoriesToRemove);
			if (!permsToRemove.isEmpty()) {
				for (Iterator permIter = permsToRemove.iterator(); permIter.hasNext();) {
					Permission perm = (Permission) permIter.next();
					logger.debug("Permission " + perm.getId() + " was deleted b/c category deleted");
					getGradebookManager().deletePermission(perm);
				}
			}
		}
		
		//SAK-22417 When changing to a category gradebook, items that move to unassigned still have included in course grade as YES
		//This also includes the case where a GB category is deleted and the item is set to uncategorized.
		if((GradebookService.CATEGORY_TYPE_ONLY_CATEGORY == localGradebook.getCategory_type() || GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY == localGradebook.getCategory_type()) 
				&& (GradebookService.CATEGORY_TYPE_NO_CATEGORY == origCategorySetting || (categoriesToRemove != null && categoriesToRemove.size() > 0))){
			setUncategoriedAssignmentsToNotCounted(localGradebook.getId());
		}

		getGradebookManager().updateGradebook(localGradebook);

		FacesUtil.addRedirectSafeMessage(getLocalizedString("gb_save_msg"));
		reset();
		return null;
	}
	
	private void setUncategoriedAssignmentsToNotCounted(Long gradebookId){
		List assigns = getGradebookManager().getAssignmentsWithNoCategory(gradebookId, null, true);
		for(Iterator iter = assigns.iterator(); iter.hasNext();)
		{
			GradebookAssignment assignment = (GradebookAssignment) iter.next();
			assignment.setCounted(false);
			getGradebookManager().updateAssignment(assignment);
		}
	}

	/**
	 * Removes the selected category from the local list. If category
	 * exists in gb, retains id for future use if/when setup is saved
	 * @param event
	 * @return
	 */
	public String processRemoveCategory(ActionEvent event)
	{
		if (categories == null || categories.isEmpty())
			return GB_SETUP_PAGE;

		try
		{
			Map params = FacesUtil.getEventParameterMap(event);
			Integer index = (Integer) params.get(ROW_INDEX_PARAM);
			if (index == null) {
				return GB_SETUP_PAGE;
			}
			int indexToRemove = index.intValue();
			Category catToRemove = (Category)categories.get(indexToRemove);
			// new categories will not have an id yet so don't need to be retained
			if (catToRemove.getId() != null)
			{
				categoriesToRemove.add(catToRemove.getId());
			}
			categories.remove(indexToRemove);
		}
		catch(Exception e)
		{
			// do nothing
		}

		return GB_SETUP_PAGE;
	}

	public String processCategorySettingChange(ValueChangeEvent vce)
	{
		String changeAssign = (String) vce.getNewValue(); 
		if (changeAssign != null && (changeAssign.equals(CATEGORY_OPT_NONE) || 
				changeAssign.equals(CATEGORY_OPT_CAT_AND_WEIGHT) || 
				changeAssign.equals(CATEGORY_OPT_CAT_ONLY)))
		{
			categorySetting = changeAssign;
		}

		return GB_SETUP_PAGE;
	}
	
	public String processGradeEntryMethodChange(ValueChangeEvent vce)
	{
//		Object changeAssign = (Object) vce.getNewValue();
//		if(changeAssign instanceof String) {
//			String newValue = (String) vce.getNewValue();
//			if (newValue != null && (newValue.equals(ENTRY_OPT_POINTS) || 
//					newValue.equals(ENTRY_OPT_PERCENT) || 
//					newValue.equals(ENTRY_OPT_LETTER)))
//			{
//				gradeEntryMethod = newValue;
//				if(categories != null) {
//				    for(Object obj : categories) {
//				        if(obj instanceof Category) {
//				            Category category = (Category)obj;
//                            category.setItemValue(0.0);
//				        }
//				    }
//				}
//			}
//		}
		return GB_SETUP_PAGE;
	}



	public String processCancelGradebookSetup()
	{
		reset();
		return GB_OVERVIEW_PAGE;
	}

	/*
	 * Returns list of categories
	 * Also includes blank categories to allow the user to enter new categories
	 */
	public List getCategories()
	{		
		//first, iterate through the list and remove blank lines
		for (int i=0; i < categories.size(); i++)
		{
			Object obj = categories.get(i);
			if(!(obj instanceof Category)){
				categories.remove(i);
				continue;
			}
			Category cat = (Category)categories.get(i);
			int assignmentCount = 0;
			if(cat.getAssignmentList() != null){
				assignmentCount = cat.getAssignmentList().size();
			}
			cat.setAssignmentCount(assignmentCount);
			if (cat.getName() == null || cat.getName().trim().length() == 0)
			{
				if (cat.getId() != null) 
				{
					// this will take care of instances where user just deleted cat name
					// instead of hitting "remove"
					categoriesToRemove.add(cat.getId());
				}
				categories.remove(cat);
				i--;
			}
		}

		// always display 5 blank entries for new categories
		for (int i=0; i < NUM_EXTRA_CAT_ENTRIES; i++)
		{
			Category blankCat = new Category();
			categories.add(blankCat);
		}

		return categories;
	}
	
	public String getRowClasses()
	{
		StringBuilder rowClasses = new StringBuilder();
		//first add the row class "bogus" for current categories
		for (int i=0; i<categories.size(); i++){
			Object obj = categories.get(i);
			if(!(obj instanceof Category)){
				continue;
			}
			Category cat = (Category)categories.get(i);
			if (cat.getName() != null && cat.getName().trim().length() != 0)
			{
				if(i != 0){
					rowClasses.append(",");
				}
				rowClasses.append("bogus");
			}
		}
		
		//add row class "bogus_hide" for blank categories
		for (int i=0; i < NUM_EXTRA_CAT_ENTRIES; i++){
			if(i == 0 && categories.size() == 0){
				rowClasses.append("bogus");
			}
			rowClasses.append(",");
			rowClasses.append("bogus hide");
		}
		
		return rowClasses.toString();
	}

    /**
	 * Returns sum of all category weights minus the adjustment categories.  This one must be 100% to process correctly.
	 * @return
	 */
	public double getRegularTotal()
	{	
		return regularTotal;
	}


	/**
	 * Returns % needed to reach 100% for category weights
	 * @return
	 */
	public double getNeededTotal() 
	{
		return neededTotal;
	}
	
	/**
	 * Returns sum of the adjustment category weights
	 * @return
	 */
	/**
	 * Returns sum of all category weights
	 * @return
	 */
	public double getGrandTotal() 
	{
		return grandTotal;
	}

	/**
	 * Simplifies some javascript/rendering relationships. The highlight
	 * class is only applied if the running total not equal to 100% 
	 * @return
	 */
	public String getRegularTotalStyle() 
	{
		if (regularTotal != 100)
			return "highlight";

		return "";
	}
	
	/**
	 * For retaining the pageName variable upon save or cancel
	 */
	public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
    
    /**
     * Grading scale used if grade entry by letter
     * @return
     */
    public List getLetterGradeRows() {
    	return letterGradeRows;
    }
    public void setLetterGradeRows(List letterGradeRows) {
    	this.letterGradeRows = letterGradeRows;
    }


	/**
	 * Set gradeEntryType and categorySetting
	 *
	 */
	private void intializeGradeEntryAndCategorySettings()
	{	
		// Grade entry setting
		int gradeEntryType = localGradebook.getGrade_type();
		if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE)
			gradeEntryMethod = ENTRY_OPT_PERCENT;
		else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER)
			gradeEntryMethod = ENTRY_OPT_LETTER;
		else
			gradeEntryMethod = ENTRY_OPT_POINTS;

		// Category setting
		int categoryType = localGradebook.getCategory_type();

		if (categoryType == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
			categorySetting = CATEGORY_OPT_CAT_ONLY;
		else if (categoryType == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			categorySetting = CATEGORY_OPT_CAT_AND_WEIGHT;
		else
			categorySetting = CATEGORY_OPT_NONE;
	}

	/**
	 * Calculates the sum of the category weights
	 * @return
	 */
	private void calculateRunningTotal()
	{
		BigDecimal total = new BigDecimal(0);
		BigDecimal extraCredit = new BigDecimal(0);

		if (categories != null && categories.size() > 0)
		{
			Iterator catIter = categories.iterator();
			while (catIter.hasNext())
			{
				Object obj = catIter.next();
				if(!(obj instanceof Category)){
					continue;
				}
				Category cat = (Category) obj;
				Boolean iec = cat.isExtraCredit();
				if (iec!=null)
				{
					if (cat.getWeight() != null && !cat.isExtraCredit())
					{
						BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						total=total.add(weight);
					}
					else if (cat.getWeight() != null && cat.isExtraCredit())
					{
						BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						extraCredit = extraCredit.add(weight);
					}
				}
				else
				{
					if (cat.getWeight() != null)
					{
						BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						total=total.add(weight);
					}
				}
			}
		}

		regularTotal = total.doubleValue(); // this will probably change later, but make it function to spec for now
		grandTotal = (total.add(extraCredit)).doubleValue();
		adjustmentTotal = extraCredit.doubleValue();
		neededTotal = 100 - total.doubleValue();
	}
	
	/**
	 * Because we display input as "percentage" to user but store it as
	 * decimal, we need a way to convert our weights from decimal to %
	 */
	private void convertWeightsFromDecimalsToPercentages() {
		if (!getWeightingEnabled())
			return;
		
		if (categories != null && !categories.isEmpty()) {
			Iterator iter = categories.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if(!(obj instanceof Category)){
					continue;
				}
				Category myCat = (Category) obj;
				Double weight = myCat.getWeight();
				if (weight != null && weight.doubleValue() > 0) {
					myCat.setWeight(new Double(weight.doubleValue() * 100));
				}
			}
		}
	}
	
	private boolean isMappingValid(LetterGradePercentMapping lgpm) {
		boolean valid = true;
		Double previousPercentage = null;
		for (Iterator iter = letterGradesList.iterator(); iter.hasNext(); ) {
			String grade = (String)iter.next();
			Double percentage = (Double)lgpm.getValue(grade);
			if (logger.isDebugEnabled()) logger.debug("checking percentage " + percentage + " for validity");

			// Grades that are percentage-based need to remain percentage-based,
			// be in descending order, and end with 0.
			if (percentage == null) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("gb_setup_require_all_values"));
				valid = false;
			} else if (percentage.doubleValue() < 0) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("gb_setup_require_positive"));
				valid = false;
			} else if ((previousPercentage != null) && (previousPercentage.doubleValue() < percentage.doubleValue())) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("gb_setup_require_descending_order"));
				valid = false;
			}
			previousPercentage = percentage;
		}
		return valid;
	}
	
	/**
	 * UI for the letter grade entry scale
	 */
	public class LetterGradeRow implements Serializable {
    	private String grade;
    	private boolean editable;
    	private LetterGradePercentMapping lgpm;
    	
    	public LetterGradeRow() {
    	}
    	
    	public LetterGradeRow(LetterGradePercentMapping lgpm, String grade, boolean editable) {
    		this.lgpm = lgpm;
    		this.grade = grade;
    		this.editable = editable;
    	}

    	public String getGrade() {
    		return grade;
    	}

    	public Double getMappingValue() {
    		return (Double)lgpm.getGradeMap().get(grade);
    	}
    	public void setMappingValue(Double value) {
    		lgpm.getGradeMap().put(grade, value);
    	}

    	public boolean isEditable() {
			return editable;
		}
	}

	public boolean getEnableLetterGrade()
	{
		enableLetterGrade = ServerConfigurationService.getBoolean(GradebookService.enableLetterGradeString, false);
		return enableLetterGrade;
	}

	public void setEnableLetterGrade(boolean enableLetterGrade)
	{
		this.enableLetterGrade = enableLetterGrade;
	}

	public boolean getIsValidWithCourseGrade()
	{
		return isValidWithCourseGrade;
	}

	public void setIsValidWithCourseGrade(boolean isValidWithCourseGrade)
	{
		this.isValidWithCourseGrade = isValidWithCourseGrade;
	}
	
	public boolean isConflictWithCourseGrade()
	{
		Gradebook gb = getGradebookManager().getGradebookWithGradeMappings(getGradebookManager().getGradebook(localGradebook.getUid()).getId());
		if (gradeEntryMethod.equals(ENTRY_OPT_LETTER))
		{
			if((gb.getSelectedGradeMapping().getGradingScale() != null && gb.getSelectedGradeMapping().getGradingScale().getUid().equals("LetterGradeMapping"))
					|| (gb.getSelectedGradeMapping().getGradingScale() == null && gb.getSelectedGradeMapping().getName().equals("Letter Grades")))
			{
				return false;
			}
			Set mappings = gb.getGradeMappings();
			for(Iterator iter = mappings.iterator(); iter.hasNext();)
			{
				GradeMapping gm = (GradeMapping) iter.next();
				
				if(gm != null)
				{
					if((gm.getGradingScale() != null && (gm.getGradingScale().getUid().equals("LetterGradeMapping") || gm.getGradingScale().getUid().equals("LetterGradePlusMinusMapping")))
							|| (gm.getGradingScale() == null && (gb.getSelectedGradeMapping().getName().equals("Letter Grades") || gb.getSelectedGradeMapping().getName().equals("Letter Grades with +/-"))))
					{
						Map defaultMapping = gm.getDefaultBottomPercents();
						for (Iterator gradeIter = gm.getGrades().iterator(); gradeIter.hasNext(); ) 
						{
							String grade = (String)gradeIter.next();
							Double percentage = (Double)gm.getValue(grade);
							Double defautPercentage = (Double)defaultMapping.get(grade);
							if (percentage != null && !percentage.equals(defautPercentage)) 
							{
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	
	public String navigateToDeleteAllGrades() {
		return "delteAllGrades";
	}


	public boolean getIsLetterGrade() {
		isLetterGrade = gradeEntryMethod.equals(ENTRY_OPT_LETTER);
		return isLetterGrade;
	}

	public void setIsLetterGrade(boolean isLetterGrade) {
		this.isLetterGrade = isLetterGrade;
	}
	
	public boolean getIsPointGrade() {
		isPointGrade = gradeEntryMethod.equals(ENTRY_OPT_POINTS);
		return isPointGrade;
	}

	public void setPointGrade(boolean isPointGrade) {
		this.isPointGrade = isPointGrade;
	}

	public boolean getIsPercentageGrade() {
		isPercentageGrade = gradeEntryMethod.equals(ENTRY_OPT_PERCENT);
		return isPercentageGrade;
	}

	public void setPercentageGrade(boolean isPercentageGrade) {
		this.isPercentageGrade = isPercentageGrade;
	}
	

}
