/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at:
 *
 * http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.signup.impl;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.Permission;
import org.sakaiproject.signup.api.Retry;
import org.sakaiproject.signup.api.SignupEmailFacade;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.signup.api.SignupMessageTypes;
import org.sakaiproject.signup.api.SignupUserActionException;
import org.sakaiproject.signup.api.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.api.model.MeetingTypes;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupGroup;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.api.repository.SignupMeetingRepository;
import org.sakaiproject.signup.api.restful.SignupTargetSiteEventInfo;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import static org.sakaiproject.signup.api.SignupConstants.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SignupMeetingService that provides functionality for managing signup meetings.
 * Handles operations such as:
 * - Creating, updating and deleting meetings
 * - Managing calendar events
 * - Handling permissions and access control
 * - Sending email notifications
 * - Retrieving meeting data
 */
@Slf4j
@Transactional
public class SignupMeetingServiceImpl implements SignupMeetingService, Retry, MeetingTypes, SignupMessageTypes {

    @Setter private CalendarService calendarService;
    @Setter private FormattedText formattedText;
    @Setter private FunctionManager functionManager;
    @Setter private ResourceLoader resourceLoader;
    @Setter private SignupMeetingRepository repository;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private SignupEmailFacade signupEmailFacade;
    @Setter private SiteService siteService;
    @Setter private TimeService timeService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private UserTimeService userTimeService;

    public void init() {
        // register Sakai permissions for this tool
        functionManager.registerFunction(SIGNUP_VIEW, true);
        functionManager.registerFunction(SIGNUP_VIEW_ALL, true);
        functionManager.registerFunction(SIGNUP_ATTEND, true);
        functionManager.registerFunction(SIGNUP_ATTEND_ALL, true);
        functionManager.registerFunction(SIGNUP_CREATE_SITE, true);
        functionManager.registerFunction(SIGNUP_CREATE_GROUP, true);
        functionManager.registerFunction(SIGNUP_CREATE_GROUP_ALL, true);
        functionManager.registerFunction(SIGNUP_DELETE_SITE, true);
        functionManager.registerFunction(SIGNUP_DELETE_GROUP, true);
        functionManager.registerFunction(SIGNUP_DELETE_GROUP_ALL, true);
        functionManager.registerFunction(SIGNUP_UPDATE_SITE, true);
        functionManager.registerFunction(SIGNUP_UPDATE_GROUP, true);
        functionManager.registerFunction(SIGNUP_UPDATE_GROUP_ALL, true);
    }

    @Override
    public List<SignupMeeting> getAllSignupMeetings(String currentSiteId, String userId) {
        List<SignupMeeting> meetings = repository.findAllBySiteId(currentSiteId);
        return screenAllowableMeetings(currentSiteId, userId, meetings);
    }

    @Override
    public List<SignupMeeting> getSignupMeetings(String currentSiteId, String userId, Date searchEndDate) {
        List<SignupMeeting> meetings = repository.findBySiteIdAndStartTimeBefore(currentSiteId, searchEndDate);
        return screenAllowableMeetings(currentSiteId, userId, meetings);
    }

    @Override
    public List<SignupMeeting> getSignupMeetings(String currentSiteId, String userId, Date startDate, Date endDate) {
        List<SignupMeeting> meetings = repository.findBySiteIdAndDateRange(currentSiteId, startDate, endDate);
        return screenAllowableMeetings(currentSiteId, userId, meetings);
    }

    @Override
    public List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, int timeFrameInDays) {
        if (siteIds == null || siteIds.isEmpty()) return List.of();

        Instant startInstant = startDate.toInstant();
        Instant endInstant = startInstant.plus(timeFrameInDays, ChronoUnit.DAYS);
        Date endDate = Date.from(endInstant);

        // Get all matching IDs for all sites in one query (more efficient)
        List<Long> ids = repository.findIdsBySiteIdsAndDateRange(siteIds, startDate, endDate);

        if (ids.isEmpty()) return List.of();

        // Load entities in a single batch query
        List<SignupMeeting> meetings = repository.findAllByIds(ids);

        // Sort by start time (already sorted by query, but ensuring order)
        meetings.sort(Comparator.comparing(SignupMeeting::getStartTime));

        return meetings;
    }

    @Override
    public List<SignupMeeting> getRecurringSignupMeetings(String currentSiteId, String userId, Long recurrenceId, Date startDate) {
        List<SignupMeeting> meetings = repository.findRecurringMeetings(currentSiteId, recurrenceId, startDate);
        return screenAllowableMeetings(currentSiteId, userId, meetings);
    }

    /**
     * This method will return a list of meetings, which contain all the
     * populated permission information
     *
     * @param currentSiteId
     *            a unique id which represents the current site
     * @param userId
     *            the internal user id (not username)
     * @param meetings
     *            a list of SignupMeeting objects
     * @return a list of SignupMeeting object
     */
    private List<SignupMeeting> screenAllowableMeetings(String currentSiteId, String userId, List<SignupMeeting> meetings) {
        List<SignupMeeting> allowedMeetings = new ArrayList<>();

        for (SignupMeeting meeting : meetings) {
            if (isAllowedToView(meeting, userId, currentSiteId)) {
                allowedMeetings.add(meeting);
            }
        }
        updatePermissions(userId, currentSiteId, allowedMeetings);

        return allowedMeetings;
    }

    /**
     * This will obtain the permission for attend, update and delete
     *
     * @param userId
     *            a unique id which represents the current site
     * @param siteId
     *            a unique id which represents the current site
     * @param meetings
     *            a list of SignupMeeting objects
     */
    private void updatePermissions(String userId, String siteId, List<SignupMeeting> meetings) {
        for (SignupMeeting meeting : meetings) {
            boolean attend = isAllowToAttend(userId, siteId, meeting);
            boolean update = isAllowToUpdate(userId, siteId, meeting);
            boolean delete = isAllowToDelete(userId, siteId, meeting);
            Permission permission = new Permission(attend, update, delete);
            meeting.setPermission(permission);
        }

    }

    private String assignPermission(String userId, String siteId, SignupMeeting meeting) {
        String targetSiteId = siteId;
        if (targetSiteId == null) {
            targetSiteId = findSiteWithHighestPermissionLevel(userId, meeting);
        }

        if (targetSiteId != null) {
            boolean attend = isAllowToAttend(userId, targetSiteId, meeting);
            boolean update = isAllowToUpdate(userId, targetSiteId, meeting);
            boolean delete = isAllowToDelete(userId, targetSiteId, meeting);
            Permission permission = new Permission(attend, update, delete);
            meeting.setPermission(permission);
        }

        return targetSiteId;
    }

    private String findSiteWithHighestPermissionLevel(String userId, SignupMeeting meeting) {
        List<SignupSite> sites = meeting.getSignupSites();

        // look if user has update and delete permissions
        for (SignupSite site : sites) {
            String sId = site.getSiteId();
            boolean update = isAllowToUpdate(userId, sId, meeting);
            boolean delete = isAllowToDelete(userId, sId, meeting);
            if (update && delete) {
                return sId;
            }
        }
        // look if user has update permission
        for (SignupSite site : sites) {
            String sId = site.getSiteId();
            boolean update = isAllowToUpdate(userId, sId, meeting);
            if (update) {
                return sId;
            }
        }

        // look if user has attend permission
        for (SignupSite site : sites) {
            String sId = site.getSiteId();
            boolean attend = isAllowToAttend(userId, sId, meeting);
            if (attend) {
                return sId;
            }
        }

        return null;
    }

    /**
     * TODO Under what condition the organizer can delete the meeting? Have to
     * have all level permission? across site
     */
    private boolean isAllowToDelete(String userId, String siteId, SignupMeeting meeting) {
        if (securityService.isSuperUser(userId)) return true;

        SignupSite site = currentSite(meeting, siteId);
        if (site != null) {
            if (site.isSiteScope()) { // GroupList is null or empty case
                return securityService.unlock(userId, SIGNUP_DELETE_SITE, siteService.siteReference(site.getSiteId()));
            }

            // it's group scoped
            if (securityService.unlock(userId, SIGNUP_DELETE_GROUP_ALL, site.getSiteId()) || securityService.unlock(userId, SIGNUP_DELETE_SITE, siteService.siteReference(site.getSiteId())))
                return true;

            // organizer has to have permission to delete every group in the list, otherwise can't delete
            boolean allowedTodelete = true;
            List<SignupGroup> signupGroups = site.getSignupGroups();
            for (SignupGroup group : signupGroups) {
                if (!(securityService.unlock(userId, SIGNUP_DELETE_GROUP, siteService.siteGroupReference(site.getSiteId(), group.getGroupId())) || securityService.unlock(userId, SIGNUP_DELETE_GROUP_ALL, siteService.siteGroupReference(siteId, group.getGroupId())))) {
                    allowedTodelete = false;
                    break;
                }
            }
            return allowedTodelete;

        }
        return false;

    }

    /**
     * Checks if the current user has update permission at any level (site or group) for the meeting.
     *
     * @param userId the ID of the user to check permissions for
     * @param siteId the ID of the site containing the meeting
     * @param meeting the SignupMeeting object to check permissions against
     * @return true if the user has update permission, false otherwise
     */
    private boolean isAllowToUpdate(String userId, String siteId, SignupMeeting meeting) {
        if (securityService.isSuperUser(userId)) return true;

        SignupSite site = currentSite(meeting, siteId);
        if (site != null) {
            if (site.isSiteScope()) {
                if (securityService.unlock(userId, SIGNUP_UPDATE_SITE, siteService.siteReference(site.getSiteId()))) return true;
            }
            /* Do we allow people with a group.all permission to update the meeting with a site scope?
             * currently we allow them to update
             */
            if (securityService.unlock(userId, SIGNUP_UPDATE_GROUP_ALL, site.getSiteId()) || securityService.unlock(userId, SIGNUP_UPDATE_SITE, siteService.siteReference(site.getSiteId())))
                return true;

            List<SignupGroup> signupGroups = site.getSignupGroups();
            for (SignupGroup group : signupGroups) {
                if (securityService.unlock(userId, SIGNUP_UPDATE_GROUP, siteService.siteGroupReference(site.getSiteId(), group.getGroupId())) || securityService.unlock(userId, SIGNUP_UPDATE_GROUP_ALL, siteService.siteGroupReference(siteId, group.getGroupId())))
                    return true;
            }
        }

        return false;

    }

    /**
     * Checks if the current user can attend the meeting based on their permissions.
     * Returns true if:
     * - User is an admin 
     * - User has SIGNUP_ATTEND_ALL permission in the site
     * - For site-scoped meetings: user has SIGNUP_ATTEND permission in the site
     * - For group-scoped meetings: user has SIGNUP_ATTEND or SIGNUP_ATTEND_ALL permission in any of the meeting's groups
     *
     * @param userId the ID of the user to check permissions for
     * @param siteId the ID of the site containing the meeting
     * @param meeting the SignupMeeting object to check permissions against  
     * @return true if the user has permission to attend, false otherwise
     */
    private boolean isAllowToAttend(String userId, String siteId, SignupMeeting meeting) {
        if (securityService.isSuperUser(userId)) return true;

        if (securityService.unlock(userId, SIGNUP_ATTEND_ALL, siteService.siteReference(siteId))) return true;

        SignupSite site = currentSite(meeting, siteId);
        if (site != null) {
            if (site.isSiteScope()) return securityService.unlock(userId, SIGNUP_ATTEND, siteService.siteReference(site.getSiteId()));

            List<SignupGroup> signupGroups = site.getSignupGroups();
            for (SignupGroup group : signupGroups) {
                if (securityService.unlock(userId, SIGNUP_ATTEND, siteService.siteGroupReference(siteId, group.getGroupId())) || securityService.unlock(userId, SIGNUP_ATTEND_ALL, siteService.siteGroupReference(siteId, group.getGroupId())))
                    return true;
            }
        }
        return false;

    }

    /**
     * Check if the current user can see all items (super user or has appropriate permissions).
     *
     * @param meeting The SignupMeeting object to check permissions for
     * @param userId The ID of the user to check permissions for
     * @param siteId The ID of the site containing the meeting
     * @return true if user is admin, has view-all permission in site, site-scoped view permission, 
     *         or group-scoped view permission for any of the meeting's groups
     */
    private boolean isAllowedToView(SignupMeeting meeting, String userId, String siteId) {
        if (securityService.isSuperUser(userId)) return true;
        if (securityService.unlock(userId, SIGNUP_VIEW_ALL, siteService.siteReference(siteId))) return true;

        SignupSite site = currentSite(meeting, siteId);
        if (site != null) {
            if (site.isSiteScope()) return securityService.unlock(userId, SIGNUP_VIEW, siteService.siteReference(siteId));

            List<SignupGroup> signupGroups = site.getSignupGroups();
            for (SignupGroup group : signupGroups) {
                if (securityService.unlock(userId, SIGNUP_VIEW, siteService.siteGroupReference(siteId, group.getGroupId())) || securityService.unlock(userId, SIGNUP_VIEW_ALL, siteService.siteGroupReference(siteId, group.getGroupId())))
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets the SignupSite object associated with a site ID from a meeting.
     *
     * @param meeting The SignupMeeting object containing the site information
     * @param siteId The ID of the site to find
     * @return The SignupSite object for the specified siteId, or null if not found
     */
    private SignupSite currentSite(SignupMeeting meeting, String siteId) {
        List<SignupSite> signupSites = meeting.getSignupSites();
        for (SignupSite site : signupSites) {
            if (site.getSiteId().equals(siteId)) return site;
        }
        return null;
    }

    @Override
    public Long saveMeeting(SignupMeeting signupMeeting, String userId) throws PermissionException {
        if (isAllowedToCreate(userId, signupMeeting)) {
            return repository.save(signupMeeting).getId();
        }
        throw new PermissionException(userId, "signup.create", "signup tool");
    }

    @Override
    public void saveMeetings(List<SignupMeeting> signupMeetings, String userId) throws PermissionException {
        if (signupMeetings == null || signupMeetings.isEmpty()) return;

        List<SignupMeeting> toSave = new ArrayList<>();
        SignupMeeting first = signupMeetings.get(0);
        if (isAllowedToCreate(userId, first)) {
            int size = signupMeetings.size();
            if (first.isRecurredMeeting() && size > 1) {
                // Use the first unique meeting id as the recurrenceId for all recurring meetings
                SignupMeeting saved = repository.save(first);
                Long recurrenceId = saved.getId();
                toSave.add(saved);
                toSave.addAll(signupMeetings.subList(1, size));
                toSave.forEach(m -> m.setRecurrenceId(recurrenceId));
            } else {
                toSave.addAll(signupMeetings);
            }
            repository.saveAll(toSave);
        } else {
            throw new PermissionException(userId, "signup.create", "signup tool");
        }
    }

    // check to see if the user has create permission at any level
    private boolean isAllowedToCreate(String userId, SignupMeeting signupMeeting) {
        if (securityService.isSuperUser(userId)) return true;

        List<SignupSite> signupSites = signupMeeting.getSignupSites();
        for (SignupSite site : signupSites) {
            if (site.isSiteScope()) {
                if (!securityService.unlock(userId, SIGNUP_CREATE_SITE, siteService.siteReference(site.getSiteId()))) {
                    return false;
                }
            } else {
                if (securityService.unlock(userId, SIGNUP_CREATE_SITE, siteService.siteReference(site.getSiteId()))
                        || securityService.unlock(userId, SIGNUP_CREATE_GROUP_ALL, siteService.siteReference(site.getSiteId()))) {
                    continue;
                }

                List<SignupGroup> signupGroups = site.getSignupGroups();
                for (SignupGroup group : signupGroups) {
                    if (!(securityService.unlock(userId, SIGNUP_CREATE_GROUP, siteService.siteGroupReference(site.getSiteId(), group.getGroupId())) || securityService.unlock(userId, SIGNUP_CREATE_GROUP_ALL, siteService.siteGroupReference(site.getSiteId(), group.getGroupId()))))
                        return false;
                }
            }
        }

        return true;

    }

    @Override
    public boolean isAllowedToCreateinGroup(String userId, String siteId, String groupId) {
        return securityService.unlock(userId, SIGNUP_CREATE_GROUP_ALL, siteService.siteReference(siteId))
                || securityService.unlock(userId, SIGNUP_CREATE_GROUP, siteService.siteGroupReference(siteId, groupId));
    }

    @Override
    public boolean isAllowedToCreateinSite(String userId, String siteId) {
        return securityService.unlock(userId, SIGNUP_CREATE_SITE, siteService.siteReference(siteId));
    }

    @Override
    public boolean isAllowedToCreateAnyInSite(String userId, String siteId) {
        if (securityService.isSuperUser(userId)) return true;

        if (securityService.unlock(userId, SIGNUP_CREATE_SITE, siteService.siteReference(siteId))) return true;

        // check groups
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            log.warn("Unable to retrieve site [{}], {}", siteId, e.toString());
            return false;
        }
        Collection<Group> groups = site.getGroups();
        if (groups == null || groups.isEmpty()) return false;

        return groups.stream().anyMatch(gp -> isAllowedToCreateinGroup(userId, siteId, gp.getId()));
    }

    @Override
    public SignupMeeting updateSignupMeeting(SignupMeeting meeting, boolean isOrganizer) throws Exception {
        Permission permission = meeting.getPermission();

        // if null, only consider an organizer-updating case (higher requirement level)
        if (permission == null) {
            if (isAllowToUpdate(sessionManager.getCurrentSessionUserId(), meeting.getCurrentSiteId(), meeting)) {
                return repository.save(meeting);
            }
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
        }

        if (isOrganizer) {
            if (permission.isUpdate()) {
                return repository.save(meeting);
            } else {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
            }
        } else {
            if (permission.isAttend()) {
                return repository.save(meeting);
            } else {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.attend", "SignupTool");
            }
        }
    }

    @Override
    public void updateSignupMeetings(List<SignupMeeting> meetings, boolean isOrganizer) throws Exception {

        if (meetings == null || meetings.isEmpty()) {
            return;
        }

        SignupMeeting oneMeeting = meetings.get(0);
        // Here, assuming that organizer has the update permission for any one meeting, then he has the permissions for all
        Permission permission = oneMeeting.getPermission();

        // if null, only consider an organizer-updating case (higher requirement level)
        if (permission == null) {
            if (isAllowToUpdate(sessionManager.getCurrentSessionUserId(), oneMeeting.getCurrentSiteId(), oneMeeting)) {
                repository.updateAll(meetings);
                return;
            }
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
        }

        if (isOrganizer) {
            if (permission.isUpdate()) repository.updateAll(meetings);
            else throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
        } else {
            if (permission.isAttend()) repository.updateAll(meetings);
            else throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.attend", "SignupTool");
        }
    }

    @Override
    public void updateModifiedMeetings(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots, boolean isOrganizer) throws Exception {

        if (meetings == null || meetings.isEmpty()) {
            return;
        }

        SignupMeeting oneMeeting = meetings.get(0);
        // Here, assuming that organizer has the update permission for any one meeting, then he has the permissions for all
        Permission permission = oneMeeting.getPermission();

        // if null, only consider an organizer-updating case (higher requirement level)
        if (permission == null) {
            if (isAllowToUpdate(sessionManager.getCurrentSessionUserId(), oneMeeting.getCurrentSiteId(), oneMeeting)) {
                repository.updateMeetingsAndRemoveTimeslots(meetings, removedTimeslots);
                return;
            }
            throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
        }

        if (isOrganizer) {
            if (permission.isUpdate()) repository.updateMeetingsAndRemoveTimeslots(meetings, removedTimeslots);
            else throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.update", "SignupTool");
        } else {
            if (permission.isAttend()) repository.updateMeetingsAndRemoveTimeslots(meetings, removedTimeslots);
            else throw new PermissionException(sessionManager.getCurrentSessionUserId(), "signup.attend", "SignupTool");
        }
    }

    @Override
    public SignupMeeting loadSignupMeeting(Long meetingId, String userId, String siteId) {
        Optional<SignupMeeting> meeting = repository.findById(meetingId);
        meeting.ifPresent(m -> updatePermissions(userId, siteId, List.of(m)));
        return meeting.orElse(null);
    }

    @Override
    public SignupTargetSiteEventInfo loadSignupMeetingWithAutoSelectedSite(Long meetingId, String userId, String siteId) {
        SignupMeeting meeting = repository.findById(meetingId).orElse(null);
        if (meeting == null) return null;
        String sId = assignPermission(userId, siteId, meeting);
        return new SignupTargetSiteEventInfo(meeting, sId);
    }

    @Override
    public void postToCalendar(SignupMeeting meeting) throws Exception {
        modifyCalendar(meeting);
    }

    @Override
    public void modifyCalendar(SignupMeeting meeting) throws Exception {
        List<SignupSite> signupSites = meeting.getSignupSites();
        boolean saveMeeting = false;
        List<SignupTimeslot> calendarBlocks = scanDivideCalendarBlocks(meeting);
        boolean hasMultipleBlock = calendarBlocks.size() > 1;

        /* we remove all the calendar events for custom-defined type to simplify process
         * when existing meetings come here. New meeting don't have permission setting yet
         * and it is null.
         */
        if (meeting.getPermission() != null && meeting.getPermission().isUpdate()) {
            //only instructor or maintainer/TF can do this since they can create/delete/move new blocks
            if (CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())) {
                List<SignupMeeting> smList = new ArrayList<>();
                smList.add(meeting);
                removeCalendarEvents(smList);
            }
        }

        int sequence = 1;
        boolean firstBlockLoop = true;
        // only custom-defined events have multiple discontinued calendar blocks*/
        int calBlock = 0;
        for (SignupTimeslot calendarBlock : calendarBlocks) {
            for (SignupSite site : signupSites) {
                try {
                    final Calendar calendar = chooseCalendar(site);

                    if (calendar == null) continue; // something went wrong when fetching the calendar

                    String eventId = null;
                    if (site.isSiteScope()) {
                        eventId = site.getCalendarEventId();
                    } else {
                        List<SignupGroup> signupGroups = site.getSignupGroups();
                        for (SignupGroup group : signupGroups) {
                            eventId = group.getCalendarEventId();
                            break;
                        }
                    }

                    CalendarEventEdit eventEdit;
                    if (meeting.getPermission() != null && !meeting.getPermission().isUpdate() && CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())) {
                        // Case: for customed Calendar blocks, we need to break down the eventIds and update one by one.
                        // only attendee can come here. Instructor/TF will first remove old Calendar blocks and create new ones again for simplicity!!!
                        eventId = retrieveCustomCalendarEventId(calBlock, eventId);
                    } else if (CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())) {
                        // make sure that instructor/TF will create new Calendar blocks since calendars is already removed
                        eventId = null;
                    }

                    boolean isNew = true;
                    // for custom_ts type, TF/instructor has no modification here eventId is null
                    if (eventId != null && eventId.trim().length() > 1) {
                        // allow attendee to update calendar
                        SecurityAdvisor advisor = (userId, function, reference) -> {
                            if (calendar.canModifyAnyEvent(function)) {
                                return SecurityAdvisor.SecurityAdvice.ALLOWED;
                            } else {
                                return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
                            }
                        };

                        securityService.pushAdvisor(advisor);
                        try {
                            eventEdit = calendar.getEditEvent(eventId, CalendarService.EVENT_MODIFY_CALENDAR);
                            isNew = false;
                            if (!calendar.allowEditEvent(eventId)) {
                                continue;
                            }
                        } catch (IdUnusedException e) {
                            log.debug("IdUnusedException: {}", e.toString());
                            // If the event was removed from the calendar.
                            eventEdit = calendarEvent(calendar, site);
                        } finally {
                            securityService.popAdvisor(advisor);
                        }
                    } else {
                        eventEdit = calendarEvent(calendar, site);
                    }
                    if (eventEdit == null) continue;

                    // new time frame
                    String title_suffix = "";
                    if (hasMultipleBlock) {
                        String partSequence = MessageFormat.format(resourceLoader.getString("signup.event.part"), sequence);
                        title_suffix = " (" + partSequence + ")";
                        sequence++;
                    }

                    populateDataForEventEditObject(eventEdit, meeting, title_suffix, calendarBlock.getStartTime(), calendarBlock.getEndTime());

                    calendar.commitEvent(eventEdit);

                    if (isNew) {
                        saveMeeting = true; // Need to save these back
                        if (site.isSiteScope()) {
                            if (firstBlockLoop) {
                                site.setCalendarEventId(eventEdit.getId());
                                site.setCalendarId(calendar.getId());
                            } else {
                                // only custom-defined event type could come here
                                String lastEventId = site.getCalendarEventId();
                                // appending new one
                                site.setCalendarEventId(lastEventId + "|" + eventEdit.getId());
                            }
                        } else {
                            List<SignupGroup> signupGroups = site.getSignupGroups();
                            for (SignupGroup group : signupGroups) {
                                if (firstBlockLoop) {
                                    group.setCalendarEventId(eventEdit.getId());
                                    group.setCalendarId(calendar.getId());
                                } else {
                                    String lastEventId = group.getCalendarEventId();
                                    // appending new one
                                    group.setCalendarEventId(lastEventId + "|" + eventEdit.getId());
                                }

                            }
                        }
                    }
                } catch (PermissionException pe) {
                    log.info("PermissionException for calendar-modification: {}", pe.getMessage());
                    throw pe;
                }
            }

            firstBlockLoop = false;
            calBlock++;
        }
        if (saveMeeting) {
            updateSignupMeeting(meeting, true);
        }
    }

    /**
     * Only custom-defined event type has discontinued calendar blocks
     * since the user can do anything with the time slots in discontinued way.
     * It will make the calendar unreadable.
     */
    private void populateDataForEventEditObject(CalendarEventEdit eventEdit, SignupMeeting meeting, String title_suffix, Date startTime, Date endTime) {
        Time start = timeService.newTime(startTime.getTime());
        Time end = timeService.newTime(endTime.getTime());
        TimeRange timeRange = timeService.newTimeRange(start, end, true, false);
        eventEdit.setRange(timeRange);

        StringBuilder attendeeNamesMarkup = new StringBuilder();
        int num = 0;

        if (!meeting.getSignupTimeSlots().isEmpty()) {
            attendeeNamesMarkup.append("<br /><br /><span style=\"font-weight: bold\"><b>").append(resourceLoader.getString("signup.event.attendees")).append("</b></span><br />");
        }

        boolean displayAttendeeName = false;
        for (SignupTimeslot ts : meeting.getSignupTimeSlots()) {
            displayAttendeeName = ts.isDisplayAttendees();
            // just need one of TS, it is not fine-grained yet
            // case: custom calender blocks, only print the related info
            if ((startTime.getTime() <= ts.getStartTime().getTime()) && endTime.getTime() >= ts.getEndTime().getTime()) {
                num += ts.getAttendees().size();
                if (ts.isDisplayAttendees() && !ts.getAttendees().isEmpty()) {
                    // privacy issue
                    for (SignupAttendee attendee : ts.getAttendees()) {
                        userDirectoryService.getOptionalUser(attendee.getAttendeeUserId()).ifPresent(u ->
                                attendeeNamesMarkup.append("<span style=\"font-weight: italic\"><i>")
                                        .append(u.getDisplayName())
                                        .append("</i></span><br />"));
                    }
                }
            }
        }

        if (!displayAttendeeName || num < 1) {
            String currentAttendees = MessageFormat.format(resourceLoader.getString("signup.event.currentattendees"), num);
            attendeeNamesMarkup.append("<span style=\"font-weight: italic\"><i>").append(currentAttendees).append("</i></span><br />");
        }

        String desc = meeting.getDescription() + attendeeNamesMarkup;
        eventEdit.setDescription(new PlainTextFormat(formattedText).convertFormattedHtmlTextToPlaintext(desc));
        eventEdit.setLocation(meeting.getLocation());
        String eventTitleAttendees = MessageFormat.format(resourceLoader.getString("signup.event.attendeestitle"), num);
        eventEdit.setDisplayName(meeting.getTitle() + title_suffix + " (" + eventTitleAttendees + ")");
        eventEdit.setRange(timeRange);
    }

    private String retrieveCustomCalendarEventId(int blockNum, String eventIds) {
        // separate the eventIds token by '|'
        StringTokenizer token = new StringTokenizer(eventIds, "|");
        int index = 0;
        while (token.hasMoreTokens()) {
            if (blockNum == index++) return token.nextToken().trim();
            else token.nextToken();
        }

        return null;
    }

    /**
     * Scans a meeting to determine if it has multiple calendar blocks, particularly for custom-defined time slots.
     * A calendar block is a time segment created in the calendar tool to represent meeting time slots.
     * Multiple blocks are created when time slots are not continuous and have significant gaps between them.
     *
     * @param meeting The SignupMeeting object to scan for calendar blocks
     * @return A list of SignupTimeslot objects representing the calendar blocks. For regular meetings,
     *         returns a single block with the meeting's start and end time. For custom meetings with
     *         discontinuous time slots, returns multiple blocks based on the time gaps between slots.
     */
    private boolean hasMeetingWithMultipleCalendarBlocks(SignupMeeting meeting) {
        if (!CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())) {
            return false;
        }
        boolean hasMultipleBlocks = false;
        List<SignupSite> sites = meeting.getSignupSites();
        if (sites == null || sites.isEmpty()) return false;

        for (SignupSite site : sites) {
            String eventId;
            if (site.isSiteScope()) {
                eventId = site.getCalendarEventId();
                if (eventId != null && eventId.contains("|")) {
                    return true;
                }
            } else {
                List<SignupGroup> signupGroups = site.getSignupGroups();
                for (SignupGroup group : signupGroups) {
                    eventId = group.getCalendarEventId();
                    if (eventId != null && eventId.contains("|")) {
                        return true;
                    }
                }
            }

        }
        return hasMultipleBlocks;
    }

    private List<SignupTimeslot> scanDivideCalendarBlocks(SignupMeeting meeting) {
        final int timeApart = 2 * 60 * 60 * 1000; // 2 hours
        List<SignupTimeslot> tsList = new ArrayList<>();
        if (CUSTOM_TIMESLOTS.equals(meeting.getMeetingType()) && (meeting.isInMultipleCalendarBlocks() || hasMeetingWithMultipleCalendarBlocks(meeting))) {
            List<SignupTimeslot> tsLs = meeting.getSignupTimeSlots();
            if (tsLs != null && !tsLs.isEmpty()) {
                // The meetings timeslots are already sorted before coming here
                Date startTime = tsLs.get(0).getStartTime();
                int cursor = 0;
                for (int i = 0; i < tsLs.size() - 1; i++) {
                    long firstBlockEndTime = tsLs.get(cursor).getEndTime().getTime();

                    // to see if next block is within first block
                    long secondBlockEndTime = tsLs.get(i + 1).getEndTime().getTime();
                    if (secondBlockEndTime - firstBlockEndTime >= 0) {
                        cursor = i + 1;//advance to next block
                    }

                    long nextBlockStartTime = tsLs.get(i + 1).getStartTime().getTime();
                    if (nextBlockStartTime - firstBlockEndTime > timeApart) {
                        SignupTimeslot newTs = new SignupTimeslot();
                        newTs.setStartTime(startTime);
                        newTs.setEndTime(tsLs.get(i).getEndTime());
                        tsList.add(newTs);
                        // reset start time
                        startTime = tsLs.get(i + 1).getStartTime();
                    }

                }

                // add last block
                SignupTimeslot newTs = new SignupTimeslot();
                newTs.setStartTime(startTime);
                newTs.setEndTime(meeting.getEndTime());
                tsList.add(newTs);
            }
        } else {
            // otherwise there is only one block in calendar
            SignupTimeslot newTs = new SignupTimeslot();
            newTs.setStartTime(meeting.getStartTime());
            newTs.setEndTime(meeting.getEndTime());
            tsList.add(newTs);
        }

        return tsList;
    }

    @Override
    public void removeCalendarEvents(List<SignupMeeting> meetings) throws Exception {
        if (meetings == null || meetings.isEmpty()) return;

        for (SignupMeeting meeting : meetings) {
            List<SignupSite> sites = meeting.getSignupSites();
            if (sites == null || sites.isEmpty()) continue;

            for (SignupSite site : sites) {
                try {
                    Calendar calendar = chooseCalendar(site);

                    if (calendar == null) continue; // something went wrong when fetching the calendar

                    String eventIds = null;
                    if (site.isSiteScope()) {
                        eventIds = site.getCalendarEventId();
                    } else {
                        List<SignupGroup> signupGroups = site.getSignupGroups();
                        for (SignupGroup group : signupGroups) {
                            eventIds = group.getCalendarEventId();
                            break;
                        }
                    }

                    if (eventIds == null || eventIds.trim().isEmpty()) continue;

                    // separate the eventIds token by '|'
                    StringTokenizer token = new StringTokenizer(eventIds, "|");
                    List<String> evtIds = new ArrayList<>();
                    while (token.hasMoreTokens()) {
                        evtIds.add(token.nextToken().trim());
                    }

                    for (String evtId : evtIds) {
                        CalendarEventEdit eventEdit = calendar.getEditEvent(evtId, org.sakaiproject.calendar.api.CalendarService.EVENT_REMOVE_CALENDAR);
                        if (eventEdit == null) continue;
                        if (!calendar.allowEditEvent(evtId)) continue;
                        calendar.removeEvent(eventEdit);
                    }
                } catch (PermissionException e) {
                    log.info("PermissionException for removal of calendar: {}", e.toString());
                }
            }
        }

    }

    // See if additional calendar is deployed and ready to use.
    // If not, we use the Sakai calendar tool by default.
    private Calendar chooseCalendar(SignupSite site) throws PermissionException {
        Calendar calendar = null;
        // TODO          sakaiFacade.getAdditionalCalendar(site.getSiteId());
        if (calendar == null) {
            try {
                calendar = calendarService.getCalendar(siteService.siteReference(site.getSiteId()));
            } catch (IdUnusedException e) {
                log.warn("Could not find calendar for site: {}", site.getSiteId());
            }
        }
        return calendar;
    }

    @Override
    public void removeCalendarEventsOnModifiedMeeting(List<SignupMeeting> meetings) {
        try {
            removeCalendarEvents(meetings);

            // remove calendar info in the event object
            for (SignupMeeting sm : meetings) {
                List<SignupSite> sites = sm.getSignupSites();
                if (sites != null && !sites.isEmpty()) {
                    for (SignupSite s : sites) {
                        if (s.getCalendarEventId() != null) {
                            s.setCalendarEventId(null);
                            s.setCalendarId(null);
                        }
                        List<SignupGroup> grps = s.getSignupGroups();
                        if (grps != null && !grps.isEmpty()) {
                            for (SignupGroup g : grps) {
                                if (g.getCalendarEventId() != null) {
                                    g.setCalendarEventId(null);
                                    g.setCalendarId(null);
                                }
                            }
                        }
                    }
                }
                updateSignupMeeting(sm, true);
            }
        } catch (Exception e) {
            log.warn("Exception for removal of calendar and calendar info may not be removed from events objects: {}", e.toString());
        }
    }

    /**
     * This method will create a skeleton  event/meeting in the Scheduler tool
     * in that site. It still needs to be committed.
     */
    private CalendarEventEdit calendarEvent(Calendar calendar, SignupSite site) throws IdUnusedException, PermissionException {
        CalendarEventEdit addEvent = calendar.addEvent();
        addEvent.setType("Meeting");
        if (!site.isSiteScope()) {
            List<Group> groups = groupIds(site);
            addEvent.setGroupAccess(groups, true);
        }
        return addEvent;
    }

    /**
     * Gets a list of Group objects associated with a site.
     *
     * @param site SignupSite object containing the site and group information
     * @return A list of Group objects. Returns empty list if no groups are found or on error.
     */
    private List<Group> groupIds(SignupSite site) {
        List<Group> groups = new ArrayList<>();
        List<SignupGroup> signupGroups = site.getSignupGroups();
        for (SignupGroup group : signupGroups) {
            try {
                Site s = siteService.getSite(site.getSiteId());
                groups.add(s.getGroup(group.getGroupId()));
            } catch (IdUnusedException e) {
                log.warn("Unable to find group in site {} with group ID {}, {}", site.getSiteId(), group.getGroupId(), e.toString());
            }
        }
        return groups;
    }


    @Override
    public void sendEmail(SignupMeeting signupMeeting, String messageType) throws Exception {
        signupEmailFacade.sendEmailAllUsers(signupMeeting, messageType);

    }

    @Override
    public void sendEmailToOrganizer(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
        signupEmailFacade.sendEmailToOrganizer(signupEventTrackingInfo);
    }

    @Override
    public void sendCancellationEmail(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
        signupEmailFacade.sendCancellationEmail(signupEventTrackingInfo);
    }

    @Override
    public void sendUpdateCommentEmail(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
        signupEmailFacade.sendUpdateCommentEmail(signupEventTrackingInfo);
    }

    @Override
    public void removeMeetings(List<SignupMeeting> meetings) throws Exception {
        repository.deleteAll(meetings);
        Set<Long> sent = new HashSet<>();

        for (SignupMeeting m : meetings) {
            if (!m.isMeetingExpired()) {
                // Only send once per recurrenceid
                if (!sent.contains(m.getRecurrenceId())) {
                    sent.add(m.getRecurrenceId());
                    log.info("Meeting is still available, email notifications will be sent");
                    // If an event is cancelled, all the site members get an email
                    m.setSendEmailToSelectedPeopleOnly(SEND_EMAIL_ONLY_SIGNED_UP_ATTENDEES);
                    signupEmailFacade.sendEmailAllUsers(m, SignupMessageTypes.SIGNUP_CANCEL_MEETING);
                } else {
                    log.debug("Not sending email for duplicate reurrenceId: {}", m.getRecurrenceId());
                }
            }
        }
    }

    @Override
    public void sendEmailToParticipantsByOrganizerAction(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
        signupEmailFacade.sendEmailToParticipantsByOrganizerAction(signupEventTrackingInfo);

    }

    @Override
    public boolean isEventExisted(Long eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void sendEmailToAttendee(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
        signupEmailFacade.sendEmailToAttendee(signupEventTrackingInfo);
    }

    @Override
    public List<String> getAllLocations(String siteId) {
        return repository.findAllLocationsBySiteId(siteId);
    }

    @Override
    public List<String> getAllCategories(String siteId) {
        return repository.findAllCategoriesBySiteId(siteId);
    }


    @Override
    public String getUsersLocalDateTimeString(final Instant date) {
        if (date == null) {
            log.warn("Date was null, returning empty string");
            return "";
        }
        final ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
        final DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withZone(zone)
                .withLocale(resourceLoader.getLocale());
        return df.format(date);
    }

}
