/*
 * Lightweight tests for AttendanceLogicImpl methods that don't require DAO wiring.
 */
package org.sakaiproject.attendance.logic;

import java.time.Instant;

import org.junit.Test;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStats;
import org.sakaiproject.attendance.model.Status;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttendanceLogicImplTest {

    @Test
    public void getStatsForStatusReturnsCorrectField() {
        AttendanceStats stats = new AttendanceStats();
        stats.setPresent(7);
        stats.setUnexcused(3);
        stats.setExcused(2);
        stats.setLate(5);
        stats.setLeftEarly(1);

        AttendanceLogicImpl logic = new AttendanceLogicImpl();

        assertEquals(7, logic.getStatsForStatus(stats, Status.PRESENT));
        assertEquals(3, logic.getStatsForStatus(stats, Status.UNEXCUSED_ABSENCE));
        assertEquals(2, logic.getStatsForStatus(stats, Status.EXCUSED_ABSENCE));
        assertEquals(5, logic.getStatsForStatus(stats, Status.LATE));
        assertEquals(1, logic.getStatsForStatus(stats, Status.LEFT_EARLY));
    }

    @Test
    public void updateAttendanceEventAuditsNewRecordsBeforeSaving() {
        AttendanceDao dao = mock(AttendanceDao.class);
        SakaiProxy sakaiProxy = mock(SakaiProxy.class);
        AttendanceLogicImpl logic = new AttendanceLogicImpl();
        logic.setDao(dao);
        logic.setSakaiProxy(sakaiProxy);

        AttendanceEvent event = new AttendanceEvent();
        event.setId(1L);
        AttendanceRecord newRecord = new AttendanceRecord(event, "student", Status.PRESENT);
        event.getRecords().add(newRecord);
        Instant previousModification = Instant.EPOCH;
        AttendanceRecord existingRecord = new AttendanceRecord(event, "another-student", Status.PRESENT);
        existingRecord.setId(2L);
        existingRecord.setLastModifiedBy("previous-instructor");
        existingRecord.setLastModifiedDate(previousModification);
        event.getRecords().add(existingRecord);

        when(sakaiProxy.getCurrentUserId()).thenReturn("instructor");
        when(dao.updateAttendanceEvent(event)).thenAnswer(invocation -> {
            assertEquals("instructor", newRecord.getLastModifiedBy());
            assertNotNull(newRecord.getLastModifiedDate());
            assertEquals("previous-instructor", existingRecord.getLastModifiedBy());
            assertEquals(previousModification, existingRecord.getLastModifiedDate());
            return true;
        });

        assertTrue(logic.updateAttendanceEvent(event));
    }
}
