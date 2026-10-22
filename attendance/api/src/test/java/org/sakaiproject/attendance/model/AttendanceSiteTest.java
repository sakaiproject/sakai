/*
 * Basic unit tests for AttendanceSite convenience getters and defaults.
 */
package org.sakaiproject.attendance.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AttendanceSiteTest {

    @Test
    public void defaultsFromConstructor() {
        AttendanceSite site = new AttendanceSite("site-123");

        assertEquals("site-123", site.getSiteID());
        assertNotNull(site.getDefaultStatus());

        // New sites use manual grading until explicitly configured.
        assertFalse(site.getUseAutoGrading());

        // Constructor sets booleans to false
        assertFalse(site.getSendToGradebook());
        assertFalse(site.getIsGradeShown());
        assertFalse(site.getIsSyncing());
        assertFalse(site.getShowCommentsToStudents());
    }

    @Test
    public void nullBooleansReturnSafeDefaults() {
        AttendanceSite site = new AttendanceSite("x");
        site.setSendToGradebook(null);
        site.setIsSyncing(null);
        site.setShowCommentsToStudents(null);

        assertFalse(site.getSendToGradebook());
        assertFalse(site.getIsSyncing());
        assertFalse(site.getShowCommentsToStudents());
        site.setShowCommentsToStudents(true);
        assertTrue(site.getShowCommentsToStudents());
        site.setShowCommentsToStudents(false);
        assertFalse(site.getShowCommentsToStudents());
    }
    @Test
    public void autoGradingRequiresAnEnabledMethod() {
        AttendanceSite site = new AttendanceSite("site");
        site.setGradingMethod(null);
        assertFalse(site.getUseAutoGrading());
        for (int method : new int[] { 1, 2, 3 }) {
            site.setGradingMethod(method);
            assertTrue(site.getUseAutoGrading());
        }
    }
}

