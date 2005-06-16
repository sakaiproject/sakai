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

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.Role;

/**
 * Sakai2 implementation of the gradebook's Authz facade.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AuthzSakai2Impl implements Authz {
    public static final String STUDENT_PERMISSION = "gradebook.access";
    public static final String INSTRUCTOR_PERMISSION = "gradebook.maintain";

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Authz#getGradebookRole(java.lang.String, java.lang.String)
	 */
	public Role getGradebookRole(String gradebookUid, String userUid) {
         boolean isInstructor = SecurityService.unlock(INSTRUCTOR_PERMISSION, getContext());
         boolean isStudent = SecurityService.unlock(STUDENT_PERMISSION, getContext());

         if(isInstructor) {
            return Role.INSTRUCTOR;
         } else if(isStudent) {
            return Role.STUDENT;
         } else {
            return Role.NONE;
         }
	}

    private String getContext() {
        Placement placement = ToolManager.getCurrentPlacement();        
        String context = placement.getContext();
        return "/gradebook/" + context + "/main";
    }

}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
