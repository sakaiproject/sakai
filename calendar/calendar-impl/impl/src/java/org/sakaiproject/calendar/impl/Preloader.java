/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
