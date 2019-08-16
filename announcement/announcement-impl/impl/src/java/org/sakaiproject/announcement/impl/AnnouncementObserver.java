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

    private String getChannel(final Event event) {
        final String[] resourceSplitted = event.getResource().split("/");
        final String site = resourceSplitted[3];
        return "/announcement/channel/" + site + "/main";
    }

}