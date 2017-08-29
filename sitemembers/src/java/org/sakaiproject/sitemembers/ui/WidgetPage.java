/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.sitemembers.ui;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.model.BasicConnection;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.sitemembers.SiteRole;
import org.sakaiproject.sitemembers.ui.components.ConnectionsGrid;
import org.sakaiproject.authz.cover.FunctionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Main page for the Site Members widget
 * Note: Far too much of this is too similar to the sitemembers widget and the
 * two should be refactored and combined.
 */
@Slf4j
public class WidgetPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
    private ProfileConnectionsLogic connectionsLogic;

    @SpringBean(name = "org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;

    @SpringBean(name = "org.sakaiproject.site.api.SiteService")
    private SiteService siteService;

    @SpringBean(name = "org.sakaiproject.user.api.UserDirectoryService")
    private UserDirectoryService userDirectoryService;

    @SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

    public static final String SITE_MEMBERS_HIDE = "sitemembers.hide";

    static {
        registerFunction(SITE_MEMBERS_HIDE);
    }

    /**
     * Class that contains a single site member (just the part needed)
     */
    public static class GridPerson implements Serializable{
        public String uuid;
        public String displayName;
        public String role;
        public int onlineStatus;
    }

    /**
     *
     * Can be overridden via:
     * widget.sitemembers.maxusers=60
     * widget.sitemembers.cols=4
     */
    int maxUsers = 60;
    int cols = 4;

    public WidgetPage() {
        log.debug("WidgetPage()");
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        // get maxUsers, cols
        this.maxUsers = this.serverConfigurationService.getInt("widget.sitemembers.maxusers", this.maxUsers);
        this.cols = this.serverConfigurationService.getInt("widget.sitemembers.cols", this.cols);

        // get current site id
        final String currentSiteId = this.toolManager.getCurrentPlacement().getContext();

        // get members who are hidden because they are in one of the special, hidden roles
        Site currentSite = null;
        try {
            currentSite = this.siteService.getSite(currentSiteId);
        } catch (final IdUnusedException e) {
            log.error("No site with id " + currentSiteId, e);
        }
        if(currentSite == null) {
            return;
        }

        boolean isCourse = currentSite.isType("course");

        // Get the list of hidden users
        final Set<String> hiddenUserIds = currentSite.getUsersIsAllowed(SITE_MEMBERS_HIDE);

        // Get the lists of the users of various types
        final List<GridPerson> instructors = getMembersWithRole(currentSite, SiteRole.INSTRUCTOR, hiddenUserIds);
        final List<GridPerson> tas = getMembersWithRole(currentSite, SiteRole.TA, hiddenUserIds);
        final List<GridPerson> students = getMembersWithRole(currentSite, SiteRole.STUDENT, hiddenUserIds);
        final List<GridPerson> finalList = new ArrayList<>();

        // Concat 3 list into 1
        finalList.addAll(instructors);
        finalList.addAll(tas);
        finalList.addAll(students);

        // add instructors grid
        add(new ConnectionsGrid("roster", Model.ofList(finalList), cols, isCourse) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public boolean isVisible() {
                return !((List<GridPerson>) getDefaultModelObject()).isEmpty();
            }
        });
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // get the Sakai skin header fragment from the request attribute
        final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();

        response.render(StringHeaderItem.forString((String) request.getAttribute("sakai.html.head")));
        response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

        // Tool additions (at end so we can override if required)
        response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));

        // render jQuery and the Wicket event library
        // Both must be priority so they are emitted into the head
        final String cdnQuery = PortalUtils.getCDNQuery();
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format(PortalUtils.getLatestJQueryPath()+ "?version=%s", cdnQuery))));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forUrl(String.format("/my-calendar/scripts/wicket/wicket-event-jquery.min.js?version=%s", cdnQuery))));

        // NOTE: All libraries apart from jQuery and Wicket Event must be rendered inline with the application. See WidgetPage.html.
    }

    /**
     * Get the members with the given role.
     *
     * Sorted by online status then display name. Maximum returned is 30 but can be overridden.
     *
     * @param siteId the site id to get the members for
     * @param role the role we want to get the users for
     * @return list of {@link GridPerson} or an empty list if none
     */
    final List<GridPerson> getMembersWithRole(final Site site, final SiteRole role,
        final Set<String> hiddenUserIds) {

        List<BasicConnection> userList = new ArrayList<>();

        Set<String> userUuids = site.getUsersIsAllowed(role.getPermissionName());
        userUuids.removeAll(hiddenUserIds);
        final List<User> users = this.userDirectoryService.getUsers(userUuids);
        userList = this.connectionsLogic.getBasicConnections(users);
        // sort
        Collections.sort(userList, new Comparator<BasicConnection>() {

            @Override
            public int compare(final BasicConnection o1, final BasicConnection o2) {
                // Here we sort the users by their online status. Annoyingly, the constants are
                // 0: offline, 1: online, 2:away, so we can't just sort by that.
                return new CompareToBuilder()
                        .append(o1.getOnlineStatus()==ProfileConstants.ONLINE_STATUS_OFFLINE?2:1,
                                o2.getOnlineStatus()==ProfileConstants.ONLINE_STATUS_OFFLINE?2:1)
                        .append(o1.getDisplayName(), o2.getDisplayName())
                        .toComparison();
            }
        });

        // get slice
        userList = userList
                .stream()
                .limit(this.maxUsers)
                .collect(Collectors.toList());

        List<GridPerson> rval = new ArrayList<>();

        for (BasicConnection person : userList) {
            GridPerson gridPerson = new GridPerson();
            gridPerson.uuid = person.getUuid();
            gridPerson.displayName = person.getDisplayName();
            gridPerson.role = role.toString();
            gridPerson.onlineStatus = person.getOnlineStatus();

            rval.add(gridPerson);
        }

        return rval;
    }

    public final static void registerFunction(String function) {

        List functions = FunctionManager.getRegisteredFunctions("sitemembers.");

        if (!functions.contains(function)) {
            FunctionManager.registerFunction(function);
        }
    }
}
