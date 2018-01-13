/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 *
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

package org.sakaiproject.tags.tool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tags.api.I18n;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tags.api.TagServiceException;
import org.sakaiproject.tags.tool.handlers.*;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

/**
 * The entry point for the Tags Service administration tool.  Takes a request,
 * routes it to a handler, renders a template in response.
 */
@Slf4j
public class TagServiceServlet extends HttpServlet {

    private static final String FLASH_MESSAGE_KEY = "tags-tool.flash.errors";

    private TagService tagService;
    private ServerConfigurationService serverConfigurationService = null;
    private SecurityService securityService = null;
    private SessionManager sessionManager = null;
    private ToolManager toolManager = null;
    private TimeService timeService = null;


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        tagService = (TagService) ComponentManager.get(TagService.class);
        serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
        securityService = (SecurityService) ComponentManager.get(SecurityService.class);
        sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
        toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
        timeService = (TimeService) ComponentManager.get(TimeService.class);

    }

    private Handler handlerForRequest(HttpServletRequest request) {
        String path = request.getPathInfo();

        if (path == null) {
            path = "";
        }

        if (path.contains("/tagsintagcollection/")) {
            return new TagsInTagCollectionsHandler(tagService);
        } else if (path.contains("/tagcollections/")) {
            return new TagCollectionsHandler(tagService);
        } else if (path.contains("/tags/")) {
            return new TagsHandler(tagService);
        } else {
            return new IndexHandler(tagService);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        checkAccessControl();

        I18n i18n = tagService.getI18n(this.getClass().getClassLoader(), "org.sakaiproject.tags.tool.i18n.tagservice");

        response.setHeader("Content-Type", "text/html");

        URL toolBaseURL = determineBaseURL();
        Handlebars handlebars = loadHandlebars(toolBaseURL, i18n);

        try {
            Template template = handlebars.compile("org/sakaiproject/tags/tool/views/layout");
            Map<String, Object> context = new HashMap<>();

            context.put("baseURL", toolBaseURL);
            context.put("layout", true);
            context.put("skinRepo", serverConfigurationService.getString("skin.repo", ""));
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
        String siteId = toolManager.getCurrentPlacement().getContext();
        if (!securityService.unlock("tagservice.manage", "/site/" + siteId)) {
            log.error("Access denied to Tags management tool for user " + sessionManager.getCurrentSessionUserId());
            throw new TagServiceException("Access denied");
        }
    }

    private void storeFlashMessages(Map<String, List<String>> messages) {
        Session session = sessionManager.getCurrentSession();
        session.setAttribute(FLASH_MESSAGE_KEY, messages);
    }

    private Map<String, List<String>> loadFlashMessages() {
        Session session = sessionManager.getCurrentSession();

        if (session.getAttribute(FLASH_MESSAGE_KEY) != null) {
            Map<String, List<String>> flashErrors = (Map<String, List<String>>) session.getAttribute(FLASH_MESSAGE_KEY);
            session.removeAttribute(FLASH_MESSAGE_KEY);

            return flashErrors;
        } else {
            return new HashMap<>();
        }
    }

    private URL determineBaseURL() {
        String siteId = toolManager.getCurrentPlacement().getContext();
        String toolId = toolManager.getCurrentPlacement().getId();

        try {
            return new URL(serverConfigurationService.getPortalUrl() + "/site/" + siteId + "/tool/" + toolId + "/");
        } catch (MalformedURLException e) {
            throw new TagServiceException("Couldn't determine tool URL", e);
        }
    }

    private Handlebars loadHandlebars(final URL baseURL, final I18n i18n) {
        Handlebars handlebars = new Handlebars();

        handlebars.registerHelper("subpage", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String subpage = options.param(0);
                try {
                    Template template = handlebars.compile("org/sakaiproject/tags/tool/views/" + subpage);
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

                Time time = timeService.newTime(utcTime);

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
                    throw new TagServiceException("Failed while building action URL", e);
                }
            }
        });

        handlebars.registerHelper("actionURLPaginated", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String type = options.param(0);
                String uuid = options.param(1);
                String action = options.param(2);
                int pageNum = options.param(3);
                int pageSize = options.param(4);

                try {
                    return new URL(baseURL, type + "/" + uuid + "/" + action  + "/" + pageNum + "/" + pageSize).toString();
                } catch (MalformedURLException e) {
                    throw new TagServiceException("Failed while building action URL", e);
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
                    throw new TagServiceException("Failed while building newURL", e);
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

        handlebars.registerHelper("display", new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) {
                String a = options.param(0);
                String b = options.param(1);
                return a.equals(b) ? "display:none" : "";
            }
        });

        handlebars.registerHelper("pagination", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {

                Map<String, Object> paginationInfoMap;

                try {
                    int currentPageNumber = options.param(0, 1); // parameter. default 1
                    int totalPageCount = options.param(1, 1); // parameter. default 1
                    int pageGroupCount = options.param(2, 10); // parameter. default 10. max displayed page count

                    int firstPageIdx = (((currentPageNumber - 1) / pageGroupCount)) * pageGroupCount + 1; // 첫번째 index
                    int lastPageIdx = (((currentPageNumber - 1) / pageGroupCount)) * pageGroupCount + pageGroupCount; // 마지막 index

                    int previousIdx = lastPageIdx - pageGroupCount; // 이전 index
                    int nextIdx = lastPageIdx + 1; // 다음 index

                    boolean canGoPrevious = firstPageIdx > 1 ? true : false; // previous 버튼 active 여부
                    boolean canGoNext = totalPageCount > lastPageIdx ? true : false; // next 버튼 active 여부

                    int displayedLastPage = totalPageCount < lastPageIdx ? totalPageCount : lastPageIdx;

                    paginationInfoMap = this.makePaginationInfoMap(canGoPrevious, canGoNext, currentPageNumber, firstPageIdx, displayedLastPage, previousIdx,
                            nextIdx);

                } catch (Exception e) {
                    log.warn(e.getMessage());
                    paginationInfoMap = new HashMap<>();
                }

                return options.fn(paginationInfoMap);
            }

            private Map<String, Object> makePaginationInfoMap(boolean canGoPrevious, boolean canGoNext, int page, int firstPage, int displayedLastPage,
                                                              int previousIdx, int nextIdx) {

                Map<String, Object> paginationInfoMap = new HashMap<>();
                List<Map> pageList = new ArrayList<>();

                for (int i = firstPage; i <= displayedLastPage; i++) {
                    Map<String, Object> numberMap = new HashMap<>();
                    numberMap.put("page", String.valueOf(i));
                    numberMap.put("pageInt", i);
                    numberMap.put("isCurrent", (i == page ? true : false));
                    pageList.add(numberMap);
                }

                paginationInfoMap.put("canGoPrevious", canGoPrevious);
                paginationInfoMap.put("previousIdx", previousIdx);
                paginationInfoMap.put("pages", pageList);
                paginationInfoMap.put("canGoNext", canGoNext);
                paginationInfoMap.put("nextIdx", nextIdx);

                return paginationInfoMap;
            }
        });

        return handlebars;
    }
}
