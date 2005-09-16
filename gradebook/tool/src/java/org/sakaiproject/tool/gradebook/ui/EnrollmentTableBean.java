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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.facades.Enrollment;

/**
 * This is an abstract base class for gradebook dependent backing
 * beans that support searching, sorting, and paging student data.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class EnrollmentTableBean
    extends GradebookDependentBean implements Paging, Serializable {
	private static final Log logger = LogFactory.getLog(EnrollmentTableBean.class);

    protected static Map columnSortMap;
    protected String searchString;
    protected int firstScoreRow;
    protected int maxDisplayedScoreRows;
    protected int scoreDataRows;
    protected boolean emptyEnrollments;	// Needed to render buttons

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
        this.searchString = searchString;
    }
    public void search(ActionEvent event) {
        // We don't need to do anything special here, since init will handle the search
        setFirstRow(0); // clear the paging when we update the search
    }
    public void clear(ActionEvent event) {
        searchString = null;
        setFirstRow(0); // clear the paging when we update the search
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

	public boolean isFilteredSearch() {
        String defaultSearchString = getLocalizedString("search_default_student_search_string");
        if (StringUtils.trimToNull(searchString) == null) {
            searchString = defaultSearchString;
        }
        return !defaultSearchString.equals(searchString);
	}

	protected Map getOrderedEnrollmentMap(Collection enrollments) {
        Map enrollmentMap;

		// This is where we used to allow for optimized queries. A lost cause for the forseeable
		// future....

		if (isFilteredSearch()) {
			enrollments = getCourseManagementService().findEnrollmentsByStudentNameOrDisplayUid(getGradebookUid(), searchString);
		}

		scoreDataRows = enrollments.size();
		if (isEnrollmentSort()) {
			// Handle sorting and paging in memory, since the service facades
			// didn't do it.
			List enrollmentList = new ArrayList(enrollments);
			Collections.sort(enrollmentList, (Comparator)columnSortMap.get(getSortColumn()));
			enrollments = finalizeSortingAndPaging(enrollmentList);
		}

		emptyEnrollments = enrollments.isEmpty();

		if (isEnrollmentSort()) {
			enrollmentMap = new LinkedHashMap();	// Preserve ordering
        } else {
        	enrollmentMap = new HashMap();
        }

        for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
        	Enrollment enr = (Enrollment)iter.next();
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
			if (logger.isDebugEnabled()) logger.debug("finalizeSortingAndPaging subList " + firstScoreRow + ", " + nextPageRow);
		}
		return finalList;
	}

	public boolean isEnrollmentSort() {
		String sortColumn = getSortColumn();
		return (sortColumn.equals(PreferencesBean.SORT_BY_NAME) || sortColumn.equals(PreferencesBean.SORT_BY_UID));
	}
}
