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

package org.sakaiproject.tool.gradebook.facades.test;

import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.Role;

/**
 * An in-memory stub implementation of Authz, used for testing.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AuthzTestImpl implements Authz {

    private Boolean instructor; // Spring managed flag for testing, not defined in the interface

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Authz#getGradebookRole(java.lang.String, java.lang.String)
	 */
	public Role getGradebookRole(String gradebookUid, String userUid) {
        if(instructor == null) {
            return Role.NONE;
        } else if(instructor.booleanValue()) {
            return Role.INSTRUCTOR;
        } else {
            return Role.STUDENT;
        }
	}

	/**
	 * @return Returns the instructor.
	 */
	public Boolean getInstructor() {
		return instructor;
	}
	/**
	 * @param instructor The instructor to set.
	 */
	public void setInstructor(Boolean instructor) {
		this.instructor = instructor;
	}
}


