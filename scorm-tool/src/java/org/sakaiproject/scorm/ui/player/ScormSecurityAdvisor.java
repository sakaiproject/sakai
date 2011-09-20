package org.sakaiproject.scorm.ui.player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.tool.cover.ToolManager;

@SuppressWarnings("deprecation")
public class ScormSecurityAdvisor implements SecurityAdvisor {
	private static Log log = LogFactory.getLog(ScormSecurityAdvisor.class);
	public SecurityAdvice isAllowed(String userId, String function, String reference) {
		log.debug("isAllowed: userId="+userId+", function="+function+", reference="+reference);
		if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.launch", currentSiteReference()) || SecurityService.unlock(userId, "scorm.upload", currentSiteReference())) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_HIDDEN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.launch", currentSiteReference()) || SecurityService.unlock(userId, "scorm.upload", currentSiteReference())) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_ADD.equals(function) || ContentHostingService.AUTH_RESOURCE_WRITE_ANY.equals(function) || ContentHostingService.AUTH_RESOURCE_WRITE_OWN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.upload", currentSiteReference())) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_REMOVE_ANY.equals(function) || ContentHostingService.AUTH_RESOURCE_REMOVE_OWN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.delete", currentSiteReference())) {
				return SecurityAdvice.ALLOWED;
			}
		}
		return SecurityAdvice.PASS;
	}
	private String currentSiteReference() {
		String siteId = ToolManager.getCurrentPlacement().getContext();
		String reference = "/site/"+siteId;
		return reference;
	}
}