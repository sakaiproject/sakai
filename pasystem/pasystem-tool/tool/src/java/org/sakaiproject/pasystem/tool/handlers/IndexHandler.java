/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.tool.handlers;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.pasystem.api.PASystem;

/**
 * A handler for the index page in the PA System administration tool.
 */
public class IndexHandler extends BaseHandler {

    private final PASystem paSystem;

    public IndexHandler(PASystem pasystem) {
        this.paSystem = pasystem;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        context.put("subpage", "index");
        context.put("banners", paSystem.getBanners().getAll());
        context.put("popups", paSystem.getPopups().getAll());
        context.put("timezoneCheckActive", ServerConfigurationService.getBoolean("pasystem.timezone-check", true));
        context.put("paSystemActive", ServerConfigurationService.getBoolean("pasystem.enabled", true));
    }
}
