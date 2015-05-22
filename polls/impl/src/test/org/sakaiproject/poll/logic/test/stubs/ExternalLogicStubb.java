/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.test.stubs;



import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.test.TestDataPreload;
import org.sakaiproject.poll.model.PollRolePerms;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.tool.api.ToolSession;

public class ExternalLogicStubb implements ExternalLogic {

	public String getCurrentLocationId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentLocationReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String currentUserId = null;
	public String getCurrentUserId() {
		return currentUserId;
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		return isAllowedInLocation(permission, locationReference, getCurrentUserId());	
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference, String userRefence) {
		
		if (TestDataPreload.USER_NO_ACCEESS.equals(userRefence)) {
			//this user should always have no rights
			return false;
		}
		
		if (TestDataPreload.USER_UPDATE.equals(userRefence)) {
			return true;
		}
		
		/*
		if (userRefence.equals(TestDataPreload.USER_NO_UPDATE)) {
			if (locationReference.equals(TestDataPreload.LOCATION1_ID)) {
				if (permission.equals(TestDataPreload.QNA_UPDATE)) {
					return false;
				}
			}
		} else if (userRefence.equals(TestDataPreload.USER_UPDATE)) {
			if (locationReference.equals(LOCATION1_ID)) {
				if (permission.equals(QNA_UPDATE) || permission.equals(QNA_NEW_QUESTION) || permission.equals(QNA_NEW_CATEGORY) || permission.equals(QNA_NEW_ANSWER)) {
					return true;
				} 
			}
		} else if (userRefence.equals(USER_LOC_3_UPDATE_1)
				|| userRefence.equals(USER_LOC_3_UPDATE_2)
				|| userRefence.equals(USER_LOC_3_UPDATE_3)) {
			if (locationReference.equals(LOCATION3_ID)) {
				if (permission.equals(QNA_UPDATE) || permission.equals(QNA_NEW_QUESTION) || permission.equals(QNA_NEW_CATEGORY) || permission.equals(QNA_NEW_ANSWER)) {
					return true;
				}
			}
		} 
		*/
		return false;
	}

	public boolean isUserAdmin(String userId) {
		if ("admin".equals(userId))
			return true;
		
		return false;
	}

	public boolean isUserAdmin() {
		return isUserAdmin(getCurrentUserId());
	}

	public List<String> getSitesForUser(String userId, String permission) {
		// TODO Auto-generated method stub
		return null;
	}

	public void postEvent(String eventId, String reference, boolean modify) {
		// TODO Auto-generated method stub
		
	}

	public void registerFunction(String function) {
		// TODO Auto-generated method stub
		
	}

	public TimeZone getLocalTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getRoleIdsInRealm(String RealmId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRoleAllowedInRealm(String RoleId, String realmId, String permission) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getSiteTile(String locationReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setToolPermissions(Map<String, PollRolePerms> permMap,
			String locationReference) throws SecurityException,
			IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	public Map<String, PollRolePerms> getRoles(String locationReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSiteRefFromId(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentuserReference() {
		// TODO Auto-generated method stub
		return currentUserId;
	}

	public boolean userIsViewingAsRole() {
		// TODO Auto-generated method stub
		return false;
	}

	public void notifyDeletedOption(List<String> userEids, String siteTitle,
			String pollQuestion) {
		// TODO Auto-generated method stub
		
	}

	public String getUserEidFromId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolSession getCurrentToolSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isResultsChartEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowPublicAccess() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMobileBrowser() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String> getPermissionKeys() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void registerStatement(String pollText, Vote vote) {
        // TODO Auto-generated method stub
    }

    @Override
    public void registerStatement(String pollText, boolean newPoll) {
        // TODO Auto-generated method stub
    }

}
