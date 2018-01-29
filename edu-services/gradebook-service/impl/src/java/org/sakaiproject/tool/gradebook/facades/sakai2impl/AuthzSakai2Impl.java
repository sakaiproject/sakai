/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.sections.AuthzSectionsImpl;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * An implementation of Gradebook-specific authorization needs based
 * on a combination of fine-grained site-scoped Sakai permissions and the
 * shared Section Awareness API. This is a transtional stage between
 * coarse-grained site-and-role-based authz and our hoped-for fine-grained
 * role-determined group-scoped authz.
 */
@Slf4j
public class AuthzSakai2Impl extends AuthzSectionsImpl implements Authz {
    public static final String
    	PERMISSION_GRADE_ALL = "gradebook.gradeAll",
    	PERMISSION_GRADE_SECTION = "gradebook.gradeSection",
    	PERMISSION_EDIT_ASSIGNMENTS = "gradebook.editAssignments",
    	PERMISSION_VIEW_OWN_GRADES = "gradebook.viewOwnGrades",
        PERMISSION_VIEW_STUDENT_NUMBERS = "gradebook.viewStudentNumbers";

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

        if(!registered.contains(PERMISSION_VIEW_STUDENT_NUMBERS)) {
            FunctionManager.registerFunction(PERMISSION_VIEW_STUDENT_NUMBERS);
        }
    }

	public boolean isUserAbleToGrade(String gradebookUid) {
		return (hasPermission(gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(gradebookUid, PERMISSION_GRADE_SECTION));
	}
	
	public boolean isUserAbleToGrade(String gradebookUid, String userUid) {
	    try {
	        User user = UserDirectoryService.getUser(userUid);
	        return (hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL) || hasPermission(user, gradebookUid, PERMISSION_GRADE_SECTION));
	    } catch (UserNotDefinedException unde) {
	        log.warn("User not found for userUid: " + userUid);
	        return false;
	    }

	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		return hasPermission(gradebookUid, PERMISSION_GRADE_ALL);
	}
	
	public boolean isUserAbleToGradeAll(String gradebookUid, String userUid) {
	    try {
	        User user = UserDirectoryService.getUser(userUid);
	        return hasPermission(user, gradebookUid, PERMISSION_GRADE_ALL);
	    } catch (UserNotDefinedException unde) {
	        log.warn("User not found for userUid: " + userUid);
	        return false;
	    }
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
	
	public boolean isUserAbleToViewStudentNumbers(String gradebookUid)
	{
		return hasPermission(gradebookUid, PERMISSION_VIEW_STUDENT_NUMBERS);
	}

	private boolean hasPermission(String gradebookUid, String permission) {
		return SecurityService.unlock(permission, SiteService.siteReference(gradebookUid));
	}
	
	private boolean hasPermission(User user, String gradebookUid, String permission) {
	    return SecurityService.unlock(user, permission, SiteService.siteReference(gradebookUid));
	}

}
