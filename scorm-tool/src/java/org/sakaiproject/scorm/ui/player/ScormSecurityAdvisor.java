package org.sakaiproject.scorm.ui.player;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.cover.ContentHostingService;

public class ScormSecurityAdvisor implements SecurityAdvisor {
	public SecurityAdvice isAllowed(String userId, String function, String reference) {
		if (ContentHostingService.AUTH_RESOURCE_READ.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.launch", reference)) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_HIDDEN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.launch", reference) || SecurityService.unlock(userId, "scorm.upload", reference)) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_ADD.equals(function) || ContentHostingService.AUTH_RESOURCE_WRITE_ANY.equals(function) || ContentHostingService.AUTH_RESOURCE_WRITE_OWN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.upload", reference)) {
				return SecurityAdvice.ALLOWED;
			}
		} else if (ContentHostingService.AUTH_RESOURCE_REMOVE_ANY.equals(function) || ContentHostingService.AUTH_RESOURCE_REMOVE_OWN.equals(function)) {
			if (SecurityService.unlock(userId, "scorm.delete", reference)) {
				return SecurityAdvice.ALLOWED;
			}
		}
		return SecurityAdvice.PASS;
	}
}