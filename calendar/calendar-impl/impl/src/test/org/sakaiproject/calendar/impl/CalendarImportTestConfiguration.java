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
package org.sakaiproject.calendar.impl;

import java.util.TimeZone;

import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.time.impl.BasicTimeService;
import org.sakaiproject.time.impl.UserLocaleServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Real import, recurrence and time conversion; only user preference services are boundaries. */
@Configuration
public class CalendarImportTestConfiguration {
    @Bean
    public UserTimeService userTimeService() {
        UserTimeService service = mock(UserTimeService.class);
        when(service.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone("Europe/Madrid"));
        return service;
    }

    @Bean
    public UserLocaleServiceImpl userLocaleService() {
        return mock(UserLocaleServiceImpl.class);
    }

    @Bean(initMethod = "init")
    public BasicTimeService timeService(UserTimeService userTimeService, UserLocaleServiceImpl userLocaleService) {
        BasicTimeService service = new BasicTimeService();
        service.setUserTimeService(userTimeService);
        service.setUserLocaleService(userLocaleService);
        return service;
    }

    @Bean
    public DbCalendarService calendarService() {
        // Import uses the production recurrence factories; no calendar is persisted.
        return new DbCalendarService();
    }

    @Bean(initMethod = "init")
    public GenericCalendarImporter calendarImporterService(DbCalendarService calendarService, TimeService timeService) {
        GenericCalendarImporter service = new GenericCalendarImporter();
        service.setCalendarService(calendarService);
        service.setTimeService(timeService);
        service.setFormattedText(new org.sakaiproject.util.impl.FormattedTextImpl());
        return service;
    }
}
