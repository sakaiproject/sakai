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

import java.util.Date;
import java.util.List;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;

/**
 * Manages GradableObject persistence.  Since any implementation of this
 * interface will certainly depend on other services, implementations should
 * implement GradableObjectManagerBeanIfc rather than this interface.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface GradableObjectManager {

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
     *
     * @param assignmentId The assignment ID
     * @param name The assignment's name (must be unique in the gradebook and not be null)
     * @param points The number of points possible for this assignment (must not be null)
     * @param dueDate The due date for the assignment (optional)
     */
    public void updateAssignment(Long assignmentId, String name, Double points, Date dueDate)
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
     * @param manuallyEnteredRecords Whether to
     */
    public void updateCourseGradeRecordSortValues(Long gradebookId, boolean manuallyEnteredRecords);

    public double getTotalPoints(Long gradebookId);

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
