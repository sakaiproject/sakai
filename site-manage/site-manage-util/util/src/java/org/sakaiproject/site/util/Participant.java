/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.site.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class Participant {
	protected String NULL_STRING = "";
	
	public String name = NULL_STRING;

	public String displayName = NULL_STRING;

	// Note: uniqname is really a user ID
	public String uniqname = NULL_STRING;

	public String role = NULL_STRING;

	/** role from provider */
	public String providerRole = NULL_STRING;

	/** The member credits */
	public String credits = NULL_STRING;

	/** The section */
	public String section = NULL_STRING;

	private Set sectionEidList;
	
	/** The regestration id */
	public String regId = NULL_STRING;

	/** removeable if not from provider */
	public boolean removeable = true;
	
	/** the status, active vs. inactive */
	public boolean active = true;

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		if (NULL_STRING.equals(displayName)) {
			displayName = name;
		} else {
			try {
				User user = UserDirectoryService.getUser(uniqname);
				displayName = user.getDisplayName();
			} catch (UserNotDefinedException e) {
				displayName = uniqname;
			}
		}
		return displayName;
	}

	public String getUniqname() {
		return uniqname;
	}

	public String getRole() {
		return role;
	} // cast to Role

	public String getProviderRole() {
		return providerRole;
	}

	public boolean isRemoveable() {
		return removeable;
	}
	
	public boolean isActive()  {
		return active;
	}

	// extra info from provider
	public String getCredits() {
		return credits;
	} // getCredits

	public String getSection() {
		if (sectionEidList == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		Iterator it = sectionEidList.iterator();
		for (int i = 0; i < sectionEidList.size(); i ++) {
			String sectionEid = (String)it.next();
			if (i > 0)
				sb.append(",<br />");
			sb.append(sectionEid);
		}
				
		return sb.toString();
	} // getSection
	
	public Set getSectionEidList() {
		if (sectionEidList == null)
			sectionEidList = new HashSet();
		
		return sectionEidList;
	}
	
	public void addSectionEidToList(String eid) {
		if (sectionEidList == null)
			sectionEidList = new HashSet();
			
			sectionEidList.add(eid);
	}

	public String getRegId() {
		return regId;
	} // getRegId

	/**
	 * Access the user eid, if we can find it - fall back to the id if not.
	 * 
	 * @return The user eid.
	 */
	public String getEid() {
		try {
			return UserDirectoryService.getUserEid(uniqname);
		} catch (UserNotDefinedException e) {
			return uniqname;
		}
	}

	/**
	 * Access the user display id, if we can find it - fall back to the id
	 * if not.
	 * 
	 * @return The user display id.
	 */
	public String getDisplayId() {
		try {
			User user = UserDirectoryService.getUser(uniqname);
			return user.getDisplayId();
		} catch (UserNotDefinedException e) {
			return uniqname;
		}
	}

} // Participant
