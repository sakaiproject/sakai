/*
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.attendance.dao;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.attendance.test.AttendanceTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttendanceTestConfiguration.class)
@Transactional(transactionManager = "org.sakaiproject.springframework.orm.jpa.GlobalJpaTransactionManager")
public class AttendanceDaoTest {
    @Autowired private AttendanceDao dao;
    @Autowired @Qualifier("attendanceEntityManager") private EntityManager entityManager;

    @Test
    public void persistsAndReloadsEventRecordsAndSharedKeyStatistics() {
        AttendanceSite site = new AttendanceSite(UUID.randomUUID().toString());
        assertTrue(dao.addAttendanceSite(site));
        AttendanceEvent event = event(site);
        event.getRecords().add(record(event, "student"));
        assertTrue(dao.updateAttendanceEvent(event));
        assertNotNull(event.getId());
        entityManager.flush();
        entityManager.clear();

        AttendanceEvent loaded = dao.getAttendanceEvent(event.getId());
        assertEquals("Meeting", loaded.getName());
        assertEquals(1, loaded.getRecords().size());
        assertEquals("instructor", loaded.getRecords().iterator().next().getLastModifiedBy());

        AttendanceItemStats stats = new AttendanceItemStats(loaded);
        stats.setPresent(1);
        assertTrue(dao.updateAttendanceItemStats(stats));
        entityManager.flush();
        entityManager.clear();
        assertEquals(event.getId(), dao.getAttendanceItemStats(event).getId());
        assertEquals(1, dao.getAttendanceItemStats(event).getPresent());

        // Update a detached shared-primary-key object, as the Wicket UI does.
        stats.setPresent(2);
        assertTrue(dao.updateAttendanceItemStats(stats));
        entityManager.flush();
        entityManager.clear();
        assertEquals(2, dao.getAttendanceItemStats(event).getPresent());
        assertTrue(dao.deleteAttendanceEvent(dao.getAttendanceEvent(event.getId())));
        entityManager.flush();
        entityManager.clear();
        assertNull(dao.getAttendanceEvent(event.getId()));
        assertNull(dao.getAttendanceItemStats(event));
    }

    @Test
    public void updatesExistingStudentRecordWithoutCreatingDuplicate() {
        AttendanceSite site = new AttendanceSite(UUID.randomUUID().toString());
        dao.addAttendanceSite(site);
        AttendanceEvent event = event(site);
        dao.addAttendanceEventNow(event);
        AttendanceRecord first = record(event, "student");
        dao.updateAttendanceRecord(first);
        entityManager.flush();
        entityManager.clear();
        AttendanceRecord replacement = record(dao.getAttendanceEvent(event.getId()), "student");
        replacement.setStatus(Status.LATE);
        dao.updateAttendanceRecord(replacement);
        entityManager.flush();
        entityManager.clear();
        assertEquals(first.getId(), replacement.getId());
        assertEquals(1, dao.getAttendanceEvent(event.getId()).getRecords().size());
        assertEquals(Status.LATE, dao.getStatusRecord(first.getId()).getStatus());
    }

    @Test
    public void persistsStatusesGradesRulesAndSyncMarkers() {
        AttendanceSite site = new AttendanceSite(UUID.randomUUID().toString());
        dao.addAttendanceSite(site);
        AttendanceStatus status = new AttendanceStatus();
        status.setAttendanceSite(site);
        status.setStatus(Status.PRESENT);
        status.setIsActive(true);
        dao.updateAttendanceStatuses(List.of(status));
        AttendanceGrade grade = new AttendanceGrade(site, "student");
        grade.setLastModifiedBy("instructor");
        grade.setLastModifiedDate(Instant.now());
        grade.setGrade(9.0);
        dao.updateAttendanceGrade(grade);
        GradingRule rule = new GradingRule();
        rule.setAttendanceSite(site);
        rule.setLastModifiedBy("instructor");
        rule.setLastModifiedDate(Instant.now());
        rule.setStatus(Status.PRESENT);
        rule.setPoints(1.0);
        rule.setStartRange(0);
        dao.addGradingRule(rule);
        AttendanceUserStats stats = new AttendanceUserStats("student", site);
        stats.setPresent(2);
        dao.updateAttendanceUserStats(stats);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, dao.getActiveStatusesForSite(site).size());
        assertEquals(1, dao.getAllStatusesForSite(site).size());
        assertEquals(Double.valueOf(9), dao.getAttendanceGrade("student", site).getGrade());
        assertEquals(1, dao.getAttendanceGrades(site).size());
        assertEquals(2, dao.getAttendanceUserStats("student", site).getPresent());
        assertEquals(1, dao.getAttendanceUserStatsForSite(site).size());
        assertEquals(1, dao.getGradingRulesForSite(site).size());
        Instant sync = Instant.now();
        assertTrue(dao.getAttendanceSiteBatch(sync, 0L).contains(site.getId()));
        assertTrue(dao.markAttendanceSiteForSync(List.of(site.getId()), sync));
        entityManager.clear();
        assertTrue(dao.getAttendanceSitesInSync().contains(site.getId()));
        assertFalse(dao.getAttendanceSiteBatch(sync, 0L).contains(site.getId()));
        assertTrue(dao.deleteGradingRule(dao.getGradingRulesForSite(site).get(0)));
    }

    @Test
    public void updateAttendanceEventRejectsRecordsWithoutAuditFields() {
        AttendanceEvent event = event(null);
        event.getRecords().add(new AttendanceRecord(event, "student", Status.PRESENT));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.updateAttendanceEvent(event));
        assertEquals("AttendanceRecord audit fields must be set before persistence", exception.getMessage());
    }

    private AttendanceEvent event(AttendanceSite site) {
        AttendanceEvent event = new AttendanceEvent();
        event.setName("Meeting");
        event.setAttendanceSite(site);
        event.setStartDateTime(Instant.parse("2026-10-22T14:00:00Z"));
        event.setLastModifiedBy("instructor");
        event.setLastModifiedDate(Instant.now());
        return event;
    }

    private AttendanceRecord record(AttendanceEvent event, String user) {
        AttendanceRecord record = new AttendanceRecord(event, user, Status.PRESENT);
        record.setLastModifiedBy("instructor");
        record.setLastModifiedDate(Instant.now());
        return record;
    }
}
