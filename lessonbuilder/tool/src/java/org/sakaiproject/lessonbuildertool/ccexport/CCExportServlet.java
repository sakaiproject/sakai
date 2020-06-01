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
package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CCExportServlet extends HttpServlet {

    @Autowired private CCExport ccExport;
    @Autowired private SecurityService securityService;
    @Autowired private SiteService siteService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String siteId = request.getParameter("siteid");
        String version = request.getParameter("version");
        String bank = request.getParameter("bank");

        try {
            if (StringUtils.isNoneBlank(siteId, version, bank)) {
                Site site = siteService.getSite(siteId);
                if (securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, site.getReference())) {
                    ccExport.doExport(response, siteId, version, bank);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                log.warn("Missing params: siteId={}, version={}, bank={}", siteId, version, bank);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IdUnusedException iue) {
            log.warn("Could not access site {}, {}", siteId, iue.toString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException ioe) {
            log.warn("Could not send response, {}", ioe.toString());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
