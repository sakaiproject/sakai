/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2025 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.shared;

import java.util.Map;

import javax.faces.application.ResourceHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.PortalUtils;

/**
 * Replicates the legacy header.inc logic for Facelets views.
 */
@ManagedBean(name = "portalHeaderConfigurator")
@RequestScoped
@Slf4j
public class PortalHeaderConfigurator {

    public void configure(ComponentSystemEvent event) {
        configure();
    }

    public void configure() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }

        ExternalContext externalContext = context.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();

        // Prevent Mojarra from injecting its own jsf.js tag into component markup.
        try {
            ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
            resourceHandler.markResourceRendered(context, "jsf.js", "javax.faces");
        } catch (Exception e) {
            log.debug("Unable to mark jsf.js as rendered", e);
        }

        Object sakaiHead = requestMap.get("sakai.html.head");
        if (sakaiHead instanceof String) {
            String head = (String) sakaiHead;
            head += PortalUtils.includeLatestJQuery("Samigo");
            head += "<script type=\"text/javascript\" src=\"/library/webjars/jquery.tablesorter/2.27.7/dist/js/jquery.tablesorter.min.js\"></script>\n";
            head += "<link href=\"/library/webjars/jquery.tablesorter/2.27.7/dist/css/theme.default.min.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
            head += "<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n";
            head += "<script>includeWebjarLibrary(\"qtip2\");</script>";

            requestMap.put("html.head", head);
            requestMap.put("html.body.onload", requestMap.get("sakai.html.body.onload"));
            requestMap.put("html.head.sakai", sakaiHead);
        } else {
            try {
                StringBuilder head = new StringBuilder();
                head.append("<link href=\"")
                        .append(CSSUtils.getCssToolBase())
                        .append("\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n");
                head.append("<link href=\"")
                        .append(CSSUtils.getCssToolSkin(CSSUtils.adjustCssSkinFolder(null)))
                        .append("\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n");
                head.append("<link href=\"/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n");
                head.append("<script type=\"text/javascript\" src=\"/library/webjars/jquery/1.12.4/jquery.min.js\"></script>\n");
                head.append("<script type=\"text/javascript\" src=\"/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js\"></script>\n");
                head.append("<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n");
                head.append("<script type=\"text/javascript\" src=\"/library/js/headscripts.js\"></script>\n");

                requestMap.put("html.head", head.toString());
                requestMap.put("html.body.onload", "");
            } catch (Exception e) {
                log.warn("Unable to configure default Samigo header", e);
            }
        }
    }
}
