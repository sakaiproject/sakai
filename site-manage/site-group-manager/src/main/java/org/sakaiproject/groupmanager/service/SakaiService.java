/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.groupmanager.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.sakaiproject.assignment.api.model.Assignment;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.JoinableSetReminderScheduleService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SakaiService  {

    @Inject
    public AssignmentService assignmentService;

    @Inject
    private EventTrackingService eventTrackingService;

    @Inject
    private SiteService siteService;

    @Inject
    private PreferencesService preferencesService;

    @Inject
    private ServerConfigurationService serverConfigurationService;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private ToolManager toolManager;

    @Inject
    private UserDirectoryService userDirectoryService;

    @Inject
    private UserNotificationProvider userNotificationProvider;

    @Inject
    private JoinableSetReminderScheduleService jSetReminderScheduleService;

    @Autowired
    @Qualifier("org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;

    private final String STATE_SITE_ID = "site.instance.id";

    public Optional<Site> getCurrentSite() {
        String siteId;

        // Try to get site ID from session context first
        try {
            siteId = sessionManager.getCurrentToolSession().getAttribute(STATE_SITE_ID).toString();
        } catch (NullPointerException ex) {
            // Site ID wasn't set in the helper call, get the current site ID
            log.debug("Site ID not found in session data");
            siteId = toolManager.getCurrentPlacement().getContext();
        }

        try {
            return Optional.of(siteService.getSite(siteId));
        } catch (Exception ex) {
            log.error("Unable to find the site with Id {}.", siteId);
        }
        return Optional.empty();
    }

    public Locale getLocaleForCurrentSiteAndUser() {
        Locale locale = null;

        // First try to get site locale
        Optional<Site> currentSite = getCurrentSite();
        if (currentSite.isPresent()) {
            ResourceProperties siteProperties = currentSite.get().getProperties();
            String siteLocale = (String) siteProperties.get("locale_string");
            if (StringUtils.isNotBlank(siteLocale)) {
                locale = serverConfigurationService.getLocaleFromString(siteLocale);
            }
        }

        // If there is not site locale defined, get user default locale
        if (locale == null) {
            locale = getCurrentUserLocale();
        }

        return locale;
    }

    public Locale getCurrentUserLocale() {
        String userId = sessionManager.getCurrentSessionUserId();
        return StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
    }

    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    public boolean saveSite(Site site) {
        try {
            siteService.save(site);
        } catch (IdUnusedException | PermissionException e) {
            log.error("Error saving the site {}.", site.getId());
            return false;
        }
        return true;
    }

    public Optional<User> getUser(String userId) {
        try {
            return Optional.of(userDirectoryService.getUser(userId));
        } catch (UserNotDefinedException e) {
            log.error("Unable to get user by id {}.", userId);
        }
        return Optional.empty();
    }

    public Optional<User> getUserByEid(String userEid) {
        try {
            return Optional.of(userDirectoryService.getUserByEid(userEid));
        } catch (UserNotDefinedException e) {
            log.error("Unable to get user by eid {}.", userEid);
        }
        return Optional.empty();
    }

    public Optional<Group> findGroupById(String groupId) {
        Group group = siteService.findGroup(groupId);
        if (group != null) {
            return Optional.of(group);
        }
        return Optional.empty();
    }

    public boolean getBooleanProperty(String propertyKey, boolean defaultValue) {
        return serverConfigurationService.getBoolean(propertyKey, defaultValue);
    }

    public void postEvent(String event, String userId) {
        eventTrackingService.post(eventTrackingService.newEvent(event, userId, true/*update event*/));
    }

    public Map<String, List<String>> getGroupLockingEntities (Group group) {
        Map<String, List<String>> lockingEntitiesMap = new HashMap<>();
        List<String[]> groupRealmLocks = group.getRealmLocks();

        // Add one list per entity that locks groups.
        List<String> assignmentTitles = new ArrayList<>();
        List<String> assessmentTitles = new ArrayList<>();

        for (String[] groupRealmLock : groupRealmLocks) {
            String objectReference = groupRealmLock[0];
            if (StringUtils.isNotBlank(objectReference)) {
                if (objectReference.startsWith(AssignmentServiceConstants.REFERENCE_ROOT)) {
                	AssignmentReferenceReckoner.AssignmentReference reckoner = AssignmentReferenceReckoner.reckoner().reference(objectReference).reckon();
                    try {
                        Assignment ab = assignmentService.getAssignment(reckoner.getId());
                        assignmentTitles.add(ab.getTitle());
                    } catch (Exception e) {
                        log.error("Cannot find assignment with id {}.", reckoner.getId());
                    }
                } else {
                    try {
                        //Validate that the reference is an integer
                        Integer assessmentId = Integer.parseInt(objectReference);
                        PublishedAssessmentService assessmentService = new PublishedAssessmentService();
                        assessmentTitles.add(assessmentService.getAssessment(Long.valueOf(assessmentId)).getTitle());
                    } catch (Exception e) {
                        log.error("Cannot find assessment with id {}.", objectReference);
                    }
                }
            }
        }

        // Add one key per entity that locks groups.
        lockingEntitiesMap.put("assignments", assignmentTitles);
        lockingEntitiesMap.put("assessments", assessmentTitles);

        return lockingEntitiesMap;
        
    }

    /**
     * Get datetime conversion from UTC to user's time zone.
     * @param utcDate datetime without time-zone
     * @param formatted send Locale to get formatted output, else null
     * @return datetime with user's time zone, or null
     */
    public String dateFromUtcToUserTimeZone(String utcDate, boolean formatted) {
        return userTimeService.dateFromUtcToUserTimeZone(utcDate, formatted);
    }

    /**
     * Convert datetime from user's time zone to UTC (meant to be stored in DB).
     * Output ready to be compared with other date.
     * @param zonedDate datetime without time-zone
     * @return datetime object without time-zone
     */
    public LocalDateTime dateFromUserTimeZoneToUtc(String zonedDate) {
        return userTimeService.dateFromUserTimeZoneToUtc(zonedDate);
    }

    /**
     * Notify about JoinableSet when created or updated
     * @param siteName
     * @param userId
     * @param joinableGroup
     * @param isNew
     */
    public void notifyAboutJoinableSet(String siteName, String userId, Group joinableGroup, boolean isNew) {
        userNotificationProvider.notifyAboutJoinableSet(siteName, userId, joinableGroup, isNew);
    }

    /**
     * Notify 24h before JoinableSet's close date
     * @param dateTime
     * @param dataPair siteId + joinableSetId
     */
    public void scheduleReminder(Instant dateTime, String dataPair) {
        jSetReminderScheduleService.scheduleJSetReminder(dateTime, dataPair);
    }
}
