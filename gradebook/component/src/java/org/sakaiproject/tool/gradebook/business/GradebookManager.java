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

import java.util.List;

import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Manages Gradebook persistence.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface GradebookManager {

    /**
     * Updates a gradebook's representation in persistence.
     *
     * If the gradebook's selected grade mapping has been modified, the sort
     * value on all explicitly graded course grade records must be updated when
     * the gradebook is updated.
     *
     * A gradebook's selected grade mapping may only change (to a different kind
     * of mapping) if there are no explicitly graded course grade records.
     *
     * @param gradebook The gradebook to update
     * @throws StaleObjectModificationException
     */
    public void updateGradebook(Gradebook gradebook) throws StaleObjectModificationException;

    /**
     * Fetches a gradebook based on its surrogate key
     *
     * @param id The ID of the gradebook
     * @return The gradebook
     */
    public Gradebook getGradebook(Long id);

    /**
     * Internal services use a Long ID to identify a gradebook.
     * External facades use a String UID instead. This method
     * translates.
     */
    public String getGradebookUid(Long id);

    /**
     * Fetches a gradebook based on its unique string id
     *
     * @param uid The UID of the gradebook
     * @return The gradebook
     */
    public Gradebook getGradebook(String uid) throws GradebookNotFoundException;

    /**
     * Fetches all of the students "enrolled" in this gradebook
     *
     * @param gradebookId The gradebook ID
     * @return A List of all enrolled students
     */
    public List getEnrollments(Long gradebookId);

    /**
     * Removes an assignment from a gradebook.  The assignment should not be
     * deleted, but the assignment and all grade records associated with the
     * assignment should be ignored by the application.  A removed assignment
     * should not count toward the total number of points in the gradebook.
     *
     * This method is obviously the oddball in this class, and should be in
     * GradeManager.  However, since we need to do course grade
     * recalculations, we need to have a reference to the GradeManager.  This
     * is the easiest place to grab both of the needed managers.
     * 
     * TODO Refactor manager split so grades and gradable objects can be more
     * easiy manipulated.
     * 
     * @param assignmentId The assignment id
     */
    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException;

}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
