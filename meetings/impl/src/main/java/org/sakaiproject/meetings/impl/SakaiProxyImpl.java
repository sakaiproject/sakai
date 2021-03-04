/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.impl;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.meetings.api.SakaiProxy;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * Proxy class for interacting with multiple Sakai APIs.
 * @author Adrian Fish
 */
@Slf4j
class SakaiProxyImpl implements SakaiProxy {

    @Resource private MeetingsService bbbMeetingManager;
    @Resource private UserDirectoryService userDirectoryService;
    @Resource private SiteService siteService;
    @Resource private ToolManager toolManager;
    @Resource private ServerConfigurationService serverConfigurationService;

    public User getCurrentUser() {

        try {
            return userDirectoryService.getCurrentUser();
        } catch (Throwable t) {
            log.error("Exception caught whilst getting current user.",t);
            log.debug("Returning null ...");
            return null;
        }
    }

    public String getCurrentSiteId() {

        Placement placement = toolManager.getCurrentPlacement();
        if (placement == null) {
            log.warn("Current tool placement is null.");
            return null;
        }

        return placement.getContext();
    }

    public String getCurrentToolId() {

        Placement placement = toolManager.getCurrentPlacement();
        if (placement == null) {
            log.warn("Current tool placement is null.");
            return null;
        }

        return placement.getId();
    }

    public String getUserLanguageCode() {

        Locale locale = (new ResourceLoader()).getLocale();
        return (locale.toString() == null || "".equals(locale.toString()))? "en": locale.toString();
    }
    public String getSakaiVersion() {
        return serverConfigurationService.getString("version.sakai");
    }

    public String getSakaiSkin() {

        String skin = serverConfigurationService.getString("skin.default");
        String siteSkin = siteService.getSiteSkin(getCurrentSiteId());
        return siteSkin != null ? siteSkin : (skin != null ? skin : "default");
    }

    public long getServerTimeInUserTimezone() {

        Map<String, Object> serverTimeInUserTimezone = bbbMeetingManager.getServerTimeInUserTimezone();
        return Long.parseLong( (String) serverTimeInUserTimezone.get("timestamp"));
    }

    public long getUserTimezoneOffset() {

        Map<String, Object> serverTimeInUserTimezone = bbbMeetingManager.getServerTimeInUserTimezone();
        return Long.parseLong( (String) serverTimeInUserTimezone.get("timezoneOffset"));
    }

    public String getUserTimezone() {

        Map<String, Object> serverTimeInUserTimezone = bbbMeetingManager.getServerTimeInUserTimezone();
        return (String) serverTimeInUserTimezone.get("timezone");
    }

    public void checkPermissions() {
        bbbMeetingManager.checkPermissions(getCurrentSiteId());
    }

    public String getFileSizeMax() {
        return serverConfigurationService.getString(MeetingsService.SYSTEM_UPLOAD_MAX);
    }
}
