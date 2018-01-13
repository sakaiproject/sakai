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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookSetupBean extends GradebookDependentBean implements Serializable
{
	private String gradeEntryMethod;
	private String categorySetting;
	private boolean showDropHighestDisplayed;
	private boolean showDropLowestDisplayed;
	private boolean showKeepHighestDisplayed;
	private List categories;
	private Gradebook localGradebook;
	private List categoriesToRemove;
	private double regularTotal;
	private double neededTotal;
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

	private static final String GB_SETUP_PAGE = "gradebookSetup";
	private static final String GB_OVERVIEW_PAGE = "overview";

	private static final String ROW_INDEX_PARAM = "rowIndex";

	@Override
	protected void init()
	{
		if (this.localGradebook == null)
		{
			this.localGradebook = getGradebook();
			this.categories = getGradebookManager().getCategoriesWithStats(getGradebookId(),
                    GradebookAssignment.DEFAULT_SORT, true, Category.SORT_BY_NAME, true);
            populateCategoryAssignments(this.categories);
			convertWeightsFromDecimalsToPercentages();
			intializeGradeEntryAndCategorySettings();
			this.categoriesToRemove = new ArrayList();
		}

		calculateRunningTotal();

		this.defaultLGPM = getGradebookManager().getDefaultLetterGradePercentMapping();
		this.letterGradesList = new ArrayList(this.defaultLGPM.getGradeMap().keySet());
		Collections.sort(this.letterGradesList, GradebookService.lettergradeComparator);

		this.lgpm = getGradebookManager().getLetterGradePercentMapping(this.localGradebook);
		if (this.lgpm != null && this.lgpm.getGradeMap().size() > 0) {
			initLetterGradeRows();
		}

        if(getAnyCategoriesWithDropHighest()) {
            this.showDropHighestDisplayed = true;
        } else {
            this.showDropHighestDisplayed = false;
        }

        if(getAnyCategoriesWithDropLowest()) {
            this.showDropLowestDisplayed = true;
        } else {
            this.showDropLowestDisplayed = false;
        }

        if(getAnyCategoriesWithKeepHighest()) {
            this.showKeepHighestDisplayed = true;
        } else {
            this.showKeepHighestDisplayed = false;
        }
	}

    /*
     * For category requests to drop scores, need their assignments populated
     * so that system can determine eligibility of category to drop scores
     * if assignments have unequal pointsPossible, then they cannot drop scores
     */
    private void populateCategoryAssignments(final List categories) {
        if(categories != null) {
            for(final Object obj : categories) {
                if(obj instanceof Category) {
                    final Category category = (Category)obj;
                    List assignments = category.getAssignmentList();
                    if(category.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
                        assignments = getGradebookManager().getAssignmentsForCategory(category.getId());
                        final List assignmentsToUpdate = new ArrayList();
                     // only include assignments which are not adjustments must not update adjustment item pointsPossible
                        for(final Object o : assignments) {
                            if(o instanceof GradebookAssignment) {
                                final GradebookAssignment assignment = (GradebookAssignment)o;
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

	private void initLetterGradeRows() {
		this.letterGradeRows = new ArrayList();

		for (final Iterator iter = this.letterGradesList.iterator(); iter.hasNext(); ) {
			final String grade = (String)iter.next();

			// Bottom grades (with a lower bound of 0%)
			final Double d = this.defaultLGPM.getValue(grade);
			final boolean editable = ((d != null) && (d.doubleValue() > 0.0));
			this.letterGradeRows.add(new LetterGradeRow(this.lgpm, grade, editable));
		}
	}

	private void reset()
	{
		this.localGradebook = null;
		this.categories = null;
		this.categorySetting = null;
		this.gradeEntryMethod = null;
		this.isValidWithCourseGrade = true;
	}

	public Gradebook getLocalGradebook()
	{
		return this.localGradebook;
	}

	public String getGradeEntryMethod()
	{
		return this.gradeEntryMethod;
	}

	public void setGradeEntryMethod(final String gradeEntryMethod)
	{
		this.gradeEntryMethod = gradeEntryMethod;
	}

	public String getCategorySetting()
	{
		return this.categorySetting;
	}

	public void setCategorySetting(final String categorySetting)
	{
		this.categorySetting = categorySetting;
	}

	public boolean getShowDropHighestDisplayed() {
        return this.showDropHighestDisplayed;
    }

    public void setShowDropHighestDisplayed(final boolean showDropHighestDisplayed) {
        this.showDropHighestDisplayed = showDropHighestDisplayed;
    }

    public boolean getShowDropLowestDisplayed() {
        return this.showDropLowestDisplayed;
    }

    public void setShowDropLowestDisplayed(final boolean showDropLowestDisplayed) {
        this.showDropLowestDisplayed = showDropLowestDisplayed;
    }

    public boolean getShowKeepHighestDisplayed() {
        return this.showKeepHighestDisplayed;
    }

    public void setShowKeepHighestDisplayed(final boolean showKeepHighestDisplayed) {
        this.showKeepHighestDisplayed = showKeepHighestDisplayed;
    }

    public void setAnyCategoriesWithDropHighest(final boolean anyCategoriesWithDropHighest) {
    }

    public void setAnyCategoriesWithDropLowest(final boolean anyCategoriesWithDropLowest) {
    }

    public void setAnyCategoriesWithKeepHighest(final boolean anyCategoriesWithKeepHighest) {
    }

    public boolean getAnyCategoriesWithDropHighest() {
        boolean anyDrops = false;
        if(this.categories != null) {
            for(final Object obj : this.categories) {
                if(obj instanceof Category) {
                    final Category category = (Category)obj;
                    anyDrops = category.getDropHighest() > 0;
                    setShowDropHighestDisplayed(anyDrops);
                    if(anyDrops) {
						break;
					}
                }
            }
        }
        return anyDrops;
    }

    public boolean getAnyCategoriesWithDropLowest() {
        boolean anyDrops = false;
        if(this.categories != null) {
            for(final Object obj : this.categories) {
                if(obj instanceof Category) {
                    final Category category = (Category)obj;
					anyDrops = category.getDropLowest() > 0;
                    setShowDropLowestDisplayed(anyDrops);
                    if(anyDrops) {
						break;
					}
                }
            }
        }
        return anyDrops;
    }

    public boolean getAnyCategoriesWithKeepHighest() {
        boolean anyDrops = false;
        if(this.categories != null) {
            for(final Object obj : this.categories) {
                if(obj instanceof Category) {
                    final Category category = (Category)obj;
                    anyDrops = category.getKeepHighest() > 0;
                    setShowKeepHighestDisplayed(anyDrops);
                    if(anyDrops) {
						break;
					}
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
		if (this.gradeEntryMethod != null && this.gradeEntryMethod.equals(ENTRY_OPT_LETTER)) {
			return "display:block;";
		} else {
			return "display:none;";
		}
	}

	/**
	 * Returns true if categories are used in gb
	 * @return
	 */
	public boolean isDisplayCategories()
	{
		return !this.categorySetting.equals(CATEGORY_OPT_NONE);
	}

	/**
	 * Returns true if weighting is used in gb
	 * @return
	 */
	public boolean isDisplayWeighting()
	{
		return this.categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT);
	}

	/**
	 * Save gradebook settings (including categories and weighting)
	 * @return
	 */
	public String processSaveGradebookSetup()
	{
		if (this.gradeEntryMethod == null || (!this.gradeEntryMethod.equals(ENTRY_OPT_POINTS) &&
				!this.gradeEntryMethod.equals(ENTRY_OPT_PERCENT) && !this.gradeEntryMethod.equals(ENTRY_OPT_LETTER)))
		{
			FacesUtil.addErrorMessage(getLocalizedString("grade_entry_invalid"));
			return "failure";
		}
    if(!isConflictWithCourseGrade())
    {
    	this.isValidWithCourseGrade = false;
    	return null;
    } else {
		this.isValidWithCourseGrade = true;
	}

		if (this.gradeEntryMethod.equals(ENTRY_OPT_PERCENT))
		{
			this.localGradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		}
		else if (this.gradeEntryMethod.equals(ENTRY_OPT_LETTER))
		{
			this.localGradebook.setGrade_type(GradebookService.GRADE_TYPE_LETTER);
		}
		else
		{
			this.localGradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
		}

		if (this.categorySetting == null || (!this.categorySetting.equals(CATEGORY_OPT_CAT_ONLY) &&
				!this.categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT) && !this.categorySetting.equals(CATEGORY_OPT_NONE)))
		{
			FacesUtil.addErrorMessage(getLocalizedString("cat_setting_invalid"));
			return "failure";
		}
		final int origCategorySetting = this.localGradebook.getCategory_type();
		if (this.categorySetting.equals(CATEGORY_OPT_NONE))
		{
			this.localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
			// remove current categories
			final List gbCategories = getGradebookManager().getCategories(this.localGradebook.getId());
			if (gbCategories != null && !gbCategories.isEmpty())
			{
				final Iterator removeIter = gbCategories.iterator();
				while (removeIter.hasNext())
				{
					final Category removeCat = (Category) removeIter.next();
					getGradebookManager().removeCategory(removeCat.getId());
				}
			}

			// check to see if any permissions need to be removed
			final List sections = getAllSections();
			final List gbPermissions = getGradebookManager().getPermissionsForGB(this.localGradebook.getId());
			if (gbPermissions != null) {
				for (final Iterator permIter = gbPermissions.iterator(); permIter.hasNext();) {
					final Permission perm = (Permission) permIter.next();
					// if there is a specific category associated with this permission or if
					// there are no sections defined in the site, we need to delete this permission
					if (perm.getCategoryId() != null || sections == null || sections.size() == 0) {
						log.debug("Permission " + perm.getId() + " was deleted b/c gb changed to no categories");
						getGradebookManager().deletePermission(perm);
					}
				}
			}

			getGradebookManager().updateGradebook(this.localGradebook);
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

		if (this.categorySetting.equals(CATEGORY_OPT_CAT_ONLY))
		{
			this.localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);

			// set all weighting to 0 for existing categories
			final Iterator unweightIter = this.categories.iterator();
			while (unweightIter.hasNext())
			{
				final Category unweightCat = (Category) unweightIter.next();
				unweightCat.setWeight(new Double(0));
			}
		}
		else if (this.categorySetting.equals(CATEGORY_OPT_CAT_AND_WEIGHT))
		{
			// we need to make sure all of the weights add up to 100
			calculateRunningTotal();
			if (this.neededTotal != 0)
			{
				FacesUtil.addErrorMessage(getLocalizedString("cat_weight_total_not_100"));
				return "failure";
			}
			this.localGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
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
        final Iterator itr = this.categories.iterator();
        while (itr.hasNext()) {
            final Object obj = itr.next();
            if(!(obj instanceof Category)) {
                continue;
            }

            final Category uiCategory = (Category) obj;
            final Long categoryId = uiCategory.getId();

            // do cross validation
			if ((uiCategory.getDropLowest() > 0 || uiCategory.getDropHighest() > 0) && uiCategory.getKeepHighest() > 0) {
               FacesUtil.addErrorMessage(getLocalizedString("cat_keep_and_drop_mutually_exclusive"));
               return "failure";
            }
			if (uiCategory.getItemValue() < 0
					&& (uiCategory.getDropLowest() > 0 || uiCategory.getDropHighest() > 0 || uiCategory.getKeepHighest() > 0)) {
               FacesUtil.addErrorMessage(getLocalizedString("cat_pointvalue_not_valid"));
               return "failure";
            }

            if(uiCategory.isDropScores()) {
               if (!uiCategory.isAssignmentsEqual()) {
                   if(this.gradeEntryMethod != null && this.gradeEntryMethod.equals(ENTRY_OPT_POINTS)) {
                       FacesUtil.addErrorMessage(getLocalizedString("cat_pointvalue_not_valid"));
                   } else if(this.gradeEntryMethod != null && this.gradeEntryMethod.equals(ENTRY_OPT_PERCENT)) {
                       FacesUtil.addErrorMessage(getLocalizedString("cat_relativeweight_not_valid"));
                   }
                   return "failure";
               }
            }

            // we will be updating an existing category
            if (categoryId != null) {
               final Category updatedCategory = getGradebookManager().getCategory(categoryId);
               if(updatedCategory.isDropScores()) {
                   if(!updatedCategory.isAssignmentsEqual()) {
                       if (this.gradeEntryMethod != null && this.gradeEntryMethod.equals(ENTRY_OPT_POINTS)) {
                           FacesUtil.addErrorMessage(getLocalizedString("cat_point_values_unequal"));
                       }
                       if (this.gradeEntryMethod != null && this.gradeEntryMethod.equals(ENTRY_OPT_PERCENT)) {
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

		final Iterator catIter = this.categories.iterator();
		while (catIter.hasNext())
		{
			try {
				final Object obj = catIter.next();
				if(!(obj instanceof Category)){
					continue;
				}

				final Category uiCategory = (Category) obj;
				final Long categoryId = uiCategory.getId();
				final String categoryName = uiCategory.getName();

				if ((categoryName == null || categoryName.trim().length() < 1) && categoryId != null)
				{
					this.categoriesToRemove.add(categoryId);
				}

				if (categoryName != null && categoryName.length() > 0)
				{
					// treat blank weight fields as 0
					if (this.localGradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY &&
							uiCategory.getWeight() == null) {
						uiCategory.setWeight(new Double(0));
					}

					if ((this.localGradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY
							&& this.localGradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
							|| uiCategory.isExtraCredit() == null)
					{
						uiCategory.setExtraCredit(false);
					}

					if (categoryId == null) {
						// must be a new or blank category
						if (uiCategory.getWeight() != null && uiCategory.getWeight().doubleValue() > 0) {
							getGradebookManager().createCategory(this.localGradebook.getId(), categoryName.trim(),
									new Double(uiCategory.getWeight().doubleValue() / 100), uiCategory.getDropLowest(),
									uiCategory.getDropHighest(), uiCategory.getKeepHighest(), uiCategory.isExtraCredit());
						} else {
							getGradebookManager().createCategory(this.localGradebook.getId(), categoryName.trim(), uiCategory.getWeight(),
									uiCategory.getDropLowest(), uiCategory.getDropHighest(), uiCategory.getKeepHighest(),
									uiCategory.isExtraCredit());
						}
					}
					else {
						// we are updating an existing category
						final Category updatedCategory = getGradebookManager().getCategory(categoryId);
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

						updatedCategory.setDropLowest(uiCategory.getDropLowest());
                        updatedCategory.setDropHighest(uiCategory.getDropHighest());
                        updatedCategory.setKeepHighest(uiCategory.getKeepHighest());

                        if(updatedCategory.isDropScores() && updatedCategory.isAssignmentsEqual()) {
                            if((updatedCategory.getAssignmentList() == null || updatedCategory.getAssignmentList().size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
                                final List assignments = getGradebookManager().getAssignmentsForCategory(updatedCategory.getId());
                                final List assignmentsToUpdate = new ArrayList();
                                for(final Object o : assignments) { // must not update adjustment item pointsPossible
                                    if(o instanceof GradebookAssignment) {
                                        final GradebookAssignment assignment = (GradebookAssignment)o;
                                        if(!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
                                            assignmentsToUpdate.add(assignment);
                                        }
                                    }
                                }
                                updatedCategory.setAssignmentList(assignmentsToUpdate);
                            }
                            // now update the pointsPossible of any assignments within the category that drop scores
                            getGradebookManager().updateCategoryAndAssignmentsPointsPossible(this.localGradebook.getId(), updatedCategory);
                        } else {
                            getGradebookManager().updateCategory(updatedCategory);
                        }
					}
				}
			}
			catch (final ConflictingCategoryNameException cne) {
				FacesUtil.addErrorMessage(getLocalizedString("cat_same_name_error"));
				return "failure";
			}
			catch (final StaleObjectModificationException e) {
				log.error(e.getMessage());
				FacesUtil.addErrorMessage(getLocalizedString("cat_locking_failure"));
				return "failure";
			}
		}

		// remove any categories marked to remove
		if (this.categoriesToRemove != null && this.categoriesToRemove.size() > 0) {
			final Iterator removeIter = this.categoriesToRemove.iterator();
			while (removeIter.hasNext()) {
				final Long removeId = (Long) removeIter.next();
				getGradebookManager().removeCategory(removeId);
			}

			final List permsToRemove = getGradebookManager().getPermissionsForGBForCategoryIds(this.localGradebook.getId(), this.categoriesToRemove);
			if (!permsToRemove.isEmpty()) {
				for (final Iterator permIter = permsToRemove.iterator(); permIter.hasNext();) {
					final Permission perm = (Permission) permIter.next();
					log.debug("Permission " + perm.getId() + " was deleted b/c category deleted");
					getGradebookManager().deletePermission(perm);
				}
			}
		}

		//SAK-22417 When changing to a category gradebook, items that move to unassigned still have included in course grade as YES
		//This also includes the case where a GB category is deleted and the item is set to uncategorized.
		if((GradebookService.CATEGORY_TYPE_ONLY_CATEGORY == this.localGradebook.getCategory_type() || GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY == this.localGradebook.getCategory_type())
				&& (GradebookService.CATEGORY_TYPE_NO_CATEGORY == origCategorySetting || (this.categoriesToRemove != null && this.categoriesToRemove.size() > 0))){
			setUncategoriedAssignmentsToNotCounted(this.localGradebook.getId());
		}

		getGradebookManager().updateGradebook(this.localGradebook);

		FacesUtil.addRedirectSafeMessage(getLocalizedString("gb_save_msg"));
		reset();
		return null;
	}

	private void setUncategoriedAssignmentsToNotCounted(final Long gradebookId){
		final List assigns = getGradebookManager().getAssignmentsWithNoCategory(gradebookId, null, true);
		for(final Iterator iter = assigns.iterator(); iter.hasNext();)
		{
			final GradebookAssignment assignment = (GradebookAssignment) iter.next();
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
	public String processRemoveCategory(final ActionEvent event)
	{
		if (this.categories == null || this.categories.isEmpty()) {
			return GB_SETUP_PAGE;
		}

		try
		{
			final Map params = FacesUtil.getEventParameterMap(event);
			final Integer index = (Integer) params.get(ROW_INDEX_PARAM);
			if (index == null) {
				return GB_SETUP_PAGE;
			}
			final int indexToRemove = index.intValue();
			final Category catToRemove = (Category)this.categories.get(indexToRemove);
			// new categories will not have an id yet so don't need to be retained
			if (catToRemove.getId() != null)
			{
				this.categoriesToRemove.add(catToRemove.getId());
			}
			this.categories.remove(indexToRemove);
		}
		catch(final Exception e)
		{
			// do nothing
		}

		return GB_SETUP_PAGE;
	}

	public String processCategorySettingChange(final ValueChangeEvent vce)
	{
		final String changeAssign = (String) vce.getNewValue();
		if (changeAssign != null && (changeAssign.equals(CATEGORY_OPT_NONE) ||
				changeAssign.equals(CATEGORY_OPT_CAT_AND_WEIGHT) ||
				changeAssign.equals(CATEGORY_OPT_CAT_ONLY)))
		{
			this.categorySetting = changeAssign;
		}

		return GB_SETUP_PAGE;
	}

	public String processGradeEntryMethodChange(final ValueChangeEvent vce)
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
		for (int i=0; i < this.categories.size(); i++)
		{
			final Object obj = this.categories.get(i);
			if(!(obj instanceof Category)){
				this.categories.remove(i);
				continue;
			}
			final Category cat = (Category)this.categories.get(i);
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
					this.categoriesToRemove.add(cat.getId());
				}
				this.categories.remove(cat);
				i--;
			}
		}

		// always display 5 blank entries for new categories
		for (int i=0; i < NUM_EXTRA_CAT_ENTRIES; i++)
		{
			final Category blankCat = new Category();
			this.categories.add(blankCat);
		}

		return this.categories;
	}

	public String getRowClasses()
	{
		final StringBuilder rowClasses = new StringBuilder();
		//first add the row class "bogus" for current categories
		for (int i=0; i<this.categories.size(); i++){
			final Object obj = this.categories.get(i);
			if(!(obj instanceof Category)){
				continue;
			}
			final Category cat = (Category)this.categories.get(i);
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
			if(i == 0 && this.categories.size() == 0){
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
		return this.regularTotal;
	}


	/**
	 * Returns % needed to reach 100% for category weights
	 * @return
	 */
	public double getNeededTotal()
	{
		return this.neededTotal;
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
		return this.grandTotal;
	}

	/**
	 * Simplifies some javascript/rendering relationships. The highlight
	 * class is only applied if the running total not equal to 100%
	 * @return
	 */
	public String getRegularTotalStyle()
	{
		if (this.regularTotal != 100) {
			return "highlight";
		}

		return "";
	}

	/**
	 * For retaining the pageName variable upon save or cancel
	 */
	@Override
	public String getPageName() {
        return this.pageName;
    }

    @Override
	public void setPageName(final String pageName) {
        this.pageName = pageName;
    }

    /**
     * Grading scale used if grade entry by letter
     * @return
     */
    public List getLetterGradeRows() {
    	return this.letterGradeRows;
    }
    public void setLetterGradeRows(final List letterGradeRows) {
    	this.letterGradeRows = letterGradeRows;
    }


	/**
	 * Set gradeEntryType and categorySetting
	 *
	 */
	private void intializeGradeEntryAndCategorySettings()
	{
		// Grade entry setting
		final int gradeEntryType = this.localGradebook.getGrade_type();
		if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			this.gradeEntryMethod = ENTRY_OPT_PERCENT;
		} else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
			this.gradeEntryMethod = ENTRY_OPT_LETTER;
		} else {
			this.gradeEntryMethod = ENTRY_OPT_POINTS;
		}

		// Category setting
		final int categoryType = this.localGradebook.getCategory_type();

		if (categoryType == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY) {
			this.categorySetting = CATEGORY_OPT_CAT_ONLY;
		} else if (categoryType == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
			this.categorySetting = CATEGORY_OPT_CAT_AND_WEIGHT;
		} else {
			this.categorySetting = CATEGORY_OPT_NONE;
		}
	}

	/**
	 * Calculates the sum of the category weights
	 * @return
	 */
	private void calculateRunningTotal()
	{
		BigDecimal total = new BigDecimal(0);
		BigDecimal extraCredit = new BigDecimal(0);

		if (this.categories != null && this.categories.size() > 0)
		{
			final Iterator catIter = this.categories.iterator();
			while (catIter.hasNext())
			{
				final Object obj = catIter.next();
				if(!(obj instanceof Category)){
					continue;
				}
				final Category cat = (Category) obj;
				final Boolean iec = cat.isExtraCredit();
				if (iec!=null)
				{
					if (cat.getWeight() != null && !cat.isExtraCredit())
					{
						final BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						total=total.add(weight);
					}
					else if (cat.getWeight() != null && cat.isExtraCredit())
					{
						final BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						extraCredit = extraCredit.add(weight);
					}
				}
				else
				{
					if (cat.getWeight() != null)
					{
						final BigDecimal weight = new BigDecimal(cat.getWeight().doubleValue());
						total=total.add(weight);
					}
				}
			}
		}

		this.regularTotal = total.doubleValue(); // this will probably change later, but make it function to spec for now
		this.grandTotal = (total.add(extraCredit)).doubleValue();
		this.neededTotal = 100 - total.doubleValue();
	}

	/**
	 * Because we display input as "percentage" to user but store it as
	 * decimal, we need a way to convert our weights from decimal to %
	 */
	private void convertWeightsFromDecimalsToPercentages() {
		if (!getWeightingEnabled()) {
			return;
		}

		if (this.categories != null && !this.categories.isEmpty()) {
			final Iterator iter = this.categories.iterator();
			while (iter.hasNext()) {
				final Object obj = iter.next();
				if(!(obj instanceof Category)){
					continue;
				}
				final Category myCat = (Category) obj;
				final Double weight = myCat.getWeight();
				if (weight != null && weight.doubleValue() > 0) {
					myCat.setWeight(new Double(weight.doubleValue() * 100));
				}
			}
		}
	}

	/**
	 * UI for the letter grade entry scale
	 */
	public class LetterGradeRow implements Serializable {
		private String grade;
		private boolean editable;
		private LetterGradePercentMapping mapping;

		public LetterGradeRow() {
		}

		public LetterGradeRow(final LetterGradePercentMapping mapping, final String grade, final boolean editable) {
			this.mapping = mapping;
			this.grade = grade;
			this.editable = editable;
		}

		public String getGrade() {
			return this.grade;
		}

		public Double getMappingValue() {
			return this.mapping.getGradeMap().get(this.grade);
		}

		public void setMappingValue(final Double value) {
			this.mapping.getGradeMap().put(this.grade, value);
		}

		public boolean isEditable() {
			return this.editable;
		}
	}

	public boolean getEnableLetterGrade() {
		this.enableLetterGrade = ServerConfigurationService.getBoolean(GradebookService.enableLetterGradeString, false);
		return this.enableLetterGrade;
	}

	public void setEnableLetterGrade(final boolean enableLetterGrade) {
		this.enableLetterGrade = enableLetterGrade;
	}

	public boolean getIsValidWithCourseGrade() {
		return this.isValidWithCourseGrade;
	}

	public void setIsValidWithCourseGrade(final boolean isValidWithCourseGrade) {
		this.isValidWithCourseGrade = isValidWithCourseGrade;
	}

	public boolean isConflictWithCourseGrade() {
		final Gradebook gb = getGradebookManager()
				.getGradebookWithGradeMappings(getGradebookManager().getGradebook(this.localGradebook.getUid()).getId());
		if (this.gradeEntryMethod.equals(ENTRY_OPT_LETTER))
		{
			if ((gb.getSelectedGradeMapping().getGradingScale() != null
					&& gb.getSelectedGradeMapping().getGradingScale().getUid().equals("LetterGradeMapping"))
					|| (gb.getSelectedGradeMapping().getGradingScale() == null
							&& gb.getSelectedGradeMapping().getName().equals("Letter Grades"))) {
				return false;
			}
			final Set mappings = gb.getGradeMappings();
			for (final Iterator iter = mappings.iterator(); iter.hasNext();)
			{
				final GradeMapping gm = (GradeMapping) iter.next();

				if (gm != null)
				{
					if ((gm.getGradingScale() != null && (gm.getGradingScale().getUid().equals("LetterGradeMapping")
							|| gm.getGradingScale().getUid().equals("LetterGradePlusMinusMapping")))
							|| (gm.getGradingScale() == null && (gb.getSelectedGradeMapping().getName().equals("Letter Grades")
									|| gb.getSelectedGradeMapping().getName().equals("Letter Grades with +/-"))))
					{
						final Map defaultMapping = gm.getDefaultBottomPercents();
						for (final Object element : gm.getGrades()) {
							final String grade = (String) element;
							final Double percentage = gm.getValue(grade);
							final Double defautPercentage = (Double) defaultMapping.get(grade);
							if (percentage != null && !percentage.equals(defautPercentage)) {
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
		this.isLetterGrade = this.gradeEntryMethod.equals(ENTRY_OPT_LETTER);
		return this.isLetterGrade;
	}

	public void setIsLetterGrade(final boolean isLetterGrade) {
		this.isLetterGrade = isLetterGrade;
	}

	public boolean getIsPointGrade() {
		this.isPointGrade = this.gradeEntryMethod.equals(ENTRY_OPT_POINTS);
		return this.isPointGrade;
	}

	public void setPointGrade(final boolean isPointGrade) {
		this.isPointGrade = isPointGrade;
	}

	public boolean getIsPercentageGrade() {
		this.isPercentageGrade = this.gradeEntryMethod.equals(ENTRY_OPT_PERCENT);
		return this.isPercentageGrade;
	}

	public void setPercentageGrade(final boolean isPercentageGrade) {
		this.isPercentageGrade = isPercentageGrade;
	}
}
