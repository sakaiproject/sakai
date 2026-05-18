/**
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
package org.sakaiproject.site.tool.helper.order.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.model.ToolOrderPage;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.comparator.ToolTitleComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for Site Info's Tool Order helper.
 */
@Component
@Slf4j
public class SitePageEditHandler {

    private static final String TOOL_CFG_MULTI = "allowMultiple";
    private static final String HELPER_ID = "sakai.tool.helper.id";
    private static final String HELPER_TOOL_ID = "sakai-site-pageorder-helper";
    private static final String HELPER_SITE_INFO_MENU = HELPER_ID + ".siteInfoMenu";
    private static final String UNHIDEABLES_CFG = "poh.unhideables";
    private static final String UNEDITABLES_CFG = "poh.uneditables";
    private static final String PAGE_ADD = "pageorder.add";
    private static final String PAGE_DELETE = "pageorder.delete";
    private static final String PAGE_RENAME = "pageorder.rename";
    private static final String PAGE_SHOW = "pageorder.show";
    private static final String PAGE_HIDE = "pageorder.hide";
    private static final String PAGE_ENABLE = "pageorder.enable";
    private static final String PAGE_DISABLE = "pageorder.disable";
    private static final String SITE_REORDER = "pageorder.reorder";
    private static final String SITE_RESET = "pageorder.reset";
    private static final String MULTI_TOOLS = "sakai.site.multiPlacementTools";
    private static final String SITE_SETUP_TOOL = "sakai.sitesetup";
    private static final String SITE_INFO_TOOL = "sakai.siteinfo";
    private static final Set<String> DEFAULT_MULTI_TOOLS = new HashSet<>(Arrays.asList("sakai.news", "sakai.iframe"));
    private static final List<String> INSTRUCTOR_PERMISSIONS_ONLY = Arrays.asList(
            SiteService.SECURE_UPDATE_SITE, SiteService.SITE_VISIT, "rubrics.manager.view");

    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";
    public static final String DISABLE_ENABLED_CFG = "poh.allow.lock";
    public static final String HIDDEN_ENABLED_CFG = "poh.allow.hide";
    public static final String ALLOW_TITLE_EDIT_CFG = "org.sakaiproject.site.tool.helper.order.rsf.PageListProducer.allowTitleEdit";
    public static final String ALLOW_REORDER_CFG = "site-manage.pageorder.allowreorder";

    private SiteService siteService;
    private ToolManager toolManager;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private ContentHostingService contentHostingService;
    private AuthzGroupService authzGroupService;
    private EventTrackingService eventTrackingService;

    public Site getCurrentSite() {
        String siteId = getCurrentSiteId();
        if (StringUtils.isBlank(siteId)) {
            throw new IllegalStateException("No current site in context");
        }

        try {
            return siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalStateException("Unable to load site " + siteId, e);
        }
    }

    public boolean canUpdateCurrentSite() {
        String siteId = getCurrentSiteId();
        return StringUtils.isNotBlank(siteId) && siteService.allowUpdateSite(siteId);
    }

    public Site requireCurrentSite() {
        Site site = getCurrentSite();
        if (!siteService.allowUpdateSite(site.getId())) {
            throw new SecurityException("Current user may not update site " + site.getId());
        }
        return site;
    }

    public List<ToolOrderPage> getPages() {
        Site site = requireCurrentSite();
        List<SitePage> pages = site.getOrderedPages();
        List<ToolOrderPage> rows = new ArrayList<>(pages.size());

        for (int i = 0; i < pages.size(); i++) {
            rows.add(toToolOrderPage(site, pages.get(i), i == 0, i == pages.size() - 1));
        }

        return rows;
    }

    public ToolOrderPage getPage(String pageId) {
        Site site = requireCurrentSite();
        return toToolOrderPage(site, requirePage(site, pageId), false, false);
    }

    public boolean isReorderAllowed() {
        return serverConfigurationService.getBoolean(ALLOW_REORDER_CFG, true);
    }

    public boolean isSiteOrdered() {
        return requireCurrentSite().isCustomPageOrdered();
    }

    public void reorderPages(List<String> pageIds) {
        if (pageIds == null || pageIds.isEmpty()) {
            throw new IllegalArgumentException("Page order must include at least one page id");
        }

        if (!isReorderAllowed()) {
            throw new SecurityException("Page ordering is disabled by configuration");
        }

        Site site = requireCurrentSite();
        List<SitePage> currentPages = site.getOrderedPages();
        Set<String> currentIds = currentPages.stream().map(SitePage::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> requestedIds = new LinkedHashSet<>(pageIds);

        if (currentPages.size() != pageIds.size() || !currentIds.equals(requestedIds)) {
            throw new IllegalArgumentException("Submitted page order does not match the current site pages");
        }

        for (int i = 0; i < pageIds.size(); i++) {
            SitePage page = site.getPage(pageIds.get(i));
            page.setPosition(i);
        }

        site.setCustomPageOrdered(true);
        saveSite(site);
        eventTrackingService.post(eventTrackingService.newEvent(SITE_REORDER, "/site/" + site.getId(), false));
        markTopRefresh();
    }

    public ToolOrderPage renamePage(String pageId, String newTitle, String iframeSource) {
        Site site = requireCurrentSite();
        SitePage page = requirePage(site, pageId);
        if (!allowEdit(page)) {
            throw new SecurityException("Page title editing is disabled for page " + pageId);
        }

        if (StringUtils.isBlank(newTitle)) {
            resetTitle(site, page);
        } else {
            setTitle(site, page, newTitle);
        }

        setIframeSource(site, page, iframeSource);
        markTopRefresh();
        return toToolOrderPage(site, page, false, false);
    }

    public ToolOrderPage setPageVisible(String pageId, boolean visible) {
        Site site = requireCurrentSite();
        SitePage page = requirePage(site, pageId);

        if (!allowsHide(page)) {
            throw new SecurityException("Page visibility editing is disabled for page " + pageId);
        }

        try {
            if (visible) {
                showPage(site, pageId);
            } else {
                hidePage(site, pageId);
            }
        } catch (SakaiException e) {
            throw new IllegalStateException("Unable to update page visibility", e);
        }

        markTopRefresh();
        return toToolOrderPage(site, page, false, false);
    }

    public ToolOrderPage setPageEnabled(String pageId, boolean enabled) {
        Site site = requireCurrentSite();
        SitePage page = requirePage(site, pageId);

        if (!allowDisable(page)) {
            throw new SecurityException("Page access editing is disabled for page " + pageId);
        }

        try {
            if (enabled) {
                enablePage(site, pageId);
            } else {
                disablePage(site, pageId);
            }
        } catch (SakaiException e) {
            throw new IllegalStateException("Unable to update page access", e);
        }

        markTopRefresh();
        return toToolOrderPage(site, page, false, false);
    }

    public ToolOrderPage deletePage(String pageId) {
        Site site = requireCurrentSite();
        SitePage page = requirePage(site, pageId);
        ToolOrderPage row = toToolOrderPage(site, page, false, false);
        if (!row.isDeletable()) {
            throw new SecurityException("Page may not be deleted: " + pageId);
        }

        site.removePage(page);
        saveSite(site);
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_DELETE,
                "/site/" + site.getId() + "/page/" + page.getId(), false));
        markTopRefresh();
        return row;
    }

    public void resetOrder() {
        Site site = requireCurrentSite();
        site.setCustomPageOrdered(false);
        saveSite(site);
        eventTrackingService.post(eventTrackingService.newEvent(SITE_RESET, "/site/" + site.getId(), false));
        markTopRefresh();
    }

    public String getDoneUrl() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session == null) {
            return "/";
        }

        String doneUrl = (String) session.getAttribute(HELPER_TOOL_ID + Tool.HELPER_DONE_URL);
        if (StringUtils.isNotBlank(doneUrl)) {
            return doneUrl;
        }

        Tool currentTool = toolManager.getCurrentTool();
        doneUrl = currentTool == null ? null : (String) session.getAttribute(currentTool.getId() + Tool.HELPER_DONE_URL);
        return StringUtils.defaultIfBlank(doneUrl, "/");
    }

    public String getDoneBaseUrl() {
        String doneUrl = getDoneUrl();
        int queryStart = doneUrl.indexOf('?');
        return queryStart < 0 ? doneUrl : doneUrl.substring(0, queryStart);
    }

    public Object getSiteInfoMenu() {
        ToolSession session = sessionManager.getCurrentToolSession();
        return session == null ? null : session.getAttribute(HELPER_SITE_INFO_MENU);
    }

    public List<Tool> getAvailableTools() {
        Site site = requireCurrentSite();
        return getAvailableTools(site);
    }

    private List<Tool> getAvailableTools(Site site) {
        List<Tool> tools = new ArrayList<>();
        Set<String> categories = new HashSet<>();

        if (site.getType() == null || siteService.isUserSite(site.getId())) {
            categories.add("myworkspace");
        } else {
            categories.add(site.getType());
        }

        Set<Tool> toolRegistrations = toolManager.findTools(categories, null);
        Set<String> multiPlacementToolIds = serverConfigurationService.getCommaSeparatedListAsSet(MULTI_TOOLS);
        if (multiPlacementToolIds.isEmpty()) {
            multiPlacementToolIds = DEFAULT_MULTI_TOOLS;
        }

        SortedIterator sortedTools = new SortedIterator(toolRegistrations.iterator(), new ToolTitleComparator());
        while (sortedTools.hasNext()) {
            Tool tool = (Tool) sortedTools.next();
            if (tool != null && canAddTool(site, tool, multiPlacementToolIds)) {
                tools.add(tool);
            }
        }

        return tools;
    }

    public SitePage addPage(String toolId, String title) {
        Site site = requireCurrentSite();
        if (!isToolAvailable(site, toolId)) {
            throw new IllegalArgumentException("Tool is not available for site " + site.getId() + ": " + toolId);
        }

        try {
            SitePage page = site.addPage();
            page.setTitle(title);
            ToolConfiguration placement = page.addTool(toolId);
            saveSite(site);
            eventTrackingService.post(eventTrackingService.newEvent(PAGE_ADD,
                    "/site/" + site.getId() + "/page/" + page.getId()
                            + "/tool/" + toolId + "/placement/" + placement.getId(), false));
            markTopRefresh();
            return page;
        } catch (Exception e) {
            throw new IllegalStateException("Error adding page " + title, e);
        }
    }

    public boolean isVisible(SitePage page) {
        for (ToolConfiguration placement : page.getTools()) {
            Properties roleConfig = placement.getConfig();
            String visibility = roleConfig.getProperty(ToolManager.PORTAL_VISIBLE);

            if (!"false".equals(visibility)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEnabled(SitePage page) {
        return toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page);
    }

    public boolean allowsHide(SitePage page) {
        if (!serverConfigurationService.getBoolean(HIDDEN_ENABLED_CFG, true)) {
            return false;
        }

        Set<String> unhideables = serverConfigurationService.getCommaSeparatedListAsSet(UNHIDEABLES_CFG);
        for (ToolConfiguration placement : page.getTools()) {
            if (unhideables != null && unhideables.contains(placement.getToolId())) {
                return false;
            }
        }

        return true;
    }

    public boolean allowDisable(SitePage page) {
        if (!serverConfigurationService.getBoolean(DISABLE_ENABLED_CFG, true)) {
            return false;
        }

        List<String> permissions = getSingleToolPagePermissions(page).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return !(permissions.isEmpty() || permissions.stream().allMatch(INSTRUCTOR_PERMISSIONS_ONLY::contains));
    }

    public boolean allowEdit(SitePage page) {
        if (!serverConfigurationService.getBoolean(ALLOW_TITLE_EDIT_CFG, true)) {
            return false;
        }

        Set<String> uneditables = serverConfigurationService.getCommaSeparatedListAsSet(UNEDITABLES_CFG);
        if (uneditables == null || uneditables.isEmpty()) {
            return true;
        }

        for (ToolConfiguration placement : page.getTools()) {
            if (uneditables.contains(placement.getToolId())) {
                return false;
            }
        }

        return true;
    }

    public boolean isRequired(Site site, String toolId) {
        List<String> requiredTools;
        if (site.getType() == null || siteService.isUserSite(site.getId())) {
            requiredTools = serverConfigurationService.getToolsRequired("myworkspace");
        } else {
            requiredTools = serverConfigurationService.getToolsRequired(site.getType());
        }

        return requiredTools != null && requiredTools.contains(toolId);
    }

    public void saveSite(Site site) {
        try {
            siteService.save(site);
        } catch (IdUnusedException | PermissionException e) {
            throw new IllegalStateException("Error saving site " + site.getId(), e);
        }
    }

    private String getCurrentSiteId() {
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        Object helperSiteId = toolSession == null ? null : toolSession.getAttribute(HELPER_ID + ".siteId");
        if (helperSiteId != null) {
            return Objects.toString(helperSiteId, null);
        }

        return toolManager.getCurrentPlacement() == null ? null : toolManager.getCurrentPlacement().getContext();
    }

    private ToolOrderPage toToolOrderPage(Site site, SitePage page, boolean first, boolean last) {
        List<ToolConfiguration> tools = page.getTools();
        String toolId = tools.isEmpty() ? "unknown-tool" : tools.get(0).getToolId();
        String iframeSource = "";
        if (tools.size() == 1 && "sakai.iframe".equals(toolId)) {
            iframeSource = StringUtils.defaultString(tools.get(0).getPlacementConfig().getProperty("source"));
        }

        boolean visible = isVisible(page);
        boolean enabled = isEnabled(page);
        boolean singleTool = tools.size() == 1;
        boolean required = singleTool && isRequired(site, toolId);
        boolean siteInfoTool = SITE_SETUP_TOOL.equals(toolId) || SITE_INFO_TOOL.equals(toolId);

        ToolOrderPage row = new ToolOrderPage();
        row.setId(page.getId());
        row.setTitle(page.getTitle());
        row.setToolId(toolId);
        row.setToolIconClass("tool-order-tool-icon si si-" + toolId.replace('.', '-'));
        row.setIframeSource(iframeSource);
        row.setVisible(visible);
        row.setEnabled(enabled);
        row.setHidden(!visible && enabled);
        row.setLocked(!enabled);
        row.setAllowsHide(allowsHide(page));
        row.setAllowsLock(allowDisable(page));
        row.setAllowsEdit(allowEdit(page));
        row.setDeletable(singleTool && !required && !siteInfoTool);
        row.setIframe(singleTool && "sakai.iframe".equals(toolId));
        row.setFirst(first);
        row.setLast(last);
        return row;
    }

    private boolean canAddTool(Site site, Tool tool, Set<String> multiPlacementToolIds) {
        Properties config = tool.getRegisteredConfig();
        String allowMultiple = config.getProperty(TOOL_CFG_MULTI);
        return multiPlacementToolIds.contains(tool.getId())
                || "true".equals(allowMultiple)
                || site.getToolForCommonId(tool.getId()) == null;
    }

    private boolean isToolAvailable(Site site, String toolId) {
        return StringUtils.isNotBlank(toolId)
                && getAvailableTools(site).stream().anyMatch(tool -> toolId.equals(tool.getId()));
    }

    private SitePage requirePage(Site site, String pageId) {
        SitePage page = site.getPage(pageId);
        if (page == null) {
            throw new IllegalArgumentException("Page is not part of the current site: " + pageId);
        }
        return page;
    }

    private void setTitle(Site site, SitePage page, String newTitle) {
        String oldTitle = page.getTitle();
        page.setTitle(newTitle);
        page.setTitleCustom(true);

        if (page.getTools().size() == 1) {
            ToolConfiguration tool = page.getTools().get(0);
            tool.setTitle(newTitle);
        }

        saveSite(site);
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_RENAME,
                "/site/" + site.getId() + "/page/" + page.getId()
                        + "/old_title/" + oldTitle + "/new_title/" + page.getTitle(), false));
    }

    private void resetTitle(Site site, SitePage page) {
        String oldTitle = page.getTitle();
        page.setTitleCustom(false);
        String newTitle = page.getTitle();
        page.setTitle(newTitle);

        if (page.getTools().size() == 1) {
            ToolConfiguration tool = page.getTools().get(0);
            tool.setTitle(newTitle);
        }

        saveSite(site);
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_RENAME,
                "/site/" + site.getId() + "/page/" + page.getId()
                        + "/old_title/" + oldTitle + "/new_title/" + page.getTitle(), false));
    }

    private void setIframeSource(Site site, SitePage page, String iframeSource) {
        if (iframeSource == null || page.getTools().size() != 1) {
            return;
        }

        ToolConfiguration tool = page.getTools().get(0);
        if ("sakai.iframe".equals(tool.getToolId())) {
            tool.getPlacementConfig().setProperty("source", iframeSource);
            saveSite(site);
        }
    }

    private void disablePage(Site site, String pageId) throws SakaiException {
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_DISABLE,
                "/site/" + site.getId() + "/page/" + pageId, false));
        setEnabled(site, pageId, false);
        setVisibility(site, pageId, false);
    }

    private void enablePage(Site site, String pageId) throws SakaiException {
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_ENABLE,
                "/site/" + site.getId() + "/page/" + pageId, false));
        setEnabled(site, pageId, true);
        setVisibility(site, pageId, true);
    }

    private void hidePage(Site site, String pageId) throws SakaiException {
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_HIDE,
                "/site/" + site.getId() + "/page/" + pageId, false));
        setVisibility(site, pageId, false);
    }

    private void showPage(Site site, String pageId) throws SakaiException {
        eventTrackingService.post(eventTrackingService.newEvent(PAGE_SHOW,
                "/site/" + site.getId() + "/page/" + pageId, false));
        setVisibility(site, pageId, true);
        setEnabled(site, pageId, true);
    }

    private void setVisibility(Site site, String pageId, boolean visible) throws SakaiException {
        SitePage page = requirePage(site, pageId);

        for (ToolConfiguration placement : page.getTools()) {
            String toolId = placement.getToolId();
            Properties roleConfig = placement.getPlacementConfig();
            String visibility = roleConfig.getProperty(ToolManager.PORTAL_VISIBLE);
            boolean saveChanges = false;

            if ("false".equals(visibility) && visible) {
                visibility = "true";
                saveChanges = true;
            } else if (!"false".equals(visibility) && !visible) {
                visibility = "false";
                saveChanges = true;
            }

            if (saveChanges) {
                if (getSitePropertySpecialHidden() && "sakai.resources".equals(toolId)) {
                    String siteCollectionId = contentHostingService.getSiteCollection(placement.getSiteId());
                    try {
                        if ("true".equals(visibility)) {
                            contentHostingService.removeProperty(siteCollectionId, ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT);
                        } else {
                            contentHostingService.addProperty(siteCollectionId, ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
                        }
                    } catch (InUseException | ServerOverloadException e) {
                        log.warn("Exception occurred when attempting to add/remove hidden property from site collection {}", siteCollectionId, e);
                    }
                }
                roleConfig.setProperty(ToolManager.PORTAL_VISIBLE, visibility);
                placement.save();
            }
        }
    }

    private void setEnabled(Site site, String pageId, boolean enabled) throws SakaiException {
        SitePage page = requirePage(site, pageId);

        try {
            AuthzGroup authzGroup = authzGroupService.getAuthzGroup(site.getReference());
            List<String> permissions = getSingleToolPagePermissions(page).stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (!permissions.isEmpty()) {
                permissions.remove(SiteService.SECURE_UPDATE_SITE);
                permissions.remove(SiteService.SITE_VISIT);

                Set<Role> roles = getRolesWithout(authzGroup, SiteService.SECURE_UPDATE_SITE);
                for (Role role : roles) {
                    if (enabled) {
                        role.allowFunctions(permissions);
                    } else {
                        role.disallowFunctions(permissions);
                    }
                }
                authzGroupService.save(authzGroup);
            }
        } catch (GroupNotDefinedException | AuthzPermissionException e) {
            throw new SakaiException(e);
        }
    }

    private Set<Role> getRolesWithout(AuthzGroup authzGroup, String function) {
        Set<Role> roles = new HashSet<>(authzGroup.getRoles());
        roles.removeIf(role -> role.isAllowed(function));
        return roles;
    }

    private List<Set<String>> getSingleToolPagePermissions(SitePage page) {
        List<ToolConfiguration> tools = page.getTools();
        if (tools.size() == 1) {
            return toolManager.getRequiredPermissions(tools.get(0));
        }
        return Collections.emptyList();
    }

    private boolean getSitePropertySpecialHidden() {
        return serverConfigurationService.getBoolean(SiteConstants.SITE_PROPERTY_HIDE_RESOURCES_SPECIAL_HIDDEN,
                SiteConstants.SITE_PROPERTY_HIDE_RESOURCES_SPECIAL_HIDDEN_DEFAULT);
    }

    private void markTopRefresh() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session != null) {
            session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
        }
    }

    @Autowired
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Autowired
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    @Autowired
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Autowired
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Autowired
    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    @Autowired
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    @Autowired
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }
}
