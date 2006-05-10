/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

import org.sakaiproject.tool.gradebook.Assignment;

/**
 * Session-scoped preferences for the sakai gradebook.  These are currently
 * not persistent across http sessions.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class PreferencesBean {

    public static final String SORT_BY_NAME = "studentSortName";
    public static final String SORT_BY_UID = "studentDisplayId";

    private String assignmentSortColumn;
    private boolean assignmentSortAscending;

    private String rosterTableSortColumn;
    private boolean rosterTableSortAscending;

	private String assignmentDetailsTableSortColumn;
    private boolean assignmentDetailsTableSortAscending;

    private String courseGradeDetailsTableSortColumn;
    private boolean courseGradeDetailsTableSortAscending;

    private int defaultMaxDisplayedScoreRows;

    public PreferencesBean() {
        loadPreferences();
    }

    /**
	 * This could eventually be loaded from persistence or from the framework
	 */
	protected void loadPreferences() {
        assignmentSortAscending = true;
        assignmentSortColumn = Assignment.SORT_BY_DATE;

        rosterTableSortAscending = true;
        rosterTableSortColumn = SORT_BY_NAME;

        assignmentDetailsTableSortAscending = true;
        assignmentDetailsTableSortColumn = SORT_BY_NAME;

        courseGradeDetailsTableSortAscending = true;
        courseGradeDetailsTableSortColumn = SORT_BY_NAME;

        defaultMaxDisplayedScoreRows = 20;
    }

	// Paging preferences
    public int getDefaultMaxDisplayedScoreRows() {
        return defaultMaxDisplayedScoreRows;
    }

	// Assignment sorting (for overview, assignment details, and possibly roster)
    public boolean isAssignmentSortAscending() {
        return assignmentSortAscending;
    }
    public void setAssignmentSortAscending(boolean assignmentSortAscending) {
        this.assignmentSortAscending = assignmentSortAscending;
    }
    public String getAssignmentSortColumn() {
        return assignmentSortColumn;
    }
    public void setAssignmentSortColumn(String assignmentSortColumn) {
        this.assignmentSortColumn = assignmentSortColumn;
    }

    // Roster table sorting
    public boolean isRosterTableSortAscending() {
		return rosterTableSortAscending;
	}
	public void setRosterTableSortAscending(boolean rosterTableSortAscending) {
		this.rosterTableSortAscending = rosterTableSortAscending;
	}
	public String getRosterTableSortColumn() {
		return rosterTableSortColumn;
	}
	public void setRosterTableSortColumn(String rosterTableSortColumn) {
		this.rosterTableSortColumn = rosterTableSortColumn;
	}

    // Assignment details table sorting
	public boolean isAssignmentDetailsTableSortAscending() {
		return assignmentDetailsTableSortAscending;
	}
	public void setAssignmentDetailsTableSortAscending(
			boolean assignmentDetailsTableSortAscending) {
		this.assignmentDetailsTableSortAscending = assignmentDetailsTableSortAscending;
	}
	public String getAssignmentDetailsTableSortColumn() {
		return assignmentDetailsTableSortColumn;
	}
	public void setAssignmentDetailsTableSortColumn(
			String assignmentDetailsTableSortColumn) {
		this.assignmentDetailsTableSortColumn = assignmentDetailsTableSortColumn;
	}

    // Course grade details table sorting
    public boolean isCourseGradeDetailsTableSortAscending() {
        return courseGradeDetailsTableSortAscending;
    }
    public void setCourseGradeDetailsTableSortAscending(
            boolean courseGradeDetailsTableSortAscending) {
        this.courseGradeDetailsTableSortAscending = courseGradeDetailsTableSortAscending;
    }
    public String getCourseGradeDetailsTableSortColumn() {
        return courseGradeDetailsTableSortColumn;
    }
    public void setCourseGradeDetailsTableSortColumn(
            String courseGradeDetailsTableSortColumn) {
        this.courseGradeDetailsTableSortColumn = courseGradeDetailsTableSortColumn;
    }
}



