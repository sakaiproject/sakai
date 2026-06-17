/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.service;

import static org.sakaiproject.poll.api.PollConstants.PERMISSION_ADD;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_EDIT_ANY;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_EDIT_OWN;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollPermissionsService {

    private static final String PERMISSION_SITE_UPDATE = "site.upd";

    private final SecurityService securityService;
    private final SiteService siteService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;

    public boolean canAddPoll() {
        if (securityService.isSuperUser()) {
            return true;
        }
        String siteRef = currentSiteReference();
        return siteRef != null && securityService.unlock(PERMISSION_ADD, siteRef);
    }

    public boolean canEditPoll(Poll poll) {
        if (poll == null) {
            return false;
        }
        if (securityService.isSuperUser()) {
            return true;
        }
        if (StringUtils.isBlank(poll.getSiteId())) {
            return false;
        }
        String siteRef = siteService.siteReference(poll.getSiteId());
        if (siteRef == null) {
            return false;
        }
        if (securityService.unlock(PERMISSION_EDIT_ANY, siteRef)) {
            return true;
        }
        return securityService.unlock(PERMISSION_EDIT_OWN, siteRef)
                && StringUtils.equals(poll.getOwner(), sessionManager.getCurrentSessionUserId());
    }

    public boolean isSiteOwner() {
        if (securityService.isSuperUser()) {
            return true;
        }
        String siteRef = currentSiteReference();
        return siteRef != null && securityService.unlock(PERMISSION_SITE_UPDATE, siteRef);
    }

    private String currentSiteReference() {
        Placement placement = toolManager.getCurrentPlacement();
        if (placement == null) {
            log.warn("Unable to resolve current site reference outside a tool placement context");
            return null;
        }
        return siteService.siteReference(placement.getContext());
    }
}
