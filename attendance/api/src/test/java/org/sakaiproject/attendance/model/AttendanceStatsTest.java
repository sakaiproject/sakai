/*
 * Basic unit tests for AttendanceStats counters.
 */
package org.sakaiproject.attendance.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AttendanceStatsTest {

    @Test
    public void negativeValuesClampedToZero() {
        AttendanceStats stats = new AttendanceStats();
        stats.setPresent(-5);
        stats.setUnexcused(-1);
        stats.setExcused(-2);
        stats.setLate(-3);
        stats.setLeftEarly(-4);

        assertEquals(0, stats.getPresent());
        assertEquals(0, stats.getUnexcused());
        assertEquals(0, stats.getExcused());
        assertEquals(0, stats.getLate());
        assertEquals(0, stats.getLeftEarly());
    }

    @Test
    public void positiveValuesPersist() {
        AttendanceStats stats = new AttendanceStats();
        stats.setPresent(3);
        stats.setUnexcused(1);
        stats.setExcused(2);
        stats.setLate(4);
        stats.setLeftEarly(5);

        assertEquals(3, stats.getPresent());
        assertEquals(1, stats.getUnexcused());
        assertEquals(2, stats.getExcused());
        assertEquals(4, stats.getLate());
        assertEquals(5, stats.getLeftEarly());
    }
}

