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

        // Constructor sets gradingMethod = 0; getUseAutoGrading() checks non-null
        assertTrue(site.getUseAutoGrading());

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

        assertFalse(site.getSendToGradebook());
        assertFalse(site.getIsSyncing());
    }
}

