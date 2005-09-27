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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
public class RosterBean extends EnrollmentTableBean implements Serializable, Paging {
	private static final Log logger = LogFactory.getLog(RosterBean.class);

	// Used to generate IDs for the dynamically created assignment columns.
	private static final String ASSIGNMENT_COLUMN_PREFIX = "asg_";

	// View maintenance fields - serializable.
	private List gradableObjectColumns;	// Needed to build table columns
	private boolean emptyEnrollments;	// Needed to render the export buttons

	public class GradableObjectColumn implements Serializable {
		private Long id;
		private String name;

		public GradableObjectColumn() {
		}
		public GradableObjectColumn(GradableObject gradableObject) {
			id = gradableObject.getId();
			if (gradableObject.isCourseGrade()) {
				name = getLocalizedString("roster_course_grade_column_name");
			} else {
				name = gradableObject.getName();
			}
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
	private transient Map scoresMap;

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
			return (Map)scoresMap.get(enrollment.getUser().getUserUid());
		}
	}

	protected void init() {
		super.init();

        Map enrollmentMap = getOrderedEnrollmentMap(getEnrollments());

		List gradeRecords;

        if (isFilteredSearch() || isEnrollmentSort()) {
        	gradeRecords = getGradeManager().getPointsEarnedSortedAllGradeRecords(getGradebookId(), enrollmentMap.keySet());
        } else {
        	gradeRecords = getGradeManager().getPointsEarnedSortedAllGradeRecords(getGradebookId());
        }

        List workingEnrollments = new ArrayList(enrollmentMap.values());

        if (!isEnrollmentSort()) {
        	// Need to sort and page based on a scores column.
        	String sortColumn = getSortColumn();
        	List scoreSortedEnrollments = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
				if(agr.getGradableObject().getName().equals(sortColumn)) {
					scoreSortedEnrollments.add(enrollmentMap.get(agr.getStudentId()));
				}
			}

            // Put enrollments with no scores at the beginning of the final list.
            workingEnrollments.removeAll(scoreSortedEnrollments);

            // Add all sorted enrollments with scores into the final list
            workingEnrollments.addAll(scoreSortedEnrollments);

            workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
		}

        emptyEnrollments = workingEnrollments.size() == 0;

        scoresMap = new HashMap();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord gradeRecord = (AbstractGradeRecord)iter.next();
			String studentUid = gradeRecord.getStudentId();
			Map studentMap = (Map)scoresMap.get(studentUid);
			if (studentMap == null) {
				studentMap = new HashMap();
				scoresMap.put(studentUid, studentMap);
			}

			// The current specification shows the final column as "Cumulative" points
			// rather than as a course display grade.
			studentMap.put(gradeRecord.getGradableObject().getId(), gradeRecord.getPointsEarned());
		}
		if (logger.isDebugEnabled()) logger.debug("init - scoresMap.keySet().size() = " + scoresMap.keySet().size());

		studentRows = new ArrayList(workingEnrollments.size());
        for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
            EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
            studentRows.add(new StudentRow(enrollment));
        }

		List gradableObjects = getGradeManager().getAssignments(getGradebookId());
		CourseGrade courseGrade = getGradeManager().getCourseGrade(getGradebookId());
		gradableObjectColumns = new ArrayList();
		for (Iterator iter = gradableObjects.iterator(); iter.hasNext(); ) {
			gradableObjectColumns.add(new GradableObjectColumn((GradableObject)iter.next()));
		}
		gradableObjectColumns.add(new GradableObjectColumn(courseGrade));

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
                if(iter.hasNext()) {
                    sortHeader.setColumnName(columnData.getName());
                } else {
                    sortHeader.setColumnName(CourseGrade.COURSE_GRADE_NAME);
                }
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
				contents.setId(ASSIGNMENT_COLUMN_PREFIX + "cell_" + colpos);
				contents.setValueBinding("value",
					app.createValueBinding("#{row.scores[rosterBean.gradableObjectColumns[" + colpos + "].id]}"));
                NumberConverter numberConverter = new NumberConverter();
                numberConverter.setPattern("#");
                contents.setConverter(numberConverter);

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

	public Map getScoresMap() {
		return scoresMap;
	}

	public boolean isEmptyEnrollments() {
        return emptyEnrollments;
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
}



