/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.util.SakaiToolData;

public class DeveloperHelperServiceStub implements DeveloperHelperService {

	public <T> T cloneBean(T arg0, int arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T convert(Object arg0, Class<T> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void copyBean(Object arg0, Object arg1, int arg2, String[] arg3,
			boolean arg4) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Object> decodeData(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String encodeData(Object arg0, String arg1, String arg2,
			Map<String, Object> arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean entityExists(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object fetchEntity(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void fireEvent(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public <T> T getConfigurationSetting(String arg0, T arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getCurrentLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentLocationId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentLocationReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentToolReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentUserId() {
		// TODO Auto-generated method stub
		return "admin";
	}

	public String getCurrentUserReference() {
		// TODO Auto-generated method stub
		return "/usr/admin";
	}

	public Set<String> getEntityReferencesForUserAndPermission(String arg0,
			String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityURL(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocationIdFromRef(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocationReferenceURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPortalURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStartingLocationReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public SakaiToolData getToolData(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolIdFromToolRef(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolViewURL(String arg0, String arg1,
			Map<String, String> arg2, String arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeLocationReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeLocationURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserIdFromRef(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserRefFromUserEid(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserRefFromUserId(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getUserReferencesForEntityReference(String arg0,
			String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEntityRequestInternal(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserAdmin(String arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isUserAllowedInEntityReference(String arg0, String arg1,
			String arg2) {
		// TODO Auto-generated method stub
		return true;
	}

	public List<String> populate(Object arg0, Map<String, Object> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerPermission(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public String restoreCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public String setCurrentUser(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMessage(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
