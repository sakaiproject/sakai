/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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