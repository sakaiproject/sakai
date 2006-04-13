/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.*;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * This is an abstract base class for gradebook dependent backing
 * beans that support searching, sorting, and paging student data.
 */
public abstract class EnrollmentTableBean
    extends GradebookDependentBean implements Paging, Serializable {
	private static final Log log = LogFactory.getLog(EnrollmentTableBean.class);

	private static final int ALL_SECTIONS_SELECT_VALUE = -1;

    private static Map columnSortMap;
    private String searchString;
    private int firstScoreRow;
    private int maxDisplayedScoreRows;
    private int scoreDataRows;
    private boolean emptyEnrollments;	// Needed to render buttons
    private String defaultSearchString;

	// The section selection menu will include some choices that aren't
	// real sections (e.g., "All Sections" or "Unassigned Students".
	private Integer selectedSectionFilterValue = new Integer(ALL_SECTIONS_SELECT_VALUE);
	private List sectionFilterSelectItems;
	private transient List availableSections;	// The real sections accessible by this user

	// We only store grader UIDs in the grading event history, but the
	// log displays grader names instead. This map cuts down on possibly expensive
	// calls to the user directory service.
	private Map graderIdToNameMap;

    public EnrollmentTableBean() {
        maxDisplayedScoreRows = getPreferencesBean().getDefaultMaxDisplayedScoreRows();
    }

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(PreferencesBean.SORT_BY_NAME, FacadeUtils.ENROLLMENT_NAME_COMPARATOR);
        columnSortMap.put(PreferencesBean.SORT_BY_UID, FacadeUtils.ENROLLMENT_DISPLAY_UID_COMPARATOR);
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
    }
    public void clear(ActionEvent event) {
        if (log.isDebugEnabled()) log.debug("clear");
        setSearchString(null);
    }

    // Sorting
    public void sort(ActionEvent event) {
        setFirstRow(0); // clear the paging whenever we update the sorting
    }

    public abstract boolean isSortAscending();
    public abstract void setSortAscending(boolean sortAscending);
    public abstract String getSortColumn();
    public abstract void setSortColumn(String sortColumn);

    // Paging.
    public int getFirstRow() {
        return firstScoreRow;
    }
    public void setFirstRow(int firstRow) {
        firstScoreRow = firstRow;
    }
    public int getMaxDisplayedRows() {
        return maxDisplayedScoreRows;
    }
    public void setMaxDisplayedRows(int maxDisplayedRows) {
        maxDisplayedScoreRows = maxDisplayedRows;
    }
    public int getDataRows() {
        return scoreDataRows;
    }

	private boolean isFilteredSearch() {
        return !StringUtils.equals(searchString, defaultSearchString);
	}

	protected Map getOrderedEnrollmentMap() {
        List enrollments = getWorkingEnrollments();

		scoreDataRows = enrollments.size();
		emptyEnrollments = enrollments.isEmpty();

		return transformToOrderedEnrollmentMap(enrollments);
	}

	protected List getWorkingEnrollments() {
		List enrollments;

		if (isFilteredSearch()) {
			String sectionUid;
			if (isAllSectionsSelected()) {
				sectionUid = null;
			} else {
				sectionUid = getSelectedSectionUid();
			}
			enrollments = findMatchingEnrollments(searchString, sectionUid);
		} else if (isAllSectionsSelected()) {
			enrollments = getAvailableEnrollments();
		} else {
			// The user has selected a particular section.
			enrollments = getSectionEnrollments(getSelectedSectionUid());
		}

		return enrollments;
	}

	private Map transformToOrderedEnrollmentMap(List enrollments) {
		Map enrollmentMap;
		if (isEnrollmentSort()) {
			Collections.sort(enrollments, (Comparator)columnSortMap.get(getSortColumn()));
			enrollments = finalizeSortingAndPaging(enrollments);
			enrollmentMap = new LinkedHashMap();	// Preserve ordering
        } else {
        	enrollmentMap = new HashMap();
        }

        for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
        	EnrollmentRecord enr = (EnrollmentRecord)iter.next();
        	enrollmentMap.put(enr.getUser().getUserUid(), enr);
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
		SectionAwareness sectionAwareness = getSectionAwareness();
		availableSections = getAvailableSections();
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
	}

	public boolean isAllSectionsSelected() {
		return (selectedSectionFilterValue.intValue() == ALL_SECTIONS_SELECT_VALUE);
	}

	public String getSelectedSectionUid() {
		int filterValue = selectedSectionFilterValue.intValue();
		if (filterValue == ALL_SECTIONS_SELECT_VALUE) {
			return null;
		} else {
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
		}
	}

	public List getSectionFilterSelectItems() {
		return sectionFilterSelectItems;
	}

    public boolean isEmptyEnrollments() {
        return emptyEnrollments;
    }

    // Map grader UIDs to grader names for the grading event log.
    public String getGraderNameForId(String graderId) {
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
			return grade;
		}

		public String getGraderName() {
			return graderName;
		}
    }
}
