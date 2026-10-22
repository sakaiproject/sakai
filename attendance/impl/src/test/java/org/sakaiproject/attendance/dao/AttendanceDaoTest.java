package org.sakaiproject.attendance.dao;

import java.time.Instant;

import org.junit.Test;
import org.sakaiproject.attendance.dao.impl.AttendanceDaoImpl;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AttendanceDaoTest {

    @Test
    public void updateAttendanceEventRejectsRecordsWithoutAuditFields() {
        AttendanceEvent event = new AttendanceEvent();
        event.setLastModifiedBy("instructor");
        event.setLastModifiedDate(Instant.now());
        event.getRecords().add(new AttendanceRecord(event, "student", Status.PRESENT));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new AttendanceDaoImpl().updateAttendanceEvent(event));

        assertEquals("AttendanceRecord audit fields must be set before persistence", exception.getMessage());
    }
}
