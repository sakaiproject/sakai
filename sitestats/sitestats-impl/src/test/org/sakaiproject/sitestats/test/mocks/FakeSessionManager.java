package org.sakaiproject.sitestats.test.mocks;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

public class FakeSessionManager implements SessionManager {
	private Session currentSession	= new FakeSession();

	public int getActiveUserCount(int arg0) {
		return 0;
	}

	public Session getCurrentSession() {
		return currentSession;
	}

	public String getCurrentSessionUserId() {
		return currentSession.getUserId();
	}

	public ToolSession getCurrentToolSession() {
		return null;
	}

	public Session getSession(String arg0) {
		return currentSession;
	}

	public String makeSessionId(HttpServletRequest req, Principal principal) {
		return null;
	}

	public void setCurrentSession(Session arg0) {
	}

	public void setCurrentToolSession(ToolSession arg0) {
	}

	public Session startSession() {
		return null;
	}

	public Session startSession(String arg0) {
		return null;
	}

	public List<Session> getSessions() {
		// TODO Auto-generated method stub
		return null;
	}

}
