/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.rubrics.controller;

import javax.annotation.Resource;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.rubrics.logic.RubricsService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainController {

    @Resource(name = "org.sakaiproject.rubrics.logic.RubricsService")
    private RubricsService rubricsService;

    @Resource
    private SecurityService securityService;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private ServerConfigurationService serverConfigurationService;

    @Resource
    private ToolManager toolManager;

    @GetMapping("/")
    public String indexRedirect() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(ModelMap model) {

        String token = rubricsService.generateJsonWebToken("sakai.rubrics");
        model.addAttribute("token", token);

        model.addAttribute("sakaiSessionId", sessionManager.getCurrentSession().getId());
        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());

        String currentUserId = sessionManager.getCurrentSessionUserId();

        String siteId = toolManager.getCurrentPlacement().getContext();
        if (securityService.unlock(currentUserId, RubricsConstants.RBCS_PERMISSIONS_EDITOR, "/site/" + siteId)) {
            return "editor_index";
        } else {
            return "viewer_index";
        }
    }
}
