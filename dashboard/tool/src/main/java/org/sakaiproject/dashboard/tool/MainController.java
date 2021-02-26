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
package org.sakaiproject.dashboard.tool;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.dashboard.tool.exception.MissingSessionException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

	@Resource
	private SessionManager sessionManager;

	@Resource
	private ToolManager toolManager;

	@Resource
	private SiteService siteService;

	@GetMapping(value = {"/", "/index"})
	public String pageIndex(Model model, HttpServletRequest request) {

		Session session = checkSakaiSession();

        String siteId = toolManager.getCurrentPlacement().getContext();
        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());

        if (siteService.isUserSite(siteId)) {
            model.addAttribute("userId", session.getUserId());
	        return "home_dashboard";
        } else {
            model.addAttribute("siteId", siteId);
            model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
	        return "course_dashboard";
        }
	}

    /**
     * Check for a valid session
     * if not valid a 403 Forbidden will be returned
     */
	private Session checkSakaiSession() {

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
}
