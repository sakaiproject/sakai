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
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This is an abstract base class for gradebook dependent backing beans that support searching, sorting, and paging student data.
 */
@Slf4j
public abstract class EnrollmentTableBean
		extends GradebookDependentBean implements Paging, Serializable {

	/**
	 * A comparator that sorts enrollments by student sortName
	 */
	static final Comparator<EnrollmentRecord> ENROLLMENT_NAME_COMPARATOR = new Comparator<EnrollmentRecord>() {
		Collator collator;
		{
			this.collator = Collator.getInstance();
			try {
				this.collator = new RuleBasedCollator(
						((RuleBasedCollator) this.collator).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
			} catch (final ParseException e) {
				log.warn(this + " Cannot init RuleBasedCollator. Will use the default Collator instead.", e);
			}
		}

		@Override
		public int compare(final EnrollmentRecord o1, final EnrollmentRecord o2) {
			return this.collator.compare(o1.getUser().getSortName(), o2.getUser().getSortName());
		}
	};

	/**
	 * A comparator that sorts enrollments by student display UID (for installations where a student UID is not a number)
	 */
	static final Comparator<EnrollmentRecord> ENROLLMENT_DISPLAY_UID_COMPARATOR = new Comparator<EnrollmentRecord>() {
		@Override
		public int compare(final EnrollmentRecord o1, final EnrollmentRecord o2) {
			return o1.getUser().getDisplayId().compareToIgnoreCase(o2.getUser().getDisplayId());
		}
	};

	/**
	 * A comparator that sorts enrollments by student display UID (for installations where a student UID is a number)
	 */
	static final Comparator<EnrollmentRecord> ENROLLMENT_DISPLAY_UID_NUMERIC_COMPARATOR = new Comparator<EnrollmentRecord>() {
		@Override
		public int compare(final EnrollmentRecord o1, final EnrollmentRecord o2) {
			final long user1DisplayId = Long.parseLong(o1.getUser().getDisplayId());
			final long user2DisplayId = Long.parseLong(o2.getUser().getDisplayId());
			return (int) (user1DisplayId - user2DisplayId);
		}
	};

	public static final int ALL_SECTIONS_SELECT_VALUE = -1;
	public static final int ALL_CATEGORIES_SELECT_VALUE = -1;

	private static Map columnSortMap;
	private String searchString;
	private int firstScoreRow;
	public int maxDisplayedScoreRows;
	private int scoreDataRows;
	private boolean emptyEnrollments; // Needed to render buttons
	private String defaultSearchString;

	private boolean refreshRoster = true; // To prevent unnecessary roster loading

	// The section selection menu will include some choices that aren't
	// real sections (e.g., "All Sections" or "Unassigned Students".
	private Integer selectedSectionFilterValue = new Integer(ALL_SECTIONS_SELECT_VALUE);
	private List sectionFilterSelectItems;
	private List availableSections; // The real sections accessible by this user

	// The category selection menu will include some choices that aren't
	// real categories (e.g., "All Categories") and will only be rendered if
	// categories exists.
	private Integer selectedCategoryFilterValue = new Integer(ALL_CATEGORIES_SELECT_VALUE);
	private List availableCategories;
	private List categoryFilterSelectItems;

	// We only store grader UIDs in the grading event history, but the
	// log displays grader names instead. This map cuts down on possibly expensive
	// calls to the user directory service.
	private Map graderIdToNameMap;

	public EnrollmentTableBean() {
		this.maxDisplayedScoreRows = getPreferencesBean().getDefaultMaxDisplayedScoreRows();
	}

	static {
		columnSortMap = new HashMap();
		columnSortMap.put(PreferencesBean.SORT_BY_NAME, ENROLLMENT_NAME_COMPARATOR);
		columnSortMap.put(PreferencesBean.SORT_BY_UID, ENROLLMENT_DISPLAY_UID_COMPARATOR);
	}

	// Searching
	public String getSearchString() {
		return this.searchString;
	}

	public void setSearchString(String searchString) {
		if (StringUtils.trimToNull(searchString) == null) {
			searchString = this.defaultSearchString;
		}
		if (!StringUtils.equals(searchString, this.searchString)) {
			if (log.isDebugEnabled()) {
				log.debug("setSearchString " + searchString);
			}
			this.searchString = searchString;
			setFirstRow(0); // clear the paging when we update the search
		}
	}

	public void search(final ActionEvent event) {
		// We don't need to do anything special here, since init will handle the search
		if (log.isDebugEnabled()) {
			log.debug("search");
		}
		setRefreshRoster(true);
	}

	public void clear(final ActionEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("clear");
		}
		setSearchString(null);
		setRefreshRoster(true);
	}

	// Sorting
	public void sort(final ActionEvent event) {
		setFirstRow(0); // clear the paging whenever we update the sorting
		setRefreshRoster(true);
	}

	public abstract boolean isSortAscending();

	public abstract void setSortAscending(boolean sortAscending);

	public abstract String getSortColumn();

	public abstract void setSortColumn(String sortColumn);

	// Paging.

	/**
	 * This method more or less turns a JSF input component (namely the Sakai Pager tag) into a JSF command component. We want the Pager to
	 * cause immediate pseudo-navigation to a new state, throwing away any score input values without bothering to validate them. But
	 * because the Pager is a UIInput component, it doesn't have an action method that will be called before full validation is done.
	 * Instead, we declare our Pager input tag to be immediate, set this valueChangeListener, and explicitly jump over all other intervening
	 * phases directly to the rendering phase, which should then pick up the new paging values.
	 */
	public void changePagingState(final ValueChangeEvent valueChange) {
		if (log.isDebugEnabled()) {
			log.debug("changePagingState: old=" + valueChange.getOldValue() + ", new=" + valueChange.getNewValue());
		}
		FacesContext.getCurrentInstance().renderResponse();
	}

	@Override
	public int getFirstRow() {
		if (log.isDebugEnabled()) {
			log.debug("getFirstRow " + this.firstScoreRow);
		}
		return this.firstScoreRow;
	}

	@Override
	public void setFirstRow(final int firstRow) {
		if (log.isDebugEnabled()) {
			log.debug("setFirstRow from " + this.firstScoreRow + " to " + firstRow);
		}
		this.firstScoreRow = firstRow;
		setRefreshRoster(true);
	}

	@Override
	public int getMaxDisplayedRows() {
		return this.maxDisplayedScoreRows;
	}

	@Override
	public void setMaxDisplayedRows(final int maxDisplayedRows) {
		this.maxDisplayedScoreRows = maxDisplayedRows;
		setRefreshRoster(true);
	}

	@Override
	public int getDataRows() {
		return this.scoreDataRows;
	}

	private boolean isFilteredSearch() {
		return !StringUtils.equals(this.searchString, this.defaultSearchString);
	}

	/**
	 *
	 * @return Map of EnrollmentRecord to Map of gbItems and function (grade/view). This is the group of students viewable on the roster
	 *         page
	 */
	protected Map getOrderedEnrollmentMapForAllItems() {
		final Map enrollments = getWorkingEnrollmentsForAllItems();

		this.scoreDataRows = enrollments.size();
		this.emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMapForAllItems(enrollments);
	}

	/**
	 *
	 * @return Map of studentId to EnrollmentRecord in order. This is the group of students viewable for the course grade page
	 */
	protected Map getOrderedEnrollmentMapForCourseGrades() {
		final Map enrollments = getWorkingEnrollmentsForCourseGrade();

		this.scoreDataRows = enrollments.size();
		this.emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMapWithFunction(enrollments);
	}

	/**
	 *
	 * @param itemId
	 * @param categoryId
	 * @return Map of studentId to EnrollmentRecord in order. This is the group of students viewable for a particular category associated
	 *         with gb item
	 */
	protected Map getOrderedEnrollmentMapForItem(final Long categoryId) {
		final Map enrollments = getWorkingEnrollmentsForItem(categoryId);

		this.scoreDataRows = enrollments.size();
		this.emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMapWithFunction(enrollments);
	}

	/**
	 *
	 * @param categoryId - optional category filter
	 * @return Map of studentId to EnrollmentRecord in order.
	 */
	protected Map getOrderedStudentIdEnrollmentMapForItem(final Long categoryId) {
		final Map enrollments = getWorkingEnrollmentsForItem(categoryId);

		this.scoreDataRows = enrollments.size();
		this.emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMap(new ArrayList(enrollments.keySet()));
	}

	protected Map getWorkingEnrollmentsForAllItems() {
		Map enrollments;

		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = this.searchString;
		}

		String selSectionUid = null;
		if (!isAllSectionsSelected()) {
			selSectionUid = getSelectedSectionUid();
		}

		enrollments = findMatchingEnrollmentsForAllItems(selSearchString, selSectionUid);

		return enrollments;
	}

	protected Map getWorkingEnrollmentsForItem(final Long categoryId) {
		Map enrollments;
		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = this.searchString;
		}

		String selSectionUid = null;
		if (!isAllSectionsSelected()) {
			selSectionUid = getSelectedSectionUid();
		}

		enrollments = findMatchingEnrollmentsForItem(categoryId, selSearchString, selSectionUid);

		return enrollments;
	}

	/**
	 *
	 * @return Map of EnrollmentRecord --> function (view/grade) that the current user has grade/view permission for every gb item
	 */
	protected Map getWorkingEnrollmentsForCourseGrade() {
		Map enrollments;
		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = this.searchString;
		}

		String selSectionUid = null;
		if (!isAllSectionsSelected()) {
			selSectionUid = getSelectedSectionUid();
		}

		enrollments = findMatchingEnrollmentsForViewableCourseGrade(selSearchString, selSectionUid);

		return enrollments;
	}

	/**
	 *
	 * @param enrollmentList - list of EnrollmentRecords
	 * @return Ordered Map of student Id to EnrollmentRecord
	 */
	private Map transformToOrderedEnrollmentMap(List enrollmentList) {
		Map enrollmentMap;

		if (isEnrollmentSort()) {
			Collections.sort(enrollmentList, (Comparator) columnSortMap.get(getSortColumn()));
			enrollmentList = finalizeSortingAndPaging(enrollmentList);
			enrollmentMap = new LinkedHashMap(); // Preserve ordering
		} else {
			enrollmentMap = new HashMap();
		}

		for (final Iterator iter = enrollmentList.iterator(); iter.hasNext();) {
			final EnrollmentRecord enr = (EnrollmentRecord) iter.next();
			enrollmentMap.put(enr.getUser().getUserUid(), enr);
		}

		return enrollmentMap;
	}

	/**
	 *
	 * @param enrollmentMap - map of EnrollmentRecord --> function
	 * @return Ordered Map of student Id to map of EnrollmentRecord to function
	 */
	private Map transformToOrderedEnrollmentMapWithFunction(final Map enrRecFunctionMap) {
		Map enrollmentMap;
		List enrollmentList = new ArrayList(enrRecFunctionMap.keySet());

		if (isEnrollmentSort()) {
			Collections.sort(enrollmentList, (Comparator) columnSortMap.get(getSortColumn()));
			enrollmentList = finalizeSortingAndPaging(enrollmentList);
			enrollmentMap = new LinkedHashMap(); // Preserve ordering
		} else {
			enrollmentMap = new HashMap();
		}

		for (final Iterator iter = enrollmentList.iterator(); iter.hasNext();) {
			final EnrollmentRecord enr = (EnrollmentRecord) iter.next();
			final Map newEnrRecFunctionMap = new HashMap();
			newEnrRecFunctionMap.put(enr, enrRecFunctionMap.get(enr));
			enrollmentMap.put(enr.getUser().getUserUid(), newEnrRecFunctionMap);
		}

		return enrollmentMap;
	}

	/**
	 *
	 * @param enrollmentMapAllItems
	 * @return an ordered map of EnrollmentRecords to a map of gb Items to function (grade/view)
	 */
	private Map transformToOrderedEnrollmentMapForAllItems(final Map enrollmentMapAllItems) {
		Map enrollmentMap;

		List enrRecList = new ArrayList(enrollmentMapAllItems.keySet());

		if (isEnrollmentSort()) {
			Collections.sort(enrRecList, (Comparator) columnSortMap.get(getSortColumn()));
			enrRecList = finalizeSortingAndPaging(enrRecList);
			enrollmentMap = new LinkedHashMap(); // Preserve ordering
		} else {
			enrollmentMap = new HashMap();
		}

		for (final Iterator iter = enrRecList.iterator(); iter.hasNext();) {
			final EnrollmentRecord enr = (EnrollmentRecord) iter.next();
			enrollmentMap.put(enr, enrollmentMapAllItems.get(enr));
		}

		return enrollmentMap;
	}

	protected List finalizeSortingAndPaging(final List list) {
		List finalList;
		if (!isSortAscending()) {
			Collections.reverse(list);
		}
		if (this.maxDisplayedScoreRows == 0) {
			finalList = list;
		} else {
			final int nextPageRow = Math.min(this.firstScoreRow + this.maxDisplayedScoreRows, this.scoreDataRows);
			finalList = new ArrayList(list.subList(this.firstScoreRow, nextPageRow));
			if (log.isDebugEnabled()) {
				log.debug("finalizeSortingAndPaging subList " + this.firstScoreRow + ", " + nextPageRow);
			}
		}
		return finalList;
	}

	public boolean isEnrollmentSort() {
		final String sortColumn = getSortColumn();
		return (sortColumn.equals(PreferencesBean.SORT_BY_NAME) || sortColumn.equals(PreferencesBean.SORT_BY_UID));
	}

	@Override
	protected void init() {
		this.graderIdToNameMap = new HashMap();

		this.defaultSearchString = getLocalizedString("search_default_student_search_string");
		if (this.searchString == null) {
			this.searchString = this.defaultSearchString;
		}

		// Section filtering.
		this.availableSections = getViewableSections();
		this.sectionFilterSelectItems = new ArrayList();

		// The first choice is always "All available enrollments"
		this.sectionFilterSelectItems
				.add(new SelectItem(new Integer(ALL_SECTIONS_SELECT_VALUE), FacesUtil.getLocalizedString("search_sections_all")));

		// TODO If there are unassigned students and the current user is allowed to see them, add them next.

		// Add the available sections.
		for (int i = 0; i < this.availableSections.size(); i++) {
			final CourseSection section = (CourseSection) this.availableSections.get(i);
			this.sectionFilterSelectItems.add(new SelectItem(new Integer(i), section.getTitle()));
		}

		// If the selected value now falls out of legal range due to sections
		// being deleted, throw it back to the default value (meaning everyone).
		final int selectedSectionVal = this.selectedSectionFilterValue.intValue();
		if ((selectedSectionVal >= 0) && (selectedSectionVal >= this.availableSections.size())) {
			if (log.isInfoEnabled()) {
				log.info("selectedSectionFilterValue=" + this.selectedSectionFilterValue.intValue() + " but available sections="
						+ this.availableSections.size());
			}
			this.selectedSectionFilterValue = new Integer(ALL_SECTIONS_SELECT_VALUE);
		}

		// Category filtering
		this.availableCategories = getViewableCategories();
		this.categoryFilterSelectItems = new ArrayList();

		// The first choice is always "All Categories"
		this.categoryFilterSelectItems
				.add(new SelectItem(new Integer(ALL_CATEGORIES_SELECT_VALUE), FacesUtil.getLocalizedString("search_categories_all")));

		// Add available categories
		for (int i = 0; i < this.availableCategories.size(); i++) {
			final Category cat = (Category) this.availableCategories.get(i);
			this.categoryFilterSelectItems.add(new SelectItem(new Integer(cat.getId().intValue()), cat.getName()));
		}
	}

	public boolean isAllSectionsSelected() {
		return (this.selectedSectionFilterValue.intValue() == ALL_SECTIONS_SELECT_VALUE);
	}

	public String getSelectedSectionUid() {
		final int filterValue = this.selectedSectionFilterValue.intValue();
		if (filterValue == ALL_SECTIONS_SELECT_VALUE) {
			return null;
		} else {
			if (this.availableSections == null) {
				this.availableSections = getViewableSections();
			}
			final CourseSection section = (CourseSection) this.availableSections.get(filterValue);
			return section.getUuid();
		}
	}

	public Integer getSelectedSectionFilterValue() {
		return this.selectedSectionFilterValue;
	}

	public void setSelectedSectionFilterValue(final Integer selectedSectionFilterValue) {
		if (!selectedSectionFilterValue.equals(this.selectedSectionFilterValue)) {
			this.selectedSectionFilterValue = selectedSectionFilterValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}

	public List getSectionFilterSelectItems() {
		return this.sectionFilterSelectItems;
	}

	public boolean isAllCategoriesSelected() {
		return (this.selectedCategoryFilterValue.intValue() == ALL_CATEGORIES_SELECT_VALUE);
	}

	public String getSelectedCategoryUid() {
		final int filterValue = this.selectedCategoryFilterValue.intValue();
		if (filterValue == ALL_CATEGORIES_SELECT_VALUE) {
			return null;
		} else {
			return Integer.toString(filterValue);
			// Category cat = (Category) availableCategories.get(filterValue);
			// return cat.getId().toString();
		}
	}

	public String getCategoryUid(final String uid) {
		if (uid == null) {
			return null;
		}
		final Integer Uid = new Integer(uid);
		if (Uid == ALL_CATEGORIES_SELECT_VALUE) {
			return null;
		} else {
			return uid;
		}

	}

	public Integer getSelectedCategoryFilterValue() {
		return this.selectedCategoryFilterValue;
	}

	public void setSelectedCategoryFilterValue(final Integer selectedCategoryFilterValue) {
		if (!selectedCategoryFilterValue.equals(this.selectedCategoryFilterValue)) {
			this.selectedCategoryFilterValue = selectedCategoryFilterValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}

	public void setSelectedCategoryFilterValue(final ValueChangeEvent event) {
		final Integer newValue = (Integer) event.getNewValue();
		if (!newValue.equals(this.selectedCategoryFilterValue)) {
			this.selectedCategoryFilterValue = newValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}

	public List getCategoryFilterSelectItems() {
		return this.categoryFilterSelectItems;
	}

	public boolean isEmptyEnrollments() {
		return this.emptyEnrollments;
	}

	public boolean isRefreshRoster() {
		return this.refreshRoster;
	}

	public void setRefreshRoster(final boolean refreshRoster) {
		this.refreshRoster = refreshRoster;
	}

	// Map grader UIDs to grader names for the grading event log.
	public String getGraderNameForId(final String graderId) {
		if (this.graderIdToNameMap == null) {
			this.graderIdToNameMap = new HashMap();
		}
		String graderName = (String) this.graderIdToNameMap.get(graderId);
		if (graderName == null) {
			try {
				graderName = getGradebookBean().getUserDisplayName(graderId);
			} catch (final UnknownUserException e) {
				log.warn("Unable to find grader with uid=" + graderId);
				graderName = graderId;
			}
			this.graderIdToNameMap.put(graderId, graderName);
		}
		return graderName;
	}

	// Support grading event logs.
	public class GradingEventRow implements Serializable {
		private final Date date;
		private final String graderName;
		private String grade;

		public GradingEventRow(final GradingEvent gradingEvent) {
			this.date = gradingEvent.getDateGraded();
			this.grade = gradingEvent.getGrade();
			this.graderName = getGraderNameForId(gradingEvent.getGraderId());
		}

		public Date getDate() {
			return this.date;
		}

		public String getGrade() {
			if (this.grade != null) {
				try {
					final Double gradeDouble = new Double(this.grade);
					// we may have gained decimal places in the conversion from points to %
					this.grade = FacesUtil.getRoundDown(gradeDouble.doubleValue(), 2) + "";
				} catch (final NumberFormatException nfe) {
					// ignore b/c may be letter grade
				}
			}
			return this.grade;
		}

		public String getGraderName() {
			return this.graderName;
		}
	}
}
