/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.servlet;

import java.util.Optional;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

public abstract class SamigoBaseServlet extends HttpServlet {


    private SecurityService securityService = ComponentManager.get(SecurityService.class);
    private UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);


    public boolean hasPrivilege(String functionName, String siteId) {
        return securityService.unlock(functionName, "/site/" + siteId);
    }

    public Optional<String> getUserId() {
        User user = userDirectoryService.getCurrentUser();

        return user != null && StringUtils.isNotEmpty(user.getId()) ? Optional.of(user.getId()) : Optional.empty();
    }
}
