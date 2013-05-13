package org.sakaiproject.sitestats.test.perf.mock;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.user.api.Authentication;

public class MockUsageSessionService implements UsageSessionService {

	@Override
	public UsageSession startSession(String userId, String remoteAddress,
			String userAgent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UsageSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionState getSessionState(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UsageSession getSession(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UsageSession> getSessions(List<String> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UsageSession> getSessions(String joinTable, String joinAlias,
			String joinColumn, String joinCriteria, Object[] values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSessionInactiveTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSessionLostTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<UsageSession> getOpenSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, UsageSession> getOpenSessionsByServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean login(Authentication authn, HttpServletRequest req) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean login(Authentication authn, HttpServletRequest req,
			String event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean login(String uid, String eid, String remoteaddr, String ua,
			String event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public int closeSessionsOnInvalidServers(List<String> validServerIds) {
		// TODO Auto-generated method stub
		return 0;
	}

}
