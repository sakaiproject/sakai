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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

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

	@Override
	protected void init() {
		log.debug("init assignment=" + this.assignment);

		if (this.assignment == null) {
			if (this.assignmentId != null) {
				this.assignment = getGradebookManager().getAssignment(this.assignmentId);
			}
			if (this.assignment == null) {
				// it is a new assignment
				this.assignment = new GradebookAssignment();
				this.assignment.setReleased(true);
			}
		}

		// initialization; shouldn't enter here after category drop down changes
		if (this.selectedCategory == null && !UNASSIGNED_CATEGORY.equals(this.assignmentCategory)) {
			final Category assignCategory = this.assignment.getCategory();
			if (assignCategory != null) {
				this.assignmentCategory = assignCategory.getId().toString();
				this.selectedCategoryDropsScores = assignCategory.isDropScores();
				assignCategory.setAssignmentList(retrieveCategoryAssignmentList(assignCategory));
				this.selectedAssignmentIsOnlyOne = isAssignmentTheOnlyOne(this.assignment, assignCategory);
				this.selectedCategory = assignCategory;
			} else {
				this.assignmentCategory = getLocalizedString("cat_unassigned");
			}
		}

		this.categoriesSelectList = new ArrayList();
		// create comma seperate string representation of the list of EC categories
		final List<String> extraCreditCategoriesList = new ArrayList<String>();
		// The first choice is always "Unassigned"
		this.categoriesSelectList.add(new SelectItem(UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		final List gbCategories = getGradebookManager().getCategories(getGradebookId());
		if (gbCategories != null && gbCategories.isEmpty()) {
			final Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				final Category cat = (Category) catIter.next();
				this.categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
				if (cat.isExtraCredit()) {
					extraCreditCategoriesList.add(cat.getId().toString());
				}
			}
		}
		this.extraCreditCategories = StringUtils.join(extraCreditCategoriesList, ",");

		// To support bulk creation of assignments
		if (this.newBulkItems == null) {
			this.newBulkItems = new ArrayList();
		}

		// initialize the number of items to add dropdown
		this.addItemSelectList = new ArrayList();
		this.addItemSelectList.add(new SelectItem("0", ""));
		for (int i = 1; i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			this.addItemSelectList.add(new SelectItem(new Integer(i).toString(), new Integer(i).toString()));
		}

		if (this.newBulkItems.size() == NUM_EXTRA_ASSIGNMENT_ENTRIES) {
			applyPointsPossibleForDropScoreCategories(this.newBulkItems);
		} else {
			for (int i = this.newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
				final BulkAssignmentDecoratedBean item = getNewAssignment();

				if (i == 0) {
					item.setSaveThisItem("true");
				}

				this.newBulkItems.add(item);
			}
		}

		if (isScoringAgentEnabled()) {
			this.scoringAgentData = initializeScoringAgentData(getGradebookUid(), this.assignment.getId(), null);
		}
	}

	private boolean isAssignmentTheOnlyOne(final GradebookAssignment assignment, final Category category) {
		if (assignment != null && category != null && category.getAssignmentList() != null) {
			if (category.getAssignmentList().size() == 1) {
				return ((GradebookAssignment) category.getAssignmentList().get(0)).getId().equals(assignment.getId());
			}
			if (category.getAssignmentList().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * sets pointsPossible for items in categories that drop scores and sets the assignment's category, so that the ui can read
	 * item.assignment.category.dropScores
	 */
	private void applyPointsPossibleForDropScoreCategories(final List items) {
		this.categories = getCategories();

		final Map<String, Category> categoryCache = new HashMap<String, Category>();

		for (int i = 0; i < items.size(); i++) {
			final BulkAssignmentDecoratedBean bulkAssignment = (BulkAssignmentDecoratedBean) items.get(i);

			final String assignmentCategory = bulkAssignment.getCategory();
			if (getLocalizedString("cat_unassigned").equalsIgnoreCase(assignmentCategory)) {
				final Category unassigned = new Category();
				bulkAssignment.getAssignment().setCategory(unassigned); // set this unassigned category, so that in the ui,
																		// item.assignment.category.dropScores will return false
				if (this.categoryChanged || this.gradeEntryTypeChanged) {
					// bulkAssignment.setPointsPossible(null);
					bulkAssignment.getAssignment().setPointsPossible(null);
				}
			} else {
				for (int j = 0; j < this.categories.size(); j++) {
					Category category = this.categories.get(j);

					if (assignmentCategory.equals(category.getId().toString())) {
						if (categoryCache.containsKey(category.getId().toString())) {
							category = categoryCache.get(category.getId().toString());
						} else {
							category = retrieveSelectedCategory(category.getId().toString(), true);
							categoryCache.put(category.getId().toString(), category);
						}

						bulkAssignment.getAssignment().setCategory(category); // set here, because need to read
																				// item.assignment.category.dropScores in the ui
						if (category.getAssignmentList() != null && category.getAssignmentList().size() > 0
								&& category.isDropScores() && !GB_ADJUSTMENT_ENTRY.equals(bulkAssignment.getSelectedGradeEntryValue())) {
							bulkAssignment.setPointsPossible(category.getItemValue().toString());
							bulkAssignment.getAssignment().setPointsPossible(category.getItemValue());
						} else if (this.categoryChanged || this.gradeEntryTypeChanged) {
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

	private BulkAssignmentDecoratedBean getNewAssignment() {
		final GradebookAssignment assignment = new GradebookAssignment();
		assignment.setReleased(true);
		if (this.selectedCategory != null && this.selectedCategory.isDropScores()) {
			assignment.setPointsPossible(this.selectedCategory.getItemValue());
		}
		return new BulkAssignmentDecoratedBean(assignment, getItemCategoryString(assignment));
	}

	private String getItemCategoryString(final GradebookAssignment assignment) {
		String assignmentCategory;
		final Category assignCategory = assignment.getCategory();
		if (assignCategory != null) {
			assignmentCategory = assignCategory.getId().toString();
		} else {
			assignmentCategory = getLocalizedString("cat_unassigned");
		}

		return assignmentCategory;
	}

	/**
	 * Used to check if all gradebook items are valid before saving. Due to the way JSF works, had to turn off validators for bulk
	 * assignments so had to perform checks here. This is an all-or-nothing save, ie, either all items are OK and we save them all, or
	 * return to add page and highlight errors.
	 */
	public String saveNewAssignment() {
		String resultString = "overview";
		boolean saveAll = true;

		// keep list of new assignment names just in case
		// duplicates entered on screen
		final List newAssignmentNameList = new ArrayList();

		// used to hold assignments that are OK since we
		// need to determine if all are correct before saving
		final List itemsToSave = new ArrayList();

		final Iterator assignIter = this.newBulkItems.iterator();
		int i = 0;
		final Map<String, Category> categoryCache = new HashMap<String, Category>();
		while (i < this.numTotalItems && assignIter.hasNext()) {
			final BulkAssignmentDecoratedBean bulkAssignDecoBean = (BulkAssignmentDecoratedBean) assignIter.next();

			if (bulkAssignDecoBean.getBlnSaveThisItem()) {
				final GradebookAssignment bulkAssignment = bulkAssignDecoBean.getAssignment();

				// Check for blank entry else check if duplicate within items to be
				// added or with item currently in gradebook.
				if ("".equals(bulkAssignment.getName().toString().trim())) {
					bulkAssignDecoBean.setBulkNoTitleError("blank");
					saveAll = false;
					resultString = "failure";
				} else if (newAssignmentNameList.contains(bulkAssignment.getName().trim()) ||
						!getGradebookManager().checkValidName(getGradebookId(), bulkAssignment)) {
					bulkAssignDecoBean.setBulkNoTitleError("dup");
					saveAll = false;
					resultString = "failure";
				} else {
					bulkAssignDecoBean.setBulkNoTitleError("OK");
					newAssignmentNameList.add(bulkAssignment.getName().trim());
				}

				Category selectedCategory;
				if (categoryCache.containsKey(bulkAssignDecoBean.getCategory())) {
					selectedCategory = categoryCache.get(bulkAssignDecoBean.getCategory());
				} else {
					selectedCategory = retrieveSelectedCategory(bulkAssignDecoBean.getCategory(), true);
					categoryCache.put(bulkAssignDecoBean.getCategory(), selectedCategory);
				}
				if (selectedCategory != null && selectedCategory.isDropScores()
						&& selectedCategory.getAssignmentList() != null && selectedCategory.getAssignmentList().size() > 0) {
					if (!GB_ADJUSTMENT_ENTRY.equals(bulkAssignDecoBean.getSelectedGradeEntryValue())) {
						bulkAssignDecoBean.setPointsPossible(selectedCategory.getItemValue().toString()); // if category drops scores and is
																											// not adjustment, point value
																											// will come from the category
																											// level
					}
				}
				// Check if points possible is blank else convert to double. Exception at else point
				// means non-numeric value entered.
				if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
					bulkAssignDecoBean.setBulkNoPointsError("blank");
					saveAll = false;
					resultString = "failure";
				} else {
					try {
						// Check if PointsPossible uses local number format
						final String strPointsPossible = bulkAssignDecoBean.getPointsPossible();
						final NumberFormat nf = NumberFormat.getInstance(new ResourceLoader().getLocale());

						final double dblPointsPossible = new Double(nf.parse(strPointsPossible).doubleValue());

						// Added per SAK-13459: did not validate if point value was valid (> zero)
						if (dblPointsPossible > 0) {
							// No more than 2 decimal places can be entered.
							BigDecimal bd = new BigDecimal(dblPointsPossible);
							bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
							final double roundedVal = bd.doubleValue();
							final double diff = dblPointsPossible - roundedVal;
							if (diff != 0) {
								saveAll = false;
								resultString = "failure";
								bulkAssignDecoBean.setBulkNoPointsError("precision");
							} else {
								bulkAssignDecoBean.setBulkNoPointsError("OK");
								bulkAssignDecoBean.getAssignment().setPointsPossible(new Double(bulkAssignDecoBean.getPointsPossible()));
							}
						} else {
							saveAll = false;
							resultString = "failure";
							bulkAssignDecoBean.setBulkNoPointsError("invalid");
						}
					} catch (final Exception e) {
						bulkAssignDecoBean.setBulkNoPointsError("NaN");
						saveAll = false;
						resultString = "failure";
					}
				}

				if (saveAll) {
					bulkAssignDecoBean.getAssignment().setCategory(retrieveSelectedCategory(bulkAssignDecoBean.getCategory(), false));
					// if points possible is still 0 at this point, set it to null to avoid Division By Zero exceptions. These should never
					// be allowed in the database.
					if (null != bulkAssignDecoBean.getAssignment().getPointsPossible()
							&& bulkAssignDecoBean.getAssignment().getPointsPossible() == 0) {
						bulkAssignDecoBean.getAssignment().setPointsPossible(null);
					}
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

				final String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ? "instructor"
						: "TA";
				for (final Iterator gbItemIter = itemsToSave.iterator(); gbItemIter.hasNext();) {
					final String itemName = ((GradebookAssignment) gbItemIter.next()).getName();
					FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save",
							new String[] { itemName }));
					getGradebookBean().postEvent("gradebook.newItem", "/gradebook/" + getGradebookId() + "/" + itemName + "/" + authzLevel,
							true);
				}
			} catch (final MultipleAssignmentSavingException e) {
				FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
				resultString = "failure";
			}
		} else {
			// There are errors so need to put an error message at top
			FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
		}

		return resultString;
	}

	public String updateAssignment() {
		try {
			final Category category = retrieveSelectedCategory();
			this.assignment.setCategory(category);

			if (!GB_ADJUSTMENT_ENTRY.equals(this.assignment.getSelectedGradeEntryValue()) && category != null && category.isDropScores()
					&& !isAssignmentTheOnlyOne(this.assignment, category)) {
				this.assignment.setPointsPossible(category.getItemValue()); // if category drops scores, point value will come from the
																			// category level
			}

			final GradebookAssignment originalAssignment = getGradebookManager().getAssignment(this.assignmentId);
			final Double origPointsPossible = originalAssignment.getPointsPossible();
			final Double newPointsPossible = this.assignment.getPointsPossible();
			final boolean scoresEnteredForAssignment = getGradebookManager().isEnteredAssignmentScores(this.assignmentId);

			/*
			 * If grade entry by percentage or letter and the points possible has changed for this assignment, we need to convert all of the
			 * stored point values to retain the same value
			 */
			if ((getGradeEntryByPercent() || getGradeEntryByLetter()) && scoresEnteredForAssignment) {
				if (!newPointsPossible.equals(origPointsPossible)) {
					final List enrollments = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
					final List studentUids = new ArrayList();
					for (final Iterator iter = enrollments.iterator(); iter.hasNext();) {
						studentUids.add(((EnrollmentRecord) iter.next()).getUser().getUserUid());
					}
					getGradebookManager().convertGradePointsForUpdatedTotalPoints(getGradebook(), originalAssignment,
							this.assignment.getPointsPossible(), studentUids);
				}
			}

			getGradebookManager().updateAssignment(this.assignment);
			long dueDateMillis = -1;
			final Date dueDate = this.assignment.getDueDate();
			if (dueDate != null) {
				dueDateMillis = dueDate.getTime();
			}
			getGradebookBean().postEvent("gradebook.updateAssignment",
					"/gradebook/" + getGradebookUid() + "/" + this.assignment.getName() + "/" + this.assignment.getPointsPossible() + "/"
							+ dueDateMillis + "/" + this.assignment.isReleased() + "/" + this.assignment.isCounted() + "/"
							+ getAuthzLevel(),
					true);

			if ((!origPointsPossible.equals(newPointsPossible)) && scoresEnteredForAssignment) {
				if (getGradeEntryByPercent()) {
					FacesUtil.addRedirectSafeMessage(
							getLocalizedString("edit_assignment_save_percentage", new String[] { this.assignment.getName() }));
				} else if (getGradeEntryByLetter()) {
					FacesUtil.addRedirectSafeMessage(
							getLocalizedString("edit_assignment_save_converted", new String[] { this.assignment.getName() }));
				} else {
					FacesUtil.addRedirectSafeMessage(
							getLocalizedString("edit_assignment_save_scored", new String[] { this.assignment.getName() }));
				}

			} else {
				FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save", new String[] { this.assignment.getName() }));
			}

		} catch (final ConflictingAssignmentNameException e) {
			log.error(e.getMessage());
			FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
			return "failure";
		} catch (final StaleObjectModificationException e) {
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
	 * Go to assignment details page. InstructorViewBean contains duplicate of this method, cannot migrate up to GradebookDependentBean
	 * since needs assignmentId, which is defined here.
	 */
	public String navigateBack() {
		String breadcrumbPage = getBreadcrumbPage();
		final Boolean middle = new Boolean((String) SessionManager.getCurrentToolSession().getAttribute("middle"));

		if (breadcrumbPage == null || middle) {
			final AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean) FacesUtil.resolveVariable("assignmentDetailsBean");
			assignmentDetailsBean.setAssignmentId(this.assignmentId);
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
		if (log.isDebugEnabled()) {
			log.debug("getAssignmentId " + this.assignmentId);
		}
		return this.assignmentId;
	}

	public void setAssignmentId(final Long assignmentId) {
		if (log.isDebugEnabled()) {
			log.debug("setAssignmentId " + assignmentId);
		}
		if (assignmentId != null) {
			this.assignmentId = assignmentId;
		}
	}

	public GradebookAssignment getAssignment() {
		if (log.isDebugEnabled()) {
			log.debug("getAssignment " + this.assignment);
		}
		if (this.assignment == null) {
			if (this.assignmentId != null) {
				this.assignment = getGradebookManager().getAssignment(this.assignmentId);
			}
			if (this.assignment == null) {
				// it is a new assignment
				this.assignment = new GradebookAssignment();
				this.assignment.setReleased(true);
			}
		}

		return this.assignment;
	}

	public List getCategoriesSelectList() {
		return this.categoriesSelectList;
	}

	public String getExtraCreditCategories() {
		return this.extraCreditCategories;
	}

	public List getAddItemSelectList() {
		return this.addItemSelectList;
	}

	public String getAssignmentCategory() {
		return this.assignmentCategory;
	}

	public void setAssignmentCategory(final String assignmentCategory) {
		this.assignmentCategory = assignmentCategory;
	}

	/**
	 * getNewBulkItems
	 *
	 * Generates and returns a List of blank GradebookAssignment objects. Used to support bulk gradebook item creation.
	 */
	public List getNewBulkItems() {
		if (this.newBulkItems == null) {
			this.newBulkItems = new ArrayList();
		}

		for (int i = this.newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			this.newBulkItems.add(getNewAssignment());
		}

		return this.newBulkItems;
	}

	public void setNewBulkItems(final List newBulkItems) {
		this.newBulkItems = newBulkItems;
	}

	public List getCategories() {
		if (this.categories == null) {
			this.categories = getGradebookManager().getCategories(getGradebookId());
		}

		return this.categories;
	}

	public int getNumTotalItems() {
		return this.numTotalItems;
	}

	public void setNumTotalItems(final int numTotalItems) {
		this.numTotalItems = numTotalItems;
	}

	public Gradebook getLocalGradebook() {
		return getGradebook();
	}

	/**
	 * Returns the Category associated with assignmentCategory If unassigned or not found, returns null
	 *
	 * added parameterized version to support bulk gradebook item creation
	 */
	private Category retrieveSelectedCategory() {
		return retrieveSelectedCategory(this.assignmentCategory, true);
	}

	private Category retrieveSelectedCategory(final String assignmentCategory, final boolean includeAssignments) {
		Long catId = null;
		Category category = null;

		if (assignmentCategory != null && !assignmentCategory.equals(UNASSIGNED_CATEGORY)) {
			try {
				catId = new Long(assignmentCategory);
			} catch (final Exception e) {
				catId = null;
			}

			if (catId != null) {
				// check to make sure there is a corresponding category
				category = getGradebookManager().getCategory(catId);
				if (includeAssignments) {
					// populate assignments list
					category.setAssignmentList(retrieveCategoryAssignmentList(category));
				}
			}
		}

		return category;
	}

	private List retrieveCategoryAssignmentList(final Category cat) {
		final List assignmentsToUpdate = new ArrayList();
		if (cat != null) {
			List assignments = cat.getAssignmentList();
			if (cat.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in
																							// category (to improve performance)
				assignments = getGradebookManager().getAssignmentsForCategory(cat.getId());

				// only include assignments which are not adjustments must not update adjustment item pointsPossible
				for (final Object o : assignments) {
					if (o instanceof GradebookAssignment) {
						final GradebookAssignment assignment = (GradebookAssignment) o;
						if (!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
							assignmentsToUpdate.add(assignment);
						}
					}
				}
			}
		}
		return assignmentsToUpdate;
	}

	public boolean isSelectedCategoryDropsScores() {
		return this.selectedCategoryDropsScores;
	}

	public void setSelectedCategoryDropsScores(final boolean selectedCategoryDropsScores) {
		this.selectedCategoryDropsScores = selectedCategoryDropsScores;
	}

	public Category getSelectedCategory() {
		return this.selectedCategory;
	}

	public void setSelectedCategory(final Category selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	public String processCategoryChangeInEditAssignment(final ValueChangeEvent vce) {
		final String changeCategory = (String) vce.getNewValue();
		this.assignmentCategory = changeCategory;
		if (vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue())) {
			if (changeCategory.equals(UNASSIGNED_CATEGORY)) {
				this.selectedCategoryDropsScores = false;
				this.selectedAssignmentIsOnlyOne = false;
				this.selectedCategory = null;
				this.assignmentCategory = getLocalizedString("cat_unassigned");
			} else {
				final List<Category> categories = getGradebookManager().getCategories(getGradebookId());
				if (categories != null && categories.size() > 0) {
					for (final Category category : categories) {
						if (changeCategory.equals(category.getId().toString())) {
							this.selectedCategoryDropsScores = category.isDropScores();
							category.setAssignmentList(retrieveCategoryAssignmentList(category));
							this.selectedCategory = category;
							this.selectedAssignmentIsOnlyOne = isAssignmentTheOnlyOne(this.assignment, this.selectedCategory);
							this.assignmentCategory = category.getId().toString();
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
	public String getRowClasses() {
		final StringBuffer rowClasses = new StringBuffer();

		if (this.newBulkItems == null) {
			this.newBulkItems = getNewBulkItems();
		}

		// if shown in UI, set class to 'bogus show' otherwise 'bogus hide'
		for (int i = 0; i < this.newBulkItems.size(); i++) {
			final Object obj = this.newBulkItems.get(i);
			if (obj instanceof BulkAssignmentDecoratedBean) {
				final BulkAssignmentDecoratedBean assignment = (BulkAssignmentDecoratedBean) this.newBulkItems.get(i);

				if (i != 0) {
					rowClasses.append(",");
				}

				if (assignment.getBlnSaveThisItem() || i == 0) {
					rowClasses.append("show bogus");
				} else {
					rowClasses.append("hide bogus");
				}
			}
		}

		return rowClasses.toString();
	}

	public boolean isSelectedAssignmentIsOnlyOne() {
		return this.selectedAssignmentIsOnlyOne;
	}

	public void setSelectedAssignmentIsOnlyOne(final boolean selectedAssignmentIsOnlyOne) {
		this.selectedAssignmentIsOnlyOne = selectedAssignmentIsOnlyOne;
	}

	public ScoringAgentData getScoringAgentData() {
		return this.scoringAgentData;
	}

}
