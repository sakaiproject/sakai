/**********************************************************************************
 *
 * $Id: GradebookSetupBean.java 20001 2007-04-01 19:41:33Z wagnermr@iupui.edu $
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
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;

public class GradebookSetupBean extends GradebookDependentBean implements Serializable
{
	private static final Log logger = LogFactory.getLog(GradebookSetupBean.class);

	private String gradeEntryMethod;
	private String categorySetting;
	private List categories;
	private Gradebook localGradebook;
	private List categoriesToRemove;
	private double runningTotal;
	private double neededTotal;

	private static final int NUM_EXTRA_CAT_ENTRIES = 50;
	private static final String ENTRY_OPT_POINTS = "points";
	private static final String ENTRY_OPT_PERCENT = "percent";
	private static final String ENTRY_OPT_LETTER = "letterGrade";
	private static final String CATEGORY_OPT_NONE = "noCategories";
	private static final String CATEGORY_OPT_CAT_ONLY = "onlyCategories";
	private static final String CATEGORY_OPT_CAT_AND_WEIGHT = "categoriesAndWeighting";

	private static final String GB_SETUP_PAGE = "gradebookSetup";

	private static final String ROW_INDEX_PARAM = "rowIndex";

	protected void init() 
	{
		if (localGradebook == null)
		{
			localGradebook = getGradebook();
			categories = getGradebookManager().getCategories(getGradebookId());
			intializeGradeEntryAndCategorySettings();
			categoriesToRemove = new ArrayList();
		}

		calculateRunningTotal();
	}

	private void reset()
	{
		localGradebook = null;
		categories = null;
		categorySetting = null;
		gradeEntryMethod = null;
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

	/*
	 * True if grade entry type is "Letter Grades"
	 */
	public boolean isDisplayGradeMap()
	{
		return gradeEntryMethod != null && gradeEntryMethod.equals(ENTRY_OPT_LETTER);
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

		if (gradeEntryMethod.equals(ENTRY_OPT_PERCENT))
		{
			localGradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		}
		/*else if (gradeEntryMethod.equals(ENTRY_OPT_LETTER))
			{
				localGradebook.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
			}*/
		else
		{
			localGradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		}

		if (categorySetting == null || (!categorySetting.equals(CATEGORY_OPT_CAT_ONLY) && 
				!categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT) && !categorySetting.equals(CATEGORY_OPT_NONE)))
		{
			FacesUtil.addErrorMessage(getLocalizedString("cat_setting_invalid"));
			return "failure";
		}

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

			getGradebookManager().updateGradebook(localGradebook);
			reset();

			FacesUtil.addRedirectSafeMessage(getLocalizedString("gb_save_msg"));

			return null;
		}
		
		// if we are going from no categories to having categories, we need to set
		// counted = false for all existing assignments b/c the category will
		// now be "unassigned"
		if (localGradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY &&
				(categorySetting.equals(CATEGORY_OPT_CAT_ONLY) || categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT))) {
			List assignmentsInGb = getGradebookManager().getAssignments(getGradebookId(), Assignment.DEFAULT_SORT, true);
			if (assignmentsInGb != null && !assignmentsInGb.isEmpty()) {
				Iterator assignIter = assignmentsInGb.iterator();
				while (assignIter.hasNext()) {
					Assignment assignment = (Assignment) assignIter.next();
					assignment.setCounted(false);
					getGradebookManager().updateAssignment(assignment);
				}
			}
		}

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
			localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);

			// we need to make sure all of the weights add up to 100
			calculateRunningTotal();
			if (runningTotal != 100)
			{
				FacesUtil.addErrorMessage(getLocalizedString("cat_weight_total_not_100"));
				return "failure";
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
				Category uiCategory = (Category)catIter.next();
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

					if (categoryId == null) {
						// must be a new or blank category
						getGradebookManager().createCategory(localGradebook.getId(), categoryName.trim(), uiCategory.getWeight(), 0);
					}
					else {
						// we are updating an existing category
						Category updatedCategory = getGradebookManager().getCategory(categoryId);
						updatedCategory.setName(categoryName.trim());
						updatedCategory.setWeight(uiCategory.getWeight());
						getGradebookManager().updateCategory(updatedCategory);
					}
				}
			}
			catch (ConflictingCategoryNameException cne) {
				FacesUtil.addErrorMessage(getLocalizedString("cat_same_name_error"));
				return "failure";
			}
			catch (StaleObjectModificationException e) {
				logger.error(e);
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
		}

		getGradebookManager().updateGradebook(localGradebook);

		FacesUtil.addRedirectSafeMessage(getLocalizedString("gb_save_msg"));
		return null;
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

	public String processCancelGradebookSetup()
	{
		reset();
		return GB_SETUP_PAGE;
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
			Category cat = (Category)categories.get(i);
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
		StringBuffer rowClasses = new StringBuffer();
		//first add the row class "bogus" for current categories
		for (int i=0; i<categories.size(); i++){
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
			if(!(i == 0 && categories.size() == 0)){
				rowClasses.append(",");
			}
			rowClasses.append("bogus hide");
		}
		
		return rowClasses.toString();
	}

	/**
	 * Returns sum of all category weights
	 * @return
	 */
	public double getRunningTotal()
	{	
		return runningTotal;
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
	 * Simplifies some javascript/rendering relationships. The highlight
	 * class is only applied if the running total not equal to 100% 
	 * @return
	 */
	public String getRunningTotalStyle() 
	{
		if (runningTotal != 100)
			return "highlight";

		return "";
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
		double total = 0;

		if (categories != null || categories.size() > 0)
		{
			Iterator catIter = categories.iterator();
			while (catIter.hasNext())
			{
				Category cat = (Category) catIter.next();
				if (cat.getWeight() != null)
				{
					double weight = cat.getWeight().doubleValue();
					total += weight;
				}
			}
		}

		runningTotal = total;
		neededTotal = 100 - total;
	}


}
