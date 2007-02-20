/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California, The MIT Corporation
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import org.sakaiproject.tool.gradebook.*;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.io.Serializable;

/**
 *
 * Backing bean to format roster data for export
 *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Feb 9, 2007
 * Time: 2:22:55 PM
 */

public class RosterExportFormatterBean extends GradebookDependentBean implements Serializable,ExportFormatterBean {

    private static final Log logger = LogFactory.getLog(RosterExportFormatterBean.class);
    private transient Map gradeRecordMap;

    /**
     * utility method to determine type of gradable Object
     * and what data to display
     *
     * @param fromScoreMap
     * @return value as string
     */

    private String getScoreAsCellValue(Object fromScoreMap) {
        if (fromScoreMap == null) {
            return null;
        } else if ((fromScoreMap instanceof AssignmentGradeRecord)) {
            Double pointsEarned = ((AbstractGradeRecord)fromScoreMap).getPointsEarned();
            return Double.toString(pointsEarned.doubleValue());
        } else {
            Double pointsEarned = ((CourseGradeRecord)fromScoreMap).getPointsEarned();
            return Double.toString(pointsEarned.doubleValue());
        }
    }


    /**
     * creates a List of lists each entry consists of
     * that represents a row
     *
     * @param gradableObjects
     * @param enrollments
     * @return   list of Lists sort of like a multidimensional array
     */
    public List getExportRows(List gradableObjects, List enrollments) {

        //prepare a list to stor formatted data
        List formattedRows = new ArrayList();

        CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
        gradableObjects.add(courseGrade);

        Set studentUids = new HashSet();
        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
            EnrollmentRecord enr = (EnrollmentRecord)iter.next();
            studentUids.add(enr.getUser().getUserUid());
        }
        //initialize gradeRecordmap
        gradeRecordMap = new HashMap();
        //get grade records and create an populate the gradeRecordMap
        List gradeRecords = getGradebookManager().getAllAssignmentGradeRecords(getGradebookId(), studentUids);
        gradeRecordMap = getGradeRecordMap(gradeRecords);
        //get course grade records (required for the cumulative score) and add the course grade to the gradeRecordMap
        List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
        gradeRecordMap = getGradeRecordMap(courseGradeRecords);

        List heading  = new ArrayList();
        // Add the headers
        heading.add(getLocalizedString("export_student_id"));
        heading.add(getLocalizedString("export_student_name"));


        for(Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
            GradableObject go = (GradableObject)goIter.next();
            if(go.isCourseGrade()){
                heading.add(getLocalizedString("roster_course_grade_column_name"));
            }else{
                heading.add(go.getName());
            }
        }
        formattedRows.add(heading);
        // Add the data
        Collections.sort(enrollments, EnrollmentTableBean.ENROLLMENT_NAME_COMPARATOR);
        for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
            List row = new ArrayList();

            EnrollmentRecord enr = (EnrollmentRecord)enrIter.next();
            Map studentMap = (Map)gradeRecordMap.get(enr.getUser().getUserUid());
            if(logger.isDebugEnabled())logger.debug("userid = "+ enr.getUser().getUserUid());
            row.add(enr.getUser().getDisplayId());
            row.add(enr.getUser().getSortName());
            for(Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
                GradableObject go = (GradableObject)goIter.next();
                if(logger.isDebugEnabled()) logger.debug("userUid=" + enr.getUser().getUserUid() + ", go=" + go.getName() + ", studentMap=" + studentMap);
                Object cellValue = getScoreAsCellValue((studentMap != null) ? studentMap.get(go.getId()) : null);
                if(cellValue != null) {
                    row.add(cellValue);
                }else{
                    row.add("");
                }
            }
            formattedRows.add(row);
        }
        return formattedRows;
    }

    /**
     * method to create a grade Map
     * @param gradeRecords
     * @return  Map of maps of student id and grades
     */
    public Map getGradeRecordMap( List gradeRecords) {
        for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord gradeRecord = (AbstractGradeRecord)iter.next();
			String studentUid = gradeRecord.getStudentId();
			Map studentMap = (Map)gradeRecordMap.get(studentUid);
			if (studentMap == null) {
				studentMap = new HashMap();
				gradeRecordMap.put(studentUid, studentMap);
			}
			studentMap.put(gradeRecord.getGradableObject().getId(), gradeRecord);
		}
        return gradeRecordMap;
    }
}

