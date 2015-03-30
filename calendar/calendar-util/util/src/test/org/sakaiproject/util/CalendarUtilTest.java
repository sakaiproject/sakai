package org.sakaiproject.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

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
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        // This a day that the clocks go forward in London
        DateTime dateTime = new DateTime(2015, 3, 29, 3,0, zone);
        assertEquals("AM", CalendarUtil.getLocalAMString(dateTime));
    }

    @Test
    public void testLocalPMString() {
        // This is to test that the schedule tool works through GMT -> BST transitions
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        // This a day that the clocks go forward in London
        DateTime dateTime = new DateTime(2015, 3, 29, 16,0, zone);
        assertEquals("PM", CalendarUtil.getLocalPMString(dateTime));
    }

    @Test
    public void testLocalPMStringStartOfDay() {
        // This is to test that the schedule tool works through GMT -> BST transitions
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/London"));
        // This a day that the clocks go forward in London
        DateTime dateTime = new DateTime(2015, 3, 29, 0, 0, zone);
        assertEquals("PM", CalendarUtil.getLocalPMString(dateTime));
    }


}
