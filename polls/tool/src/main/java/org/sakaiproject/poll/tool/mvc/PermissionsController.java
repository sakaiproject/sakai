/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
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

package org.sakaiproject.poll.tool.mvc;

import lombok.RequiredArgsConstructor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.poll.tool.service.PollPermissionsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PermissionsController {

    private final SiteService siteService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;
    private final ServerConfigurationService serverConfigurationService;
    private final PollPermissionsService pollPermissionsService;

    @GetMapping("/votePermissions")
    public String permissions(Model model) {
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        String placementId = sessionManager.getCurrentToolSession().getPlacementId();
        String toolUrl = serverConfigurationService.getPortalUrl() + siteRef + "/tool/" + placementId;
        model.addAttribute("toolUrl", toolUrl);
        model.addAttribute("canAdd", pollPermissionsService.canAddPoll());
        model.addAttribute("isSiteOwner", pollPermissionsService.isSiteOwner());
        return "polls/permissions";
    }
}
