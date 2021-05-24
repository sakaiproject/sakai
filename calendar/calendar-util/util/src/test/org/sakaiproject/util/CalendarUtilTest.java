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
package org.sakaiproject.util;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CalendarUtilTest {


    @Before
    public void setUp() {
        // Need this so that developers in locales that don't use AM/PM have passing tests
        // If you want to test this setting is working set it to Locale.JAPAN which should
        // result in failing tests.
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testLocalAMString() {
        // This is to test that the schedule tool works through GMT -> BST transitions
       /* DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        // This a day that the clocks go forward in London
        DateTime dateTime = new DateTime(2015, 3, 29, 3,0, zone);
        assertEquals("AM", CalendarUtil.getLocalAMString(dateTime));
        DateTime dateTimePM = new DateTime(2015, 3, 29, 17,0, zone);
        assertEquals("AM", CalendarUtil.getLocalAMString(dateTimePM));
        */
        
       ZoneId zoneJ = TimeZone.getTimeZone("Europe/London").toZoneId();
       TimeZone timeZone = TimeZone.getDefault(); 
       Instant dateTimeJ = LocalDateTime.of(2015, 3, 29, 3,0).atZone(zoneJ).toInstant();
       assertEquals("AM", CalendarUtil.getLocalAMString(dateTimeJ, timeZone.toZoneId()));
       Instant ld = LocalDateTime.of(2015, 3, 29, 20, 0).toInstant(ZoneOffset.UTC);
       assertEquals("AM", CalendarUtil.getLocalAMString(ld, timeZone.toZoneId()));
       
       //assertEquals("PM", CalendarUtil.getLocalPMString(dateTimeJ, timeZone.toZoneId()));
       //assertEquals("PM", CalendarUtil.getLocalPMString(ld, timeZone.toZoneId()));
    }

    @Test
    public void testLocalPMString() {
        // This is to test that the schedule tool works through GMT -> BST transitions
        ZoneId zone = TimeZone.getTimeZone("Europe/London").toZoneId();
        // This a day that the clocks go forward in London
        Instant dateTime = LocalDateTime.of(2015, 3, 29, 16,0).atZone(zone).toInstant();
        assertEquals("PM", CalendarUtil.getLocalPMString(dateTime, zone));
    }

    @Test
    public void testLocalPMStringStartOfDay() {
        // This is to test that the schedule tool works through GMT -> BST transitions
        ZoneId zone = TimeZone.getTimeZone("Europe/London").toZoneId();
        // This a day that the clocks go forward in London
        Instant dateTime = LocalDateTime.of(2015, 3, 29, 0, 0).atZone(zone).toInstant();
        assertEquals("PM", CalendarUtil.getLocalPMString(dateTime, zone));
    }

    @Test
    public void testDayOfMonthAtEnd() {
        // This tests a problem with how Sakai was calculating the month when it was the last day of the month.
        ResourceLoader rb = Mockito.mock(ResourceLoader.class);
        Mockito.when(rb.getLocale()).thenReturn(Locale.ENGLISH);
        Instant instant = Instant.parse("2007-08-31T09:30:00Z");
        CalendarUtil util = new CalendarUtil(Clock.fixed(instant, ZoneOffset.ofHours(0)),rb);
        String[] calendarMonthNames = util.getCalendarMonthNames(false);
        String[] expected = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Assert.assertArrayEquals(expected, calendarMonthNames);
    }


}
