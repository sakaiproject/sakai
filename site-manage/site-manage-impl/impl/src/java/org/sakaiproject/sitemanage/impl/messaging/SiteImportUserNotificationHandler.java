/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitemanage.impl.messaging;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * Notifies the user who started a background site import once it finishes, so they get
 * feedback even when email notification is unavailable. The displayed text is built by the
 * sakai-notifications web component from the site title.
 */
@Slf4j
public class SiteImportUserNotificationHandler extends AbstractUserNotificationHandler {

    private static final String SITE_INFO_TOOL_ID = "sakai.siteinfo";

    @Autowired private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return List.of(SiteService.EVENT_SITE_IMPORT_END);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String userId = e.getUserId();
        String siteId = e.getContext();

        if (StringUtils.isAnyBlank(userId, siteId)) {
            return Optional.empty();
        }

        try {
            Site site = siteService.getSite(siteId);
            return Optional.of(List.of(new UserNotificationData(
                    userId, userId, siteId, site.getTitle(), site.getUrl(), SITE_INFO_TOOL_ID, false, null)));
        } catch (IdUnusedException idue) {
            log.warn("No site {} for a completed import, skipping notification", siteId);
            return Optional.empty();
        }
    }
}
