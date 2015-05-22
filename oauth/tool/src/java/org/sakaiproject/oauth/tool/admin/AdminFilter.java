/*
 * #%L
 * OAuth Tool
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.tool.admin;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter users before displaying the oAuth administration page.
 *
 * @author Colin Hebert
 */
public class AdminFilter implements Filter {
    private static final String OAUTH_ADMIN_RIGHT = "oauth.admin";
    private SecurityService securityService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        FunctionManager functionManager = (FunctionManager) ComponentManager.get(FunctionManager.class);
        functionManager.registerFunction(OAUTH_ADMIN_RIGHT, false);
        securityService = (SecurityService) ComponentManager.get(SecurityService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (securityService.unlock(OAUTH_ADMIN_RIGHT, "")) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void destroy() {
    }
}
