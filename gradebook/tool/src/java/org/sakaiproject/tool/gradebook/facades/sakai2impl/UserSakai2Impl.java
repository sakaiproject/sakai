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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Enrollment;

/**
 * An implementation of User and Enrollment for the gradebook, based on the
 * sakai2 representation of a User.  Note that this class implements both
 * Enrollment and User, since an enrollment in the sakai 2.0 gradebook has no
 * additional semantics beyond a simple User.  This should change in the near
 * future, when information about a student's status in a class (waitlisted,
 * auditing, dropped, etc) will influence gradebook calculations such as grade
 * curving.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class UserSakai2Impl implements org.sakaiproject.tool.gradebook.facades.User, Enrollment, Comparable {
    private static final Log log = LogFactory.getLog(UserSakai2Impl.class);
    
    private org.sakaiproject.service.legacy.user.User sakaiUser;
    
    /**
     * Create a UserSakai2Impl for consumption in the gradebook based on sakai's
     * User object.
     *  
     * @param sakaiUser
     */
    public UserSakai2Impl(org.sakaiproject.service.legacy.user.User sakaiUser) {
        this.sakaiUser = sakaiUser;
    }

    /**
	 * @see org.sakaiproject.tool.gradebook.facades.User#getUserUid()
	 */
	public String getUserUid() {
		return sakaiUser.getId();
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.User#getSortName()
	 */
	public String getSortName() {
        return sakaiUser.getSortName();
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.User#getDisplayUid()
	 */
	public String getDisplayUid() {
        return sakaiUser.getEmail();
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.User#getDisplayName()
	 */
	public String getDisplayName() {
        return sakaiUser.getDisplayName();
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.facades.Enrollment#getUser()
	 */
	public org.sakaiproject.tool.gradebook.facades.User getUser() {
        return this;
	}

	/**
     * Compares based on email address.  If we need other ways to sort this, we
     * can add comparators.
     * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
        return this.getDisplayUid().compareToIgnoreCase(((UserSakai2Impl)o).getDisplayUid());
	}

}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
