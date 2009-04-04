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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.TimeZone;

import org.sakaiproject.poll.logic.ExternalLogic;

public class ExternalLogicStubb implements ExternalLogic {

	public String getCurrentLocationId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentLocationReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference, String userRefence) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserAdmin(String userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserAdmin() {
		// TODO Auto-generated method stub
		return false;
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

}
