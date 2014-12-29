/*
 * Copyright Leidse Onderwijsinstellingen. All Rights Reserved.
 */

package org.sakaiproject.portal.service;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalChatPermittedHelper;
import org.sakaiproject.site.api.SiteService;

/**
 * Implementation of the portal chat permitted helper.
 * 
 * @author Tania Tritean, ISDC!
 * @author Adrian Fish, Lancaster University
 */
public class PortalChatPermittedHelperImpl implements PortalChatPermittedHelper {

	private static final String	PORTAL_CHAT_PERMITTED	= "portal.chat.permitted";
	
	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private FunctionManager functionManager;
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private boolean securedByUser = false;

	public void init() {
		this.functionManager.registerFunction(PORTAL_CHAT_PERMITTED);
		securedByUser = serverConfigurationService.getBoolean("portal.chat.securedByUser", false);
	}

	/**
	 * Checks if the user has permissions to use the chat. The permission is
	 * set in the user's MyWorkspace site.
	 * 
	 * @param userId The user we want to check
	 * @return true if the user has portal.chat.permitted set in their user site, false otherwise.
	 */
	public boolean checkChatPermitted(String userId) {
		
		if(securedByUser) {
			
			if (userId != null && !userId.equals("")) {
				String userSiteId = siteService.getUserSiteId(userId);
				return securityService.unlock(userId, PORTAL_CHAT_PERMITTED, "/site/" + userSiteId);
			} else {
				return false;
			}
		} else {
			
			// We're not securing by user. Portal chat is available to everybody.
			return true;
		}
	}
}
