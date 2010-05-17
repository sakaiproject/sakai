/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.user.api.Authentication;

public class FakeUsageSessionService implements UsageSessionService {

	public int closeSessionsOnInvalidServers(List<String> arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<UsageSession> getOpenSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, UsageSession> getOpenSessionsByServer() {
		// TODO Auto-generated method stub
		return null;
	}

	public UsageSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public UsageSession getSession(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSessionInactiveTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSessionLostTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public SessionState getSessionState(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<UsageSession> getSessions(List<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<UsageSession> getSessions(String arg0, String arg1, String arg2, String arg3, Object[] arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean login(Authentication arg0, HttpServletRequest arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean login(Authentication arg0, HttpServletRequest arg1, String arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean login(String arg0, String arg1, String arg2, String arg3, String arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout() {
		// TODO Auto-generated method stub

	}

	public UsageSession setSessionActive(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public UsageSession startSession(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public UsageSession getActiveUserSession(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, UsageSession> getActiveUserSessions(List<String> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isUserActive(String userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<String> getActiveUsers(List<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
