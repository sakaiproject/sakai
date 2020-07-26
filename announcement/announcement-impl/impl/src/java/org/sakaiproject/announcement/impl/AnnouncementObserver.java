/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.announcement.impl;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.message.api.Message;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnouncementObserver implements Observer {

    @Setter
    private EventTrackingService eventTrackingService;
    @Setter
    private MemoryService memoryService;
    private Cache<String, List<Message>> messagesCache;

    public void init() {
        eventTrackingService.addLocalObserver(this);
        messagesCache = memoryService.getCache("org.sakaiproject.announcement.tool.messages.cache");
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    public void update(Observable arg0, Object arg) {
        if (!(arg instanceof Event)) {
            return;
        }

        final Event event = (Event) arg;
        final String eventType = event.getEvent();

        if (eventType != null) {
            switch(eventType) {
                case AnnouncementService.SECURE_ANNC_ADD:
                case AnnouncementService.SECURE_ANNC_UPDATE_ANY:
                case AnnouncementService.SECURE_ANNC_UPDATE_OWN:
                case AnnouncementService.SECURE_ANNC_REMOVE_ANY:
                case AnnouncementService.SECURE_ANNC_REMOVE_OWN:
                    String channelName = getChannel(event);
                    log.debug("Announcement event: {}", eventType);
                    messagesCache.remove(channelName);
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * Utility method to generate the channel ID based on the event reference.
     * Normal channel ID: "/announcement/channel/<siteID>/main"
     * Admin Workspace channel ID: "/announcement/channel/!site/motd"
     */
    private String getChannel(final Event event) {
        final String[] resourceSplitted = event.getResource().split("/");
        final String site = "!admin".equals(resourceSplitted[3]) ? "!site" : resourceSplitted[3];
        return "/announcement/channel/" + site + ("!site".equals(site) ? "/motd" : "/main");
    }

}