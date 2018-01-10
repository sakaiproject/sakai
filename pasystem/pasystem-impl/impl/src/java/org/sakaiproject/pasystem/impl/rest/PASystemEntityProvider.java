/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl.rest;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.pasystem.api.Acknowledger;
import org.sakaiproject.pasystem.api.AcknowledgementType;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * Web services supporting AJAX requests from the PA System end user display.
 */
@Slf4j
public class PASystemEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    protected DeveloperHelperService developerHelperService;
    private EntityProviderManager entityProviderManager;

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @Override
    public String getEntityPrefix() {
        return "pasystem";
    }

    @EntityCustomAction(action = "popupAcknowledge", viewKey = EntityView.VIEW_NEW)
    public String popupAcknowledge(EntityView view, Map<String, Object> params) {
        PASystem paSystem = (PASystem) ComponentManager.get(PASystem.class);
        return doAcknowledge(paSystem.getPopups(), params);
    }

    @EntityCustomAction(action = "bannerAcknowledge", viewKey = EntityView.VIEW_NEW)
    public String bannerAcknowledge(EntityView view, Map<String, Object> params) {
        PASystem paSystem = (PASystem) ComponentManager.get(PASystem.class);
        return doAcknowledge(paSystem.getBanners(), params);
    }

    private boolean checkCSRFToken(Map<String, Object> params) {
        Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");

        if (sessionToken == null || !sessionToken.equals(params.get("sakai_csrf_token"))) {
            log.warn("CSRF token validation failed");
            return false;
        }

        return true;
    }

    private String doAcknowledge(Acknowledger acknowledger, Map<String, Object> params) {
        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        if (!checkCSRFToken(params)) {
            return result.toJSONString();
        }

        User currentUser = UserDirectoryService.getCurrentUser();
        String uuid = (String) params.get("uuid");
        String acknowledgement = (String) params.get("acknowledgement");
        String userId = currentUser.getId();

        if (uuid == null || userId == null) {
            log.warn("Parameter mismatch: {}", params);
            return result.toJSONString();
        }

        if (acknowledgement == null) {
            acknowledger.acknowledge(uuid, userId);
        } else {
            acknowledger.acknowledge(uuid, userId, AcknowledgementType.of(acknowledgement));
        }
                
        result.put("status", "SUCCESS");

        return result.toJSONString();
    }

    @EntityCustomAction(action = "clearBannerAcknowledgements", viewKey = EntityView.VIEW_NEW)
    public String clearBannerAcknowledgements(EntityView view, Map<String, Object> params) {
        PASystem paSystem = (PASystem) ComponentManager.get(PASystem.class);

        JSONObject result = new JSONObject();

        result.put("status", "ERROR");

        if (!checkCSRFToken(params)) {
            return result.toJSONString();
        }

        User currentUser = UserDirectoryService.getCurrentUser();
        String userId = currentUser.getId();

        if (userId == null) {
            log.warn("Parameter mismatch: {}", params);
            return result.toJSONString();
        }

        paSystem.getBanners().clearTemporaryDismissedForUser(userId);
        result.put("status", "SUCCESS");

        return result.toJSONString();
    }

    @EntityCustomAction(action = "checkTimeZone", viewKey = EntityView.VIEW_LIST)
    public String checkTimeZone(EntityView view, Map<String, Object> params) {
        TimezoneChecker checker = new TimezoneChecker();
        JSONObject result = new JSONObject();

        result.put("status", "OK");

        String timezoneFromUser = (String) params.get("timezone");

        if (timezoneFromUser != null && checker.timezoneMismatch(timezoneFromUser)) {
            result.put("status", "MISMATCH");
            result.put("setTimezoneUrl", checker.getTimezoneToolUrlForUser());
            result.put("prefsTimezone", checker.formatTimezoneFromProfile());
            result.put("reportedTimezone", checker.formatReportedTimezone(timezoneFromUser));
        }

        return result.toJSONString();
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    static class TimezoneChecker {

        public String getTimezoneToolUrlForUser() {
            User thisUser = UserDirectoryService.getCurrentUser();
            String userid = thisUser.getId();

            // If there is no user (e.g. on the gateway site!) there's no timezone
            if(StringUtils.isEmpty(userid)) {
                return null;
            }

            try {
                Site userSite = SiteService.getSite("~" + userid);
                ToolConfiguration preferences = userSite.getToolForCommonId("sakai.preferences");
                return String.format("/portal/site/~%s/tool/%s/timezone", userid, preferences.getId());
            } catch (Exception e) {
                log.warn("Couldn't find a timezone tool for user {}", userid, e);
                return null;
            }
        }

        public boolean timezoneMismatch(String timezoneFromUser) {
            TimeZone preferredTimeZone = TimeService.getLocalTimeZone();
            TimeZone reportedTimeZone = TimeZone.getTimeZone(timezoneFromUser);

            long now = new Date().getTime();

            return preferredTimeZone.getOffset(now) != reportedTimeZone.getOffset(now);
        }

        public String formatTimezoneFromProfile() {
            return formatTimezone(TimeService.getLocalTimeZone());
        }

        public String formatReportedTimezone(String timezoneFromUser) {
            return formatTimezone(TimeZone.getTimeZone(timezoneFromUser));
        }

        public String formatTimezone(TimeZone tz) {
            return tz.getID() + " " + formatOffset(tz);
        }

        private String formatOffset(TimeZone tz) {
            long now = new Date().getTime();

            long offset = tz.getOffset(now);

            int mins = 60 * 1000;
            int hour = 60 * mins;
            return "(GMT " + String.format("%s%0,2d:%0,2d",
                    ((offset >= 0) ? "+" : ""),
                    (offset / hour),
                    ((offset % hour) / mins)) + ")";
        }

    }
}
