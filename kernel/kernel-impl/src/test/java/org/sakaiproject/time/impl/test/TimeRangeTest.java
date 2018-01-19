package org.sakaiproject.time.impl.test;

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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

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
    }

    @Test
    public void testTimeRangeParseReversed() {
        // TimeRange will swap times if end is before start.
        TimeRange swapped = timeService.newTimeRange("19700101010000000-19700101000000000");
        TimeRange correct = timeService.newTimeRange("19700101000000000-19700101010000000");
        assertEquals(correct, swapped);
    }

}
