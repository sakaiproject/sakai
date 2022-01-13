package org.sakaiproject.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.timesheet.api.TimeSheetEntry;

public class TimeSheetUtilTest {

    private static TimeSheetUtil timeSheetUtil;
    private final static int SIXTY_MINUTES = 60;

    @BeforeClass
    public static void setup() {
        timeSheetUtil = new TimeSheetUtil();
    }

    @Test
    public void intToTimeTest() {
        Assert.assertEquals("1h 0m", timeSheetUtil.intToTime(SIXTY_MINUTES));
        Assert.assertEquals("", timeSheetUtil.intToTime(-1));
        Assert.assertEquals("1m", timeSheetUtil.intToTime(1));
        Assert.assertEquals("30m", timeSheetUtil.intToTime(SIXTY_MINUTES / 2));
        Assert.assertEquals("3h 0m", timeSheetUtil.intToTime(SIXTY_MINUTES * 3));
        Assert.assertEquals("5h 3m", timeSheetUtil.intToTime(SIXTY_MINUTES * 5 + 3));
        Assert.assertEquals("30h 30m", timeSheetUtil.intToTime(SIXTY_MINUTES * 30 + 30));
    }

    @Test
    public void timeToIntTest() {
        Assert.assertEquals(SIXTY_MINUTES, timeSheetUtil.timeToInt("1h 0m"));
        Assert.assertEquals(0, timeSheetUtil.timeToInt(""));
        Assert.assertEquals(1, timeSheetUtil.timeToInt("1m"));
        Assert.assertEquals(SIXTY_MINUTES / 2, timeSheetUtil.timeToInt("30m"));
        Assert.assertEquals(SIXTY_MINUTES, timeSheetUtil.timeToInt("1h"));
        Assert.assertEquals(SIXTY_MINUTES, timeSheetUtil.timeToInt("1H"));
        Assert.assertEquals(SIXTY_MINUTES * 3, timeSheetUtil.timeToInt("3h 0m"));
        Assert.assertEquals(SIXTY_MINUTES * 3, timeSheetUtil.timeToInt("3H 0m"));
        Assert.assertEquals(SIXTY_MINUTES * 5 + 3, timeSheetUtil.timeToInt("5h 3m"));
        Assert.assertEquals(SIXTY_MINUTES * 5 + 3, timeSheetUtil.timeToInt("5H 3H"));
        Assert.assertEquals(SIXTY_MINUTES * 5 + 3, timeSheetUtil.timeToInt("5h 3M"));
        Assert.assertEquals(SIXTY_MINUTES * 30 + 30, timeSheetUtil.timeToInt("30h 30m"));
    }

    @Test
    public void getTotalTimeSheetTest() {
        TimeSheetEntry entry1 = new TimeSheetEntry();
        TimeSheetEntry entry2 = new TimeSheetEntry();
        TimeSheetEntry entry3 = new TimeSheetEntry();
        TimeSheetEntry entry4 = new TimeSheetEntry();
        TimeSheetEntry entry5 = new TimeSheetEntry();
        entry1.setDuration(""); // this should return "0m"
        entry2.setDuration("5m"); // this has to return "5m"
        entry3.setDuration("1h 5m"); // this has to return "1h 5m"
        entry4.setDuration("2h 483m"); // this has to return "10h 3m"
        entry5.setDuration("15h 5m"); // and this "15h 5m"
        List<TimeSheetEntry> entries = Stream.of(entry1, entry2, entry3, entry4, entry5).collect(Collectors.toList());

        //So, total time is the addition of the durations, that should be "26h 18m"
        Assert.assertEquals("26h 18m", timeSheetUtil.getTotalTimeSheet(entries));
    }
}
