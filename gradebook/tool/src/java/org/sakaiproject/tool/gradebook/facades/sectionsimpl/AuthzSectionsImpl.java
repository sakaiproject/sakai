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

package org.sakaiproject.tool.gradebook.facades.sectionsimpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;

import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.Role;


/**
 * An implementation of Gradebook-specific authorization needs based
 * on the shared Section Awareness API.
 */
public class AuthzSectionsImpl extends AbstractSectionsImpl implements Authz {
    private static final Log log = LogFactory.getLog(AuthzSectionsImpl.class);

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Authz#getGradebookRole(java.lang.String, java.lang.String)
	 */
	public Role getGradebookRole(final String gradebookUid, final String userUid) {
		// TODO Either re-do Gradebook logic to more efficiently deal with lack of a Role object, or
		// request that SectionAwareness add a "getRole(context, user)" method.

		if (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, org.sakaiproject.api.section.facade.Role.INSTRUCTOR)) {
			return Role.INSTRUCTOR;
		} else if (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, org.sakaiproject.api.section.facade.Role.STUDENT)) {
			return Role.STUDENT;
		} else {
			// No TAs yet.
			return Role.NONE;
		}
	}

}



