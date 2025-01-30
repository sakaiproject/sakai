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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_ADD;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_HIDDEN;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_READ;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_REMOVE_ANY;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_REMOVE_OWN;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_WRITE_ANY;
import static org.sakaiproject.content.api.ContentHostingService.AUTH_RESOURCE_WRITE_OWN;
import static org.sakaiproject.scorm.api.ScormConstants.PERM_DELETE;
import static org.sakaiproject.scorm.api.ScormConstants.PERM_LAUNCH;
import static org.sakaiproject.scorm.api.ScormConstants.PERM_UPLOAD;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public class ScormSecurityAdvisor implements SecurityAdvisor
{
	private static final SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);
	private static final ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);

	@Override
	public SecurityAdvice isAllowed(String userId, String function, String reference)
	{
		log.debug("isAllowed: userId={}, function={}, reference={}", userId, function, reference);
		if (AUTH_RESOURCE_READ.equals(function) && (unlock(userId, PERM_LAUNCH) || unlock(userId, PERM_UPLOAD)))
		{
			return SecurityAdvice.ALLOWED;
		}
		else if (AUTH_RESOURCE_HIDDEN.equals(function) && (unlock(userId, PERM_LAUNCH) || unlock(userId, PERM_UPLOAD)))
		{
			return SecurityAdvice.ALLOWED;
		}
		else if ((AUTH_RESOURCE_ADD.equals(function) || AUTH_RESOURCE_WRITE_ANY.equals(function) || AUTH_RESOURCE_WRITE_OWN.equals(function)) && unlock(userId, PERM_UPLOAD))
		{
			return SecurityAdvice.ALLOWED;
		}
		else if ((AUTH_RESOURCE_REMOVE_ANY.equals(function) || AUTH_RESOURCE_REMOVE_OWN.equals(function)) && unlock(userId, PERM_DELETE))
		{
			return SecurityAdvice.ALLOWED;
		}

		return SecurityAdvice.PASS;
	}

	private boolean unlock(String userID, String fuction)
	{
		return securityService.unlock(userID, fuction, currentSiteReference());
	}

	private String currentSiteReference()
	{
		String siteId = toolManager.getCurrentPlacement().getContext();
		String reference = "/site/" + siteId;
		return reference;
	}
}