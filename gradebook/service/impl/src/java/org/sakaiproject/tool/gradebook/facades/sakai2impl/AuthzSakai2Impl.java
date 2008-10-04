/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.section.api.facade.Role;

import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.sections.AuthzSectionsImpl;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on a combination of fine-grained site-scoped Sakai permissions and the
 * shared Section Awareness API. This is a transtional stage between
 * coarse-grained site-and-role-based authz and our hoped-for fine-grained
 * role-determined group-scoped authz.
 */
public class AuthzSakai2Impl extends AuthzSectionsImpl implements Authz {
    private static final Log log = LogFactory.getLog(AuthzSakai2Impl.class);

    public static final String
    	PERMISSION_GRADE_ALL = "gradebook.gradeAll",
    	PERMISSION_GRADE_SECTION = "gradebook.gradeSection",
    	PERMISSION_EDIT_ASSIGNMENTS = "gradebook.editAssignments",
    	PERMISSION_VIEW_OWN_GRADES = "gradebook.viewOwnGrades";

    /**
     * Perform authorization-specific framework initializations for the Gradebook.
     */
    public void init() {
        Collection registered = FunctionManager.getInstance().getRegisteredFunctions("gradebook");
        if(!registered.contains(PERMISSION_GRADE_ALL)) {
            FunctionManager.registerFunction(PERMISSION_GRADE_ALL);
        }

        if(!registered.contains(PERMISSION_GRADE_SECTION)) {
            FunctionManager.registerFunction(PERMISSION_GRADE_SECTION);
        }

        if(!registered.contains(PERMISSION_EDIT_ASSIGNMENTS)) {
            FunctionManager.registerFunction(PERMISSION_EDIT_ASSIGNMENTS);
        }

        if(!registered.contains(PERMISSION_VIEW_OWN_GRADES)) {
            FunctionManager.registerFunction(PERMISSION_VIEW_OWN_GRADES);
        }
    }

	public boolean isUserAbleToGrade(String gradebookUid) {
		return (hasPermission(gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(gradebookUid, PERMISSION_GRADE_SECTION));
	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_GRADE_ALL);
	}

	/**
	 * When group-scoped permissions are available, this is where
	 * they will go. My current assumption is that the call will look like:
	 *
	 *   return hasPermission(sectionUid, PERMISSION_GRADE_ALL);
	 */
	public boolean isUserAbleToGradeSection(String sectionUid) {
		return getSectionAwareness().isSectionMemberInRole(sectionUid, getAuthn().getUserUid(), Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_EDIT_ASSIGNMENTS);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_VIEW_OWN_GRADES);
	}

	private boolean hasPermission(String gradebookUid, String permission) {
		return SecurityService.unlock(permission, SiteService.siteReference(gradebookUid));
	}

}
