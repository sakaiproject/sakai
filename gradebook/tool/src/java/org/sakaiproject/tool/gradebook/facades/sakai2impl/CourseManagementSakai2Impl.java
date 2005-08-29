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

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.User;

/**
 * Sakai2 implementation of the gradebook CourseManagement API
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseManagementSakai2Impl implements CourseManagement {
    private static final Log log = LogFactory.getLog(CourseManagementSakai2Impl.class);

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#getEnrollments(java.lang.String)
	 */
	public Set getEnrollments(String gradebookUid) {
        List sakaiUsers = getSakaiUsers();
        Set gbUsers = new HashSet();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
            org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
            gbUsers.add(new UserSakai2Impl(sakaiUser));
        }
        return gbUsers;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#getEnrollmentsSize(java.lang.String)
	 */
	public int getEnrollmentsSize(String gradebookUid) {
        return getSakaiUsers().size();
    }

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#findEnrollmentsByStudentNameOrDisplayUid(java.lang.String, java.lang.String)
	 */
	public Set findEnrollmentsByStudentNameOrDisplayUid(String gradebookUid,
			String studentNameQuery) {
        List sakaiUsers = getSakaiUsers();
        Set gbUsers = new HashSet();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
            org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
            // TODO Make sure this is the correct search rule
            if(sakaiUser.getFirstName().toLowerCase().startsWith(studentNameQuery.toLowerCase()) ||
                    sakaiUser.getLastName().toLowerCase().startsWith(studentNameQuery.toLowerCase()) ||
                    sakaiUser.getEmail().toLowerCase().startsWith(studentNameQuery.toLowerCase())) {
                gbUsers.add(new UserSakai2Impl(sakaiUser));
            }
        }
        return gbUsers;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#findEnrollmentsPagedBySortName(java.lang.String, int, int, boolean)
	 */
	public List findEnrollmentsPagedBySortName(String gradebookUid,
			int startRange, int rangeMaximum, boolean isAscending) {
        // This is currently not being used by the gradebook tool
		return null;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#findEnrollmentsPagedByDisplayUid(java.lang.String, int, int, boolean)
	 */
	public List findEnrollmentsPagedByDisplayUid(String gradebookUid,
			int startRange, int rangeMaximum, boolean isAscending) {
        List sakaiUsers = getSakaiUsers();
        List gbUsers = new ArrayList();
        for(Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
            org.sakaiproject.service.legacy.user.User sakaiUser = (org.sakaiproject.service.legacy.user.User)iter.next();
            gbUsers.add(new UserSakai2Impl(sakaiUser));
        }

        // Sort the enrollments by user uid (in this case, the user's email)
        Collections.sort(gbUsers);

        // Return the sub list matching the start and range
        int max;
        max = gbUsers.size() > (startRange + rangeMaximum) ? startRange + rangeMaximum : gbUsers.size();
        return gbUsers.subList(startRange, max);
	}

	/**
	 * @return The list of sakai users, or an empty list if it's unavailable
	 */
	private List getSakaiUsers() {
        List access = SecurityService.unlockUsers(AuthzSakai2Impl.STUDENT_PERMISSION, getContext());

        // Users with maintain permission should not be included in the list of student users
        List maintain = SecurityService.unlockUsers(AuthzSakai2Impl.INSTRUCTOR_PERMISSION, getContext());
        access.removeAll(maintain);

        return access;
    }

    /**
     * @return The current sakai context
     */
    private String getContext() {
        Placement placement = ToolManager.getCurrentPlacement();
        String context = placement.getContext();
        return "/gradebook/" + context + "/main";
    }

    /**
     * @see org.sakaiproject.tool.gradebook.facades.CourseManagement#getUser(java.lang.String)
     */
    public User getUser(String userUid) throws UnknownUserException {
        try {
            org.sakaiproject.service.legacy.user.User sakaiUser = UserDirectoryService.getUser(userUid);
            return new UserSakai2Impl(sakaiUser);
        } catch (IdUnusedException e) {
            throw new UnknownUserException("Unknown uid: " + userUid);
        }
    }

}


