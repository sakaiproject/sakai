/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;

import org.apache.commons.fileupload.FileItem;

import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.announcement.api.ViewableFilter;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class DashboardController extends AbstractSakaiApiController {

	@Resource
	private AnnouncementService announcementService;

	@Resource
	private ContentHostingService contentHostingService;

	@Resource
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Resource
	private SiteService siteService;

	@Resource
	private UserDirectoryService userDirectoryService;

	@Resource
	private PreferencesService preferencesService;

    private List<String> courseWidgets = new ArrayList<>();
    private List<String> homeWidgets = new ArrayList<>();

    private List<String> defaultHomeLayout = new ArrayList<>();

    /** This will be the map of the default widget layouts, one entry for each template */
    private Map<String, List<String>> defaultWidgetLayouts = new HashMap<>();

    @PostConstruct
    public void init() {

        // Load up all the available widgets, from properties
        String[] courseWidgetsArray = serverConfigurationService.getStrings("dashboard.course.widgets");
        if (courseWidgetsArray == null) {
            courseWidgetsArray = new String[] { "announcements", "calendar","forums", "grades" };
        }
        courseWidgets = Arrays.asList(courseWidgetsArray);

        String[] homeWidgetsArray = serverConfigurationService.getStrings("dashboard.home.widgets");
        if (homeWidgetsArray == null) {
            homeWidgetsArray = new String[] { "tasks", "announcements", "calendar","forums", "grades" };
        }
        homeWidgets = Arrays.asList(homeWidgetsArray);

        defaultHomeLayout = Arrays.asList(new String[] { "tasks","announcements", "calendar", "grades", "forums" });

        String[] courseWidgetLayout1 = serverConfigurationService.getStrings("dashboard.course.widget.layout1");
        if (courseWidgetLayout1 == null) {
            courseWidgetLayout1 = new String[] { "calendar", "announcements", "grades" };
        }
        defaultWidgetLayouts.put("1", Arrays.asList(courseWidgetLayout1));

        String[] courseWidgetLayout2 = serverConfigurationService.getStrings("dashboard.course.widget.layout2");
        if (courseWidgetLayout2 == null) {
            courseWidgetLayout2 = new String[] { "calendar", "forums", "grades", "announcements" };
        }
        defaultWidgetLayouts.put("2", Arrays.asList(courseWidgetLayout2));

        String[] courseWidgetLayout3 = serverConfigurationService.getStrings("dashboard.course.widget.layout3");
        if (courseWidgetLayout3 == null) {
            courseWidgetLayout3 = new String[] { "calendar", "announcements", "grades", "forums" };
        }
        defaultWidgetLayouts.put("3", Arrays.asList(courseWidgetLayout3));
    }

    @ApiOperation(value = "Get a particular user's dashboard data")
	@GetMapping(value = "/users/{userId}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public DashboardRestBean getUserDashboard(@PathVariable String userId) throws UserNotDefinedException {

		Session session = checkSakaiSession();
		String currentUserId = session.getUserId();

        DashboardRestBean bean = new DashboardRestBean();

        if (!StringUtils.equals(currentUserId, userId) && !securityService.isSuperUser()) {
            return bean;
        }

        try {
            bean.setGivenName(userDirectoryService.getUser(currentUserId).getFirstName());
        } catch (UserNotDefinedException unde) {
            log.warn("No user found for id {}", currentUserId);
        }

        try {
            List<AnnouncementMessage> motdMessages = announcementService.getMessages(
                announcementService.getSummarizableReference(null, announcementService.MOTD_TOOL_ID),
                new ViewableFilter(null, null, 1, announcementService),
                false,
                false);

            if (motdMessages.size() > 0) {
                bean.setMotd(motdMessages.get(motdMessages.size() - 1).getBody());
            }
        } catch (IdUnusedException idue) {
            log.debug("No MOTD set.");
        } catch (Exception e) {
            log.warn("Failed to set the MOTD for {}", userId, e);
        }

        if (securityService.isSuperUser()) {
            try {
                Site site = siteService.getSite("~" + session.getUserId());
                ToolConfiguration tc = site.getToolForCommonId("sakai.sitesetup");
                bean.setWorksiteSetupUrl("/portal/directtool/" + tc.getId() + "?panel=Shortcut&sakai_action=doNew_site");
            } catch (IdUnusedException idue) {
                log.warn("No home site found for user {}", session.getUserId());
            } catch (Exception e) {
                log.warn("Failed to find the worksite setup tool for {}", userId, e);
            }
        }

        bean.setWidgets(homeWidgets);

        // Get the widget layout preference
        Preferences prefs = preferencesService.getPreferences(session.getUserId());
        String layoutJson = (String) prefs.getProperties("dashboard-config").get("layout");

        if (layoutJson == null) {
            bean.setLayout(defaultHomeLayout);
        } else {
            try {
                bean.setLayout((new ObjectMapper()).readValue(layoutJson, ArrayList.class));
            } catch (Exception e) {
                log.warn("Failed to deserialise widget layout from {}", layoutJson);
            }
        }

        return bean;
	}

    @ApiOperation(value = "Save a particular user's dashboard data")
	@PutMapping(value = "/users/{userId}/dashboard")
    public void saveUserDashboard(@PathVariable String userId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

		String currentUserId = checkSakaiSession().getUserId();
		if (!securityService.isSuperUser() && (!StringUtils.isBlank(userId) && !StringUtils.equals(userId, currentUserId))) {
            log.error("You can only update your own user dashboard.");
            return;
        }

        try {
            PreferencesEdit prefs = preferencesService.edit(userId);
            ResourcePropertiesEdit props = prefs.getPropertiesEdit("dashboard-config");
            props.addProperty("layout", (new ObjectMapper()).writeValueAsString(bean.getLayout()));
            preferencesService.commit(prefs);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    @ApiOperation(value = "Get a particular site's dashboard data")
	@GetMapping(value = "/sites/{siteId}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public DashboardRestBean getSiteDashboard(@PathVariable String siteId) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        DashboardRestBean bean = new DashboardRestBean();

        try {
            Site site = siteService.getSite(siteId);
            bean.setTitle(site.getTitle());
            bean.setWidgets(courseWidgets);
            bean.setProgramme(site.getShortDescription());
            bean.setOverview(site.getDescription());
            bean.setDefaultWidgetLayouts(defaultWidgetLayouts);
            String dashboardConfigJson = site.getProperties().getProperty("dashboard-config");
            if (dashboardConfigJson == null) {
                bean.setLayout(defaultWidgetLayouts.get("2"));
                bean.setTemplate(2);
            } else {
                Map<String, Object> dashboardConfig = (new ObjectMapper()).readValue(dashboardConfigJson, HashMap.class);
                bean.setLayout((List<String>) dashboardConfig.get("layout"));
                bean.setTemplate((Integer) dashboardConfig.get("template"));
            }
            bean.setEditable(securityService.isSuperUser() || securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()));
            String imageUrl = site.getIconUrl();
            if (StringUtils.isBlank(imageUrl)) {
                imageUrl = "/webcomponents/images/central_park_lamp.jpg";
            }
            bean.setImage(imageUrl);
        } catch (IdUnusedException idue) {
            log.error("No site found for {}", siteId);
        } catch (Exception e) {
            log.error("Failed to load dashboard for site {}", siteId);
        }

        return bean;
	}

    @ApiOperation(value = "Save a particular site's dashboard data")
	@PutMapping(value = "/sites/{siteId}/dashboard")
    public void saveSiteDashboard(@PathVariable String siteId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

		Session session = checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);
            site.setDescription(bean.getOverview());
            site.setShortDescription(bean.getProgramme());
            Map<String, Object> config = new HashMap<>();
            config.put("layout", bean.getLayout());
            config.put("template", bean.getTemplate());
            String configJson = (new ObjectMapper()).writeValueAsString(config);
            site.getProperties().addProperty("dashboard-config", configJson);
            siteService.save(site);
        } catch (Exception e) {
        }
	}

    @ApiOperation(value = "Save a particular site's image")
	@PostMapping(value = "/sites/{siteId}/image", produces = "text/plain")
    public String saveSiteImage(HttpServletRequest req, @PathVariable String siteId) throws Exception {

        try {
            FileItem fi = (FileItem) req.getAttribute("siteImage");
            String collectionId = contentHostingService.getSiteCollection(siteId);

            // Ensure we have a collection for this site. If we've not added resources to the 
            // site yet, there may be no collection.
            try {
                contentHostingService.checkCollection(collectionId);
            } catch (IdUnusedException idue) {
                contentHostingService.commitCollection(contentHostingService.addCollection(collectionId));
            }

            ContentResourceEdit edit;
            try {
                edit = contentHostingService.editResource(collectionId + "site_icon_image.png");
            } catch (IdUnusedException | PermissionException e) {
                edit = contentHostingService.addResource(collectionId, "site_icon_image", ".png", 1);
            }
            edit.setContent(fi.get());
            edit.setContentLength(fi.getSize());
            edit.setContentType(fi.getContentType());
            contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
            Site site = siteService.getSite(siteId);
            site.setIconUrl(edit.getUrl());
            siteService.save(site);
            return edit.getUrl();
        } catch (Exception e) {
            log.error("Failed to update image for site {}", siteId, e);
            throw e;
        }
	}
}
