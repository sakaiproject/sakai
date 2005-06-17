/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;

/**
 * Manages GradableRecord persistence.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface GradeManager {

    /**
     * Get all grade records. In the long term, we expect this will be used
     * primarily for export.
     *
     * @param gradebookId
     * @return A List of grade records
     */
    public List getAllGradeRecords(Long gradebookId);

    /**
     * Get all grade records for the given gradable object
     *
     * @param gradableObject Find grade records for this gradable object
     * @return A List of grade records
     */
    public List getPointsEarnedSortedGradeRecords(GradableObject gradableObject);

    /**
     * Get all grade records for the given gradable object and the given set of
     * student UIDs
     *
     * @param gradableObject Find grade records for this gradable object
     * @param studentUids
     * @return A List of grade records
     */
    public List getPointsEarnedSortedGradeRecords(GradableObject gradableObject, Collection studentUids);

    /**
     * Gets all grade records that belong to a collection of enrollments in a
     * gradebook.
     *
     * @param gradebookId
     * @param studentUids
     */
    public List getPointsEarnedSortedAllGradeRecords(Long gradebookId, Collection studentUids);

    /**
     * Gets all grade records in a gradebook.
     *
     * @param gradebookId
     */
    public List getPointsEarnedSortedAllGradeRecords(Long gradebookId);

    /**
     * Gets whether there are explicitly entered course grade records in a gradebook.
     * (This may include grades for students who are not currently enrolled.)
     *
     * @param gradebookId The gradebook
     * @return Whether there are course grade records that have a non-null enteredGrade field
     */
    public boolean isExplicitlyEnteredCourseGradeRecords(Long gradebookId);

    /**
     * Gets whether scores have been entered for the given assignment.
     * (This may include scores for students who are not currently enrolled.)
     *
     * @param assignmentId The assignment
     * @return How many scores have been entered for the assignment
     */
    public boolean isEnteredAssignmentScores(Long assignmentId);

    /**
     * Updates the grade records for the keys (student IDs) in the studentsToPoints map.
     * Implementations of this method should add a new GradingEvent for each
     * grade record modified, and should update the autocalculated value for
     * each graded student's CourseGradeRecord.
     *
     * @param studentsToPoints A Map of student IDs to points as java.lang.Double values
     * @return The set of student UIDs who were given scores higher than the
     * assignment's value.
     */
    public Set updateAssignmentGradeRecords(Long assignmentId, Map studentsToPoints)
        throws StaleObjectModificationException;

    /**
     * Updates the grade records for the keys (student IDs) in the studentsToPoints map.
     * Map values must be valid strings (that exist in the gradebook's grade
     * mapping) or nulls.
     *
     * @param studentsToPoints A Map of student IDs to grades
     */
    public void updateCourseGradeRecords(Long gradebookId, Map studentsToPoints)
        throws StaleObjectModificationException;

    /**
     * Update the course grade records for a collection of students.  This operation
     * must be called each time an assignment grade record is modified.  Otherwise
     * the course grade records' "pointsEarned" values will be out of sync with the
     * assignment grade records.
     *
     * @param gradebook The gradebook
     * @param studentIds The students that need their course grade records updated
     */
    public void recalculateCourseGradeRecords(final Gradebook gradebook, final Collection studentIds);

    /**
     * Gets all grade records for a single student in a single gradebook
     *
     * @param gradebookId The gradebook id
     * @param studentId The unique student identifier
     *
     * @return A List of all of this student's grade records in the gradebook
     */
    public List getStudentGradeRecords(Long gradebookId, String studentId);

    /**
     * Gets the course grade for a single student.
     */
    public CourseGradeRecord getStudentCourseGradeRecord(Gradebook gradebook, String studentId);

    /**
     * Gets the grading events for the enrollments on the given gradable object.
     * 
     * @param gradableObject
     * @param enrollments
     * @return
     */
    public GradingEvents getGradingEvents(GradableObject gradableObject, Collection enrollments);

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
