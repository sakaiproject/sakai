package org.sakaiproject.tool.impl;

import org.sakaiproject.tool.api.SessionManager;

/**
 * Export some basic information about the sessions over JMX.
 * @author buckett
 *
 */
public class JMXSessionComponent {
	
	private SessionManager sessionManager;

	public int getActive5Min() {
		return sessionManager.getActiveUserCount(300);
	}
	
	public int getActive10Min() {
		return sessionManager.getActiveUserCount(600);
	}
	
	public int getActive15Min() {
		return sessionManager.getActiveUserCount(900);
	}
	
	public int getActive(int secs) {
		return sessionManager.getActiveUserCount(secs);
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}
}
