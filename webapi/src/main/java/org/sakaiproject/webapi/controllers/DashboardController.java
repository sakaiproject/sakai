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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.fileupload.FileItem;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.portal.api.PortalConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
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
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class DashboardController extends AbstractSakaiApiController implements EntityProducer, EntityTransferrer {

    private static final String DASHBOARD_TOOL_ID = "sakai.dashboard";
    private static final String REFERENCE_ROOT = Entity.SEPARATOR + "dashboard";
    private static final String COURSE_IMAGE = "course_image";
    private static final String COURSE_IMAGE_FILE = COURSE_IMAGE + ".png";

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    @Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private SakaiPersonManager sakaiPersonManager;

    private List<String> courseWidgets = new ArrayList<>();
    private List<String> homeWidgets = new ArrayList<>();

    private List<String> defaultHomeLayout = new ArrayList<>();
    private int maxNumberMotd = 1;

    /** This will be the map of the default widget layouts, one entry for each template */
    private Map<String, List<String>> defaultWidgetLayouts = new HashMap<>();

    @PostConstruct
    public void init() {

        boolean tasksEnabled = serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false);

        // Load up all the available widgets, from properties
        courseWidgets = new ArrayList<>(serverConfigurationService.getStringList("dashboard.course.widgets", null));
        if (courseWidgets.isEmpty()) {
            courseWidgets = new ArrayList<>(List.of("tasks", "announcements", "calendar","forums", "grades"));
        }
        if (!tasksEnabled) courseWidgets.remove("tasks");

        homeWidgets = new ArrayList<>(serverConfigurationService.getStringList("dashboard.home.widgets", null));
        if (homeWidgets.isEmpty()) {
            homeWidgets = new ArrayList<>(List.of("courses", "tasks", "announcements", "calendar","forums", "grades"));
        }
        if (!tasksEnabled) homeWidgets.remove("tasks");

        defaultHomeLayout = new ArrayList<>(List.of("courses", "tasks", "announcements", "calendar", "grades", "forums"));
        if (!tasksEnabled) defaultHomeLayout.remove("tasks");

        List<String> courseWidgetLayout1 = new ArrayList<>(serverConfigurationService.getStringList("dashboard.course.widget.layout1", null));
        if (courseWidgetLayout1.isEmpty()) {
            courseWidgetLayout1 = new ArrayList<>(List.of("tasks", "calendar", "announcements", "grades"));
        }
        if (!tasksEnabled) courseWidgetLayout1.remove("tasks");
        defaultWidgetLayouts.put("1", courseWidgetLayout1);

        List<String> courseWidgetLayout2 = new ArrayList<>(serverConfigurationService.getStringList("dashboard.course.widget.layout2", null));
        if (courseWidgetLayout2.isEmpty()) {
            courseWidgetLayout2 = new ArrayList<>(List.of("tasks", "calendar", "forums", "grades", "announcements"));
        }
        if (!tasksEnabled) courseWidgetLayout2.remove("tasks");
        defaultWidgetLayouts.put("2", courseWidgetLayout2);

        List<String> courseWidgetLayout3 = new ArrayList<>(serverConfigurationService.getStringList("dashboard.course.widget.layout3", null));
        if (courseWidgetLayout3.isEmpty()) {
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

        sakaiPersonManager.getSakaiPerson(currentUserId, sakaiPersonManager.getUserMutableType())
                .map(SakaiPerson::getNickname)
                .filter(StringUtils::isNotBlank)
                .or(() -> userDirectoryService.getOptionalUser(currentUserId)
                        .map(User::getFirstName)
                        .filter(StringUtils::isNotBlank))
                .ifPresent(bean::setGivenName);

        try {
            List<AnnouncementMessage> motdMessages = announcementService.getVisibleMessagesOfTheDay(null, maxNumberMotd, false);

            String motd = motdMessages.stream()
                .map(AnnouncementMessage::getBody)
                .collect(Collectors.joining("\n"));

            bean.setMotd(motd);
        } catch (Exception e) {
            log.warn("Failed to set the MOTD for {}", userId, e);
        }

        bean.setWidgets(homeWidgets);

        // Get the widget layout preference
        Preferences prefs = preferencesService.getPreferences(session.getUserId());
        ResourceProperties props = prefs.getProperties("dashboard-config");

        if (props == null) {
            bean.setWidgetLayout(defaultHomeLayout);
            bean.setTemplate(1);
        } else {
            try {
                String widgetLayoutJson = props.getProperty("widgetLayout");
                if (widgetLayoutJson == null) {
                    widgetLayoutJson = props.getProperty("layout");
                }
                if (widgetLayoutJson == null) {
                    bean.setWidgetLayout(defaultHomeLayout);
                } else {
                    List<String> widgetLayout = (List<String>) new ObjectMapper().readValue(widgetLayoutJson, List.class);
                    if (widgetLayout == null) {
                        bean.setWidgetLayout(defaultHomeLayout);
                    } else {
                        if (!serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false))  {
                            widgetLayout.remove("tasks");
                        }
                        bean.setWidgetLayout(widgetLayout);
                    }
                }
                try {
                    int template = (int) props.getLongProperty("template");
                    bean.setTemplate(template);
                } catch (Exception e) {
                    bean.setTemplate(1);
                }
            } catch (Exception e) {
                log.warn("Failed to deserialise user dashboard config: {}", e.toString());
            }
        }

        bean.setHomeTemplate1ThumbnailUrl("/webcomponents/images/home_template1.png");
        bean.setHomeTemplate2ThumbnailUrl("/webcomponents/images/home_template2.png");
        bean.setHomeTemplate3ThumbnailUrl("/webcomponents/images/home_template3.png");

        return bean;
    }

    @PutMapping(value = "/users/{userId}/dashboard")
    public void saveUserDashboard(@PathVariable String userId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

        String currentUserId = checkSakaiSession().getUserId();
        if (!securityService.isSuperUser() && (!StringUtils.isBlank(userId) && !StringUtils.equals(userId, currentUserId))) {
            log.error("You can only update your own user dashboard.");
            return;
        }

        preferencesService.applyEditWithAutoCommit(userId, edit -> {
            ResourcePropertiesEdit props = edit.getPropertiesEdit("dashboard-config");
            try {
                String widgetLayoutJson = (new ObjectMapper()).writeValueAsString(bean.getWidgetLayout());
                props.addProperty("widgetLayout", widgetLayoutJson);
                props.addProperty("template", Integer.toString(bean.getTemplate()));
                // Remove the legacy layout property
                props.removeProperty("layout");
            } catch (JsonProcessingException jpe) {
                log.warn("Could not save dashboard config for user [{}], {}", userId, jpe.toString());
            }
        });
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
            String dashboardConfigJson = site.getProperties().getProperty("dashboard-config");
            int defaultCourseLayout = serverConfigurationService.getInt("dashoard.course.layout", 2);
            if (dashboardConfigJson == null) {
                bean.setWidgetLayout(defaultWidgetLayouts.get(Integer.toString(defaultCourseLayout)));
                bean.setTemplate(defaultCourseLayout);
            } else {
                Map<String, Object> dashboardConfig = (new ObjectMapper()).readValue(dashboardConfigJson, HashMap.class);
                List<String> widgetLayout = (List<String>) dashboardConfig.get("widgetLayout");
                if (widgetLayout == null) {
                    widgetLayout = (List<String>) dashboardConfig.get("layout");
                }
                if (widgetLayout != null) {
                    if (!serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false)) {
                        widgetLayout.remove("tasks");
                    }
                    bean.setWidgetLayout(widgetLayout);
                } else {
                    bean.setWidgetLayout(defaultWidgetLayouts.get(Integer.toString(defaultCourseLayout)));
                }
                bean.setTemplate((Integer) dashboardConfig.get("template"));
            }
            bean.setEditable(securityService.isSuperUser() || securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()));
            String imageUrl = site.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL);
            if (StringUtils.isBlank(imageUrl)) {
                imageUrl = "/webcomponents/images/central_park_lamp.jpg";
            }
            bean.setImage(imageUrl);
            bean.setCourseTemplate1ThumbnailUrl("/webcomponents/images/course_template1.png");
            bean.setCourseTemplate2ThumbnailUrl("/webcomponents/images/course_template2.png");
            bean.setCourseTemplate3ThumbnailUrl("/webcomponents/images/course_template3.png");
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
            String configJson = (new ObjectMapper())
                .writeValueAsString(Map.of("widgetLayout", bean.getWidgetLayout(), "template", bean.getTemplate()));
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
