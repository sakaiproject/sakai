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

package org.sakaiproject.pasystem.tool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.pasystem.api.I18n;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.PASystemException;
import org.sakaiproject.pasystem.tool.handlers.BannersHandler;
import org.sakaiproject.pasystem.tool.handlers.Handler;
import org.sakaiproject.pasystem.tool.handlers.IndexHandler;
import org.sakaiproject.pasystem.tool.handlers.PopupsHandler;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.PreferencesService;

/**
 * The entry point for the PA System administration tool.  Takes a request,
 * routes it to a handler, renders a template in response.
 */
@Slf4j
public class PASystemServlet extends HttpServlet {

    private static final String FLASH_MESSAGE_KEY = "pasystem-tool.flash.errors";

    private PASystem paSystem;
    private ClusterService clusterService;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        paSystem = ComponentManager.get(PASystem.class);
        clusterService = ComponentManager.get(ClusterService.class);
    }

    private Handler handlerForRequest(HttpServletRequest request) {
        String path = request.getPathInfo();

        if (path == null) {
            path = "";
        }

        if (path.contains("/popups/")) {
            return new PopupsHandler(paSystem);
        } else if (path.contains("/banners/")) {
            return new BannersHandler(paSystem, clusterService);
        } else {
            return new IndexHandler(paSystem);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        checkAccessControl();

        I18n i18n = paSystem.getI18n(this.getClass().getClassLoader(), "org.sakaiproject.pasystem.tool.i18n.pasystem");

        response.setHeader("Content-Type", "text/html");

        URL toolBaseURL = determineBaseURL();
        Handlebars handlebars = loadHandlebars(toolBaseURL, i18n);

        try {
            Template template = handlebars.compile("org/sakaiproject/pasystem/tool/views/layout");
            Map<String, Object> context = new HashMap<String, Object>();

            context.put("baseURL", toolBaseURL);
            context.put("layout", true);
            context.put("skinRepo", ServerConfigurationService.getString("skin.repo", ""));
            context.put("randomSakaiHeadStuff", request.getAttribute("sakai.html.head"));

            Handler handler = handlerForRequest(request);

            Map<String, List<String>> messages = loadFlashMessages();

            handler.handle(request, response, context);

            storeFlashMessages(handler.getFlashMessages());

            if (handler.hasRedirect()) {
                response.sendRedirect(toolBaseURL + handler.getRedirect());
            } else {
                context.put("flash", messages);
                context.put("errors", handler.getErrors().toList());

                if (Boolean.TRUE.equals(context.get("layout"))) {
                    response.getWriter().write(template.apply(context));
                }
            }
        } catch (IOException e) {
            log.warn("Write failed", e);
        }
    }

    private void checkAccessControl() {
        String siteId = ToolManager.getCurrentPlacement().getContext();

        if (!SecurityService.unlock("pasystem.manage", "/site/" + siteId)) {
            log.error("Access denied to PA System management tool for user " + SessionManager.getCurrentSessionUserId());
            throw new PASystemException("Access denied");
        }
    }

    private void storeFlashMessages(Map<String, List<String>> messages) {
        Session session = SessionManager.getCurrentSession();
        session.setAttribute(FLASH_MESSAGE_KEY, messages);
    }

    private Map<String, List<String>> loadFlashMessages() {
        Session session = SessionManager.getCurrentSession();

        if (session.getAttribute(FLASH_MESSAGE_KEY) != null) {
            Map<String, List<String>> flashErrors = (Map<String, List<String>>) session.getAttribute(FLASH_MESSAGE_KEY);
            session.removeAttribute(FLASH_MESSAGE_KEY);

            return flashErrors;
        } else {
            return new HashMap<String, List<String>>();
        }
    }

    private URL determineBaseURL() {
        String siteId = ToolManager.getCurrentPlacement().getContext();
        String toolId = ToolManager.getCurrentPlacement().getId();

        try {
            return new URL(ServerConfigurationService.getPortalUrl() + "/site/" + siteId + "/tool/" + toolId + "/");
        } catch (MalformedURLException e) {
            throw new PASystemException("Couldn't determine tool URL", e);
        }
    }

    private Handlebars loadHandlebars(final URL baseURL, final I18n i18n) {
        Handlebars handlebars = new Handlebars();

        handlebars.registerHelper("subpage", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String subpage = options.param(0);
                try {
                    Template template = handlebars.compile("org/sakaiproject/pasystem/tool/views/" + subpage);
                    return template.apply(context);
                } catch (IOException e) {
                    log.warn("IOException while loading subpage", e);
                    return "";
                }
            }
        });

        handlebars.registerHelper("show-time", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                long utcTime = options.param(0) == null ? 0 : options.param(0);

                if (utcTime == 0) {
                    return "-";
                }

                Time time = TimeService.newTime(utcTime);

                return time.toStringLocalFull();
            }
        });

        handlebars.registerHelper("actionURL", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String type = options.param(0);
                String uuid = options.param(1);
                String action = options.param(2);

                try {
                    return new URL(baseURL, type + "/" + uuid + "/" + action).toString();
                } catch (MalformedURLException e) {
                    throw new PASystemException("Failed while building action URL", e);
                }
            }
        });

        handlebars.registerHelper("newURL", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String type = options.param(0);
                String action = options.param(1);

                try {
                    return new URL(baseURL, type + "/" + action).toString();
                } catch (MalformedURLException e) {
                    throw new PASystemException("Failed while building newURL", e);
                }
            }
        });

        handlebars.registerHelper("t", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String key = Arrays.stream(options.params).map(Object::toString).collect(Collectors.joining("_"));
                return i18n.t(key);
            }
        });

        handlebars.registerHelper("selected", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String option = options.param(0);
                String value = options.param(1);

                return option.equals(value) ? "selected" : "";
            }
        });

        return handlebars;
    }
}
