/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2024 The Apereo Foundation
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

package org.sakaiproject.portal.charon.handlers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.charon.site.MoreSiteViewImpl;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;

import static org.sakaiproject.portal.api.PortalConstants.SITE_URL_PREFIX;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders the "More Sites" drawer content (the term-grouped list of every site the user can access)
 * on demand. The drawer is hidden until the user opens it, so building this list on every page
 * render was wasteful - for users in hundreds of sites it dominated render time. The morpheus
 * JS (portal.all.sites.js) fetches this fragment when the offcanvas is opened.
 */
@Slf4j
public class MoreSitesHandler extends BasePortalHandler {

    private static final String URL_FRAGMENT = "sites-drawer";

    private static final ResourceLoader rloader = new ResourceLoader("sitenav");

    private final PreferencesService preferencesService;

    public MoreSitesHandler() {
        setUrlFragment(URL_FRAGMENT);
        preferencesService = ComponentManager.get(PreferencesService.class);
    }

    @Override
    public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
            throws PortalHandlerException {

        if ((parts.length < 2) || !parts[1].equals(URL_FRAGMENT)) {
            return NEXT;
        }

        if (session == null || session.getUserId() == null) {
            return NEXT;
        }

        // /portal/sites-drawer/{siteId} - siteId identifies the current site for highlighting
        String siteId = (parts.length >= 3) ? parts[2] : null;

        try {
            SiteView siteView = portal.getSiteHelper().getSitesView(SiteView.View.DHTML_MORE_VIEW, req, session, siteId);
            siteView.setPrefix(SITE_URL_PREFIX);
            siteView.setToolContextPath(null);
            Map<String, Object> drawerContext = ((MoreSiteViewImpl) siteView).getMoreSitesDrawerContext();

            PortalRenderEngine rengine = portalService.getRenderEngine(portal.getPortalContext(), req);
            PortalRenderContext rcontext = rengine.newRenderContext(req);
            rcontext.put("rloader", rloader);
            rcontext.put("userIsLoggedIn", Boolean.TRUE);
            rcontext.put("tabDisplayLabel", preferencesService.getSiteTitleDisplayPreference());
            rcontext.put("tabsSites", drawerContext);

            portal.sendResponse(rcontext, res, "moresites-drawer", "text/html; charset=UTF-8");
            return END;
        } catch (Exception e) {
            log.warn("Failed to render the More Sites drawer for site [{}]", siteId, e);
            throw new PortalHandlerException(e);
        }
    }
}
