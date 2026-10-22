/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.dao.impl;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.Setter;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.GradingRule;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA persistence for Attendance. Exceptions propagate to the transaction boundary
 * so failed writes roll back instead of appearing to succeed with partial data.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Transactional(transactionManager = "org.sakaiproject.springframework.orm.jpa.GlobalJpaTransactionManager")
public class AttendanceDaoImpl implements AttendanceDao {

    @Setter
    private EntityManager entityManager;

    public AttendanceSite getAttendanceSite(String siteId) {
        return first(entityManager.createQuery("select s from AttendanceSite s where s.siteID = :siteId", AttendanceSite.class)
                .setParameter("siteId", siteId));
    }

    public AttendanceSite getAttendanceSite(Long id) {
        return entityManager.find(AttendanceSite.class, id);
    }

    public boolean addAttendanceSite(AttendanceSite site) {
        entityManager.persist(site);
        return true;
    }

    public boolean updateAttendanceSite(AttendanceSite site) {
        entityManager.merge(site);
        return true;
    }

    public AttendanceEvent getAttendanceEvent(long id) {
        return entityManager.find(AttendanceEvent.class, id);
    }

    public List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite site) {
        return forSite(AttendanceEvent.class, site).getResultList();
    }

    public Serializable addAttendanceEventNow(AttendanceEvent event) {
        requireAuditFields(event);
        entityManager.persist(event);
        return event.getId();
    }

    public boolean updateAttendanceEvent(AttendanceEvent event) {
        requireAuditFields(event);
        if (event.getId() == null) {
            entityManager.persist(event);
        } else {
            entityManager.merge(event);
        }
        return true;
    }

    public boolean deleteAttendanceEvent(AttendanceEvent event) {
        if (event.getStats() != null && event.getStats().getId() == null) {
            event.setStats(null);
        }
        entityManager.remove(entityManager.merge(event));
        return true;
    }

    public AttendanceRecord getStatusRecord(long id) {
        return entityManager.find(AttendanceRecord.class, id);
    }

    public boolean updateAttendanceRecord(AttendanceRecord record) {
        requireAuditFields("AttendanceRecord", record.getLastModifiedBy(), record.getLastModifiedDate());
        if (record.getId() == null && record.getAttendanceEvent() != null && record.getUserID() != null) {
            AttendanceRecord existing = first(entityManager.createQuery(
                    "select r from AttendanceRecord r where r.attendanceEvent = :event and r.userID = :userId", AttendanceRecord.class)
                    .setParameter("event", record.getAttendanceEvent()).setParameter("userId", record.getUserID()));
            if (existing != null) {
                record.setId(existing.getId());
            }
        }
        if (record.getId() == null) {
            entityManager.persist(record);
        } else {
            entityManager.merge(record);
        }
        return true;
    }

    public void updateAttendanceStatuses(List<AttendanceStatus> statuses) {
        for (AttendanceStatus status : statuses) {
            if (status.getId() == null) {
                entityManager.persist(status);
            } else {
                entityManager.merge(status);
            }
        }
    }

    public List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite site) {
        return entityManager.createQuery("select s from AttendanceStatus s where s.attendanceSite = :site and s.isActive = true", AttendanceStatus.class)
                .setParameter("site", site).getResultList();
    }

    public List<AttendanceStatus> getAllStatusesForSite(AttendanceSite site) {
        return forSite(AttendanceStatus.class, site).getResultList();
    }

    public AttendanceStatus getAttendanceStatusById(Long id) {
        return entityManager.find(AttendanceStatus.class, id);
    }

    public AttendanceGrade getAttendanceGrade(Long id) {
        return entityManager.find(AttendanceGrade.class, id);
    }

    public AttendanceGrade getAttendanceGrade(String userId, AttendanceSite site) {
        return first(entityManager.createQuery("select g from AttendanceGrade g where g.attendanceSite = :site and g.userID = :userId", AttendanceGrade.class)
                .setParameter("site", site).setParameter("userId", userId));
    }

    public List<AttendanceGrade> getAttendanceGrades(AttendanceSite site) {
        return forSite(AttendanceGrade.class, site).getResultList();
    }

    public boolean updateAttendanceGrade(AttendanceGrade grade) {
        requireAuditFields("AttendanceGrade", grade.getLastModifiedBy(), grade.getLastModifiedDate());
        if (grade.getId() == null) {
            entityManager.persist(grade);
        } else {
            entityManager.merge(grade);
        }
        return true;
    }

    public AttendanceUserStats getAttendanceUserStats(String userId, AttendanceSite site) {
        return first(entityManager.createQuery("select s from AttendanceUserStats s where s.attendanceSite = :site and s.userID = :userId", AttendanceUserStats.class)
                .setParameter("site", site).setParameter("userId", userId));
    }

    public List<AttendanceUserStats> getAttendanceUserStatsForSite(AttendanceSite site) {
        return forSite(AttendanceUserStats.class, site).getResultList();
    }

    public boolean updateAttendanceUserStats(AttendanceUserStats stats) {
        if (stats.getId() == null) {
            entityManager.persist(stats);
        } else {
            entityManager.merge(stats);
        }
        return true;
    }

    public boolean addGradingRule(GradingRule rule) {
        requireAuditFields("GradingRule", rule.getLastModifiedBy(), rule.getLastModifiedDate());
        entityManager.persist(rule);
        return true;
    }

    public boolean deleteGradingRule(GradingRule rule) {
        entityManager.remove(entityManager.merge(rule));
        return true;
    }

    public AttendanceItemStats getAttendanceItemStats(AttendanceEvent event) {
        return entityManager.find(AttendanceItemStats.class, event.getId());
    }

    public boolean updateAttendanceItemStats(AttendanceItemStats stats) {
        AttendanceEvent event = entityManager.getReference(AttendanceEvent.class, stats.getAttendanceEvent().getId());
        AttendanceItemStats managed = entityManager.find(AttendanceItemStats.class, event.getId());
        if (managed == null) {
            stats.setAttendanceEvent(event);
            event.setStats(stats);
            entityManager.persist(stats);
        } else if (managed != stats) {
            managed.setPresent(stats.getPresent());
            managed.setUnexcused(stats.getUnexcused());
            managed.setExcused(stats.getExcused());
            managed.setLate(stats.getLate());
            managed.setLeftEarly(stats.getLeftEarly());
        }
        return true;
    }

    public List<GradingRule> getGradingRulesForSite(AttendanceSite site) {
        return forSite(GradingRule.class, site).getResultList();
    }

    public List<Long> getAttendanceSiteBatch(Instant syncTime, Long lastId) {
        return entityManager.createQuery("select s.id from AttendanceSite s where (s.syncTime is null or s.syncTime < :syncTime) and s.id > :lastId order by s.id", Long.class)
                .setParameter("syncTime", syncTime).setParameter("lastId", lastId).setMaxResults(5).getResultList();
    }

    public List<Long> getAttendanceSitesInSync() {
        return entityManager.createQuery("select s.id from AttendanceSite s where s.isSyncing = true", Long.class).getResultList();
    }

    public boolean markAttendanceSiteForSync(List<Long> ids, Instant syncTime) {
        if (ids.isEmpty()) {
            return true;
        }
        return entityManager.createQuery("update AttendanceSite s set s.isSyncing = true, s.syncTime = :syncTime where s.id in :ids")
                .setParameter("syncTime", syncTime).setParameter("ids", ids).executeUpdate() == ids.size();
    }

    private <T> TypedQuery<T> forSite(Class<T> type, AttendanceSite site) {
        return entityManager.createQuery("select e from " + type.getSimpleName() + " e where e.attendanceSite = :site", type)
                .setParameter("site", site);
    }

    private <T> T first(TypedQuery<T> query) {
        // getSingleResult also detects corrupted duplicate rows; absence is expected.
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void requireAuditFields(AttendanceEvent event) {
        requireAuditFields("AttendanceEvent", event.getLastModifiedBy(), event.getLastModifiedDate());
        for (AttendanceRecord record : event.getRecords()) {
            requireAuditFields("AttendanceRecord", record.getLastModifiedBy(), record.getLastModifiedDate());
        }
    }

    private void requireAuditFields(String entityName, String modifiedBy, Instant modifiedDate) {
        if (modifiedBy == null || modifiedDate == null) {
            throw new IllegalArgumentException(entityName + " audit fields must be set before persistence");
        }
    }
}
