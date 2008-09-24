package uk.ac.lancs.e_science.profile2.impl;

import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import org.apache.log4j.Logger;
import org.sakaiproject.tool.api.SessionManager;



public class SakaiProxyImpl implements SakaiProxy {

	private transient Logger log = Logger.getLogger(SakaiProxyImpl.class);
	
	private SessionManager sessionManager;
	
	
	public void init() {
		log.debug("init");
	}
	
	
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	public String getMessage() {
		return "crap";
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}
}
