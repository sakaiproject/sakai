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
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class DashboardController extends AbstractSakaiApiController implements EntityProducer, EntityTransferrer {

    private static final String DASHBOARD_TOOL_ID = "sakai.dashboard";
    private static final String REFERENCE_ROOT = Entity.SEPARATOR + "dashboard";
    private static final String COURSE_IMAGE = "course_image";
    private static final String COURSE_IMAGE_FILE = COURSE_IMAGE + ".png";

	@Resource
	private AnnouncementService announcementService;

	@Resource
	private ContentHostingService contentHostingService;

	@Resource
	private EntityManager entityManager;

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
    private int maxNumberMotd = 1;

    /** This will be the map of the default widget layouts, one entry for each template */
    private Map<String, List<String>> defaultWidgetLayouts = new HashMap<>();

    @PostConstruct
    public void init() {

        boolean tasksEnabled = serverConfigurationService.getBoolean("dashboard.tasks.enabled", false);

        // Load up all the available widgets, from properties
        courseWidgets = serverConfigurationService.getStringList("dashboard.course.widgets", null);
        if (courseWidgets.isEmpty()) {
            courseWidgets = new ArrayList<>(List.of("tasks", "announcements", "calendar","forums", "grades"));
        }
        if (!tasksEnabled) courseWidgets.remove("tasks");

        homeWidgets = serverConfigurationService.getStringList("dashboard.home.widgets", null);
        if (homeWidgets.isEmpty()) {
            homeWidgets = new ArrayList<>(List.of("tasks", "announcements", "calendar","forums", "grades"));
        }
        if (!tasksEnabled) homeWidgets.remove("tasks");

        defaultHomeLayout = new ArrayList<>(List.of("tasks","announcements", "calendar", "grades", "forums"));
        if (!tasksEnabled) defaultHomeLayout.remove("tasks");

        List<String> courseWidgetLayout1 = serverConfigurationService.getStringList("dashboard.course.widget.layout1", null);
        if (courseWidgetLayout1 == null) {
            courseWidgetLayout1 = new ArrayList<>(List.of("tasks", "calendar", "announcements", "grades"));
        }
        if (!tasksEnabled) courseWidgetLayout1.remove("tasks");
        defaultWidgetLayouts.put("1", courseWidgetLayout1);

        List<String> courseWidgetLayout2 = serverConfigurationService.getStringList("dashboard.course.widget.layout2", null);
        if (courseWidgetLayout2 == null) {
            courseWidgetLayout2 = new ArrayList<>(List.of("tasks", "calendar", "forums", "grades", "announcements"));
        }
        if (!tasksEnabled) courseWidgetLayout2.remove("tasks");
        defaultWidgetLayouts.put("2", courseWidgetLayout2);

        List<String> courseWidgetLayout3 = serverConfigurationService.getStringList("dashboard.course.widget.layout3", null);
        if (courseWidgetLayout3 == null) {
            courseWidgetLayout3 = new ArrayList<>(List.of("tasks", "calendar", "announcements", "grades", "forums"));
        }
        if (!tasksEnabled) courseWidgetLayout3.remove("tasks");
        defaultWidgetLayouts.put("3", courseWidgetLayout3);

        maxNumberMotd = serverConfigurationService.getInt("dashboard.home.motd.display", 1);

        entityManager.registerEntityProducer(this, REFERENCE_ROOT);
    }

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
                new ViewableFilter(null, null, maxNumberMotd, announcementService),
                false,
                false);

            StringBuffer sb = new StringBuffer();
            for (AnnouncementMessage motdMessage : motdMessages) {
                sb.append(motdMessage.getBody());
            }
            bean.setMotd(sb.toString());
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
                List<String> layout = (new ObjectMapper()).readValue(layoutJson, ArrayList.class);
                if (!serverConfigurationService.getBoolean("dashboard.tasks.enabled", false))  {
                    layout.remove("tasks");
                }
                bean.setLayout(layout);
            } catch (Exception e) {
                log.warn("Failed to deserialise widget layout from {}", layoutJson);
            }
        }

        return bean;
	}

	@PutMapping(value = "/users/{userId}/dashboard")
    public void saveUserDashboard(@PathVariable String userId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

		String currentUserId = checkSakaiSession().getUserId();
		if (!securityService.isSuperUser() && (!StringUtils.isBlank(userId) && !StringUtils.equals(userId, currentUserId))) {
            log.error("You can only update your own user dashboard.");
            return;
        }

        PreferencesEdit preference = null;
        try {
            try {
                preference = preferencesService.edit(userId);
            } catch (IdUnusedException iue) {
                preference = preferencesService.add(userId);
            }
        } catch (Exception e) {
            log.warn("Could not get the preferences for user [{}], {}", userId, e.toString());
        }

        if (preference != null) {
            try {
                ResourcePropertiesEdit props = preference.getPropertiesEdit("dashboard-config");
                props.addProperty("layout", (new ObjectMapper()).writeValueAsString(bean.getLayout()));
            } catch (Exception e) {
                log.warn("Could not save dashboard config for user [{}], {}", userId, e.toString());
                preferencesService.cancel(preference);
                preference = null;
            } finally {
                if (preference != null) preferencesService.commit(preference);
            }
        }
	}

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
                int defaultCourseLayout = serverConfigurationService.getInt("dashoard.course.layout", 2);
                bean.setLayout(defaultWidgetLayouts.get(Integer.toString(defaultCourseLayout)));
                bean.setTemplate(defaultCourseLayout);
            } else {
                Map<String, Object> dashboardConfig = (new ObjectMapper()).readValue(dashboardConfigJson, HashMap.class);
                List<String> layout = (List<String>) dashboardConfig.get("layout");
                if (!serverConfigurationService.getBoolean("dashboard.tasks.enabled", false))  {
                    layout.remove("tasks");
                }
                bean.setLayout(layout);
                bean.setTemplate((Integer) dashboardConfig.get("template"));
            }
            bean.setEditable(securityService.isSuperUser() || securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()));
            String imageUrl = site.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL);
            if (StringUtils.isBlank(imageUrl)) {
                imageUrl = "/webcomponents/images/central_park_lamp.jpg";
            }
            bean.setImage(imageUrl);
            bean.setLayout1ThumbnailUrl("/webcomponents/images/layout1.png");
            bean.setLayout2ThumbnailUrl("/webcomponents/images/layout2.png");
            bean.setLayout3ThumbnailUrl("/webcomponents/images/layout3.png");
        } catch (IdUnusedException idue) {
            log.error("No site found for {}", siteId);
        } catch (Exception e) {
            log.error("Failed to load dashboard for site {}", siteId);
        }

        return bean;
	}

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
                edit = contentHostingService.editResource(collectionId + COURSE_IMAGE_FILE);
            } catch (IdUnusedException | PermissionException e) {
                edit = contentHostingService.addResource(collectionId, COURSE_IMAGE, ".png", 1);
            }
            edit.setContent(fi.get());
            edit.setContentLength(fi.getSize());
            edit.setContentType(fi.getContentType());
            contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
            Site site = siteService.getSite(siteId);
            site.getProperties().addProperty(Site.PROP_COURSE_IMAGE_URL, edit.getUrl());
            siteService.save(site);
            return edit.getUrl();
        } catch (Exception e) {
            log.error("Failed to update image for site {}: {}", siteId, e.toString());
            throw e;
        }
    }

    @Override
    public String getLabel() {
        return "dashboard";
    }

    @Override
    public boolean parseEntityReference(String reference, Reference ref) {

        if (!reference.startsWith(REFERENCE_ROOT)) {
            return false;
        }

        return true;
    }

    @Override
    public Optional<String> getTool() {
        return Optional.of(DASHBOARD_TOOL_ID);
    }

    @Override
    public String[] myToolIds() {
        return new String[] { DASHBOARD_TOOL_ID };
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

        try {
            Site fromSite = siteService.getSite(fromContext);
            String fromConfig = fromSite.getProperties().getProperty("dashboard-config");
            if (fromConfig != null) {
                Site toSite = siteService.getSite(toContext);
                toSite.setDescription(fromSite.getDescription());
                toSite.getProperties().addProperty("dashboard-config", fromConfig);
                siteService.save(toSite);
            }
        } catch (IdUnusedException idue) {
            log.error("No site found for {} or {}", fromContext, toContext);
        } catch (Exception e) {
            log.error("Failed to copy the dashboard config: {}", e.toString());
        }

        Map<String, String> map = Collections.EMPTY_MAP;

        String fromCollectionId = contentHostingService.getSiteCollection(fromContext);

        if (fromCollectionId == null) return map;

        try {
            contentHostingService.checkCollection(fromCollectionId);
        } catch (Exception e) {
            log.warn("No access to site {}'s content collection", fromContext);
            return map;
        }

        String toCollectionId = contentHostingService.getSiteCollection(toContext);

        try {
            contentHostingService.checkCollection(toCollectionId);
        } catch (Exception e) {
            try {
                contentHostingService.commitCollection(contentHostingService.addCollection(toCollectionId));
            } catch (Exception e2) {
                log.error("Failed to add collection {}: {}", toCollectionId, e2.toString());
            }
        }

        String sourceId = fromCollectionId + COURSE_IMAGE_FILE;

        try {
            contentHostingService.getResource(sourceId);
        } catch (Exception e) {
            // This is okay. No course image in the source site, not a problem.
            return map;
        }

        String targetId = toCollectionId + COURSE_IMAGE_FILE;

        // Attempt to remove the current course image
        try {
            contentHostingService.removeResource(targetId);
        } catch (Exception e) {
            // This is okay. Maybe there wasn't a course image.
        }

        try {
            String newId = contentHostingService.copy(sourceId, targetId);
            ContentResource newResource = contentHostingService.getResource(newId);
            Site toSite = siteService.getSite(toContext);
            toSite.getProperties().addProperty(Site.PROP_COURSE_IMAGE_URL, newResource.getUrl());
            siteService.save(toSite);
        } catch (Exception e) {
            log.error("Failed to copy dashboard image resource: {}", e.toString());
        }

        return map;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

        return transferCopyEntities(fromContext, toContext, ids, options);
    }
}
