/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.impl.logic;

import java.util.ArrayList;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.poll.api.logic.ExternalLogic;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.api.FormattedText;

import static org.sakaiproject.poll.api.PollConstants.*;

@Slf4j
public class ExternalLogicImpl implements ExternalLogic {
	
	private static final String USER_ENTITY_PREFIX = "/user/";
	
    @Setter private LearningResourceStoreService learningResourceStoreService;
	@Setter private DeveloperHelperService developerHelperService;
    @Setter private AuthzGroupService authzGroupService;
    @Setter private EmailTemplateService emailTemplateService;
    @Setter private SiteService siteService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SessionManager sessionManager;
	@Setter private UserTimeService userTimeService;
	@Setter private FormattedText formattedText;

    @Setter private ArrayList<String> emailTemplates;

	public String getCurrentLocationId() {
		return developerHelperService.getCurrentLocationId();
	}

	public boolean isUserAdmin(String userId) {
		return developerHelperService.isUserAdmin(USER_ENTITY_PREFIX + userId);
	}

	public boolean isUserAdmin() {
		return isUserAdmin(getCurrentUserId());
	}

	public String getCurrentUserId() {
		return developerHelperService.getCurrentUserId();
	}

    public String getCurrentLocationReference() {
		log.debug("getCurrentLocationReference");
        return developerHelperService.getCurrentLocationReference();
	}
	
	public String getCurrentToolURL()
	{
		return serverConfigurationService.getPortalUrl() + getCurrentLocationReference() + "/tool/" + getCurrentToolSession().getPlacementId();
	}

	public boolean isAllowedInLocation(String permission, String locationReference, String userReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference + " , " + userReference);
		return developerHelperService.isUserAllowedInEntityReference(userReference, permission, locationReference);
	}

	public boolean isAllowedInLocation(String permission,
			String locationReference) {
		log.debug("isAllowed in location( " + permission + " , " + locationReference);
		return isAllowedInLocation(permission, locationReference, developerHelperService.getCurrentUserReference());
	}

    public void init() {
    	log.info("init()");
    }

    public TimeZone getLocalTimeZone() {
		return userTimeService.getLocalTimeZone();
	}

    public ToolSession getCurrentToolSession() {
		return sessionManager.getCurrentToolSession();
	}

    public boolean isShowPublicAccess() {
		return serverConfigurationService.getBoolean("poll.allow.public.access", false);
	}

    @Override
    public int getNumberUsersCanVote() {
    	ArrayList<String> siteGroupRefs = new ArrayList<>();
    	siteGroupRefs.add(siteService.siteReference(developerHelperService.getCurrentLocationId()));
    	return (authzGroupService.getUsersIsAllowed(PERMISSION_VOTE, siteGroupRefs).size());
    }

    @Override
    public String processFormattedText(String text, StringBuilder errorMessages) {
        return formattedText.processFormattedText(text, errorMessages);
    }

    @Override
    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
           boolean replaceWhitespaceTags) {
        return formattedText.processFormattedText(strFromBrowser, errorMessages, checkForEvilTags, replaceWhitespaceTags);
    }

}
