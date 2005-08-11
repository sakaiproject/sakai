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

package org.sakaiproject.tool.gradebook.facades.standalone;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.tool.gradebook.facades.User;

public class UserStandalone implements User {
	private Long id;
    private String userUid;
    private String displayName;
    private String sortName;
    private String displayUid;

	/**
	 * The no-arg constructor is used only for instantiation by the persistence
	 * layer, and can not be used in application code (without using reflection)
	 */
	public UserStandalone() {
		id = null;
		userUid = null;
		displayName = null;
		sortName = null;
		displayUid = null;
	}

	/**
	 * @param userUid unique ID returned by authentication system and passed to the user
	 * directory service
	 * @param displayName what to display when only this user is being referred to
	 * (for example, "Thomas Paine" or "Wong Kar-Wai")
	 * @param sortName what to display when users are listed in order (for example,
	 * "Paine, Thomas" or "Wong Kar-Wai")
	 * @param displayUid AKA "campus ID", a human-meaningful UID for the user (for
	 * expample, a student ID number or an institutional email address)
	 */
	public UserStandalone(String userUid, String displayName, String sortName, String displayUid) {
		this.userUid = userUid;
		this.displayName = displayName;
		this.sortName = sortName;
		this.displayUid = displayUid;
	}

	/**
	 * @return Returns the displayUid.
	 */
	public String getDisplayUid() {
		return displayUid;
	}
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @return Returns the userUid.
	 */
	public String getUserUid() {
		return userUid;
	}
	/**
	 * @return Returns the sortName.
	 */
	public String getSortName() {
		return sortName;
	}

	public Long getId() {
		return id;
	}

	public String toString() {
		return new ToStringBuilder(this).
			append("id", id).
			append("userUid", userUid).
			append("displayName", displayName).
			append("sortName", sortName).
			append("displayUid", displayUid).toString();
	}

    public boolean equals(Object o) {
        if (!(o instanceof UserStandalone)) {
            return false;
        }
        UserStandalone other = (UserStandalone)o;
        return new EqualsBuilder().
		    append(userUid, other.getUserUid()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
            append(userUid).toHashCode();
    }
}



