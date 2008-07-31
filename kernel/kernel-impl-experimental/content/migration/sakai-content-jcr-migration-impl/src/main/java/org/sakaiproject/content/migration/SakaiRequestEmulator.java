package org.sakaiproject.content.migration;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

/*
 * This is a class you can extend if you need to emulate Sakai Requests.
 */
public class SakaiRequestEmulator {
	
	protected SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	protected AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public final String SUPER_USER = "admin";
	protected final String CURRENT_USER_MARKER = "originalTestUser";
	private boolean hijackedSession = false;

	/*
	 * This stuff below supports the emulation of a request, there is a lot of duplication
	 * because things are not well exposed in the RequestFilter -AZ
	 */

	protected final static String CURRENT_REMOTE_USER = "org.sakaiproject.util.RequestFilter.remote_user";
	protected final static String CURRENT_CONTEXT = "org.sakaiproject.util.RequestFilter.context";
	protected final static String CURRENT_SESSION = "org.sakaiproject.api.kernel.session.current";
	protected final static String CURRENT_TOOL_SESSION = "org.sakaiproject.api.kernel.session.current.tool";
	protected final static String CURRENT_HTTP_SESSION = "org.sakaiproject.util.RequestFilter.http_session";

	protected Boolean currentRemoteUser = false;
	protected String currentContext = null;
	protected Session currentSession = null;
	protected ToolSession currentToolSession = null;
	protected Integer currentHttpSession = 0;

	private boolean emulatingRequest = false;

	/**
	 * Change the current user for a test or test thread to another user,
	 * this will store the original user in the session and then restore it 
	 * at the end so it is safe to call this in setup
	 * (though you can call it from anywhere), Note that it will only restore the user which
	 * was the current user when the method was originally called
	 * @param userId the internal Sakai user id (not the username) of the user to switch
	 * to as the current user, you can look this up using the UserDirectoryService or
	 * create a user just for testing
	 */
	public void setTestUser(String userId) {
		Session currentSession = sessionManager.getCurrentSession();
		if (currentSession == null) {
			// start a session if none is around
			if (userId == null) {
				currentSession = sessionManager.startSession();
			} else {
				currentSession = sessionManager.startSession(userId);
			}
		} else {
			hijackedSession = true;
		}
		if (currentSession.getAttribute(CURRENT_USER_MARKER) == null) {
			// only set this if it is not already set
			String currentUserId = currentSession.getUserId();
			if (currentUserId == null) {
				currentUserId = "";
			}
			currentSession.setAttribute(CURRENT_USER_MARKER, currentUserId);
		}
		currentSession.setUserId(userId);
		currentSession.setActive();
		sessionManager.setCurrentSession(currentSession);
		authzGroupService.refreshUser(userId);
	}
	
	/**
	 * Emulate the start of a Sakai Request, this will be the same as if the code after this is running
	 * inside a the Saaki request filter (as if it were wrapped), this pairs with {@link #endEmulatedRequest()}
	 * to wrap a block of code
	 * @param userId set the current user id (the internal id) for this request, note that setting this to null will ensure there is no current
	 * user set for the emulated request and will clear the current user, use the {@link #SUPER_USER} constant to specify
	 * that this request should run as a super user
	 */
	public void startEmulatedRequest(String userId) {
		// only emulate if we are not already emulating the request
		if (!emulatingRequest) {
			emulatingRequest = true;
			// store the current values from this threads ThreadLocalManager
			currentRemoteUser = (Boolean) ThreadLocalManager.get(CURRENT_REMOTE_USER);
			currentContext = (String) ThreadLocalManager.get(CURRENT_CONTEXT);
			currentSession = (Session) ThreadLocalManager.get(CURRENT_SESSION);
			currentToolSession = (ToolSession) ThreadLocalManager.get(CURRENT_TOOL_SESSION);
			currentHttpSession = (Integer) ThreadLocalManager.get(CURRENT_HTTP_SESSION);

			if (userId != null) {
				setTestUser(userId);
			}

			// Sakai request filter guarantees a session
			Session cs = sessionManager.getCurrentSession();
			if (cs == null) {
				Session s = sessionManager.startSession();
				sessionManager.setCurrentSession(s);
			}
			// Sakai request filter guarantees a tool session with placementId (if there is a placement), we will assume there isn't for now
//			String placementId = "";
//			ToolSession toolSession = toolSession = currentSession.getToolSession(placementId);
//			sessionManager.setCurrentToolSession(toolSession);
		}
	}

	/**
	 * Emulate the end of a Sakai Request, this should be run to clear out the request data 
	 */
	public void endEmulatedRequest() {
		// only clear if we are emulating a request already
		if (emulatingRequest) {
			emulatingRequest = false;
			// wipe out all stored TL cache data
			ThreadLocalManager.clear();
			// restore the current values
			ThreadLocalManager.set(CURRENT_REMOTE_USER, currentRemoteUser);
			ThreadLocalManager.set(CURRENT_CONTEXT, currentContext);
			ThreadLocalManager.set(CURRENT_SESSION, currentSession);
			ThreadLocalManager.set(CURRENT_TOOL_SESSION, currentToolSession);
			ThreadLocalManager.set(CURRENT_HTTP_SESSION, currentHttpSession);
		}
	}
}
