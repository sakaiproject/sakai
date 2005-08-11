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

package org.sakaiproject.tool.gradebook.business;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
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
     * Updates the grade records in the GradeRecordSet.
     * Implementations of this method should add a new GradingEvent for each
     * grade record modified, and should update the autocalculated value for
     * each graded student's CourseGradeRecord.
     *
     * @return The set of student UIDs who were given scores higher than the
     * assignment's value.
     */
    public Set updateAssignmentGradeRecords(GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException;

    /**
     * Updates the grade records for the keys (student IDs) in the studentsToPoints map.
     * Map values must be valid strings (that exist in the gradebook's grade
     * mapping) or nulls.
     *
     * @param studentsToPoints A Map of student IDs to grades
     */
    public void updateCourseGradeRecords(GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException;

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


    //////////////////////////////////////////////////
    // Consolidated from GradeManager.java //
    //////////////////////////////////////////////////


    /**
     * Fetches a List of Assignments, but does not populate non-persistent
     * fields.
     *
     * @param gradebookId The gradebook ID
     * @param sortBy The field by which to sort the list.
     * @return A list of Assignments with only persistent fields populated
     */
    public List getAssignments(Long gradebookId, String sortBy, boolean ascending);

    /**
     * Convenience method to get assignments with the default sort ordering
     *
     * @param gradebookId The gradebook ID
     */
    public List getAssignments(Long gradebookId);

    /**
     * Fetches a List of Assignments for a given gradebook, and populates the
     * Assignments with all of the statistics fields available in the Assignment
     * object.
     *
     * @param gradebookId The gradebook ID
     * @param sortBy The field by which to sort the list.
     * @return A list of Assignments with their statistics fields populated
     */
    public List getAssignmentsWithStats(Long gradebookId, String sortBy, boolean ascending);

    /**
     * Convenience method to get assignments with statistics in the default sort order
     *
     * @param gradebookId The gradebook ID
     */
    public List getAssignmentsWithStats(Long gradebookId);

    /**
     * Fetches a GradableObject
     *
     * @param gradableObjectId The gradable object ID
     * @return The GradableObject
     */
    public GradableObject getGradableObject(Long gradableObjectId);

    /**
     * Fetches a GradableObject and populates its non-persistent statistics
     * fields.
     *
     * @param gradableObjectId The gradable object ID
     * @return The GradableObject with all statistics fields populated
     */
    public GradableObject getGradableObjectWithStats(Long gradableObjectId);

    /**
     * Add a new assignment to a gradebook
     *
     * @param gradebookId The gradebook ID to which this new assignment belongs
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param points The number of points possible for this assignment (must not be null)
     * @param dueDate The due date for the assignment (optional)
     *
     * @return The ID of the new assignment
     */
    public Long createAssignment(Long gradebookId, String name, Double points, Date dueDate)
        throws ConflictingAssignmentNameException, StaleObjectModificationException;

    /**
     * Updates an existing assignment
     */
    public void updateAssignment(Assignment assignment)
        throws ConflictingAssignmentNameException, StaleObjectModificationException;

    /**
     * Fetches the course grade for a gradebook
     *
     * @param gradebookId The gradebook id
     * @return The course grade
     */
    public CourseGrade getCourseGrade(Long gradebookId);

    /**
     * Fetches the course grade for a gradebook and populates its non-persistent
     * statistics fields
     *
     * @param gradebookId The gradebook id
     * @return The course grade
     */
    public CourseGrade getCourseGradeWithStats(Long gradebookId);

    /**
     * Updates the values used for sorting on any course grade record where a letter
     * grade has (or hasn't) been explicitly set.  This should happen anytime a gradebook's
     * grade mapping has been modified (using true to operate on manually entered
     * course grade records) and when an existing assignment's point value changes
     * (using false, so the sort values are changed only on auto-calculated
     * course grade records).
     *
     * @param gradebookId The gradebook id
     * @param manuallyEnteredRecords Whether to update manually entered records or
     */
    public void updateCourseGradeRecordSortValues(Long gradebookId, boolean manuallyEnteredRecords);

    public double getTotalPoints(Long gradebookId);
}



