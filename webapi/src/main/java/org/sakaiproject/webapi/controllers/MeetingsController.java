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

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.meetings.api.beans.MeetingTransferBean;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class MeetingsController extends AbstractSakaiApiController {

	@Resource private MeetingsService meetingsService;
	@Resource private UserDirectoryService userDirectoryService;
	@Resource private SiteService siteService;
	@Resource private ServerConfigurationService serverConfigurationService;
	@Resource private ContentHostingService contentHostingService;
	@Resource private SecurityService securityService;
	@Resource(name = "org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;

	@GetMapping(value = "/sites/{siteId}/meetings/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSiteSettings(@PathVariable String siteId) {

		Session session = checkSakaiSession();

        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put("autorefreshInterval", getAutorefreshInterval());
        getAddUpdateFormConfigParameters(settings);
        settings.put("serverTimeInDefaultTimezone", meetingsService.getServerTimeInDefaultTimezone());
        settings.put("serverTimeInUserTimezone", meetingsService.getServerTimeInUserTimezone());
        settings.put("recordingFormatFilterEnabled", meetingsService.isRecordingFormatFilterEnabled());
        settings.put("canAddCalendarEvent", securityService.unlock(CalendarService.AUTH_ADD_CALENDAR, "/site/" + siteId));

        MeetingTransferBean meetingTemplate = meetingsService.getMeetingTemplate(siteId);
        meetingTemplate.iCalAlarmMinutes = 30;

        Map<String, Object> data = new HashMap<>();
        data.put("settings", settings);
        data.put("selectionOptions", getUserSelectionOptions(siteId));
        data.put("meetingTemplate", meetingTemplate);
        data.put("checkICalOption", serverConfigurationService.getBoolean(MeetingsService.CFG_CHECKICALOPTION, true));
        User user = userDirectoryService.getCurrentUser();
        Map<String, String> currentUser = new HashMap<>();
        currentUser.put("id", user.getId());
        currentUser.put("displayName", user.getDisplayName());
        data.put("currentUser", currentUser);

        try {
            data.put("recordingsData", meetingsService.getSiteRecordings(siteId));
            data.put("meetings", meetingsService.getSiteMeetings(siteId));
        } catch (Exception e) {
            log.error("Failed to get recordings or meetings for site {}", siteId, e);
        }

        return ResponseEntity.ok().body(data);
    }

    private Map<String, Object> getUserSelectionOptions(String siteId) {

        try {
            Map<String, Object> map = new HashMap<>();
            Site site = siteService.getSite(siteId);

            // groups
            List<Map<String, String>> groups = new ArrayList<>();
            for (Group g : site.getGroups()) {
                Map<String, String> m = new HashMap<>();
                m.put("id", g.getId());
                m.put("title", g.getTitle());
                groups.add(m);
            }
            map.put("groups", groups);

            // roles
            List<Map<String, String>> roles = new ArrayList<>();
            for (Role r : site.getRoles()) {
                Map<String, String> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("title", r.getId());
                roles.add(m);
            }
            map.put("roles", roles);

            // users
            List<Map<String, String>> users = new ArrayList<>();
            for (Member u : site.getMembers()) {
                String displayName = null;
                try {
                    displayName = userDirectoryService.getUser(u.getUserId())
                            .getDisplayName();
                } catch (UserNotDefinedException e1) {
                    log.warn("Could not retrieve displayName for userId: " + u.getUserId());
                }

                if (displayName != null) {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", u.getUserId());
                    m.put("title", displayName + " (" + u.getUserDisplayId() + ")");
                    users.add(m);
                }
            }
            map.put("users", users);

            // defaults
            Map<String, String> dlfts = new HashMap<>();
            dlfts.put(MeetingsService.CFG_DEFAULT_ALLUSERS,
                    serverConfigurationService.getString(MeetingsService.CFG_DEFAULT_ALLUSERS, "true").toLowerCase());
            dlfts.put(MeetingsService.CFG_DEFAULT_OWNER,
                    serverConfigurationService.getString(MeetingsService.CFG_DEFAULT_OWNER, "moderator").toLowerCase());
            map.put("defaults", dlfts);

            return map;
        } catch (Exception e) {
            log.error("Error while building selection options", e);
            return null;
        }
    }

    private Map<String, String> getAutorefreshInterval() {

        Map<String, String> interval = new HashMap<>();
        String autorefreshMeetings = meetingsService.getAutorefreshForMeetings();
        if (autorefreshMeetings != null) {
            interval.put("meetings", autorefreshMeetings);
        }
        String autorefreshRecordings = meetingsService.getAutorefreshForRecordings();
        if (autorefreshRecordings != null) {
            interval.put("recordings", autorefreshRecordings);
        }
        return interval;
    }

    private Map<String, Object> getAddUpdateFormConfigParameters(Map<String, Object> map) {

        //UX settings for 'recording' checkbox
        Boolean recordingEnabled = Boolean.parseBoolean(meetingsService.isRecordingEnabled());
        if (recordingEnabled != null) {
            map.put("recordingEnabled", recordingEnabled);
        }
        Boolean recordingEditable = Boolean.parseBoolean(meetingsService.isRecordingEditable());
        if (recordingEditable != null) {
            map.put("recordingEditable", recordingEditable);
        }
        Boolean recordingDefault = Boolean.parseBoolean(meetingsService.getRecordingDefault());
        if (recordingDefault != null) {
            map.put("recordingDefault", recordingDefault);
        }
        //UX settings for 'duration' box
        Boolean durationEnabled = Boolean.parseBoolean(meetingsService.isDurationEnabled());
        if (durationEnabled != null) {
            map.put("durationEnabled", durationEnabled);
        }
        String durationDefault = meetingsService.getDurationDefault();
        if (durationDefault != null) {
            map.put("durationDefault", durationDefault);
        }
        //UX settings for 'wait moderator' box
        Boolean waitmoderatorEnabled = Boolean.parseBoolean(meetingsService.isWaitModeratorEnabled());
        if (waitmoderatorEnabled != null) {
            map.put("waitmoderatorEnabled", waitmoderatorEnabled);
        }
        Boolean waitmoderatorEditable = Boolean.parseBoolean(meetingsService.isWaitModeratorEditable());
        if (waitmoderatorEditable != null) {
            map.put("waitmoderatorEditable", waitmoderatorEditable);
        }
        Boolean waitmoderatorDefault = Boolean.parseBoolean(meetingsService.getWaitModeratorDefault());
        if (waitmoderatorDefault != null) {
            map.put("waitmoderatorDefault", waitmoderatorDefault);
        }
        //UX settings for 'multiple sessions allowed' box
        Boolean multiplesessionsallowedEnabled = Boolean.parseBoolean(meetingsService.isMultipleSessionsAllowedEnabled());
        if (multiplesessionsallowedEnabled != null) {
            map.put("multiplesessionsallowedEnabled", multiplesessionsallowedEnabled);
        }
        Boolean multiplesessionsallowedEditable = Boolean.parseBoolean(meetingsService.isMultipleSessionsAllowedEditable());
        if (multiplesessionsallowedEditable != null) {
            map.put("multiplesessionsallowedEditable", multiplesessionsallowedEditable);
        }
        Boolean multiplesessionsallowedDefault = Boolean.parseBoolean(meetingsService.getMultipleSessionsAllowedDefault());
        if (multiplesessionsallowedDefault != null) {
            map.put("multiplesessionsallowedDefault", multiplesessionsallowedDefault);
        }
        //UX settings for 'preupload presentation' box
        Boolean preuploadpresentationEnabled = Boolean.parseBoolean(meetingsService.isPreuploadPresentationEnabled());
        if (preuploadpresentationEnabled != null) {
            map.put("preuploadpresentationEnabled", preuploadpresentationEnabled);
        }
        //UX settings for 'description' box
        String descriptionMaxLength = meetingsService.getMaxLengthForDescription();
        if (descriptionMaxLength != null) {
            map.put("descriptionMaxLength", descriptionMaxLength);
        }
        String descriptionType = meetingsService.getTextBoxTypeForDescription();
        if (descriptionType != null) {
            map.put("descriptionType", descriptionType);
        }
        return map;
    }

	@GetMapping(value = "/sites/{siteId}/meetings/{meetingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeetingTransferBean> getMeeting(@PathVariable String siteId, @PathVariable String meetingId) {

		Session session = checkSakaiSession();

        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Optional<MeetingTransferBean> optMeeting = meetingsService.getMeeting(meetingId, siteId);

            if (!optMeeting.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(optMeeting.get());
        } catch (Exception e) {
            log.error("Failed to get meeting", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

	@PostMapping(value = "/sites/{siteId}/meetings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MeetingTransferBean> createMeeting(@RequestBody MeetingTransferBean meetingBean) throws Exception {

		checkSakaiSession();
        return ResponseEntity.ok(meetingsService.saveMeeting(meetingBean, false));
    }

	@GetMapping(value = "/sites/{siteId}/meetings/{meetingId}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMeetingInfo(@PathVariable String siteId, @PathVariable String meetingId) {

        log.debug("getMeetingInfo");

        try {
            return ResponseEntity.ok(meetingsService.getMeetingInfo(meetingId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

	@GetMapping(value = "/sites/{siteId}/meetings/{meetingId}/join")
    public ResponseEntity<String> joinMeeting(@PathVariable String siteId, @PathVariable String meetingId) {

        log.debug("joinMeeting");

        // get join url
        try {
            User user = userDirectoryService.getCurrentUser();
            Optional<MeetingTransferBean> optMeeting = meetingsService.getMeeting(meetingId, siteId);
            if (!optMeeting.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            String joinUrl = meetingsService.getJoinUrl(optMeeting.get().toMeeting(), user);

            if (joinUrl == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // log meeting join event
            meetingsService.logMeetingJoin(meetingId);

            try {
                meetingsService.checkJoinMeetingPreConditions(optMeeting.get().toMeeting());
            } catch (MeetingsException e) {
                System.out.println("asdfasdfasdfasdfsfd");
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(getHtmlForJoining(joinUrl));
        } catch (Exception e) {
            e.printStackTrace();
                System.out.println("asdfasdfasdfasdfsfd");
            return ResponseEntity.badRequest().build();
        }
    }

    private String getHtmlForJoining(String joinUrl) {

        return "<html>\n" +
            "  <head>\n" +
            "    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n" +
            "    <meta http-equiv='cache-control' content='max-age=0' />\n" +
            "    <meta http-equiv='cache-control' content='no-cache' />\n" +
            "    <meta http-equiv='expires' content='-1' />\n" +
            "    <meta http-equiv='expires' content='Tue, 01 Jan 1980 1:00:00 GMT' />\n" +
            "    <meta http-equiv='pragma' content='no-cache' />\n" +
            "    <meta http-equiv='refresh' content='0; url=" + joinUrl + "' />\n" +
            "  </head>\n" +
            "  <body>\n" +
            "  </body>\n" +
            "</html>\n";
    }

    @GetMapping(value = "/sites/{siteId}/meetings/{meetingId}/end")
    public ResponseEntity<String> endMeeting(@PathVariable String meetingId) {

        log.debug("endMeeting");

        try {
            return ResponseEntity.ok(Boolean.toString(meetingsService.endMeeting(meetingId)));
        } catch (Exception e) {
            log.error("Failed to end meeting {}", meetingId, e);
            return ResponseEntity.badRequest().build();
        }
    }

	@DeleteMapping(value = "/sites/{siteId}/meetings/{meetingId}")
    public void deleteMeeting(@PathVariable String meetingId) throws Exception {
        meetingsService.deleteMeeting(meetingId);
    }

    private String generatePassword() {
        return Long.toHexString(new Random(System.currentTimeMillis()).nextLong());
    }
}
