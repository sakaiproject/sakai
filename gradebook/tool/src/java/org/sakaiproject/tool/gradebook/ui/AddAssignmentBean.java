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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class AddAssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(AddAssignmentBean.class);

	private Assignment assignment;

	protected void init() {
        if(assignment == null) {
            assignment = new Assignment();
        }
	}

	public String saveNewAssignment() {
		Long assignmentId;
		try {
			assignmentId = getGradableObjectManager().createAssignment(getGradebookId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate());
            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));
		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
			return "failure";
        } catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_locking_failure"));
            return "failure";
		}
		return "overview";
	}

    public Assignment getAssignment() {
        return assignment;
    }
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
