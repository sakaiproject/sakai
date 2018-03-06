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
import javax.faces.model.SelectItem;
import javax.faces.event.ValueChangeEvent;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * This is an abstract base class for gradebook dependent backing
 * beans that support searching, sorting, and paging student data.
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
    		collator = Collator.getInstance();
	    	try
	    	{
	    		collator= new RuleBasedCollator(((RuleBasedCollator) collator).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
	    	} catch (ParseException e) {
				log.warn(this + " Cannot init RuleBasedCollator. Will use the default Collator instead.", e);
			}
    	}
    	public int compare(EnrollmentRecord o1, EnrollmentRecord o2)
		{
			return collator.compare(o1.getUser().getSortName(), o2.getUser().getSortName());
		}
	};

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is not a number)
     */
    static final Comparator<EnrollmentRecord> ENROLLMENT_DISPLAY_UID_COMPARATOR = new Comparator<EnrollmentRecord>() {
        public int compare(EnrollmentRecord o1, EnrollmentRecord o2) {
            return o1.getUser().getDisplayId().compareToIgnoreCase(o2.getUser().getDisplayId());
        }
    };

    /**
     * A comparator that sorts enrollments by student display UID (for installations
     * where a student UID is a number)
     */
    static final Comparator<EnrollmentRecord> ENROLLMENT_DISPLAY_UID_NUMERIC_COMPARATOR = new Comparator<EnrollmentRecord>() {
        public int compare(EnrollmentRecord o1, EnrollmentRecord o2) {
            long user1DisplayId = Long.parseLong(o1.getUser().getDisplayId());
            long user2DisplayId = Long.parseLong(o2.getUser().getDisplayId());
            return (int)(user1DisplayId - user2DisplayId);
        }
    };

	public static final int ALL_SECTIONS_SELECT_VALUE = -1;
	public static final int ALL_CATEGORIES_SELECT_VALUE = -1;

    private static Map columnSortMap;
    private String searchString;
    private int firstScoreRow;
    public int maxDisplayedScoreRows;
    private int scoreDataRows;
    private boolean emptyEnrollments;	// Needed to render buttons
    private String defaultSearchString;
    
    private boolean refreshRoster=true; // To prevent unnecessary roster loading

	// The section selection menu will include some choices that aren't
	// real sections (e.g., "All Sections" or "Unassigned Students".
	private Integer selectedSectionFilterValue = new Integer(ALL_SECTIONS_SELECT_VALUE);
	private List sectionFilterSelectItems;
	private List availableSections;	// The real sections accessible by this user

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
        maxDisplayedScoreRows = getPreferencesBean().getDefaultMaxDisplayedScoreRows();
    }

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(PreferencesBean.SORT_BY_NAME, ENROLLMENT_NAME_COMPARATOR);
        columnSortMap.put(PreferencesBean.SORT_BY_UID, ENROLLMENT_DISPLAY_UID_COMPARATOR);
    }

    // Searching
    public String getSearchString() {
        return searchString;
    }
    public void setSearchString(String searchString) {
        if (StringUtils.trimToNull(searchString) == null) {
            searchString = defaultSearchString;
        }
    	if (!StringUtils.equals(searchString, this.searchString)) {
	    	if (log.isDebugEnabled()) log.debug("setSearchString " + searchString);
	        this.searchString = searchString;
	        setFirstRow(0); // clear the paging when we update the search
	    }
    }
    public void search(ActionEvent event) {
        // We don't need to do anything special here, since init will handle the search
        if (log.isDebugEnabled()) log.debug("search");
        setRefreshRoster(true);
    }
    public void clear(ActionEvent event) {
        if (log.isDebugEnabled()) log.debug("clear");
        setSearchString(null);
        setRefreshRoster(true);
    }

    // Sorting
    public void sort(ActionEvent event) {
        setFirstRow(0); // clear the paging whenever we update the sorting
        setRefreshRoster(true);
    }

    public abstract boolean isSortAscending();
    public abstract void setSortAscending(boolean sortAscending);
    public abstract String getSortColumn();
    public abstract void setSortColumn(String sortColumn);

    // Paging.
    
    /**
     * This method more or less turns a JSF input component (namely the Sakai Pager tag)
     * into a JSF command component. We want the Pager to cause immediate pseudo-navigation
     * to a new state, throwing away any score input values without bothering to
     * validate them. But because the Pager is a UIInput component, it doesn't
     * have an action method that will be called before full validation is done.
     * Instead, we declare our Pager input tag to be immediate, set this
     * valueChangeListener, and explicitly jump over all other intervening
     * phases directly to the rendering phase, which should then pick up the new paging
     * values.  
     */
    public void changePagingState(ValueChangeEvent valueChange) {
    	if (log.isDebugEnabled()) log.debug("changePagingState: old=" + valueChange.getOldValue() + ", new=" + valueChange.getNewValue());
    	FacesContext.getCurrentInstance().renderResponse();
    }
        
    public int getFirstRow() {
    	if (log.isDebugEnabled()) log.debug("getFirstRow " + firstScoreRow);
    	return firstScoreRow;
    }
    public void setFirstRow(int firstRow) {
    	if (log.isDebugEnabled()) log.debug("setFirstRow from " + firstScoreRow + " to " + firstRow);
    	firstScoreRow = firstRow;
    	setRefreshRoster(true);
    }
    public int getMaxDisplayedRows() {
        return maxDisplayedScoreRows;
    }
    public void setMaxDisplayedRows(int maxDisplayedRows) {
        maxDisplayedScoreRows = maxDisplayedRows;
        setRefreshRoster(true);
    }
    public int getDataRows() {
        return scoreDataRows;
    }

	private boolean isFilteredSearch() {
        return !StringUtils.equals(searchString, defaultSearchString);
	}
	
	/**
	 * 
	 * @return Map of EnrollmentRecord to Map of gbItems and function (grade/view). 
	 * 			This is the group of students viewable on the roster page
	 */
	protected Map getOrderedEnrollmentMapForAllItems() {
        Map enrollments = getWorkingEnrollmentsForAllItems();

		scoreDataRows = enrollments.size();
		emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMapForAllItems(enrollments);
	}
	
	/**
	 * 
	 * @return Map of studentId to EnrollmentRecord in order. This is the group of
	 * 			students viewable for the course grade page
	 */
	protected Map getOrderedEnrollmentMapForCourseGrades() {
		Map enrollments = getWorkingEnrollmentsForCourseGrade();
		
		scoreDataRows = enrollments.size();
		emptyEnrollments = enrollments.isEmpty();
		
		return transformToOrderedEnrollmentMapWithFunction(enrollments);
	}
	
	/**
	 * 
	 * @param itemId
	 * @param categoryId
	 * @return Map of studentId to EnrollmentRecord in order. This is the group of
	 * 			students viewable for a particular category associated with gb item
	 */
	protected Map getOrderedEnrollmentMapForItem(Long categoryId) {
		Map enrollments = getWorkingEnrollmentsForItem(categoryId);
		
		scoreDataRows = enrollments.size();
		emptyEnrollments = enrollments.isEmpty();
		
		return transformToOrderedEnrollmentMapWithFunction(enrollments);
	}
	
	/**
	 * 
	 * @param categoryId - optional category filter
	 * @return Map of studentId to EnrollmentRecord in order.
	 */
	protected Map getOrderedStudentIdEnrollmentMapForItem(Long categoryId) {
		Map enrollments = getWorkingEnrollmentsForItem(categoryId);
		
		scoreDataRows = enrollments.size();
		emptyEnrollments = enrollments.isEmpty();
		
		return transformToOrderedEnrollmentMap(new ArrayList(enrollments.keySet()));
	}
	
	protected Map getWorkingEnrollmentsForAllItems() {
		Map enrollments;

		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = searchString;
		}
		
		String selSectionUid = null;
		if (!isAllSectionsSelected()) {
			selSectionUid = getSelectedSectionUid();
		}
		
		enrollments = findMatchingEnrollmentsForAllItems(selSearchString, selSectionUid);

		return enrollments;
	}
	
	protected Map getWorkingEnrollmentsForItem(Long categoryId) {
		Map enrollments;
		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = searchString;
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
	 * @return Map of EnrollmentRecord --> function (view/grade) that the current user
	 * has grade/view permission for every gb item
	 */
	protected Map getWorkingEnrollmentsForCourseGrade() {
		Map enrollments;
		String selSearchString = null;
		if (isFilteredSearch()) {
			selSearchString = searchString;
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
			Collections.sort(enrollmentList, (Comparator)columnSortMap.get(getSortColumn()));
			enrollmentList = finalizeSortingAndPaging(enrollmentList);
			enrollmentMap = new LinkedHashMap();	// Preserve ordering
        } else {
        	enrollmentMap = new HashMap();
        }

        for (Iterator iter = enrollmentList.iterator(); iter.hasNext(); ) {
        	EnrollmentRecord enr = (EnrollmentRecord)iter.next();
        	enrollmentMap.put(enr.getUser().getUserUid(), enr);
        }

        return enrollmentMap;
	}
	
	/**
	 * 
	 * @param enrollmentMap - map of EnrollmentRecord --> function
	 * @return Ordered Map of student Id to map of EnrollmentRecord to function
	 */
	private Map transformToOrderedEnrollmentMapWithFunction(Map enrRecFunctionMap) {
		Map studentIdEnrRecFunctionMap;
		Map enrollmentMap;
		List enrollmentList = new ArrayList(enrRecFunctionMap.keySet());

		if (isEnrollmentSort()) {
			Collections.sort(enrollmentList, (Comparator)columnSortMap.get(getSortColumn()));
			enrollmentList = finalizeSortingAndPaging(enrollmentList);
			enrollmentMap = new LinkedHashMap();	// Preserve ordering
        } else {
        	enrollmentMap = new HashMap();
        }

        for (Iterator iter = enrollmentList.iterator(); iter.hasNext(); ) {
        	EnrollmentRecord enr = (EnrollmentRecord)iter.next();
        	Map newEnrRecFunctionMap = new HashMap();
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
	private Map transformToOrderedEnrollmentMapForAllItems(Map enrollmentMapAllItems) {
		Map enrollmentMap;

		List enrRecList = new ArrayList(enrollmentMapAllItems.keySet());

		if (isEnrollmentSort()) {
			Collections.sort(enrRecList, (Comparator)columnSortMap.get(getSortColumn()));
			enrRecList = finalizeSortingAndPaging(enrRecList);
			enrollmentMap = new LinkedHashMap();	// Preserve ordering
		} else {
			enrollmentMap = new HashMap();
		}

		for (Iterator iter = enrRecList.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enr = (EnrollmentRecord)iter.next();
			enrollmentMap.put(enr, (Map)enrollmentMapAllItems.get(enr));
		}

		return enrollmentMap;
	}

	protected List finalizeSortingAndPaging(List list) {
		List finalList;
		if (!isSortAscending()) {
			Collections.reverse(list);
		}
		if (maxDisplayedScoreRows == 0) {
			finalList = list;
		} else {
			int nextPageRow = Math.min(firstScoreRow + maxDisplayedScoreRows, scoreDataRows);
			finalList = new ArrayList(list.subList(firstScoreRow, nextPageRow));
			if (log.isDebugEnabled()) log.debug("finalizeSortingAndPaging subList " + firstScoreRow + ", " + nextPageRow);
		}
		return finalList;
	}

	public boolean isEnrollmentSort() {
		String sortColumn = getSortColumn();
		return (sortColumn.equals(PreferencesBean.SORT_BY_NAME) || sortColumn.equals(PreferencesBean.SORT_BY_UID));
	}

	protected void init() {
		graderIdToNameMap = new HashMap();

        defaultSearchString = getLocalizedString("search_default_student_search_string");
		if (searchString == null) {
			searchString = defaultSearchString;
		}

		// Section filtering.
		availableSections = getViewableSections();
		sectionFilterSelectItems = new ArrayList();

		// The first choice is always "All available enrollments"
		sectionFilterSelectItems.add(new SelectItem(new Integer(ALL_SECTIONS_SELECT_VALUE), FacesUtil.getLocalizedString("search_sections_all")));

		// TODO If there are unassigned students and the current user is allowed to see them, add them next.

		// Add the available sections.
		for (int i = 0; i < availableSections.size(); i++) {
			CourseSection section = (CourseSection)availableSections.get(i);
			sectionFilterSelectItems.add(new SelectItem(new Integer(i), section.getTitle()));
		}

		// If the selected value now falls out of legal range due to sections
		// being deleted, throw it back to the default value (meaning everyone).
		int selectedSectionVal = selectedSectionFilterValue.intValue();
		if ((selectedSectionVal >= 0) && (selectedSectionVal >= availableSections.size())) {
			if (log.isInfoEnabled()) log.info("selectedSectionFilterValue=" + selectedSectionFilterValue.intValue() + " but available sections=" + availableSections.size());
			selectedSectionFilterValue = new Integer(ALL_SECTIONS_SELECT_VALUE);
		}

		// Category filtering
		availableCategories = getViewableCategories();
		categoryFilterSelectItems = new ArrayList();
		
		// The first choice is always "All Categories"
		categoryFilterSelectItems.add(new SelectItem(new Integer(ALL_CATEGORIES_SELECT_VALUE), FacesUtil.getLocalizedString("search_categories_all")));
		
		// Add available categories
		for (int i=0; i < availableCategories.size(); i++){
			Category cat = (Category) availableCategories.get(i);
			categoryFilterSelectItems.add(new SelectItem(new Integer(cat.getId().intValue()), cat.getName()));
		}
		
		// If the selected value now falls out of legal range due to categories
		// being deleted, throw it back to the default value (meaning all categories)
		int selectedCategoryVal = selectedCategoryFilterValue.intValue();
	}

	public boolean isAllSectionsSelected() {
		return (selectedSectionFilterValue.intValue() == ALL_SECTIONS_SELECT_VALUE);
	}

	public String getSelectedSectionUid() {
		int filterValue = selectedSectionFilterValue.intValue();
		if (filterValue == ALL_SECTIONS_SELECT_VALUE) {
			return null;
		} else {
			if (availableSections == null) 
				availableSections = getViewableSections();
			CourseSection section = (CourseSection)availableSections.get(filterValue);
			return section.getUuid();
		}
	}

	public Integer getSelectedSectionFilterValue() {
		return selectedSectionFilterValue;
	}
	public void setSelectedSectionFilterValue(Integer selectedSectionFilterValue) {
		if (!selectedSectionFilterValue.equals(this.selectedSectionFilterValue)) {
			this.selectedSectionFilterValue = selectedSectionFilterValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}

	public List getSectionFilterSelectItems() {
		return sectionFilterSelectItems;
	}
	
	public boolean isAllCategoriesSelected() {
		return (selectedCategoryFilterValue.intValue() == ALL_CATEGORIES_SELECT_VALUE);
	}
	
	public String getSelectedCategoryUid() {
		int filterValue = selectedCategoryFilterValue.intValue();
		if (filterValue == ALL_CATEGORIES_SELECT_VALUE) {
			return null;
		} else {
			return Integer.toString(filterValue);
			//Category cat = (Category) availableCategories.get(filterValue);
			//return cat.getId().toString();
		}
	}
	
	public String getCategoryUid(String uid){
		if (uid == null) {
			return null;
		}
		Integer Uid = new Integer(uid);
		if (Uid == ALL_CATEGORIES_SELECT_VALUE) {
			return null;
		} else {
			return uid;
		}
		
	}
	
	public Integer getSelectedCategoryFilterValue() {
		return selectedCategoryFilterValue;
	}
	
	public void setSelectedCategoryFilterValue(Integer selectedCategoryFilterValue) {
		if (!selectedCategoryFilterValue.equals(this.selectedCategoryFilterValue)) {
			this.selectedCategoryFilterValue = selectedCategoryFilterValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}
	
	public void setSelectedCategoryFilterValue(ValueChangeEvent event){
		Integer newValue = (Integer) event.getNewValue();
		if (!newValue.equals(this.selectedCategoryFilterValue)) {
			this.selectedCategoryFilterValue = newValue;
			setFirstRow(0); // clear the paging when we update the search
			setRefreshRoster(true);
		}
	}
	
	public List getCategoryFilterSelectItems() {
		return categoryFilterSelectItems;
	}

    public boolean isEmptyEnrollments() {
        return emptyEnrollments;
    }
    
    public boolean isRefreshRoster() {
    	return refreshRoster;
    }
    public void setRefreshRoster(boolean refreshRoster) {
    	this.refreshRoster = refreshRoster;
    }

    // Map grader UIDs to grader names for the grading event log.
    public String getGraderNameForId(String graderId) {
    	if (graderIdToNameMap == null)
    		graderIdToNameMap = new HashMap();
		String graderName = (String)graderIdToNameMap.get(graderId);
		if (graderName == null) {
			try {
				graderName = getUserDirectoryService().getUserDisplayName(graderId);
			} catch (UnknownUserException e) {
				log.warn("Unable to find grader with uid=" + graderId);
				graderName = graderId;
			}
			graderIdToNameMap.put(graderId, graderName);
		}
		return graderName;
    }

    // Support grading event logs.
    public class GradingEventRow implements Serializable {
		private Date date;
		private String graderName;
		private String grade;

		public GradingEventRow(GradingEvent gradingEvent) {
			date = gradingEvent.getDateGraded();
			grade = gradingEvent.getGrade();
			graderName = getGraderNameForId(gradingEvent.getGraderId());
		}

		public Date getDate() {
			return date;
		}

		public String getGrade() {
			if (grade != null) {
				try {
					Double gradeDouble = new Double(grade);
					// we may have gained decimal places in the conversion from points to %
					grade = FacesUtil.getRoundDown(gradeDouble.doubleValue(), 2) + "";
				} catch (NumberFormatException nfe) {
					// ignore b/c may be letter grade
				}
			}
			return grade;
		}

		public String getGraderName() {
			return graderName;
		}
    }
}
