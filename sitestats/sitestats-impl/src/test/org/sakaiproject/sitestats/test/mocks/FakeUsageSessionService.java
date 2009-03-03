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

	public List getOpenSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getOpenSessionsByServer() {
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

	public List getSessions(List arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getSessions(String arg0, String arg1, String arg2, String arg3, Object[] arg4) {
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

}
