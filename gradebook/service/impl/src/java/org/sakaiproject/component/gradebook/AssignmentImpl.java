/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.component.gradebook;

import java.util.*;

import org.sakaiproject.tool.gradebook.Assignment;

public class AssignmentImpl implements org.sakaiproject.service.gradebook.shared.Assignment {
	private Assignment assignment;

	AssignmentImpl(Assignment assignment) {
		this.assignment = assignment;
	}

	public String getGradebookUid() {
		return assignment.getGradebook().getUid();
	}

	public String getName() {
		return assignment.getName();
	}

	public Double getPoints() {
		return assignment.getPointsPossible();
	}

	/**
	 * @return Returns the due date for the assignment, or null if none is defined.
	 */
	public Date getDueDate() {
		return assignment.getDueDate();
	}

	/**
	 * @return Returns true if the assignment is maintained by some software
	 *         other than the Gradebook itself.
	 */
	public boolean isExternallyMaintained() {
		return assignment.isExternallyMaintained();
	}

}
