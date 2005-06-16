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

package org.sakaiproject.tool.gradebook.facades.standalone;

import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.User;

/**
 * A TeachingAssignment defines the relation between a teacher and a gradebook.
 *
 * Baseline Sakai 2.0 gradebook's requirements are minimal, and so this is more
 * of a placeholder. Fuller LMS functionality will require more data.
 */
public final class TeachingAssignmentStandalone {
    private Long id;
	private User user;
    private Gradebook gradebook;

    /**
     * The no-arg constructor is used only for instantiation by the persistence
     * layer, and can not be used in application code (without using reflection)
     */
    public TeachingAssignmentStandalone() {
        user = null;
        gradebook = null;
    }

	/**
	 */
	public TeachingAssignmentStandalone(User user, Gradebook gradebook) {
		this.user = user;
		this.gradebook = gradebook;
	}

	/**
	 * @return the user playing the instructor role
	 */
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

    public Gradebook getGradebook() {
    	return gradebook;
    }
    public void setGradebook(Gradebook gradebook) {
    	this.gradebook = gradebook;
    }
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
