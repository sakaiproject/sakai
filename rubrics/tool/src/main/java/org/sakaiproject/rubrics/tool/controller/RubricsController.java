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
package org.sakaiproject.rubrics.tool.controller;

import javax.annotation.Resource;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class RubricsController {

    @Resource
    private RubricsService rubricsService;

    @Resource
    private SessionManager sessionManager;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Resource
    private ToolManager toolManager;

    @GetMapping("/")
    public String indexRedirect() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(ModelMap model) {

        boolean enablePdfExport = serverConfigurationService.getBoolean(RubricsConstants.RBCS_EXPORT_PDF, true);
        model.addAttribute("enablePdfExport", enablePdfExport);
        model.addAttribute("siteId", toolManager.getCurrentPlacement().getContext());
        model.addAttribute("sakaiSessionId", sessionManager.getCurrentSession().getId());
        model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());
        return "index";
    }
}
