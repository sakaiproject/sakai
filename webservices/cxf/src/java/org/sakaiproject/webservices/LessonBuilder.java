/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class LessonBuilder extends AbstractWebService {

    /**
     * Key in the ThreadLocalManager for binding our current placement.
     */
    protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

    /**
     * Key in the ThreadLocalManager for binding our current tool.
     */
    protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";

    /**
     * deletes orphan pages for a site
     * @param sessionid the session to use
     * @param context   the context to use
     * @return the sessionid if active, or "null" if not.
     */
    @WebMethod
    @Path("/deleteOrphanPages")
    @Produces("text/plain")
    @GET
    public String deleteOrphanPages(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context) {
        Session session = establishSession(sessionid);


        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(context);
            // If not admin, check maintainer membership in the source site
            if (!securityService.isSuperUser(session.getUserId()) &&
                    !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                log.warn("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
                throw new RuntimeException("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + context;
            }
            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());
            return lessonBuilderAccessAPI.deleteOrphanPages(site.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "Failure";
    }

}
