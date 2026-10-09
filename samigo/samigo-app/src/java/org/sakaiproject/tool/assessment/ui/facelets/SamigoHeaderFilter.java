/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.facelets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.PortalUtils;

/** Prepares the portal header formerly supplied by the JSP prelude. */
public final class SamigoHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getAttribute("html.head") == null) {
            String head = (String) request.getAttribute("sakai.html.head");
            if (head != null) {
                request.setAttribute("html.head.sakai", head);
                head += PortalUtils.includeLatestJQuery("Samigo");
                request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));
            } else {
                String skin = (String) request.getAttribute("sakai.skin.folder");
                if (skin == null || skin.isEmpty()) { skin = CSSUtils.adjustCssSkinFolder(null); }
                head = CSSUtils.getPortalSkinLink(skin)
                    + "<link href=\"" + CSSUtils.getCssToolBase() + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
                    + "<link href=\"" + CSSUtils.getCssToolSkin(skin) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
                    + "<link href=\"" + CSSUtils.getCssPrintSkin(skin) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"print\" />\n"
                    + "<link href=\"" + CSSUtils.getCssPropertiesSkin(skin) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
                    + "<script src=\"/library/js/headscripts.js\"></script>\n"
                    + "<script>includeLatestJQuery(\"Samigo\");</script>\n"
                    + "<script>includeWebjarLibrary(\"qtip2\", { includeCss: true });</script>\n"
                    + "<script>includeWebjarLibrary(\"bootstrap\");</script>\n";
                request.setAttribute("html.body.onload", "");
            }
            request.setAttribute("html.head", head + "<script src=\"/samigo-app/js/samigo-global.js\"></script>\n");
        }
        chain.doFilter(request, response);
    }
}
