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

import org.apache.commons.fileupload2.core.FileItem;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.portal.api.PortalConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.webapi.beans.DashboardLayoutHelper;
import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.webapi.beans.DashboardWidgetTile;
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
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;

import com.fasterxml.jackson.databind.JsonNode;
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

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class DashboardController extends AbstractSakaiApiController implements EntityProducer, EntityTransferrer {

    private static final String DASHBOARD_TOOL_ID = "sakai.dashboard";
    private static final String REFERENCE_ROOT = Entity.SEPARATOR + "dashboard";
    private static final String COURSE_IMAGE = "course_image";
    private static final String COURSE_IMAGE_FILE = COURSE_IMAGE + ".png";
    private static final String SITESTATS_WIDGET_PREFIX = "sitestats-";
    private static final String PROP_SITESTATS_ENABLED = "dashboard.sitestats.enabled";
    private static final String PROP_GRADES_REVEAL_ON_CLICK = "dashboard.grades.reveal-on-click";
    private static final String DASHBOARD_CONFIG = "dashboard-config";
    private static final String PROP_WIDGET_FILTERS = "widgetFilters";
    private static final String PROP_COURSE_LAYOUTS = "courseLayouts";
    private static final String PROP_COURSE_HERO_WIDTH = "dashboard.course.hero.width";
    private static final String PROP_COURSE_SHOW_IMAGE = "dashboard.course.hero.showImage";
    private static final String PROP_COURSE_LAYOUT_STUDENT = "dashboard.course.widget.layout.student";
    private static final String PROP_COURSE_LAYOUT_INSTRUCTOR = "dashboard.course.widget.layout.instructor";
    private static final String PROP_HOME_LAYOUT = "dashboard.home.widget.layout";
    private static final String PROP_HOME_LAYOUT_ADMIN = "dashboard.home.widget.layout.admin";

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

    @Autowired
    private FormattedText formattedText;

    @Autowired(required = false)
    private SiteStatsViewService siteStatsViewService;

    private List<String> courseWidgets = new ArrayList<>();
    private List<String> homeWidgets = new ArrayList<>();

    private List<String> defaultHomeLayout = new ArrayList<>();
    private List<String> defaultHomeLayoutAdmin = new ArrayList<>();
    private List<String> defaultCourseLayoutStudent = new ArrayList<>();
    private List<String> defaultCourseLayoutInstructor = new ArrayList<>();
    private int maxNumberMotd = 1;
    private boolean sitestatsDashboardEnabled = true;

    @PostConstruct
    public void init() {

        boolean tasksEnabled = serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false);

        // Load up all the available widgets, from properties
        courseWidgets = new ArrayList<>(serverConfigurationService.getStringList("dashboard.course.widgets", null));
        if (courseWidgets.isEmpty()) {
            courseWidgets = new ArrayList<>(List.of("tasks", "announcements", "calendar", "forums", "grades"));
        }
        if (!tasksEnabled) {
            courseWidgets.remove("tasks");
        }

        homeWidgets = new ArrayList<>(serverConfigurationService.getStringList("dashboard.home.widgets", null));
        if (homeWidgets.isEmpty()) {
            homeWidgets = new ArrayList<>(List.of("courses", "tasks", "announcements", "calendar", "forums", "grades"));
        }
        if (!tasksEnabled) {
            homeWidgets.remove("tasks");
        }

        defaultHomeLayout = loadLayoutProperty(PROP_HOME_LAYOUT,
                List.of("courses", "calendar", "announcements", "tasks", "grades", "forums"), tasksEnabled);
        defaultHomeLayoutAdmin = loadLayoutProperty(PROP_HOME_LAYOUT_ADMIN,
                List.of("sitestats-platform-health", "sitestats-institutional-presence",
                        "sitestats-institutional-learner-activity", "sitestats-institutional-authoring-activity",
                        "sitestats-client-environment"),
                tasksEnabled);
        defaultCourseLayoutStudent = loadLayoutProperty(PROP_COURSE_LAYOUT_STUDENT,
                List.of("calendar", "announcements", "grades",
                        "sitestats-student-grades", "sitestats-student-submissions", "forums"),
                tasksEnabled);
        defaultCourseLayoutInstructor = loadLayoutProperty(PROP_COURSE_LAYOUT_INSTRUCTOR,
                List.of("sitestats-presence-access", "sitestats-visits", "sitestats-grades", "grades",
                        "sitestats-submissions", "sitestats-communication", "calendar", "sitestats-lessons"),
                tasksEnabled);

        maxNumberMotd = serverConfigurationService.getInt("dashboard.home.motd.display", 1);
        sitestatsDashboardEnabled = serverConfigurationService.getBoolean(PROP_SITESTATS_ENABLED, true);

        entityManager.registerEntityProducer(this, REFERENCE_ROOT);
    }

    private List<String> loadLayoutProperty(String propertyName, List<String> codeDefault, boolean tasksEnabled) {

        List<String> layout = new ArrayList<>(serverConfigurationService.getStringList(propertyName, null));
        if (layout.isEmpty()) {
            layout = new ArrayList<>(codeDefault);
        }
        if (!tasksEnabled) {
            layout.remove("tasks");
        }
        return layout;
    }

    @GetMapping(value = "/users/{userId}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public DashboardRestBean getUserDashboard(@PathVariable("userId") String userId) throws UserNotDefinedException {

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

        List<String> sitestatsHomeIds = sitestatsHomeWidgetIds();
        List<String> availableWidgets = withSitestatsWidgets(homeWidgets, sitestatsHomeIds);
        bean.setWidgets(availableWidgets);
        bean.setGradesRevealOnClick(serverConfigurationService.getBoolean(PROP_GRADES_REVEAL_ON_CLICK, true));

        boolean homeAdmin = securityService.isSuperUser() || !sitestatsHomeIds.isEmpty();
        List<String> defaultLayoutIds = homeAdmin ? defaultHomeLayoutAdmin : defaultHomeLayout;

        // Get the widget layout preference
        Preferences prefs = preferencesService.getPreferences(session.getUserId());
        ResourceProperties props = prefs.getProperties(DASHBOARD_CONFIG);

        if (props == null) {
            bean.setWidgetLayout(filterLayoutToAvailable(
                    DashboardLayoutHelper.fromIds(defaultLayoutIds), availableWidgets));
        } else {
            try {
                String widgetLayoutJson = props.getProperty("widgetLayout");
                if (widgetLayoutJson == null) {
                    widgetLayoutJson = props.getProperty("layout");
                }
                if (widgetLayoutJson == null) {
                    bean.setWidgetLayout(filterLayoutToAvailable(
                            DashboardLayoutHelper.fromIds(defaultLayoutIds), availableWidgets));
                } else {
                    JsonNode layoutNode = new ObjectMapper().readTree(widgetLayoutJson);
                    List<DashboardWidgetTile> widgetLayout = DashboardLayoutHelper.fromJsonNode(layoutNode);
                    if (widgetLayout.isEmpty()) {
                        widgetLayout = DashboardLayoutHelper.fromIds(defaultLayoutIds);
                    } else if (!serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false)) {
                        widgetLayout = DashboardLayoutHelper.removeWidget(widgetLayout, "tasks");
                    }
                    bean.setWidgetLayout(filterLayoutToAvailable(widgetLayout, availableWidgets));
                }
                String filtersJson = props.getProperty(PROP_WIDGET_FILTERS);
                if (StringUtils.isNotBlank(filtersJson)) {
                    bean.setWidgetFilters(new ObjectMapper().readValue(filtersJson, Map.class));
                }
            } catch (Exception e) {
                log.warn("Failed to deserialise user dashboard config: {}", e.toString());
                bean.setWidgetLayout(filterLayoutToAvailable(
                        DashboardLayoutHelper.fromIds(defaultLayoutIds), availableWidgets));
            }
        }

        return bean;
    }

    @PutMapping(value = "/users/{userId}/dashboard")
    public void saveUserDashboard(@PathVariable("userId") String userId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

        String currentUserId = checkSakaiSession().getUserId();
        if (!securityService.isSuperUser() && (!StringUtils.isBlank(userId) && !StringUtils.equals(userId, currentUserId))) {
            log.error("You can only update your own user dashboard.");
            return;
        }

        preferencesService.applyEditWithAutoCommit(userId, edit -> {
            ResourcePropertiesEdit props = edit.getPropertiesEdit(DASHBOARD_CONFIG);
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<DashboardWidgetTile> layout = DashboardLayoutHelper.normalize(bean.getWidgetLayout());
                props.addProperty("widgetLayout", mapper.writeValueAsString(layout));

                // Remove the legacy template property and layout properties
                props.removeProperty("template");
                props.removeProperty("layout");

                if (bean.getWidgetFilters() != null) {
                    props.addProperty(PROP_WIDGET_FILTERS, mapper.writeValueAsString(bean.getWidgetFilters()));
                }

            } catch (JsonProcessingException jpe) {
                log.warn("Could not save dashboard config for user [{}], {}", userId, jpe.toString());
            }
        });
    }

    @PutMapping(value = "/users/{userId}/dashboard/filters/{widgetId}")
    public void saveUserWidgetFilter(@PathVariable("userId") String userId,
            @PathVariable("widgetId") String widgetId,
            @RequestBody(required = false) Map<String, Object> filter) throws UserNotDefinedException {

        String currentUserId = checkSakaiSession().getUserId();
        if (!securityService.isSuperUser() && (!StringUtils.isBlank(userId) && !StringUtils.equals(userId, currentUserId))) {
            log.error("You can only update your own user dashboard filters.");
            return;
        }
        if (StringUtils.isBlank(widgetId)) {
            return;
        }

        preferencesService.applyEditWithAutoCommit(userId, edit -> {
            ResourcePropertiesEdit props = edit.getPropertiesEdit(DASHBOARD_CONFIG);
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Map<String, Object>> filters = new HashMap<>();
                String existing = props.getProperty(PROP_WIDGET_FILTERS);
                if (StringUtils.isNotBlank(existing)) {
                    filters = mapper.readValue(existing, Map.class);
                }
                if (filter == null || filter.isEmpty()) {
                    filters.remove(widgetId);
                } else {
                    filters.put(widgetId, filter);
                }
                if (filters.isEmpty()) {
                    props.removeProperty(PROP_WIDGET_FILTERS);
                } else {
                    props.addProperty(PROP_WIDGET_FILTERS, mapper.writeValueAsString(filters));
                }
            } catch (Exception e) {
                log.warn("Could not save dashboard filter for user [{}] widget [{}]: {}", userId, widgetId, e.toString());
            }
        });
    }

    @GetMapping(value = "/sites/{siteId}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public DashboardRestBean getSiteDashboard(@PathVariable("siteId") String siteId) throws UserNotDefinedException {

        Session session = checkSakaiSession();
        String userId = session.getUserId();

        DashboardRestBean bean = new DashboardRestBean();

        try {
            Site site = siteService.getSite(siteId);
            bean.setTitle(site.getTitle());
            List<String> availableWidgets = withSitestatsWidgets(courseWidgets, sitestatsCourseWidgetIds(siteId));
            bean.setWidgets(availableWidgets);
            bean.setProgramme(site.getShortDescription());
            bean.setOverview(site.getDescription());
            bean.setGradesRevealOnClick(serverConfigurationService.getBoolean(PROP_GRADES_REVEAL_ON_CLICK, true));

            Map<String, Object> siteConfig = readSiteDashboardConfig(site);
            bean.setShowImage(resolveSiteShowImage(siteConfig));
            String siteHeroWidth = resolveSiteHeroWidth(siteConfig);

            boolean siteEditable = securityService.isSuperUser()
                    || securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference());
            bean.setSiteEditable(siteEditable);
            bean.setLayoutEditable(Boolean.TRUE);

            Map<String, Object> userCourseLayout = readUserCourseLayout(userId, siteId);
            bean.setWidgetLayout(resolveUserCourseWidgetLayout(userCourseLayout, siteEditable, availableWidgets));

            String userHeroWidth = userCourseLayout != null && userCourseLayout.get("heroWidth") instanceof String
                    ? (String) userCourseLayout.get("heroWidth")
                    : null;
            bean.setHeroWidth(normalizeHeroWidth(
                    StringUtils.isNotBlank(userHeroWidth) ? userHeroWidth : siteHeroWidth));

            String imageUrl = site.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL);
            if (StringUtils.isBlank(imageUrl)) {
                imageUrl = "/webcomponents/images/central_park_lamp.jpg";
            }
            bean.setImage(imageUrl);
        } catch (IdUnusedException idue) {
            log.error("No site found for {}", siteId);
        } catch (Exception e) {
            log.error("Failed to load dashboard for site {}", siteId, e);
        }

        return bean;
    }

    @PutMapping(value = "/sites/{siteId}/dashboard")
    public void saveSiteDashboard(@PathVariable("siteId") String siteId, @RequestBody DashboardRestBean bean) throws UserNotDefinedException {

        Session session = checkSakaiSession();
        String userId = session.getUserId();

        try {
            Site site = siteService.getSite(siteId);
            boolean siteEditable = securityService.isSuperUser()
                    || securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference());

            if (siteEditable) {
                site.setDescription(sanitizeFormattedText(bean.getOverview()));
                site.setShortDescription(bean.getProgramme());
                Map<String, Object> config = readSiteDashboardConfig(site);
                config.put("showImage", bean.getShowImage() == null || Boolean.TRUE.equals(bean.getShowImage()));
                if (!(config.get("heroWidth") instanceof String)
                        || StringUtils.isBlank((String) config.get("heroWidth"))) {
                    config.put("heroWidth", normalizeHeroWidth(
                            serverConfigurationService.getString(PROP_COURSE_HERO_WIDTH, "wide")));
                }
                config.remove("widgetLayout");
                config.remove("template");
                config.remove("overviewW");
                config.remove("overviewH");
                config.remove("imageW");
                config.remove("imageH");
                site.getProperties().addProperty(DASHBOARD_CONFIG,
                        new ObjectMapper().writeValueAsString(config));
                siteService.save(site);
            }

            List<DashboardWidgetTile> layout = DashboardLayoutHelper.normalize(bean.getWidgetLayout());
            saveUserCourseLayout(userId, siteId, layout, normalizeHeroWidth(bean.getHeroWidth()));
        } catch (Exception e) {
            log.warn("Failed to save dashboard for site {}: {}", siteId, e.toString());
        }
    }

    private Map<String, Object> readSiteDashboardConfig(Site site) {

        String dashboardConfigJson = site.getProperties().getProperty(DASHBOARD_CONFIG);
        if (StringUtils.isBlank(dashboardConfigJson)) {
            return new HashMap<>();
        }
        try {
            return new ObjectMapper().readValue(dashboardConfigJson, HashMap.class);
        } catch (Exception e) {
            log.warn("Failed to parse dashboard-config for site {}: {}", site.getId(), e.toString());
            return new HashMap<>();
        }
    }

    private boolean resolveSiteShowImage(Map<String, Object> siteConfig) {

        Object showImageObj = siteConfig.get("showImage");
        if (showImageObj instanceof Boolean) {
            return (Boolean) showImageObj;
        }
        if (showImageObj instanceof String) {
            return Boolean.parseBoolean((String) showImageObj);
        }
        return serverConfigurationService.getBoolean(PROP_COURSE_SHOW_IMAGE, true);
    }

    private String resolveSiteHeroWidth(Map<String, Object> siteConfig) {

        if (siteConfig.get("heroWidth") instanceof String) {
            return normalizeHeroWidth((String) siteConfig.get("heroWidth"));
        }
        return normalizeHeroWidth(serverConfigurationService.getString(PROP_COURSE_HERO_WIDTH, "wide"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readUserCourseLayout(String userId, String siteId) {

        Preferences prefs = preferencesService.getPreferences(userId);
        ResourceProperties props = prefs.getProperties(DASHBOARD_CONFIG);
        if (props == null) {
            return null;
        }
        String json = props.getProperty(PROP_COURSE_LAYOUTS);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            Map<String, Object> all = new ObjectMapper().readValue(json, HashMap.class);
            Object entry = all.get(siteId);
            if (entry instanceof Map) {
                return (Map<String, Object>) entry;
            }
        } catch (Exception e) {
            log.warn("Failed to parse courseLayouts for user {}: {}", userId, e.toString());
        }
        return null;
    }

    private List<DashboardWidgetTile> resolveUserCourseWidgetLayout(Map<String, Object> userCourseLayout,
            boolean instructor, List<String> availableWidgets) {

        List<DashboardWidgetTile> widgetLayout = List.of();
        if (userCourseLayout != null) {
            widgetLayout = DashboardLayoutHelper.normalize(userCourseLayout.get("widgetLayout"));
        }
        if (widgetLayout.isEmpty()) {
            List<String> defaults = instructor ? defaultCourseLayoutInstructor : defaultCourseLayoutStudent;
            widgetLayout = DashboardLayoutHelper.fromIds(defaults);
        } else if (!serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false)) {
            widgetLayout = DashboardLayoutHelper.removeWidget(widgetLayout, "tasks");
        }
        return filterLayoutToAvailable(widgetLayout, availableWidgets);
    }

    private List<DashboardWidgetTile> filterLayoutToAvailable(List<DashboardWidgetTile> layout,
            List<String> availableWidgets) {

        if (layout == null || layout.isEmpty()) {
            return List.of();
        }
        if (availableWidgets == null || availableWidgets.isEmpty()) {
            return List.of();
        }
        Set<String> available = new HashSet<>(availableWidgets);
        List<DashboardWidgetTile> filtered = new ArrayList<>();
        for (DashboardWidgetTile tile : layout) {
            if (tile != null && StringUtils.isNotBlank(tile.getId()) && available.contains(tile.getId())) {
                filtered.add(tile);
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private void saveUserCourseLayout(String userId, String siteId, List<DashboardWidgetTile> layout, String heroWidth) {

        preferencesService.applyEditWithAutoCommit(userId, edit -> {
            ResourcePropertiesEdit props = edit.getPropertiesEdit(DASHBOARD_CONFIG);
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> all = new HashMap<>();
                String existing = props.getProperty(PROP_COURSE_LAYOUTS);
                if (StringUtils.isNotBlank(existing)) {
                    all = mapper.readValue(existing, HashMap.class);
                }
                Map<String, Object> entry = new HashMap<>();
                entry.put("widgetLayout", layout);
                entry.put("heroWidth", heroWidth);
                all.put(siteId, entry);
                props.addProperty(PROP_COURSE_LAYOUTS, mapper.writeValueAsString(all));
            } catch (Exception e) {
                log.warn("Could not save course dashboard layout for user [{}] site [{}]: {}",
                        userId, siteId, e.toString());
            }
        });
    }

    private static String normalizeHeroWidth(String heroWidth) {
        if ("full".equals(heroWidth) || "wide".equals(heroWidth) || "narrow".equals(heroWidth)) {
            return heroWidth;
        }
        return "wide";
    }

    private Map<String, Object> siteChromeConfigForCopy(String fromConfigJson) {

        Map<String, Object> result = new HashMap<>();
        result.put("showImage", Boolean.TRUE);
        result.put("heroWidth", "wide");
        if (StringUtils.isBlank(fromConfigJson)) {
            return result;
        }
        try {
            Map<String, Object> from = new ObjectMapper().readValue(fromConfigJson, HashMap.class);
            Object showImage = from.get("showImage");
            if (showImage instanceof Boolean) {
                result.put("showImage", showImage);
            }
            if (from.get("heroWidth") instanceof String) {
                result.put("heroWidth", normalizeHeroWidth((String) from.get("heroWidth")));
            }
        } catch (Exception e) {
            log.warn("Failed to parse dashboard-config for site copy: {}", e.toString());
        }
        return result;
    }

    private String sanitizeFormattedText(String text) {
        return formattedText.processFormattedText(text, null, FormattedText.Level.HIGH);
    }

    private List<String> withSitestatsWidgets(List<String> baseWidgets, List<String> sitestatsWidgets) {
        List<String> widgets = new ArrayList<>(baseWidgets);
        for (String widgetId : sitestatsWidgets) {
            if (!widgets.contains(widgetId)) {
                widgets.add(widgetId);
            }
        }
        return widgets;
    }

    private List<String> sitestatsCourseWidgetIds(String siteId) {
        if (!sitestatsDashboardEnabled || siteStatsViewService == null || StringUtils.isBlank(siteId)) {
            return List.of();
        }
        try {
            return toDashboardWidgetIds(siteStatsViewService.getCourseDashboardWidgetIds(siteId));
        } catch (SecurityException e) {
            log.debug("SiteStats course dashboard widgets unavailable for site {}", siteId);
            return List.of();
        } catch (RuntimeException e) {
            log.warn("Failed to resolve SiteStats course dashboard widgets for site {}: {}", siteId, e.toString());
            return List.of();
        }
    }

    private List<String> sitestatsHomeWidgetIds() {
        if (!sitestatsDashboardEnabled || siteStatsViewService == null) {
            return List.of();
        }
        try {
            return toDashboardWidgetIds(siteStatsViewService.getHomeDashboardWidgetIds(SiteService.ADMIN_SITE_ID));
        } catch (SecurityException e) {
            log.debug("SiteStats home dashboard widgets unavailable");
            return List.of();
        } catch (RuntimeException e) {
            log.warn("Failed to resolve SiteStats home dashboard widgets: {}", e.toString());
            return List.of();
        }
    }

    private List<String> toDashboardWidgetIds(List<String> siteStatsWidgetIds) {
        if (siteStatsWidgetIds == null || siteStatsWidgetIds.isEmpty()) {
            return List.of();
        }
        return siteStatsWidgetIds.stream()
                .filter(StringUtils::isNotBlank)
                .map(id -> SITESTATS_WIDGET_PREFIX + id)
                .collect(Collectors.toList());
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
            String fromConfig = fromSite.getProperties().getProperty(DASHBOARD_CONFIG);
            Site toSite = siteService.getSite(toContext);
            toSite.setDescription(sanitizeFormattedText(fromSite.getDescription()));
            Map<String, Object> chrome = siteChromeConfigForCopy(fromConfig);
            toSite.getProperties().addProperty(DASHBOARD_CONFIG,
                    new ObjectMapper().writeValueAsString(chrome));
            siteService.save(toSite);
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
