/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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

import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.component.cover.ServerConfigurationService;

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
    
    private String categorySortColumn;
    private boolean categorySortAscending;

    private String rosterTableSortColumn;
    private boolean rosterTableSortAscending;

	private String assignmentDetailsTableSortColumn;
    private boolean assignmentDetailsTableSortAscending;

    private String courseGradeDetailsTableSortColumn;
    private boolean courseGradeDetailsTableSortAscending;
    
    private Integer assignmentDetailsTableSelectedSectionFilter;  
    private Integer rosterTableSelectedSectionFilter;

    private int defaultMaxDisplayedScoreRows;

    public PreferencesBean() {
        loadPreferences();
    }

    /**
	 * This could eventually be loaded from persistence or from the framework
	 */
	protected void loadPreferences() {
        assignmentSortAscending = true;
        assignmentSortColumn = GradebookAssignment.DEFAULT_SORT;
        
        categorySortAscending = true;
        categorySortColumn = Category.SORT_BY_NAME;

        rosterTableSortAscending = true;
        rosterTableSortColumn = SORT_BY_NAME;

        assignmentDetailsTableSortAscending = true;
        assignmentDetailsTableSortColumn = SORT_BY_NAME;

        courseGradeDetailsTableSortAscending = true;
        courseGradeDetailsTableSortColumn = SORT_BY_NAME;
        
        assignmentDetailsTableSelectedSectionFilter = new Integer(EnrollmentTableBean.ALL_SECTIONS_SELECT_VALUE);
        
        rosterTableSelectedSectionFilter = new Integer(EnrollmentTableBean.ALL_SECTIONS_SELECT_VALUE);

        defaultMaxDisplayedScoreRows = 50;
        String defaultMaxDisplayedScoreRowsSakaiProp = ServerConfigurationService.getString("gradebook.defaultMaxDisplayedScoreRows");
        if(defaultMaxDisplayedScoreRowsSakaiProp != null){
        	try{
        		int defaultMaxDisplayedScoreRowsSakaiPropInt = Integer.parseInt(defaultMaxDisplayedScoreRowsSakaiProp);
        		if(defaultMaxDisplayedScoreRowsSakaiPropInt == 5 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 10 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 15 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 20 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 50 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 100 ||
        				defaultMaxDisplayedScoreRowsSakaiPropInt == 0){
        			defaultMaxDisplayedScoreRows = defaultMaxDisplayedScoreRowsSakaiPropInt;
        		}
        	}catch (Exception e) {
        		//integer parse issue, bad property
			}
        }
    }

	// Paging preferences
    public int getDefaultMaxDisplayedScoreRows() {
        return defaultMaxDisplayedScoreRows;
    }

	// GradebookAssignment sorting (for overview, assignment details, and possibly roster)
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
    
    //Category sorting
    public boolean isCategorySortAscending() {
        return categorySortAscending;
    }
    public void setCategorySortAscending(boolean categorySortAscending) {
        this.categorySortAscending = categorySortAscending;
    }
    public String getCategorySortColumn() {
        return categorySortColumn;
    }
    public void setCategorySortColumn(String categorySortColumn) {
        this.categorySortColumn = categorySortColumn;
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

    // GradebookAssignment details table sorting
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
    
    // GradebookAssignment details table filter by section
    public Integer getAssignmentDetailsTableSectionFilter() {
        return assignmentDetailsTableSelectedSectionFilter;
    }
    public void setAssignmentDetailsTableSectionFilter(
            Integer assignmentDetailsTableSelectedSectionFilter) {
        this.assignmentDetailsTableSelectedSectionFilter = assignmentDetailsTableSelectedSectionFilter;
    }
    
    // Roster table filter by section
    public Integer getRosterTableSectionFilter() {
        return rosterTableSelectedSectionFilter;
    }
    public void setRosterTableSectionFilter(Integer rosterTableSelectedSectionFilter) {
        this.rosterTableSelectedSectionFilter = rosterTableSelectedSectionFilter;
    }
}



