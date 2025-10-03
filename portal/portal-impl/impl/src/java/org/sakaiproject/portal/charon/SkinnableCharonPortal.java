/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.portal.charon;

import static org.sakaiproject.portal.api.PortalConstants.*;
import static org.sakaiproject.user.api.PreferencesService.USER_SELECTED_UI_THEME_PREFS;
import static org.sakaiproject.user.api.PreferencesService.TUTORIAL_PREFS;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.portal.api.Editor;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.portal.api.SiteView;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.portal.charon.handlers.DirectToolHandler;
import org.sakaiproject.portal.charon.handlers.ErrorDoneHandler;
import org.sakaiproject.portal.charon.handlers.ErrorReportHandler;
import org.sakaiproject.portal.charon.handlers.FavoritesHandler;
import org.sakaiproject.portal.charon.handlers.GenerateBugReportHandler;
import org.sakaiproject.portal.charon.handlers.HelpHandler;
import org.sakaiproject.portal.charon.handlers.JoinHandler;
import org.sakaiproject.portal.charon.handlers.LoginHandler;
import org.sakaiproject.portal.charon.handlers.LogoutHandler;
import org.sakaiproject.portal.charon.handlers.NavLoginHandler;
import org.sakaiproject.portal.charon.handlers.PageHandler;
import org.sakaiproject.portal.charon.handlers.PageResetHandler;
import org.sakaiproject.portal.charon.handlers.PresenceHandler;
import org.sakaiproject.portal.charon.handlers.ReLoginHandler;
import org.sakaiproject.portal.charon.handlers.RoleSwitchHandler;
import org.sakaiproject.portal.charon.handlers.RoleSwitchOutHandler;
import org.sakaiproject.portal.charon.handlers.SiteHandler;
import org.sakaiproject.portal.charon.handlers.SiteResetHandler;
import org.sakaiproject.portal.charon.handlers.StaticScriptsHandler;
import org.sakaiproject.portal.charon.handlers.TimeoutDialogHandler;
import org.sakaiproject.portal.charon.handlers.ToolHandler;
import org.sakaiproject.portal.charon.handlers.ToolResetHandler;
import org.sakaiproject.portal.charon.handlers.WorksiteHandler;
import org.sakaiproject.portal.charon.handlers.WorksiteResetHandler;
import org.sakaiproject.portal.charon.handlers.XLoginHandler;
import org.sakaiproject.portal.charon.site.PortalSiteHelperImpl;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portal.util.ToolURLManagerImpl;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolURL;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p/> Charon is the Sakai Site based portal.
 * </p>
 *
 * @version $Rev$
 * @since Sakai 2.4
 */
@Slf4j
public class SkinnableCharonPortal extends HttpServlet implements Portal {
    private static final long serialVersionUID = 2645929710236293089L;
    private static final ResourceLoader MESSAGES = new ResourceLoader("sitenav");
    private static final String INCLUDE_BOTTOM = "include-bottom";
    private static final String INCLUDE_LOGIN = "include-login";

    private String copyrightText;
    private String enableGAM;
    private String favIconUrl;
    private String gatewaySiteUrl;
    private String googleGA4Id;
    private String googleTagManagerContainerId;
    private String handlerPrefix;
    private String includeExtraHead;
    private String mathJaxPath;
    @Getter private String portalContext;
    private String portalCookieWarnUrl;
    private String portalLogOutUrl;
    private String portalLoginIcon;
    private String portalLoginText;
    private String portalLoginUrl;
    private String portalLogoutIcon;
    private String portalLogoutText;
    private String portalPath;
    private String portalUrl;
    private String portalXLoginIcon;
    private String portalXLoginText;
    private String preferenceToolId;
    private String sakaiVersion;
    private String serverId;
    private String serviceName;
    private String serviceVersion;
    private String skinRepo;
    private String toolUrlPrefix;
    private String universalAnalyticsId;
    private String[] footerUrls;
    private String[] mathJaxFormat;
    private String[] poweredByAltText;
    private String[] poweredByImage;
    private String[] poweredByUrl;
    private boolean containerLogin;
    private boolean displayUserloginInfo;
    private boolean enableDirect;
    private boolean forceContainer;
    private boolean googleAnonIp;
    private boolean displayHelpIcon;
    private boolean mathJaxEnabled;
    private boolean paSystemEnabled;
    private boolean portalCookieWarnEnabled;
    private boolean portalDirectUrlToolEnabled;
    private boolean portalLogoutConfirmation;
    private boolean portalShortUrlToolEnabled;
    private boolean portalXLoginEnabled;
    private boolean sakaiThemeSwitcherEnabled;
    private boolean sakaiThemesAutoDetectDarkEnabled;
    private boolean sakaiThemesEnabled;
    private boolean sakaiTutorialEnabled;
    private boolean showServerTime;
    private boolean tasksEnabled;
    private boolean timeoutDialogEnabled;
    private boolean topLogin;
    private boolean useBullhornAlerts;
    private int portalToolMenuMax;
    private int timeoutDialogWarningSeconds;

    private BasicAuth basicAuth = null;
    private List<Map<String, String>> poweredBy;
    private SiteHandler siteHandler;
    @Getter private PortalSiteHelper siteHelper;
    private WorksiteHandler worksiteHandler;

    @Autowired private ActiveToolManager activeToolManager;
    @Autowired private PASystem paSystem;
    @Autowired private PortalService portalService;
    @Autowired private PreferencesService preferencesService;
    @Autowired private ProfileService profileService;
    @Autowired private SecurityService securityService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteNeighbourhoodService siteNeighbourhoodService;
    @Autowired private SiteService siteService;
    @Autowired private ThreadLocalManager threadLocalManager;
    @Autowired @Qualifier("toolRenderService")
    private ToolRenderService toolRenderService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private UserMessagingService userMessagingService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());

        containerLogin = serverConfigurationService.getBoolean(PROP_CONTAINER_LOGIN, false);
        copyrightText = serverConfigurationService.getString(PROP_COPYRIGHT_TEXT);
        displayUserloginInfo = serverConfigurationService.getBoolean(PROP_DISPLAY_USER_LOGIN, true);
        enableGAM = serverConfigurationService.getString(PROP_GLOBAL_ALERT_MESSAGE, "false");
        favIconUrl = serverConfigurationService.getString(PROP_PORTAL_FAV_ICON);
        footerUrls = serverConfigurationService.getStrings(PROP_FOOTER_URLS);
        forceContainer = serverConfigurationService.getBoolean(PROP_XLOGIN_RELOGIN, true);
        gatewaySiteUrl = serverConfigurationService.getString(PROP_GATEWAY_SITE_URL, null);
        googleAnonIp = serverConfigurationService.getBoolean(PROP_GOOGLE_ANON_IP, false);
        googleGA4Id = serverConfigurationService.getString(PROP_GOOGLE_GA4_ID, null);
        googleTagManagerContainerId = serverConfigurationService.getString(PROP_GOOGLE_CONTAINER_ID, null);
        handlerPrefix = serverConfigurationService.getString(PROP_PORTAL_DEFAULT_HANDLER, "site");
        displayHelpIcon = serverConfigurationService.getBoolean(PROP_DISPLAY_HELP_ICON, true);
        includeExtraHead = serverConfigurationService.getString(PROP_INCLUDE_EXTRAHEAD, "");
        mathJaxEnabled = serverConfigurationService.getBoolean(PROP_MATHJAX_ENABLED, true);
        mathJaxFormat = serverConfigurationService.getStrings(PROP_MATHJAX_FORMAT);
        mathJaxPath = serverConfigurationService.getString(PROP_MATHJAX_SRC_PATH);
        paSystemEnabled = serverConfigurationService.getBoolean(PROP_PA_SYSTEM_ENABLED, true);
        portalCookieWarnUrl = serverConfigurationService.getString(PROP_PORTAL_COOKIE_WARN_URL, "/library/content/cookie_policy.html");
        portalCookieWarnEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_COOKIE_WARN_ENABLED,false);
        portalDirectUrlToolEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_DIRECT_TOOL_URL_ENABLED, false);
        portalLogOutUrl = serverConfigurationService.getLoggedOutUrl();
        portalLoginIcon = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_LOGIN_ICON));
        portalLoginText = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_LOGIN_TEXT));
        portalLoginUrl = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_LOGIN_URL));
        portalLogoutConfirmation = serverConfigurationService.getBoolean(PROP_PORTAL_LOGOUT_CONFIRM, false);
        portalLogoutIcon = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_LOGOUT_ICON));
        portalLogoutText = serverConfigurationService.getString(PROP_PORTAL_LOGOUT_TEXT);
        portalPath = serverConfigurationService.getString(PROP_PORTAL_PATH, "/portal");
        portalShortUrlToolEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_SHORT_URL_TOOL_ENABLED, true);
        portalToolMenuMax = serverConfigurationService.getInt(PROP_PORTAL_TOOL_MENU_MAX, 10);
        portalUrl = serverConfigurationService.getPortalUrl();
        portalXLoginEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_XLOGIN_ENABLED, false);
        portalXLoginIcon = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_XLOGIN_ICON));
        portalXLoginText = StringUtils.trimToNull(serverConfigurationService.getString(PROP_PORTAL_XLOGIN_TEXT));
        poweredByAltText = serverConfigurationService.getStrings(PROP_POWERED_BY_ALT_TEXT);
        poweredByImage = serverConfigurationService.getStrings(PROP_POWERED_BY_IMAGE);
        poweredByUrl = serverConfigurationService.getStrings(PROP_POWERED_BY_URL);
        preferenceToolId = serverConfigurationService.getString("portal.preferencestool", "sakai.preferences");
        sakaiThemeSwitcherEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_THEMES_SWITCHER, true);
        sakaiThemesAutoDetectDarkEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_THEMES_AUTO_DARK, true);
        sakaiThemesEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_THEMES, true);
        sakaiTutorialEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_TUTORIAL, true);
        sakaiVersion = serverConfigurationService.getString(PROP_SAKAI_VERSION, "?");
        serverId = serverConfigurationService.getServerId();
        serviceName = serverConfigurationService.getString(PROP_SERVICE_NAME, "Sakai");
        serviceVersion = serverConfigurationService.getString(PROP_SERVICE_VERSION, "?");
        showServerTime = serverConfigurationService.getBoolean(PROP_SHOW_SERVER_TIME, true);
        skinRepo = serverConfigurationService.getString(PROP_SKIN_REPO);
        tasksEnabled = serverConfigurationService.getBoolean(PROP_DASHBOARD_TASKS_ENABLED, false);
        timeoutDialogEnabled = serverConfigurationService.getBoolean(PROP_PORTAL_TIMEOUT_DIALOG_ENABLED, true);
        timeoutDialogWarningSeconds = serverConfigurationService.getInt(PROP_PORTAL_TIMEOUT_DIALOG_WARN_SECONDS, 600);
        toolUrlPrefix = serverConfigurationService.getToolUrl();
        topLogin = serverConfigurationService.getBoolean(PROP_TOP_LOGIN, true);
        universalAnalyticsId = serverConfigurationService.getString(PROP_GOOGLE_ANALYTICS_ID);
        useBullhornAlerts = serverConfigurationService.getBoolean(PROP_BULLHORN_ALERTS_ENABLED, true);

        poweredBy = new ArrayList<>();
        if (poweredByUrl != null
                && poweredByImage != null
                && poweredByAltText != null
                && poweredByUrl.length == poweredByImage.length
                && poweredByUrl.length == poweredByAltText.length) {
            IntStream.range(0, poweredByUrl.length).forEach(i -> {
                Map<String, String> map = new HashMap<>();
                map.put("poweredByUrl", poweredByUrl[i]);
                map.put("poweredByImage", poweredByImage[i]);
                map.put("poweredByAltText", poweredByAltText[i]);
                poweredBy.add(map);
            });
        } else {
            Map<String, String> map = new HashMap<>();
            map.put("poweredByUrl", "https://www.sakailms.org/");
            map.put("poweredByImage", "/library/image/poweredBySakai.png");
            map.put("poweredByAltText", "Powered by Sakai");
            poweredBy.add(map);
        }

        portalContext = config.getInitParameter("portal.context");
        if (StringUtils.isBlank(portalContext)) portalContext = DEFAULT_PORTAL_CONTEXT;

        siteHelper = new PortalSiteHelperImpl(this, serverConfigurationService.getBoolean(PROP_USE_PAGE_ALIAS, false));

        basicAuth = new BasicAuth();
        basicAuth.init();

        enableDirect = portalService.isEnableDirect();

        // do this before adding handlers to prevent handlers registering 2
        // times.
        // if the handlers were already there they will be re-registered,
        // but when they are added again, they will be replaced.
        // warning messages will appear, but the end state will be the same.
        portalService.addPortal(this);

        siteHandler = new SiteHandler();
        addHandler(siteHandler);
        worksiteHandler = new WorksiteHandler();
        addHandler(worksiteHandler);
        addHandler(new SiteResetHandler());
        addHandler(new ToolHandler());
        addHandler(new ToolResetHandler());
        addHandler(new PageResetHandler());
        addHandler(new PageHandler());
        addHandler(new WorksiteResetHandler());
        addHandler(new NavLoginHandler());
        addHandler(new PresenceHandler());
        addHandler(new HelpHandler());
        addHandler(new ReLoginHandler());
        addHandler(new LoginHandler());
        addHandler(new XLoginHandler());
        addHandler(new LogoutHandler());
        addHandler(new ErrorDoneHandler());
        addHandler(new ErrorReportHandler());
        addHandler(new StaticScriptsHandler());
        addHandler(new DirectToolHandler());
        addHandler(new RoleSwitchHandler());
        addHandler(new RoleSwitchOutHandler());
        addHandler(new TimeoutDialogHandler());
        addHandler(new JoinHandler());
        addHandler(new FavoritesHandler());
        addHandler(new GenerateBugReportHandler());
    }

    @Override
    public void destroy() {
        log.info("destroy()");
        portalService.removePortal(this);
        super.destroy();
    }

    @Override
    public void doError(HttpServletRequest req, HttpServletResponse res, Session session, int mode)
            throws ToolException, IOException {
        if (threadLocalManager.get(ATTR_ERROR) == null) {
            threadLocalManager.set(ATTR_ERROR, ATTR_ERROR);

            // send to the error site
            switch (mode) {
                case ERROR_SITE: {
                    // This preseves the "bad" origin site ID.
                    String[] parts = getParts(req);
                    if (parts.length >= 3) {
                        String siteId = parts[2];
                        threadLocalManager.set(PortalService.SAKAI_PORTAL_ORIGINAL_SITEID, siteId);
                    }
                    siteHandler.doGet(parts, req, res, session, "!error");
                    break;
                }
                case ERROR_WORKSITE: {
                    worksiteHandler.doWorksite(req, res, session, "!error", null, req.getContextPath() + req.getServletPath());
                    break;
                }
            }
            return;
        }

        // form a context-sensitive title
        String title = serviceName + " : Portal";

        // start the response
        PortalRenderContext rcontext = startPageContext("", title, null, req, null);

        showSession(rcontext);

        showSnoop(rcontext, getServletConfig(), req);

        sendResponse(rcontext, res, "error", null);
    }

    private void showSnoop(PortalRenderContext rcontext, ServletConfig servletConfig, HttpServletRequest req) {

        rcontext.put("snoopRequest", req.toString());

        if (servletConfig != null) {
            Map<String, Object> initParams = new HashMap<>();
            servletConfig.getInitParameterNames().asIterator().forEachRemaining(p -> initParams.put(p, servletConfig.getInitParameter(p)));
            rcontext.put("snoopServletConfigParams", initParams);
        }

        rcontext.put("snoopRequest", req);

        Map<String, Object> headerParams = new HashMap<>();
        req.getHeaderNames().asIterator().forEachRemaining(p -> headerParams.put(p, req.getHeader(p)));
        rcontext.put("snoopRequestHeaders", headerParams);

        Map<String, Object> requestParamsSingle = new HashMap<>();
        Map<String, Object> requestParamsMulti = new HashMap<>();
        req.getParameterNames().asIterator().forEachRemaining(p ->
        {
            String[] values = req.getParameterValues(p);
            if (values.length == 1) {
                requestParamsSingle.put(p, values[0]);
            } else if (values.length > 1) {
                StringBuilder sb = new StringBuilder();
                Arrays.stream(values).forEach(v -> sb.append(v).append("           "));
                requestParamsMulti.put(p, sb.toString());
            }
        });
        rcontext.put("snoopRequestParamsSingle", requestParamsSingle);
        rcontext.put("snoopRequestParamsMulti", requestParamsMulti);

        Map<String, Object> requestAttributes = new HashMap<>();
        req.getAttributeNames().asIterator().forEachRemaining(p -> requestAttributes.put(p, req.getAttribute(p)));
        rcontext.put("snoopRequestAttr", requestAttributes);
    }

    protected void doThrowableError(HttpServletRequest req, HttpServletResponse res, Throwable t) {
        ErrorReporter err = new ErrorReporter();
        err.report(req, res, t);
    }

    @Override
    public PortalRenderContext includePortal(HttpServletRequest req,
                                             HttpServletResponse res,
                                             Session session,
                                             String siteId,
                                             String toolId,
                                             String toolContextPath,
                                             String prefix,
                                             boolean doPages,
                                             boolean resetTools,
                                             boolean includeSummary,
                                             boolean expandSite)
            throws IOException {
        // find the site, for visiting
        Site site = null;
        try {
            site = siteHelper.getSiteVisit(siteId);
        } catch (IdUnusedException e) {
            log.debug("Unable to find site: {}, {}", siteId, e.toString());
            siteId = null;
            toolId = null;
        } catch (PermissionException e) {
            if (session.getUserId() == null) {
                log.debug("No permission for anonymous user to view site: [{}], {}", siteId, e.toString());
            } else {
                log.debug("No permission to view site: [{}], {}", siteId, e.toString());
            }
            siteId = null;
            toolId = null; // Tool needs the site and needs it to be visitable
        }

        // Get the Tool Placement
        ToolConfiguration placement = null;
        if (site != null && toolId != null) {
            placement = siteService.findTool(toolId);
            if (placement == null) {
                log.debug("Unable to find tool placement {}", toolId);
                toolId = null;
            }

            boolean thisTool = siteHelper.allowTool(site, placement);
            if (!thisTool) {
                log.debug("No permission to view tool placement {}", toolId);
                placement = null;
            }
        }

        // form a context-sensitive title
        String title = serviceName;
        if (site != null) {
            // SAK-29138
            title = title + ":" + siteHelper.getUserSpecificSiteTitle(site, false);
            if (placement != null) title = title + " : " + placement.getTitle();
        }

        // start the response
        String siteType = null;
        String siteSkin = null;
        if (site != null) {
            siteType = calcSiteType(siteId);
            siteSkin = site.getSkin();
        }

        PortalRenderContext rcontext = startPageContext(siteType, title, siteSkin, req, site);

        // Make the top Url where the "top" url is
        String portalTopUrl = RequestFilter.serverUrl(req) + portalPath + "/";
        if (prefix != null) portalTopUrl = portalTopUrl + prefix + "/";

        rcontext.put("portalTopUrl", portalTopUrl);
        rcontext.put("loggedIn", StringUtils.isNotBlank(session.getUserId()));
        rcontext.put("siteId", siteId);

        if (placement != null) {
            Map<String, Object> m = includeTool(res, req, placement);
            if (m != null) rcontext.put("currentPlacement", m);
        }

        if (site != null) {
            SiteView siteView = siteHelper.getSitesView(SiteView.View.CURRENT_SITE_VIEW, req, session, siteId);
            siteView.setPrefix(prefix);
            siteView.setResetTools(resetTools);
            siteView.setToolContextPath(toolContextPath);
            siteView.setIncludeSummary(includeSummary);
            siteView.setDoPages(doPages);
            if (!siteView.isEmpty()) {
                rcontext.put("currentSite", siteView.getRenderContextObject());
            }
        }

        SiteView siteView = siteHelper.getSitesView(SiteView.View.ALL_SITES_VIEW, req, session, siteId);
        siteView.setPrefix(prefix);
        siteView.setResetTools(resetTools);
        siteView.setToolContextPath(toolContextPath);
        siteView.setIncludeSummary(includeSummary);
        siteView.setDoPages(doPages);
        siteView.setExpandSite(expandSite);

        rcontext.put("allSites", siteView.getRenderContextObject());

        includeLogin(rcontext, req, session);
        includeBottom(rcontext, site);

        return rcontext;
    }

    @Override
    public boolean isPortletPlacement(Placement placement) {
        return ToolUtils.isPortletPlacement(placement);
    }

    @Override
    public Map<String, Object> includeTool(HttpServletResponse res, HttpServletRequest req, ToolConfiguration placement) throws IOException {
        boolean toolInline = "true".equals(threadLocalManager.get("sakai:inline-tool"));
        return includeTool(res, req, placement, toolInline);
    }

    @Override
    public Map<String, Object> includeTool(HttpServletResponse res, HttpServletRequest req, ToolConfiguration placement, boolean toolInline) throws IOException {
        // This will be called twice in the buffered scenario since we need to set
        // the session for neo tools with the session reset, helpurl and reseturl
        RenderResult renderResult = null;
        if (!toolInline) {
            // if not already inlined, allow a final chance for a tool to be inlined, based on its tool configuration
            // set renderInline = true to enable this, in the tool config
            renderResult = getInlineRenderingForTool(res, req, placement);
            if (renderResult != null) {
                log.debug("Using buffered content rendering");
                toolInline = true;
            }
        }

        // find the tool registered for this
        ActiveTool tool = activeToolManager.getActiveTool(placement.getToolId());
        if (tool == null) {
            return null;
        }

        // Get the Site - we could change the API call in the future to
        // pass site in, but that would break portals that extend Charon
        // so for now we simply look this up here.
        String siteId = placement.getSiteId();
        Site site;
        try {
            site = siteService.getSiteVisit(siteId);
        } catch (IdUnusedException | PermissionException e) {
            site = null;
        }

        // emit title information
        String titleString = Web.escapeHtml(placement.getTitle());
        String toolId = Web.escapeHtml(placement.getToolId());

        // for the reset button
        String toolUrl = toolUrlPrefix + "/" + Web.escapeUrl(placement.getId()) + "/";
        log.debug("includeTool toolInline={} toolUrl={}", toolInline, toolUrl);

        // Reset is different (and awesome) when inlining
        if (toolInline) {
            String newUrl = ToolUtils.getPageUrlForTool(req, site, placement);
            if (newUrl != null) toolUrl = newUrl;
        }

        // Reset the tool state if requested
        if (portalService.isResetRequested(req)) {
            Session s = sessionManager.getCurrentSession();
            ToolSession ts = s.getToolSession(placement.getId());
            ts.clearAttributes();
            portalService.setResetState(null);
            log.debug("includeTool state reset");
        }

        boolean showResetButton = !"false".equals(placement.getConfig().getProperty(
                Portal.TOOLCONFIG_SHOW_RESET_BUTTON));

        String resetActionUrl = PortalStringUtil.replaceFirst(toolUrl, "/tool/", "/tool-reset/");
        log.debug("includeTool resetActionUrl={}", resetActionUrl);

        // SAK-20462 - Pass through the sakai_action parameter
        String sakaiAction = req.getParameter("sakai_action");
        if (sakaiAction != null && sakaiAction.matches(".*[\"'<>].*")) sakaiAction = null;
        if (sakaiAction != null) resetActionUrl = URLUtils.addParameter(resetActionUrl, "sakai_action", sakaiAction);

        // Reset is different for Portlets
        if (isPortletPlacement(placement)) {
            resetActionUrl = RequestFilter.serverUrl(req) +
                    portalPath +
                    URLUtils.getSafePathInfo(req) +
                    "?sakai.state.reset=true";
        }

        // for the help button
        // get the help document ID from the tool config (tool registration
        // usually).
        // The help document ID defaults to the tool ID
        boolean helpEnabledInTool = !"false".equals(placement.getConfig().getProperty(Portal.TOOLCONFIG_SHOW_HELP_BUTTON));
        boolean showHelpButton = displayHelpIcon && helpEnabledInTool;

        String helpActionUrl = "";
        if (showHelpButton) {
            String helpDocUrl = placement.getConfig().getProperty(
                    Portal.TOOLCONFIG_HELP_DOCUMENT_URL);
            String helpDocId = placement.getConfig().getProperty(
                    Portal.TOOLCONFIG_HELP_DOCUMENT_ID);
            if (helpDocUrl != null && !helpDocUrl.isEmpty()) {
                helpActionUrl = helpDocUrl;
            } else {
                if (helpDocId == null || helpDocId.isEmpty()) {
                    helpDocId = tool.getId();
                }
                helpActionUrl = serverConfigurationService.getHelpUrl(helpDocId);
            }
        }

        Map<String, Object> toolMap = new HashMap<>();
        toolMap.put("toolInline", toolInline);

        // For JSR-168 portlets - this gets the content
        // For legacy tools, this returns the "<iframe" bit
        // For buffered legacy tools - the buffering is done outside of this

        if (renderResult == null) {
            //standard iframe
            log.debug("Using standard iframe rendering");
            renderResult = toolRenderService.render(this, placement, req, res, getServletContext());
        }

        if (renderResult.getJSR168HelpUrl() != null) {
            toolMap.put("toolJSR168Help", RequestFilter.serverUrl(req) + renderResult.getJSR168HelpUrl());
        }

        // Must have site.upd to see the Edit button
        if (renderResult.getJSR168EditUrl() != null && site != null) {
            if (securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                String editUrl = RequestFilter.serverUrl(req) + renderResult.getJSR168EditUrl();
                toolMap.put("toolJSR168Edit", editUrl);
                toolMap.put("toolJSR168EditEncode", URLUtils.encodeUrl(editUrl));
            }
        }

        toolMap.put("toolRenderResult", renderResult);
        toolMap.put("hasRenderResult", Boolean.TRUE);
        toolMap.put("toolUrl", toolUrl);

        // Allow a tool to suppress the rendering of its title nav. Defaults to false if not specified, and title nav is rendered.
        // Set suppressTitle = true to suppress
        boolean suppressTitle = BooleanUtils.toBoolean(placement.getConfig().getProperty("suppressTitle"));
        toolMap.put("suppressTitle", suppressTitle);

        Session s = sessionManager.getCurrentSession();
        ToolSession ts = s.getToolSession(placement.getId());

        if (isPortletPlacement(placement)) {
            // If the tool has requested it, pre-fetch render output.
            String doPreFetch = placement.getConfig().getProperty(Portal.JSR_168_PRE_RENDER);
            if (!"false".equals(doPreFetch)) {
                try {
                    renderResult.getContent();
                } catch (Throwable t) {
                    ErrorReporter err = new ErrorReporter();
                    String str = err.reportFragment(req, res, t);
                    renderResult.setContent(str);
                }
            }

            toolMap.put("toolPlacementIDJS", "_self");
            toolMap.put("isPortletPlacement", Boolean.TRUE);
        } else {
            toolMap.put("toolPlacementIDJS", Web.escapeJavascript("Main" + placement.getId()));
        }
        toolMap.put("toolResetActionUrl", resetActionUrl);
        toolMap.put("toolResetActionUrlEncode", URLUtils.encodeUrl(resetActionUrl));
        toolMap.put("toolTitle", titleString);
        toolMap.put("toolTitleEncode", URLUtils.encodeUrl(titleString));
        toolMap.put("toolShowResetButton", Boolean.valueOf(showResetButton));
        toolMap.put("toolShowHelpButton", Boolean.valueOf(showHelpButton));
        toolMap.put("toolHelpActionUrl", helpActionUrl);
        toolMap.put("toolId", toolId);
        toolMap.put("toolInline", Boolean.valueOf(toolInline));

        String directToolUrl = portalUrl + "/" + DirectToolHandler.URL_FRAGMENT + "/" + Web.escapeUrl(placement.getId()) + "/";
        toolMap.put("directToolUrl", directToolUrl);

        //props to enable/disable the display on a per tool/placement basis
        //will be displayed if not explicitly disabled in the tool/placement properties
        boolean showDirectToolUrl = !"false".equals(placement.getConfig().getProperty(Portal.TOOL_DIRECTURL_ENABLED_PROP));
        toolMap.put("showDirectToolUrl", showDirectToolUrl);

        return toolMap;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        int stat = PortalHandler.NEXT;
        try {
            basicAuth.doLogin(req);
            if (!toolRenderService.preprocess(this, req, res, getServletContext())) {
                return;
            }

            // Check to see if the pre-process step has redirected us - if so,
            // our work is done here - we will likely come back again to finish
            // our
            // work.
            if (res.isCommitted()) {
                return;
            }

            // get the Sakai session
            Session session = sessionManager.getCurrentSession();

            // recognize what to do from the path
            String option = URLUtils.getSafePathInfo(req);

            String[] parts = getParts(req);

            Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);

            // begin SAK-19089
            // if not logged in and accessing "/", redirect to gatewaySiteUrl
            if ((gatewaySiteUrl != null) && (option == null || "/".equals(option)) && (session.getUserId() == null)) {
                // redirect to gatewaySiteURL
                res.sendRedirect(gatewaySiteUrl);
                return;
            }
            // end SAK-19089

            // Look up the handler and dispatch
            PortalHandler ph = handlerMap.get(parts[1]);
            if (ph != null) {
                stat = ph.doGet(parts, req, res, session);
                if (res.isCommitted()) {
                    if (stat != PortalHandler.RESET_DONE) {
                        portalService.setResetState(null);
                    }
                    return;
                }
            }
            if (stat == PortalHandler.NEXT) {

                for (PortalHandler portalHandler : handlerMap.values()) {
                    ph = portalHandler;
                    stat = ph.doGet(parts, req, res, session);
                    if (res.isCommitted()) {
                        if (stat != PortalHandler.RESET_DONE) {
                            portalService.setResetState(null);
                        }
                        return;
                    }
                    // this should be
                    if (stat != PortalHandler.NEXT) {
                        break;
                    }
                }
            }
            if (stat == PortalHandler.NEXT) {
                doError(req, res, session, Portal.ERROR_SITE);
            }

        } catch (Throwable t) {
            doThrowableError(req, res, t);
        }

        // Make sure to clear any reset State at the end of the request unless
        // we *just* set it
        if (stat != PortalHandler.RESET_DONE) {
            portalService.setResetState(null);
        }
    }

    private String[] getParts(HttpServletRequest req) {

        String option = URLUtils.getSafePathInfo(req);
        //FindBugs thinks this is not used but is passed to the portal handler
        String[] parts;

        if (option == null || "/".equals(option)) {
            // Use the default handler prefix
            parts = new String[] {"", handlerPrefix};
        } else {
            //get the parts (the first will be "")
            parts = option.split("/");
        }
        return parts;
    }

    public void doLogin(HttpServletRequest req, HttpServletResponse res, Session session, String returnPath, boolean skipContainer)
            throws ToolException {
        try {
            if (basicAuth.doAuth(req, res)) {
                log.debug("BASIC Auth Request Sent to the Browser");
                return;
            }
        } catch (IOException ioe) {
            log.debug("Could not complete login, {}", ioe.toString());
            throw new ToolException(ioe);
        }

        // setup for the helper if needed (Note: in session, not tool session,
        // special for Login helper)
        // Note: always set this if we are passed in a return path... a blank
        // return path is valid... to clean up from
        // possible abandened previous login attempt -ggolden
        if (returnPath != null) {
            // where to go after
            String returnUrl = Web.returnUrl(req, returnPath);
            if (req.getQueryString() != null) {
                returnUrl += "?" + req.getQueryString();
            }
            session.setAttribute(Tool.HELPER_DONE_URL, returnUrl);
        }

        ActiveTool tool = activeToolManager.getActiveTool("sakai.login");

        // to skip container auth for this one, forcing things to be handled
        // internaly, set the "extreme" login path

        String loginPath = (!forceContainer && skipContainer ? "/xlogin" : "/relogin");

        String context = req.getContextPath() + req.getServletPath() + loginPath;

        tool.help(req, res, context, loginPath);
    }

    @Override
    public void doLogout(HttpServletRequest req, HttpServletResponse res, Session session, String returnPath)
            throws ToolException {

        // SAK-16370 to allow multiple logout urls
        String loggedOutUrl;
        String userType = userDirectoryService.getCurrentUser().getType();
        if (userType == null) {
            loggedOutUrl = portalLogOutUrl;
        } else {
            loggedOutUrl = serverConfigurationService.getString("loggedOutUrl." + userType, portalLogOutUrl);
        }

        if (returnPath != null) {
            loggedOutUrl = loggedOutUrl + returnPath;
        }
        session.setAttribute(Tool.HELPER_DONE_URL, loggedOutUrl);

        ActiveTool tool = activeToolManager.getActiveTool("sakai.login");
        String context = req.getContextPath() + req.getServletPath() + "/logout";
        tool.help(req, res, context, "/logout");
    }

    public PortalRenderContext startPageContext(String siteType, String title, String skin, HttpServletRequest request, Site site) {
        PortalRenderEngine rengine = portalService.getRenderEngine(portalContext, request);
        PortalRenderContext rcontext = rengine.newRenderContext(request);

        skin = getSkin(skin);

        rcontext.put("pageSkinRepo", skinRepo);
        rcontext.put("pageSkin", skin);
        rcontext.put("pageTitle", Web.escapeHtml(title));
        rcontext.put("pageScriptPath", PortalUtils.getScriptPath());
        rcontext.put("pageWebjarsPath", PortalUtils.getWebjarsPath());
        rcontext.put("portalCDNPath", PortalUtils.getCDNPath());
        rcontext.put("portalCDNQuery", PortalUtils.getCDNQuery());
        rcontext.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("Portal"));
        rcontext.put("pageTop", Boolean.valueOf(true));
        rcontext.put("rloader", MESSAGES);

        rcontext.put("serviceName", serverConfigurationService.getString("ui.service"));

        // Allow for inclusion of extra header code via property
        rcontext.put("includeExtraHead", includeExtraHead);

        rcontext.put("googleAnonymizeIp", googleAnonIp);

        if (universalAnalyticsId != null) {
            rcontext.put("googleUniversalAnalyticsId", universalAnalyticsId);
        }
        if (googleGA4Id != null) {
            rcontext.put("googleGA4Id", googleGA4Id);
        }

        //SAK-29668
        if (googleTagManagerContainerId != null) {
            rcontext.put("googleTagManagerContainerId", googleTagManagerContainerId);
        }


        String userId = null;
        String userEid = null;
        String userType = null;
        String roleId = null;
        String editorType = null;
        boolean loggedIn = false;

        User currentUser = userDirectoryService.getCurrentUser();
        if (currentUser != null && StringUtils.isNotBlank(currentUser.getId())) {
            userId = currentUser.getId();
            userEid = currentUser.getEid();
            userType = currentUser.getType();

            loggedIn = true;
            if (site != null) {
                Role role = site.getUserRole(userId);
                if (role != null) roleId = role.getId();
            }

            Preferences prefs = preferencesService.getPreferences(userId);
            editorType = prefs.getProperties(PreferencesService.EDITOR_PREFS_KEY).getProperty(PreferencesService.EDITOR_PREFS_TYPE);

            try {
                Site userSite = siteService.getSite(siteService.getUserSiteId(userId));
                if (userSite != null) {
                    // set profile urls
                    ToolConfiguration toolConfig = userSite.getToolForCommonId(preferenceToolId);
                    if (toolConfig != null) rcontext.put("prefsToolUrl", "/portal/directtool/" + toolConfig.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to get the users [{}] workspace, {}", userId, e.toString());
            }
        }

        rcontext.put("userId", userId);
        rcontext.put("userEid", userEid);
        rcontext.put("userType", userType);
        rcontext.put("loggedIn", loggedIn);
        rcontext.put("editorType", editorType);
        rcontext.put("userSiteRole", roleId);

        Session session = sessionManager.getCurrentSession();
        boolean justLoggedIn = session.getAttribute(Session.JUST_LOGGED_IN) != null;
        if (justLoggedIn) {
            session.removeAttribute(Session.JUST_LOGGED_IN);
        }
        rcontext.put("justLoggedIn", justLoggedIn);

        rcontext.put("loggedOutUrl", portalLogOutUrl);
        rcontext.put("portalPath", portalPath);
        rcontext.put("timeoutDialogEnabled", Boolean.valueOf(timeoutDialogEnabled));
        rcontext.put("timeoutDialogWarningSeconds", Integer.valueOf(timeoutDialogWarningSeconds));

        String cookieNoticeText = MESSAGES.getFormattedMessage("cookie_notice_text", portalCookieWarnUrl);
        rcontext.put("cookieNoticeEnabled", portalCookieWarnEnabled);
        rcontext.put("cookieNoticeText", cookieNoticeText);

        rcontext.put("pageSiteType", siteType);
        rcontext.put("toolParamResetState", portalService.getResetStateParam());

        // Get the tool header properties
        Properties props = toolHeaderProperties(request, skin, site, null);
        for (Object o : props.keySet()) {
            String key = (String) o;
            String keyund = key.replace('.', '_');
            rcontext.put(keyund, props.getProperty(key));
        }

        // Copy the minimization preferences to the context
        rcontext.put("portal_use_global_alert_message", Boolean.valueOf(enableGAM));
        // how many tools to show in portal pull downs
        rcontext.put("maxToolsInt", Integer.valueOf(portalToolMenuMax));

        rcontext.put("toolDirectUrlEnabled", portalDirectUrlToolEnabled);
        rcontext.put("toolShortUrlEnabled", portalShortUrlToolEnabled);

        rcontext.put("homeToolTitle", MESSAGES.getString("sitenav_home_tool_title"));

        rcontext.put("profileImageUrl", profileService.getProfileImageURL(userId, true));

        // Format properties for MathJax.
        rcontext.put("mathJaxFormat", mathJaxFormat);

        rcontext.put("tasksEnabled" , tasksEnabled);

        return rcontext;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        int stat = PortalHandler.NEXT;
        try {
            basicAuth.doLogin(req);
            if (!toolRenderService.preprocess(this, req, res, getServletContext())) {
                log.debug("POST FAILED, REDIRECT ?");
                return;
            }

            // Check to see if the pre-process step has redirected us - if so,
            // our work is done here - we will likely come back again to finish
            // our
            // work. T

            if (res.isCommitted()) {
                return;
            }

            // get the Sakai session
            Session session = sessionManager.getCurrentSession();

            // recognize what to do from the path
            String option = URLUtils.getSafePathInfo(req);

            // if missing, we have a stray post
            if ((option == null) || ("/".equals(option))) {
                doError(req, res, session, ERROR_SITE);
                return;
            }

            // get the parts (the first will be "")
            String[] parts = option.split("/");


            Map<String, PortalHandler> handlerMap = portalService.getHandlerMap(this);

            // Look up handler and dispatch
            PortalHandler ph = handlerMap.get(parts[1]);
            if (ph != null) {
                stat = ph.doPost(parts, req, res, session);
                if (res.isCommitted()) {
                    return;
                }
            }
            if (stat == PortalHandler.NEXT) {

                for (PortalHandler portalHandler : handlerMap.values()) {
                    ph = portalHandler;
                    stat = ph.doPost(parts, req, res, session);
                    if (res.isCommitted()) {
                        return;
                    }
                    // this should be
                    if (stat != PortalHandler.NEXT) {
                        break;
                    }

                }
            }
            if (stat == PortalHandler.NEXT) {
                doError(req, res, session, Portal.ERROR_SITE);
            }

        } catch (Throwable t) {
            doThrowableError(req, res, t);
        }
    }

    @Override
    public String getPlacement(HttpServletRequest req, HttpServletResponse res, Session session, String placementId, boolean doPage)
            throws ToolException {
        /*
         * Checks to see which form of tool or page placement we have. The normal
         * placement is a GUID. When the parameter sakai.site is added to
         * the request, the placement can be of the form sakai.resources. This
         * routine determines which form of the placement id, and if this is the
         * second type, performs the lookup and returns the GUID of the placement.
         * If we cannot resolve the placement, we simply return the passed in
         * placement ID. If we cannot visit the site, we send the user to login
         * processing and return null to the caller.
         *
         * If the reference is to the magical, indexical MyWorkspace site ('~')
         * then replace ~ by their Home.  Give them a chance to login
         * if necessary.
         */

        String siteId = req.getParameter(PARAM_SAKAI_SITE);
        if (siteId == null) return placementId; // Standard placement

        // Try to resolve the indexical MyWorkspace reference
        if ("~".equals(siteId)) {
            // If not logged in then allow login.  You can't go to your workspace if
            // you aren't known to the system.
            if (session.getUserId() == null) {
                doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
            }
            // If the login was successful lookup the myworkworkspace site.
            if (session.getUserId() != null) {
                siteId = getUserEidBasedSiteId(session.getUserEid());
            }
        }

        // find the site, for visiting
        // Sites like the !gateway site allow visits by anonymous
        Site site;
        try {
            site = getSiteHelper().getSiteVisit(siteId);
        } catch (IdUnusedException e) {
            return placementId; // cannot resolve placement
        } catch (PermissionException e) {
            // If we are not logged in, try again after we log in, otherwise
            // punt
            if (session.getUserId() == null) {
                doLogin(req, res, session, URLUtils.getSafePathInfo(req), false);
                return null;
            }
            return placementId; // cannot resolve placement
        }

        if (site == null) return placementId;
        ToolConfiguration toolConfig = site.getToolForCommonId(placementId);
        if (toolConfig == null) return placementId;

        if (doPage) {
            return toolConfig.getPageId();
        } else {
            return toolConfig.getId();
        }
    }

    public Properties toolHeaderProperties(HttpServletRequest req, String skin, Site site, Placement placement) {
        // Note - When modifying this code, make sure to review
        // org.sakaiproject.editor.EditorServlet.java
        // as it includes these values when it is running in its own frame
        Properties retval = new Properties();

        boolean isInlineReq = ToolUtils.isInlineRequest(req);
        String headCss = CSSUtils.getCssHead(skin, isInlineReq);

        Editor editor = portalService.getActiveEditor(placement);
        String preloadScript = editor.getPreloadScript() == null ? ""
                : "<script type=\"text/javascript\">"
                + editor.getPreloadScript()
                + "</script>\n";
        String editorScript = editor.getEditorUrl() == null ? ""
                : "<script type=\"text/javascript\" src=\""
                + PortalUtils.getCDNPath()
                + editor.getEditorUrl()
                + PortalUtils.getCDNQuery()
                + "\"></script>\n";
        String launchScript = editor.getLaunchUrl() == null ? ""
                : "<script type=\"text/javascript\" src=\""
                + PortalUtils.getCDNPath()
                + editor.getLaunchUrl()
                + PortalUtils.getCDNQuery()
                + "\"></script>\n";

        StringBuilder headJs = new StringBuilder();

        String contentItemUrl = portalService.getContentItemUrl(site);
        headJs.append("<script type=\"text/javascript\" src=\"");
        headJs.append(PortalUtils.getCDNPath());
        headJs.append("/library/js/headscripts.js");
        headJs.append(PortalUtils.getCDNQuery());
        headJs.append("\"></script>\n");

        String[] parts = getParts(req);
        if ((parts.length > 2) && (parts[1].equals("tool"))) {
            headJs.append("<script src=\"").append(PortalUtils.getWebjarsPath()).append("momentjs/").append(PortalUtils.MOMENTJS_VERSION).append("/min/moment-with-locales.min.js").append(PortalUtils.getCDNQuery()).append("\"></script>\n");
            headJs.append("<script type=\"module\" src=\"/webcomponents/bundles/sakai-date-picker.js?version=" + PortalUtils.getCDNQuery() + "\"></script>");
        }

        headJs.append("<script type=\"text/javascript\">var sakai = sakai || {}; sakai.editor = sakai.editor || {}; " +
                "sakai.editor.editors = sakai.editor.editors || {}; " +
                "sakai.editor.editors.ckeditor = sakai.editor.editors.ckeditor || {}; " +
                "sakai.locale = sakai.locale || {};\n");
        headJs.append("sakai.locale.userCountry = '").append(MESSAGES.getLocale().getCountry()).append("';\n");
        headJs.append("sakai.locale.userLanguage = '").append(MESSAGES.getLocale().getLanguage()).append("';\n");
        headJs.append("sakai.locale.userLocale = '").append(MESSAGES.getLocale().toString()).append("';\n");
        headJs.append("sakai.editor.collectionId = '").append(portalService.getBrowserCollectionId(placement)).append("';\n");
        headJs.append("sakai.editor.enableResourceSearch = ").append(EditorConfiguration.enableResourceSearch()).append(";\n");
        if (contentItemUrl != null) {
            headJs.append("sakai.editor.contentItemUrl = '").append(contentItemUrl).append("';\n");
        } else {
            headJs.append("sakai.editor.contentItemUrl = false;\n");
        }
        headJs.append("sakai.editor.siteToolSkin = '").append(CSSUtils.getCssToolSkin(skin)).append("';\n");
        headJs.append("sakai.editor.sitePrintSkin = '").append(CSSUtils.getCssPrintSkin(skin)).append("';\n");
        headJs.append("sakai.editor.sitePropertiesSkin = '").append(CSSUtils.getCssPropertiesSkin(skin)).append("';\n");
        headJs.append("sakai.editor.editors.ckeditor.browser = '").append(EditorConfiguration.getCKEditorFileBrowser()).append("';\n");
        headJs.append("</script>\n");
        headJs.append(preloadScript);
        headJs.append(editorScript);
        headJs.append(launchScript);

        Session s = sessionManager.getCurrentSession();
        String userWarning = (String) s.getAttribute("userWarning");
        if (StringUtils.isNotEmpty(userWarning)) {
            headJs.append("<script type=\"text/javascript\">");
            headJs.append("if ( window.self !== window.top ) {");
            headJs.append(" setTimeout(function(){ window.top.portal_check_pnotify() }, 3000);");
            headJs.append("}</script>");
        }

        if (site != null && mathJaxEnabled) {
            if (StringUtils.isNotBlank(mathJaxPath)) {
                if (BooleanUtils.toBoolean(site.getProperties().getProperty(Site.PROP_SITE_MATHJAX_ALLOWED))) {
                    // In order to get MathJax to work on the site, the config and source has to be added to the header.
                    Element config = new Element("script");
                    Element srcPath = new Element("script");
                    config.attr("src", "/library/js/mathjax-config.js" + PortalUtils.getCDNQuery());
                    srcPath.attr("type", "text/javascript").attr("src", mathJaxPath.trim());
                    headJs.append(config).append(srcPath);
                }
            }
        }

        String head = headCss + headJs;
        retval.setProperty("sakai.html.head", head);
        retval.setProperty("sakai.html.head.css", headCss);
        retval.setProperty("sakai.html.head.lang", MESSAGES.getLocale().getLanguage());
        retval.setProperty("sakai.html.head.css.base", CSSUtils.getCssToolBaseLink(skin, isInlineReq));
        retval.setProperty("sakai.html.head.css.skin", CSSUtils.getCssToolSkinLink(skin, isInlineReq));
        retval.setProperty("sakai.html.head.js", headJs.toString());

        return retval;
    }

    @Override
    public void setupForward(HttpServletRequest req, HttpServletResponse res, Placement p, String skin) {
        Site site = null;
        if (p != null) {
            try {
                site = siteService.getSite(p.getContext());
            } catch (IdUnusedException ex) {
                log.debug(ex.getMessage());
            }
        }

        // Get the tool header properties
        Properties props = toolHeaderProperties(req, skin, site, p);
        for (Object o : props.keySet()) {
            String key = (String) o;
            req.setAttribute(key, props.getProperty(key));
        }

        StringBuilder bodyonload = new StringBuilder();
        String bodyclass = "Mrphs-container";
        if (p != null) {
            String element = Web.escapeJavascript("Main" + p.getId());
            bodyonload.append("setMainFrameHeight('").append(element).append("');");
            bodyclass += " Mrphs-" + p.getToolId().replace(".", "-");
        }
        bodyonload.append("setFocus(focus_path);");
        req.setAttribute("sakai.html.body.onload", bodyonload.toString());
        req.setAttribute("sakai.html.body.class", bodyclass);

        portalService.getRenderEngine(portalContext, req).setupForward(req, res, p, skin);
    }

    private String fixPath1(String s, String c, StringBuilder ctx) {
        // SAK-28086 - Wrapped Requests have issues with NATIVE_URL
        if (s != null && s.startsWith(c)) {
            int i = s.indexOf("/", 6);
            if (i >= 0) {
                ctx.append(s, 0, i);
                s = s.substring(i);
            } else {
                ctx.append(s);
                s = null;
            }
        }
        return s;
    }

    private String fixPath(String s, StringBuilder ctx) {
        s = fixPath1(s, "/site/", ctx);
        s = fixPath1(s, "/tool/", ctx);
        s = fixPath1(s, "/page/", ctx);
        return s;
    }

    @Override
    public void forwardTool(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, Placement p, String skin, String toolContextPath, String toolPathInfo)
            throws ToolException {
        // SAK-29656 - Make sure the request URL and toolContextPath treat tilde encoding the same way
        //
        // Since we cannot easily change what the request object already knows as its URL,
        // we patch the toolContextPath to match the tilde encoding in the request URL.
        //
        // This is what we would see in Chrome and Firefox.  Firefox fails with Wicket
        // Chrome: forwardtool call http://localhost:8080/portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf
        //    toolPathInfo null ctx /portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf
        // Firefox: http://localhost:8080/portal/site/%7ecsev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf/
        //    toolPathInfo null ctx /portal/site/~csev/tool/aaf64e38-00df-419a-b2ac-63cf2d7f99cf

        String reqUrl = req.getRequestURL().toString();
        if (!reqUrl.contains(toolContextPath)) {
            log.debug("Mismatch between request url {} and toolContextPath {}", reqUrl, toolContextPath);
            if (toolContextPath.indexOf("/~") > 0 && reqUrl.indexOf("/~") < 1) {
                if (reqUrl.indexOf("/%7e") > 0) {
                    toolContextPath = toolContextPath.replace("/~", "/%7e");
                } else {
                    toolContextPath = toolContextPath.replace("/~", "/%7E");
                }
            } else if (toolContextPath.indexOf(" ") > 0 && reqUrl.indexOf(" ") < 1) {
                toolContextPath = toolContextPath.replace(" ", "%20");
            }
        }
        log.debug("forwardtool call {} toolPathInfo {} ctx {}", req.getRequestURL(), toolPathInfo, toolContextPath);

        // if there is a stored request state, and path, extract that from the
        // session and reinstance it

        StringBuilder ctx = new StringBuilder(toolContextPath);
        toolPathInfo = fixPath(toolPathInfo, ctx);
        toolContextPath = ctx.toString();
        boolean needNative;

        // let the tool do the work (forward)
        if (enableDirect) {
            StoredState ss = portalService.getStoredState();
            if (ss == null || !toolContextPath.equals(ss.getToolContextPath())) {
                setupForward(req, res, p, skin);

                req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
                log.debug("tool forward 1 {} context {}", toolPathInfo, toolContextPath);

                needNative = (req.getAttribute(Tool.NATIVE_URL) != null);
                if (needNative) req.removeAttribute(Tool.NATIVE_URL);

                tool.forward(req, res, p, toolContextPath, toolPathInfo);
                if (needNative) req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
            } else {
                log.debug("Restoring StoredState [{}]", ss);
                HttpServletRequest sreq = ss.getRequest(req);
                Placement splacement = ss.getPlacement();
                StringBuilder sctx = new StringBuilder(ss.getToolContextPath());
                String stoolPathInfo = fixPath(ss.getToolPathInfo(), sctx);
                String stoolContext = sctx.toString();

                ActiveTool stool = activeToolManager.getActiveTool(p.getToolId());
                String sskin = ss.getSkin();
                setupForward(sreq, res, splacement, sskin);
                req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
                log.debug("tool forward 2 {} context {}", stoolPathInfo, stoolContext);
                needNative = (sreq.getAttribute(Tool.NATIVE_URL) != null);
                if (needNative)
                    sreq.removeAttribute(Tool.NATIVE_URL);
                stool.forward(sreq, res, splacement, stoolContext, stoolPathInfo);
                if (needNative)
                    sreq.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
                // this is correct as we have checked the context path of the
                // tool
                portalService.setStoredState(null);
            }
        } else {
            setupForward(req, res, p, skin);
            req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));
            log.debug("tool forward 3 {} context {}", toolPathInfo, toolContextPath);
            needNative = (req.getAttribute(Tool.NATIVE_URL) != null);
            if (needNative) req.removeAttribute(Tool.NATIVE_URL);
            tool.forward(req, res, p, toolContextPath, toolPathInfo);
            if (needNative) req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
        }

    }

    @Override
    public void forwardPortal(ActiveTool tool, HttpServletRequest req, HttpServletResponse res, ToolConfiguration p, String skin, String toolContextPath, String toolPathInfo)
            throws IOException {
        // if there is a stored request state, and path, extract that from the
        // session and reinstance it

        // generate the forward to the tool page placement
        String portalPlacementUrl = portalPath + getPortalPageUrl(p) + "?" + req.getQueryString();
        res.sendRedirect(portalPlacementUrl);
    }

    @Override
    public String getPortalPageUrl(ToolConfiguration p) {
        SitePage sitePage = p.getContainingPage();
        String page = getSiteHelper().lookupPageToAlias(p.getSiteId(), sitePage);
        if (page == null) {
            // fall back to using the page id.
            page = p.getPageId();
        }

        return "/site/" +
                p.getSiteId() +
                "/page/" +
                page;
    }

    @Override
    public String getServletInfo() {
        return "Sakai Charon Portal";
    }

    @Override
    public void includeBottom(PortalRenderContext rcontext, Site site) {
        if (rcontext.uses(INCLUDE_BOTTOM)) {
            String thisUser = sessionManager.getCurrentSessionUserId();

            // get user preferences
            Preferences preferences = preferencesService.getPreferences(thisUser);

            rcontext.put("showServerTime", showServerTime);

            if (showServerTime) {
                Calendar now = Calendar.getInstance();
                Date nowDate = new Date(now.getTimeInMillis());

                // first set server date and time
                TimeZone serverTz = TimeZone.getDefault();
                now.setTimeZone(serverTz);
                rcontext.put("serverTzDisplay", serverTz.getDisplayName(serverTz.inDaylightTime(nowDate), TimeZone.SHORT));
                rcontext.put("serverTzGMTOffset", String.valueOf(now.getTimeInMillis() + now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)));

                // get the Properties object that holds user's TimeZone preferences
                String preferredTzId = preferences.getProperties(TimeService.APPLICATION_ID) != null ? preferences.getProperties(TimeService.APPLICATION_ID).getProperty(TimeService.TIMEZONE_KEY) : serverTz.getID();

                // provide the user's preferred timezone information if it is different
                if (StringUtils.isNotBlank(preferredTzId) && !preferredTzId.equals(serverTz.getID())) {
                    log.debug("Fetched timezone [{}] from user [{}] preferences", preferredTzId, thisUser);
                    TimeZone preferredTz = TimeZone.getTimeZone(preferredTzId);
                    now.setTimeZone(preferredTz);
                    rcontext.put("showPreferredTzTime", true);
                    // now set up the portal information
                    rcontext.put("preferredTzDisplay", preferredTz.getDisplayName(preferredTz.inDaylightTime(nowDate), TimeZone.SHORT));
                    rcontext.put("preferredTzGMTOffset", String.valueOf(now.getTimeInMillis() + now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)));
                } else {
                    rcontext.put("showPreferredTzTime", false);
                }
            }

            rcontext.put("pagepopup", false);

            if (sakaiTutorialEnabled && thisUser != null && ! userDirectoryService.isRoleViewType(thisUser)) {
                String userTutorialPref = preferences.getProperties(TUTORIAL_PREFS) != null ? preferences.getProperties(TUTORIAL_PREFS).getProperty("tutorialFlag") : "";
                log.debug("Fetched tutorial config [{}] from user [{}] preferences", userTutorialPref, thisUser);
                if (!StringUtils.equals("1", userTutorialPref)) {
                    rcontext.put("tutorial", true);
                }
            }

            if (sakaiThemesEnabled) {
                rcontext.put("sakaiThemesEnabled", true);

                if (sakaiThemeSwitcherEnabled) {
                    rcontext.put("themeSwitcher", true);
                }

                if (sakaiThemesAutoDetectDarkEnabled) {
                    rcontext.put("themesAutoDetectDark", true);
                }
                String userTheme = preferences.getProperties(USER_SELECTED_UI_THEME_PREFS) != null ? preferences.getProperties(USER_SELECTED_UI_THEME_PREFS).getProperty("theme") : "";
                log.debug("Fetched theme config [{}] from user [{}] preferences", userTheme, thisUser);
                rcontext.put("userTheme", StringUtils.defaultIfEmpty(userTheme, "sakaiUserTheme-notSet"));
            }

            List<HashMap<String, String>> footerLinks = Arrays.stream(footerUrls)
                    .map(url -> {
                        String[] parts = StringUtils.split(url, ";");
                        if (parts.length < 2) {
                            return null;
                        } else {
                            HashMap<String, String> linkMap = new HashMap<>();
                            linkMap.put("text", parts[0]);
                            linkMap.put("href", parts[1]);
                            if (parts.length >= 3) {
                                linkMap.put("target", parts[2]);
                            }
                            return linkMap;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            rcontext.put("footerLinks", footerLinks);

            // Replace keyword in copyright message from sakai.properties
            // with the server's current year to auto-update of Copyright end date
            String copyright = copyrightText.replaceAll(SERVER_COPYRIGHT_CURRENT_YEAR_KEYWORD, Year.now().toString());

            rcontext.put("bottomNavPoweredBy", poweredBy);
            rcontext.put("bottomNavService", serviceName);
            rcontext.put("bottomNavCopyright", copyright);
            rcontext.put("bottomNavServiceVersion", serviceVersion);
            rcontext.put("bottomNavSakaiVersion", sakaiVersion);
            rcontext.put("bottomNavServer", serverId);
            rcontext.put("useBullhornAlerts", useBullhornAlerts);

            boolean hasNotifications = false;
            if (useBullhornAlerts && session.getUserId() != null) {
                try {
                    hasNotifications = userMessagingService.hasNotifications();
                } catch (Exception e) {
                    log.debug("Unable to determine notification state for user {}", session.getUserId(), e);
                }
            }
            rcontext.put("hasNotifications", hasNotifications);
            rcontext.put("chromeInfoUrl", serverConfigurationService.getString("notifications.chrome.info.url", ""));
            rcontext.put("firefoxInfoUrl", serverConfigurationService.getString("notifications.firefox.info.url", ""));
            rcontext.put("safariInfoUrl", serverConfigurationService.getString("notifications.safari.info.url", ""));
            rcontext.put("edgeInfoUrl", serverConfigurationService.getString("notifications.edge.info.url", ""));
            rcontext.put("faviconURL", favIconUrl);

            // SAK-25931 - Do not remove this from session here - removal is done by /direct
            Session s = sessionManager.getCurrentSession();
            String userWarning = (String) s.getAttribute("userWarning");
            rcontext.put("userWarning", Boolean.valueOf(StringUtils.isNotEmpty(userWarning)));

            if (paSystemEnabled) {
                rcontext.put("paSystemEnabled", true);
                rcontext.put("paSystem", paSystem);
            }
        }
    }

    @Override
    public void includeLogin(PortalRenderContext rcontext, HttpServletRequest req, Session session) {
        if (rcontext.uses(INCLUDE_LOGIN)) {
            // for the main login/out link
            String logInOutUrl = RequestFilter.serverUrl(req);
            String message = null;
            String image1 = null;

            // for a possible second link
            String logInOutUrl2 = null;
            String message2 = null;
            String image2 = null;
            String logoutWarningMessage = "";

            // for showing user display name and id next to logout (SAK-10492)
            String loginUserDispName = null;
            String loginUserDispId = null;
            String loginUserId = null;
            String loginUserFirstName = null;

            // check for the top.login (where the login fields are present
            // instead
            // of a login link, but ignore it if container.login is set
            boolean topLogin = this.topLogin;
            boolean containerLogin = this.containerLogin;
            if (containerLogin) topLogin = false;

            // if not logged in they get login
            if (session.getUserId() == null) {
                // we don't need any of this if we are doing top login
                if (!topLogin) {
                    logInOutUrl += portalPath + "/login";

                    // let the login url be overridden by configuration
                    String overrideLoginUrl = portalLoginUrl;
                    if (overrideLoginUrl != null) logInOutUrl = overrideLoginUrl;

                    // check for a login text override
                    message = portalLoginText;
                    if (message == null) message = MESSAGES.getString("log.login");

                    // check for an image for the login
                    image1 = portalLoginIcon;

                    // check for a possible second, xlogin link
                    if (portalXLoginEnabled) {
                        // get the text and image as configured
                        message2 = portalXLoginText;
                        if (message2 == null) message2 = MESSAGES.getString("log.xlogin");
                        image2 = portalXLoginIcon;
                        logInOutUrl2 = portalPath + "/xlogin";

                    }
                }
            }

            // if logged in they get logout
            else {
                logInOutUrl += portalPath + "/logout";

                // get current user display id and name
                if (displayUserloginInfo) {
                    User thisUser = userDirectoryService.getCurrentUser();
                    loginUserDispId = Validator.escapeHtml(thisUser.getDisplayId());
                    loginUserId = Validator.escapeHtml(thisUser.getId());
                    loginUserDispName = Validator.escapeHtml(thisUser.getDisplayName());
                    loginUserFirstName = Validator.escapeHtml(thisUser.getFirstName());
                }

                // check if current user is being impersonated (by become user/sutool)
                String impersonatorDisplayId = getImpersonatorDisplayId();
                if (!impersonatorDisplayId.isEmpty()) {
                    message = MESSAGES.getFormattedMessage("sit_return", impersonatorDisplayId);
                    rcontext.put("impersonatorDisplayId", impersonatorDisplayId);
                }

                // check for a logout text override
                if (message == null) {
                    message = StringUtils.isNotBlank(portalLogoutText) ? portalLogoutText : MESSAGES.getString("sit_log");
                }

                // check for an image for the logout
                image1 = portalLogoutIcon;

                // since we are doing logout, cancel top.login
                topLogin = false;

                logoutWarningMessage = portalLogoutConfirmation ? MESSAGES.getString("sit_logout_warn") : "";
            }
            rcontext.put("userIsLoggedIn", session.getUserId() != null);
            rcontext.put("loginTopLogin", Boolean.valueOf(topLogin));
            rcontext.put("logoutWarningMessage", logoutWarningMessage);
            rcontext.put("topLogin", topLogin);

            if (!topLogin) {

                rcontext.put("loginLogInOutUrl", logInOutUrl);
                rcontext.put("loginMessage", message);
                rcontext.put("loginImage1", image1);
                rcontext.put("loginHasImage1", Boolean.valueOf(image1 != null));
                rcontext.put("loginLogInOutUrl2", logInOutUrl2);
                rcontext.put("loginHasLogInOutUrl2", Boolean.valueOf(logInOutUrl2 != null));
                rcontext.put("loginMessage2", message2);
                rcontext.put("loginImage2", image2);
                rcontext.put("loginHasImage2", Boolean.valueOf(image2 != null));
                // put out the links version

                // else put out the fields that will send to the login interface
            } else {

                rcontext.put("loginPortalPath", portalPath);

                // setup for the redirect after login
                session.setAttribute(Tool.HELPER_DONE_URL, portalUrl);
            }

            if (displayUserloginInfo) {
                rcontext.put("loginUserDispName", loginUserDispName);
                rcontext.put("loginUserFirstName", loginUserFirstName);
                rcontext.put("loginUserDispId", loginUserDispId);
                rcontext.put("loginUserId", loginUserId);
            }
            rcontext.put("displayUserloginInfo", displayUserloginInfo && loginUserDispId != null);
        }
    }


    /**
     * @param rcontext        the render context
     * @param res             the response
     * @param req             the request
     * @param session         the session
     * @param site            the site
     * @param page            the page in a site
     * @param toolContextPath the context path for a tool
     * @param portalPrefix    the prefix used by the portal
     * @throws IOException is thrown when an error occurs while reading the request or writing the response
     */
    public void includeWorksite(PortalRenderContext rcontext, HttpServletResponse res,
                                HttpServletRequest req, Session session, Site site, SitePage page,
                                String toolContextPath, String portalPrefix) throws IOException {
        worksiteHandler.includeWorksite(rcontext, res, req, session, site, page, toolContextPath, portalPrefix);
    }


    /**
     * Register a handler for a URL stub
     *
     * @param handler the handler to register with the portal
     */
    private void addHandler(PortalHandler handler) {
        portalService.addHandler(this, handler);
    }

    private void removeHandler(String urlFragment) {
        portalService.removeHandler(this, urlFragment);
    }

    /**
     * Send the POST request to login
     *
     * @param req     the request
     * @param res     the response
     * @param session the session
     * @throws ToolException a general exception that occurred in a tool
     */
    protected void postLogin(HttpServletRequest req, HttpServletResponse res, Session session, String loginPath) throws ToolException {
        ActiveTool tool = activeToolManager.getActiveTool("sakai.login");
        String context = req.getContextPath() + req.getServletPath() + "/" + loginPath;
        tool.help(req, res, context, "/" + loginPath);
    }

    /**
     * Output some session information
     *
     * @param rcontext The print writer
     */
    protected void showSession(PortalRenderContext rcontext) {
        // get the current user session information
        Session s = sessionManager.getCurrentSession();
        rcontext.put("sessionSession", s);
        ToolSession ts = sessionManager.getCurrentToolSession();
        rcontext.put("sessionToolSession", ts);
    }

    @Override
    public void sendResponse(PortalRenderContext rcontext, HttpServletResponse res, String template, String contentType)
            throws IOException {
        // headers
        res.setContentType(Objects.requireNonNullElse(contentType, "text/html; charset=UTF-8"));
        res.addHeader("Cache-Control", "no-store");

        // get the writer
        PrintWriter out = res.getWriter();

        try {
            PortalRenderEngine rengine = rcontext.getRenderEngine();
            rengine.render(template, rcontext, out);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render template ", e);
        }
    }

    @Override
    public String calcSiteType(String siteId) {
        String siteType = null;
        if (StringUtils.isNotBlank(siteId)) {
            if (siteService.isUserSite(siteId)) {
                siteType = "workspace";
            } else {
                try {
                    siteType = siteService.getSite(siteId).getType();
                } catch (IdUnusedException iue) {
                    log.debug("Could not fetch the site [{}], {}", siteId, iue.toString());
                }
            }
        }

        return StringUtils.trimToNull(siteType);
    }

    /**
     * Check for any just expired sessions and redirect
     *
     * @return true if we redirected, false if not
     */
    @Override
    public boolean redirectIfLoggedOut(HttpServletResponse res)
            throws IOException {
        // if we are in a newly created session where we had an invalid
        // (presumed timed out) session in the request,
        // send script to cause a sakai top level redirect
        if (threadLocalManager.get(sessionManager.CURRENT_INVALID_SESSION) != null) {
            sendPortalRedirect(res, portalLogOutUrl);
            return true;
        }

        return false;
    }

    /**
     * Send a redirect so our Portal window ends up at the url, via javascript.
     *
     * @param url The redirect url
     */
    protected void sendPortalRedirect(HttpServletResponse res, String url)
            throws IOException {
        PortalRenderContext rcontext = startPageContext("", null, null, null, null);
        rcontext.put("redirectUrl", url);
        sendResponse(rcontext, res, "portal-redirect", null);
    }

    @Override
    public String getUserEidBasedSiteId(String userId) {
        try {
            // use the user EID
            String eid = userDirectoryService.getUserEid(userId);
            return siteService.getUserSiteId(eid);
        } catch (UserNotDefinedException e) {
            log.warn("getUserEidBasedSiteId: user id not found for eid: " + userId);
            return siteService.getUserSiteId(userId);
        }
    }

    @Override
    public Cookie findCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * The name of the skin for a site adjusting for the overall skin/templates for the portal.
     *
     * @param siteId the site id to get the skin for
     * @return The skin
     */
    protected String getSiteSkin(String siteId) {
        String skin = siteService.getSiteSkin(siteId);
        return getSkin(skin);
    }

    /**
     * The name of the skin adjusting for the overall skin/templates for the portal.
     *
     * @param skin the name of the skin
     * @return The skin
     */
    protected String getSkin(String skin) {
        return CSSUtils.adjustCssSkinFolder(skin);
    }

    /**
     * Renders the content of a tool into a {@link BufferedContentRenderResult}
     *
     * @param res       {@link HttpServletResponse}
     * @param req       {@link HttpServletRequest}
     * @param placement {@link ToolConfiguration}
     * @return {@link BufferedContentRenderResult} with a head and body representing the appropriate bits for the tool or null if unable to render.
     */
    private RenderResult getInlineRenderingForTool(HttpServletResponse res, HttpServletRequest req, ToolConfiguration placement) {
        RenderResult rval = null;

        // allow a final chance for a tool to be inlined, based on it's tool configuration
        // set renderInline = true to enable this
        boolean renderInline = BooleanUtils.toBoolean(placement.getConfig().getProperty("renderInline"));

        if (renderInline) {

            //build tool context path directly to the tool
            String toolContextPath = req.getContextPath() + req.getServletPath() + "/site/" + placement.getSiteId() + "/tool/" + placement.getId();

            // setup the rest of the params
            String[] parts = getParts(req);
            String toolPathInfo = Web.makePath(parts, 5, parts.length);
            Session session = sessionManager.getCurrentSession();

            // get the buffered content
            Object buffer = this.siteHandler.bufferContent(req, res, session, placement.getId(), toolContextPath, toolPathInfo, placement);

            if (buffer instanceof Map) {
                Map<String, String> bufferMap = (Map<String, String>) buffer;
                rval = new BufferedContentRenderResult(placement, bufferMap.get("responseHead"), bufferMap.get("responseBody"));
            }
        }

        return rval;
    }

    /**
     * Checks if current user is being impersonated (via become user/sutool) and returns displayId of
     * the impersonator. Adapted from SkinnableLogin's isImpersonating()
     *
     * @return displayId of impersonator, or empty string if not being impersonated
     */
    private String getImpersonatorDisplayId() {
        Session currentSession = sessionManager.getCurrentSession();
        UsageSession originalSession = (UsageSession) currentSession.getAttribute(UsageSessionService.USAGE_SESSION_KEY);

        if (originalSession != null) {
            String originalUserId = originalSession.getUserId();
            if (!StringUtils.equals(currentSession.getUserId(), originalUserId)) {
                try {
                    User originalUser = userDirectoryService.getUser(originalUserId);
                    return originalUser.getDisplayId();
                } catch (UserNotDefinedException e) {
                    log.debug("Unable to retrieve user for id: {}", originalUserId);
                }
            }
        }
        return "";
    }
}
