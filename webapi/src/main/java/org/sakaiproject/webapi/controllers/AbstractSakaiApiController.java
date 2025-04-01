/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.exception.MissingSessionException;
import org.sakaiproject.webapi.exception.UnknownSiteException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
abstract class AbstractSakaiApiController {

    @Autowired
    @Qualifier("org.sakaiproject.tool.api.SessionManager")
    private SessionManager sessionManager;

    @Autowired
    protected SiteService siteService;

    @Autowired
    protected PortalService portalService;

    /**
     * Check for a valid session.
     * If not valid a 403 Forbidden will be returned.
     */
    Session checkSakaiSession() {

        try {
            Session session = sessionManager.getCurrentSession();
            if (StringUtils.isBlank(session.getUserId())) {
                log.error("Sakai user session is invalid");
                throw new MissingSessionException();
            }
            return session;
        } catch (IllegalStateException e) {
            log.error("Could not retrieve the sakai session");
            throw new MissingSessionException(e.getCause());
        }
    }

    /**
     * Checks for a valid site Id and returns site.
     * If not valid a 400 Bad Request will be returned.
     */
    Site checkSite(String siteId) {

        return siteService.getOptionalSite(siteId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No site for id: " + siteId));
    }

    protected List<Map<String, String>> getPinnedSiteList() {

        return portalService.getPinnedSites().stream().map(siteId -> {

            return siteService.getOptionalSite(siteId).map(site -> Map.of("siteId", siteId, "title", site.getTitle()))
                .orElse(Map.of("siteId", siteId, "title", siteId));
        }).collect(Collectors.toList());
    }
}
