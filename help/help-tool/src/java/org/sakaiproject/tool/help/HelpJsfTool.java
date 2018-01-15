/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.help;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HelpJsfTool extends JsfTool to support placement in the help frameset.
 *
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id$
 */
public class HelpJsfTool extends JsfTool {

    /**
     * Our log (commons).
     */
    private static Logger M_log = LoggerFactory.getLogger(HelpJsfTool.class);

    private static String HELP_DOC_REGEXP = org.sakaiproject.api.app.help.HelpManager.HELP_DOC_REGEXP;

    private static final String TOC_PATH = "/TOCDisplay/main";
    private static final String SEARCH_PATH = "/search/main";
    private static final String HELP_PATH = "/html";

    private static final String TOC_ATTRIBUTE = "tocURL";
    private static final String SEARCH_ATTRIBUTE = "searchURL";
    private static final String HELP_ATTRIBUTE = "helpURL";

    /**
     * To determine if an external webapp handles help and if so, the base url
     */
    private static final String EXTERNAL_WEBAPP_URL_BASE = "help.redirect.external.webapp";
    private static final String EXTERNAL_WEBAPP_URL = ServerConfigurationService.getString(EXTERNAL_WEBAPP_URL_BASE, "sakai");


    private static final String CONF_EXTERNAL_WEB_BASE = "help.external.web.base";
    private static final String CONF_EXTERNAL_WEB_LANGUAGES = "help.external.web.languages";
    private static final String CONF_EXTERNAL_WEB_ADD_LANGUAGE_PARAM = "help.external.web.add.language.param";
    private static final String CONF_EXTERNAL_WEB_TOOL = "help.external.web.tool.";
    private static final String CONF_EXTERNAL_WEB_USE_INTERNAL_IF_MISSING = "help.external.web.use.internal.if.missing";

    private static final String EXTERNAL_WEB_BASE = ServerConfigurationService.getString(CONF_EXTERNAL_WEB_BASE, null);
    private static final String EXTERNAL_WEB_LANGUAGES = ServerConfigurationService.getString(CONF_EXTERNAL_WEB_LANGUAGES, "").toUpperCase();
    private static final String EXTERNAL_WEB_ADD_LANGUAGE_PARAM = ServerConfigurationService.getString(CONF_EXTERNAL_WEB_ADD_LANGUAGE_PARAM, "").toUpperCase();
    private static final Boolean EXTERNAL_WEB_USE_INTERNAL_IF_MISSING = ServerConfigurationService.getBoolean(CONF_EXTERNAL_WEB_USE_INTERNAL_IF_MISSING, true);

    /**
     * @see org.sakaiproject.jsf.util.JsfTool#dispatch(HttpServletRequest, HttpServletResponse)
     */
    protected void dispatch(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // if magic switch turned on, go to external webapp
        if (!"sakai".equals(EXTERNAL_WEBAPP_URL)) {
            String helpId = getHelpId(req);
            String extUrl = EXTERNAL_WEBAPP_URL;

            if (helpId != null && !"".equals("docId")) {
                extUrl += helpId;
            }

            res.sendRedirect(extUrl);
            return;
        } else if (EXTERNAL_WEB_BASE != null) {
            if (handleExternalRedirect(req, res)) {
                return;
            }
        }

        req.setAttribute(TOC_ATTRIBUTE, Web.returnUrl(req, TOC_PATH));
        req.setAttribute(SEARCH_ATTRIBUTE, Web.returnUrl(req, SEARCH_PATH));
        req.setAttribute(HELP_ATTRIBUTE, Web.returnUrl(req, HELP_PATH));
        super.dispatch(req, res);
    }

    private String getHelpId(HttpServletRequest req) {
        String helpId = req.getParameter("help");

        if (helpId != null) {
            Pattern p = Pattern.compile(HELP_DOC_REGEXP);
            Matcher m = p.matcher(helpId);

            if (!m.matches()) {
                helpId = "unknown";
                M_log.error("Unable to parse the helpId: " + helpId);
            }
        }
        return helpId;
    }

    private String getDocId(HttpServletRequest req) {
        String docId = req.getParameter("docId");

        if (docId != null) {
            Pattern p = Pattern.compile(HELP_DOC_REGEXP);
            Matcher m = p.matcher(docId);

            if (!m.matches()) {
                docId = "unknown";
                M_log.error("Unable to parse the docId: " + docId);
            }
        }
        return docId;
    }

    private boolean handleExternalRedirect(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String lang = new ResourceLoader().getLocale().getLanguage();
        if (lang.isEmpty()) {
            // Default language quirk
            lang = "en";
        }

        // Do only use external help for language that supports it
        if (!EXTERNAL_WEB_LANGUAGES.contains(lang.toUpperCase())) {
            return false;
        }

        String redirectUrl = EXTERNAL_WEB_BASE;

        String helpId = getHelpId(req);
        if (helpId != null) {
            String toolUrl = ServerConfigurationService.getString(CONF_EXTERNAL_WEB_TOOL + helpId, null);
            if (toolUrl == null || toolUrl.isEmpty()) {
                M_log.info("Unable to find the tool help link for docId: " + helpId + " . The missing key in sakai.properties is: " + CONF_EXTERNAL_WEB_TOOL + helpId);

                if (EXTERNAL_WEB_USE_INTERNAL_IF_MISSING) {
                    M_log.info("Redirecting to internal help instead.");
                    return false;
                } else {
                    M_log.info("Redirecting to base instead: " + redirectUrl);
                }
            }  else {
                redirectUrl = toolUrl;
            }
        }
        else {
            String docId = getDocId(req);
            if (docId != null) {
                // This is the case when navigating the internal help as we do not have docId in external
                return false;
            }

            // Always redirect if main help in gateway
            if (!isGatewayHelp(req)) {
                return false;
            }
        }

        if (!EXTERNAL_WEB_ADD_LANGUAGE_PARAM.isEmpty() && EXTERNAL_WEB_ADD_LANGUAGE_PARAM.contains(lang.toUpperCase())) {
            // Add language path param in some way
            redirectUrl = redirectUrl + "/" + lang;
        }

        res.sendRedirect(redirectUrl);
        return true;
    }

    private boolean isGatewayHelp(HttpServletRequest req) {
        return "/portal/help".equals(req.getContextPath()) &&
                       "/main".equals(req.getPathInfo());
    }
}


