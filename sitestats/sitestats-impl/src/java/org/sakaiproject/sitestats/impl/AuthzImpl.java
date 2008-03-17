package org.sakaiproject.sitestats.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.Authz;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;


public class AuthzImpl implements Authz {
	private static Log				LOG								= LogFactory.getLog(AuthzImpl.class);

	/** Sakai services */
	private UserDirectoryService	M_uds;
	private SecurityService			M_secs;
	private SessionManager			M_sess;

	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setUserService(UserDirectoryService userService) {
		this.M_uds = userService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.M_secs = securityService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.M_sess = sessionManager;
	}

	public void init() {
		LOG.info("init()");		
		// register functions
		FunctionManager.registerFunction(PERMISSION_SITESTATS_VIEW);
		FunctionManager.registerFunction(PERMISSION_SITESTATS_ADMIN_VIEW);
	}

	// ################################################################
	// Public methods
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Authz#isUserAbleToViewSiteStats(java.lang.String)
	 */
	public boolean isUserAbleToViewSiteStats(String siteId) {
		return isSuperUser() || hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_VIEW);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Authz#isUserAbleToViewSiteStatsAdmin(java.lang.String)
	 */
	public boolean isUserAbleToViewSiteStatsAdmin(String siteId) {
		return isSuperUser() || hasPermission(SiteService.siteReference(siteId), PERMISSION_SITESTATS_ADMIN_VIEW);
	}

	// ################################################################
	// Private methods
	// ################################################################
	private boolean isSuperUser() {
		return M_secs.isSuperUser();
	}

	private boolean hasPermission(String reference, String permission) {
		return M_secs.unlock(permission, reference);
	}
}
