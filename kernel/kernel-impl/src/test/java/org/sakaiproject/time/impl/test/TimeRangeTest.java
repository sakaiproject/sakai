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
package org.sakaiproject.time.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.time.impl.BasicTimeService;
import org.sakaiproject.time.impl.UserLocaleServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeRangeTest {

    private TimeService timeService;
    private Clock fixed;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private UserLocaleServiceImpl userLocaleService;
    @Mock
    private UserTimeService userTimeService;

    @Before
    public void setUp() {
        fixed = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        BasicTimeService timeService = new BasicTimeService();
        timeService.setUserLocaleService(userLocaleService);
        timeService.setUserTimeService(userTimeService);
        timeService.setClock(fixed);
        timeService.init();
        this.timeService = timeService;
    }

    @Test
    public void testTimeRangeToStringStart() {
        TimeRange timeRange ;
        timeRange= timeService.newTimeRange(0, 0);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(0), true, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(0), false, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(0), true, false);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(0), false, false);
        assertEquals("19700101000000000", timeRange.toString());
        
        //Instant
        Instant epoch = Instant.EPOCH;
        timeRange = timeService.newTimeRange(epoch, epoch, true, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, false, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, true, false);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, false, false);
        assertEquals("19700101000000000", timeRange.toString());
    }

    @Test
    public void testTimeRangeToStringStartEnd() {
        TimeRange timeRange = timeService.newTimeRange(0, 3600*1000);
        assertEquals("19700101000000000-19700101010000000", timeRange.toString());
    }

    @Test
    public void testTimeRangeToStringInclusions() {
        TimeRange timeRange;
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), true, true);
        assertEquals("19700101000000000-19700101010000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), false, true);
        assertEquals("19700101000000000[19700101010000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), false, false);
        assertEquals("19700101000000000~19700101010000000", timeRange.toString());
        timeRange = timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), true, false);
        assertEquals("19700101000000000]19700101010000000", timeRange.toString());
    }

    @Test
    public void testTimeRangeParseRelative() {
        TimeRange timeRange;
        timeRange = timeService.newTimeRange("19700101000000000-=3600000");
        assertEquals(timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), true, true), timeRange);
        timeRange = timeService.newTimeRange("=3600000-19700101010000000");
        assertEquals(timeService.newTimeRange(timeService.newTime(0), timeService.newTime(3600*1000), true, true), timeRange);
    }

    @Test
    public void testTimeRangeParseInvalid() {
        // invalid parses return now(our fixed clock)
        TimeRange timeRange;
        // 2 relative dates isn't going to work.
        timeRange = timeService.newTimeRange("=1000-=1000");
        assertEquals(fixed.millis(), timeRange.firstTime().getTime());
        // Relative start time
        timeRange = timeService.newTimeRange("=1000");
        assertEquals(fixed.millis(), timeRange.firstTime().getTime());
        // Junk
        timeRange = timeService.newTimeRange("junk");
        assertEquals(fixed.millis(), timeRange.firstTime().getTime());
        //ISO
        timeRange = timeService.newTimeRange("2021-05-31T09:09:45.492550Z-2021-05-31T09:09:45.492550Z");
        assertEquals(fixed.millis(), timeRange.firstInstant().toEpochMilli());
        timeRange = timeService.newTimeRange("2021-05-31T09:09:45.492550Z-2021");
        assertEquals(fixed.millis(), timeRange.firstInstant().toEpochMilli());
        
    }

    @Test
    public void testTimeRangeParseReversed() {
        // TimeRange will swap times if end is before start.
        TimeRange swapped = timeService.newTimeRange("19700101010000000-19700101000000000");
        TimeRange correct = timeService.newTimeRange("19700101000000000-19700101010000000");
        assertEquals(correct, swapped);
    }
    
    
    //Instant
    
    @Test
    public void testTimeRangeToStringStartInstant() {
        Instant epoch = Instant.EPOCH;
        TimeRange timeRange = timeService.newTimeRange(epoch, epoch, true, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, false, true);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, true, false);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch, false, false);
        assertEquals("19700101000000000", timeRange.toString());
    }
    
    @Test
    public void testTimeRangeToStartInstant() {
        Instant epoch = Instant.EPOCH;
        TimeRange timeRange = timeService.newTimeRange(epoch);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch, epoch);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch.toEpochMilli(), 0);
        assertEquals("19700101000000000", timeRange.toString());
        timeRange = timeService.newTimeRange(epoch.toEpochMilli(), 100);
        assertNotEquals("19700101000000000", timeRange.toString());
        assertEquals("19700101000000000-19700101000000100", timeRange.toString());
        
        timeRange = timeService.newTimeRange(epoch, Instant.ofEpochSecond(100));
        assertNotEquals("19700101000000000", timeRange.toString());
        assertEquals("19700101000000000-19700101000140000", timeRange.toString());
        
    }
    @Test
    public void testTimeFudgeInstant() {
        Instant ts = Instant.ofEpochMilli(100l);
        Instant te = Instant.ofEpochMilli(142);

        TimeRange tr1 = timeService.newTimeRange(ts, te, false, false);;
        assertEquals(tr1.firstInstant(0).toEpochMilli(), 100l);
        assertEquals(tr1.firstInstant(142).toEpochMilli(), 100l + 142);
        
        assertEquals(tr1.lastInstant(0).toEpochMilli(), 142);
        assertEquals(tr1.lastInstant(142).toEpochMilli(), 0);
    }

}
