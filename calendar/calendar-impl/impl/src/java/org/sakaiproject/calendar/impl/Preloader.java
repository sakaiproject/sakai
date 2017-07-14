package org.sakaiproject.calendar.impl;

import java.util.Timer;
import java.util.TimerTask;

import static org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.INSTITUTIONAL_CONTEXT;

/**
 * This takes the preloading out of the service.
 */
public class Preloader {

    private final BaseExternalCalendarSubscriptionService service;

    private Timer m_timer = null;
    private int cacheRefreshRate = 43200;

    public Preloader(BaseExternalCalendarSubscriptionService service) {
        this.service = service;
    }

    public void init() {
        if (service.isEnabled()) {
            m_timer = new Timer("External calendar preloading"); // init timer
            preloadCalendars(cacheRefreshRate);
        }
    }

    private void preloadCalendars(long cacheRefreshRate) {
        // load institutional calendar subscriptions as timer tasks, this is so that
        // we don't slow up the loading of sakai.
        for (final BaseExternalCalendarSubscriptionService.InsitutionalSubscription sub: service.getInstitutionalSubscriptions()) {
            m_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String reference =  service.calendarSubscriptionReference(INSTITUTIONAL_CONTEXT, service.getIdFromSubscriptionUrl(sub.url));
                    service.getCalendarSubscription(reference);
                }

            }, 0, cacheRefreshRate);
        }
    }

}
