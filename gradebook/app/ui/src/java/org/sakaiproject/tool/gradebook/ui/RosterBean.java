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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.jsf.AssignmentPointsConverter;
import org.sakaiproject.tool.gradebook.jsf.SpreadsheetDataDownloaderCsv;
import org.sakaiproject.tool.gradebook.jsf.SpreadsheetDataDownloaderXls;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
public class RosterBean extends EnrollmentTableBean implements Serializable, Paging {
	private static final Log logger = LogFactory.getLog(RosterBean.class);

	// Used to generate IDs for the dynamically created assignment columns.
	private static final String ASSIGNMENT_COLUMN_PREFIX = "asg_";

	// View maintenance fields - serializable.
	private List gradableObjectColumns;	// Needed to build table columns
    private List workingEnrollments;

    public class GradableObjectColumn implements Serializable {
		private Long id;
		private String name;

		public GradableObjectColumn() {
		}
		public GradableObjectColumn(GradableObject gradableObject) {
			id = gradableObject.getId();
			name = getColumnHeader(gradableObject);
		}

		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

	// Controller fields - transient.
	private transient List studentRows;
	private transient Map gradeRecordMap;

	public class StudentRow implements Serializable {
        private EnrollmentRecord enrollment;

		public StudentRow() {
		}
		public StudentRow(EnrollmentRecord enrollment) {
            this.enrollment = enrollment;
		}

		public String getStudentUid() {
			return enrollment.getUser().getUserUid();
		}
		public String getSortName() {
			return enrollment.getUser().getSortName();
		}
		public String getDisplayId() {
			return enrollment.getUser().getDisplayId();
		}

		public Map getScores() {
			return (Map)gradeRecordMap.get(enrollment.getUser().getUserUid());
		}
	}

	protected void init() {
		super.init();

		List assignments = getGradebookManager().getAssignments(getGradebookId());
		CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		gradableObjectColumns = new ArrayList();
		for (Iterator iter = assignments.iterator(); iter.hasNext(); ) {
			gradableObjectColumns.add(new GradableObjectColumn((GradableObject)iter.next()));
		}
		gradableObjectColumns.add(new GradableObjectColumn(courseGrade));

        Map enrollmentMap = getOrderedEnrollmentMap();

		List gradeRecords = getGradebookManager().getAllAssignmentGradeRecords(getGradebookId(), enrollmentMap.keySet());
        workingEnrollments = new ArrayList(enrollmentMap.values());

        gradeRecordMap = new HashMap();
        getGradebookManager().addToGradeRecordMap(gradeRecordMap, gradeRecords);
		if (logger.isDebugEnabled()) logger.debug("init - gradeRecordMap.keySet().size() = " + gradeRecordMap.keySet().size());

		List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet(), assignments, gradeRecordMap);
		Collections.sort(courseGradeRecords, CourseGradeRecord.calcComparator);
        getGradebookManager().addToGradeRecordMap(gradeRecordMap, courseGradeRecords);
        gradeRecords.addAll(courseGradeRecords);

        if (!isEnrollmentSort()) {
        	// Need to sort and page based on a scores column.
        	String sortColumn = getSortColumn();
        	List scoreSortedEnrollments = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
				if(getColumnHeader(agr.getGradableObject()).equals(sortColumn)) {
					scoreSortedEnrollments.add(enrollmentMap.get(agr.getStudentId()));
				}
			}

            // Put enrollments with no scores at the beginning of the final list.
            workingEnrollments.removeAll(scoreSortedEnrollments);

            // Add all sorted enrollments with scores into the final list
            workingEnrollments.addAll(scoreSortedEnrollments);

            workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
		}

		studentRows = new ArrayList(workingEnrollments.size());
        for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
            EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
            studentRows.add(new StudentRow(enrollment));
        }

	}
	
	private String getColumnHeader(GradableObject gradableObject) {
		if (gradableObject.isCourseGrade()) {
			return getLocalizedString("roster_course_grade_column_name");
		} else {
			return ((Assignment)gradableObject).getName();
		}
	}

	// The roster table uses assignments as columns, and therefore the component
	// model needs to have those columns added dynamically, based on the current
	// state of the gradebook.
	// In JSF 1.1, dynamic data table columns are managed by binding the component
	// tag to a bean property.

	// It's not exactly intuitive, but the convention is for the bean to return
	// null, so that JSF can create and manage the UIData component itself.
	public HtmlDataTable getRosterDataTable() {
		if (logger.isDebugEnabled()) logger.debug("getRosterDataTable");
		return null;
	}

	public void setRosterDataTable(HtmlDataTable rosterDataTable) {
		if (logger.isDebugEnabled()) {
			logger.debug("setRosterDataTable gradableObjectColumns=" + gradableObjectColumns + ", rosterDataTable=" + rosterDataTable);
			if (rosterDataTable != null) {
				logger.debug("  data children=" + rosterDataTable.getChildren());
			}
		}

        // Set the columnClasses on the data table
        StringBuffer colClasses = new StringBuffer("left,left,");
        for(Iterator iter = gradableObjectColumns.iterator(); iter.hasNext();) {
        	iter.next();
            colClasses.append("center");
            if(iter.hasNext()) {
                colClasses.append(",");
            }
        }
        rosterDataTable.setColumnClasses(colClasses.toString());

		if (rosterDataTable.findComponent(ASSIGNMENT_COLUMN_PREFIX + "0") == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			// Add columns for each assignment. Be sure to create unique IDs
			// for all child components.
			int colpos = 0;
			for (Iterator iter = gradableObjectColumns.iterator(); iter.hasNext(); colpos++) {
				GradableObjectColumn columnData = (GradableObjectColumn)iter.next();

				UIColumn col = new UIColumn();
				col.setId(ASSIGNMENT_COLUMN_PREFIX + colpos);

                HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
                sortHeader.setId(ASSIGNMENT_COLUMN_PREFIX + "sorthdr_" + colpos);
                sortHeader.setRendererType("org.apache.myfaces.SortHeader");	// Yes, this is necessary.
                sortHeader.setArrow(true);
                sortHeader.setColumnName(columnData.getName());
                sortHeader.setActionListener(app.createMethodBinding("#{rosterBean.sort}", new Class[] {ActionEvent.class}));

                // Allow word-wrapping on assignment name columns.
                sortHeader.setStyleClass("allowWrap");

				HtmlOutputText headerText = new HtmlOutputText();
				headerText.setId(ASSIGNMENT_COLUMN_PREFIX + "hdr_" + colpos);
				// Try straight setValue rather than setValueBinding.
				headerText.setValue(columnData.getName());

                sortHeader.getChildren().add(headerText);
                col.setHeader(sortHeader);

				HtmlOutputText contents = new HtmlOutputText();
				contents.setEscape(false);
				contents.setId(ASSIGNMENT_COLUMN_PREFIX + "cell_" + colpos);
				contents.setValueBinding("value",
					app.createValueBinding("#{row.scores[rosterBean.gradableObjectColumns[" + colpos + "].id]}"));
                contents.setConverter(new AssignmentPointsConverter());

                // Distinguish the "Cumulative" score for the course, which, by convention,
                // is always the last column.
                if (!iter.hasNext()) {
                	contents.setStyleClass("courseGrade");
                }

				col.getChildren().add(contents);

				rosterDataTable.getChildren().add(col);
			}
		}
	}

	public List getGradableObjectColumns() {
		return gradableObjectColumns;
	}
	public void setGradableObjectColumns(List gradableObjectColumns) {
		this.gradableObjectColumns = gradableObjectColumns;
	}

	public List getStudentRows() {
		return studentRows;
	}

	// Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isRosterTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setRosterTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getRosterTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setRosterTableSortColumn(sortColumn);
    }

    public void exportCSV(ActionEvent event){
        if(logger.isInfoEnabled()) logger.info("exporting course grade as CSV for gradebook " + getGradebookUid());
        SpreadsheetDataDownloaderCsv downloader = new SpreadsheetDataDownloaderCsv();
        downloader.download(getSpreadsheetData(), getFileName(getLocalizedString("export_gradebook_prefix")));
    }

    public void exportExcel(ActionEvent event){
        if(logger.isInfoEnabled()) logger.info("exporting course grade as Excel for gradebook " + getGradebookUid());
        SpreadsheetDataDownloaderXls downloader = new SpreadsheetDataDownloaderXls();
        downloader.download(getSpreadsheetData(), getFileName(getLocalizedString("export_gradebook_prefix")));
    }
    
    private List<List<Object>> getSpreadsheetData() {
    	// Get the full list of filtered enrollments and scores (not just the current page's worth).
    	List filteredEnrollments = getWorkingEnrollments();
    	Collections.sort(filteredEnrollments, ENROLLMENT_NAME_COMPARATOR);
    	Set<String> studentUids = new HashSet<String>();
    	for (Iterator iter = filteredEnrollments.iterator(); iter.hasNext(); ) {
    		EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
    		studentUids.add(enrollment.getUser().getUserUid());
    	}

		Map filteredGradesMap = new HashMap();    	
    	List gradeRecords = getGradebookManager().getAllAssignmentGradeRecords(getGradebookId(), studentUids);
        getGradebookManager().addToGradeRecordMap(filteredGradesMap, gradeRecords);
        
		List gradableObjects = getGradebookManager().getAssignments(getGradebookId());
		CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids, gradableObjects, filteredGradesMap);
        getGradebookManager().addToGradeRecordMap(filteredGradesMap, courseGradeRecords);
        gradableObjects.add(courseGrade);
    	return getSpreadsheetData(filteredEnrollments, filteredGradesMap, gradableObjects);
    }
    
    private List<List<Object>> getSpreadsheetData(List enrollments, Map gradesMap, List gradableObjects) {
    	List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

    	// Build column headers.
        List<Object> headerRow = new ArrayList<Object>();
        headerRow.add(getLocalizedString("export_student_id"));
        headerRow.add(getLocalizedString("export_student_name"));
        for (Object gradableObject : gradableObjects) {
        	String colName = null;
         	if (gradableObject instanceof Assignment) {
         		colName = ((Assignment)gradableObject).getName();
         	} else if (gradableObject instanceof CourseGrade) {
         		colName = getLocalizedString("roster_course_grade_column_name");
         	}
        	headerRow.add(colName);
        }
        spreadsheetData.add(headerRow);
        
        // Build student score rows.
        for (Object enrollment : enrollments) {
        	User student = ((EnrollmentRecord)enrollment).getUser();
        	String studentUid = student.getUserUid();
        	Map studentMap = (Map)gradesMap.get(studentUid);
        	List<Object> row = new ArrayList<Object>();
        	row.add(student.getDisplayId());
        	row.add(student.getSortName());
        	for (Object gradableObject : gradableObjects) {
        		Double score = null;
        		if (studentMap != null) {
        			AbstractGradeRecord gradeRecord = (AbstractGradeRecord)studentMap.get(((GradableObject)gradableObject).getId()); 
        			if (gradeRecord != null) {
        				score = gradeRecord.getPointsEarned();
        			}
        		}
    			row.add(score);
        	}
        	spreadsheetData.add(row);
        }
    	
    	return spreadsheetData;
    }

   	/**
     * Gets the filename for the export
     *
	 * @param   prefix fro filename
	 * @return The appropriate filename for the export
	 */
    private String getFileName(String prefix) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat(getLocalizedString("export_filename_date_format"));
		StringBuffer fileName = new StringBuffer(prefix);
        String gbName = getGradebook().getName();
        if(StringUtils.trimToNull(gbName) != null) {
            gbName = gbName.replaceAll("\\s", "_"); // replace whitespace with '_'
            fileName.append("-");
            fileName.append(gbName);
        }
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}
}
