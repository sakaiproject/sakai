/**********************************************************************************
*
* $URL$
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.util.Date;

/**
 * This is the externally exposed definition of a Gradebook assignment.
 * The Course Grade is not considered an assignment.
 */
public interface Assignment {
	public String getGradebookUid();

	/**
	 * @return Returns the name of the assignment. The assignment name is unique among
	 *         currently defined assignments. However, it is not a safe UID for persistance.
	 *         An assignment can be renamed. Also, an assignment can be deleted and a
	 *         new assignment can be created re-using the old name.
	 */
	public String getName();

	/**
	 * @return Returns the total points the assignment is worth.
	 */
	public Double getPoints();

	/**
	 * @return Returns the due date for the assignment, or null if none is defined.
	 */
	public Date getDueDate();

	/**
	 * @return Returns true if the assignment is maintained by some software
	 *         other than the Gradebook itself.
	 */
	public boolean isExternallyMaintained();
}
