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

package org.sakaiproject.attendance.services;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceItemStats;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.Status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AttendanceStatCalc implements AttendanceStatCalcService {
    private static class RunSummary {
        private int sitesProcessed;
        private int sitesNotMarked;
        private int sitesInError;
        private int sitesWithNoUsers;

        @Override
        public String toString() {
            return String.format("%d Attendance Sites synced, %d Attendance Sites with no users, %d Attendance Sites unsuccessfully synced",
                    sitesProcessed, sitesWithNoUsers, sitesInError + sitesNotMarked);
        }
    }

    public void init() {
        log.debug("AttendanceStatCalc init()");
    }

    public void destroy() {
        log.debug("AttendanceStatCalc destroy()");
    }

    public void execute() {
        RunSummary summary = new RunSummary();
        log.debug("Calculating attendance statistics");
        Instant syncTime = Instant.now();
        Long lastId = 0L;

        List<Long> ids = dao.getAttendanceSiteBatch(syncTime, lastId);
        if(ids.isEmpty()) {
            String overallSummary = getOverallSummary();
            if(overallSummary.isEmpty()) {
                log.info("AttendanceStatCalc no sites left to sync");
            } else {
                log.info("Attendance statistics completed with errors: {}", overallSummary);
            }
        } else {
            while(!ids.isEmpty()) {
                // capture current batch because 'ids' will be reassigned later
                final List<Long> batchIds = new ArrayList<>(ids);
                // mark batch in-progress in a single transaction
                txTemplate.executeWithoutResult(status -> dao.markAttendanceSiteForSync(batchIds, syncTime));
                for (Long id : batchIds) {
                    // execute per-site calculation in its own transaction
                    try {
                        txTemplate.executeWithoutResult(status -> calculateStats(id, summary));
                    } catch (RuntimeException e) {
                        summary.sitesInError++;
                        log.warn("Error syncing attendance site {}", id, e);
                    }
                    lastId = Math.max(id, lastId); // never-ending loop protection
                }
                log.info("AttendanceStatCalc in progress {}", summary);
                ids = dao.getAttendanceSiteBatch(syncTime, lastId);
            }
            log.info("AttendanceStatCalc finished {}", summary);
            log.info(getOverallSummary());
        }
    }

    private void calculateStats(Long id, RunSummary summary) {
        AttendanceSite attendanceSite = dao.getAttendanceSite(id);

        if(attendanceSite.getIsSyncing()) {
            List<String> userIds = sakaiProxy.getSiteMembershipIds(attendanceSite.getSiteID());
            if(!userIds.isEmpty()) { // only calculate stats for sites still available
                Map<String, int[]> userStatsList = new HashMap<>(userIds.size());

                userIds.forEach(userId -> {
                    int[] array = {0, 0, 0, 0, 0}; // present, unexcused, excused, late, leftEarly
                    userStatsList.put(userId, array);
                });

                List<AttendanceEvent> events = attendanceLogic.getAttendanceEventsForSite(attendanceSite);
                if (!events.isEmpty()) {
                    events.forEach(attendanceEvent -> calculateEventStats(attendanceEvent, userStatsList));

                    for (String key : userStatsList.keySet()) {
                        AttendanceUserStats userStat = attendanceLogic.getStatsForUser(key, attendanceSite);
                        int[] userRecordStats = userStatsList.get(key);
                        userStat.setPresent(userRecordStats[0]);
                        userStat.setUnexcused(userRecordStats[1]);
                        userStat.setExcused(userRecordStats[2]);
                        userStat.setLate(userRecordStats[3]);
                        userStat.setLeftEarly(userRecordStats[4]);

                        dao.updateAttendanceUserStats(userStat);
                    }
                }
            } else {
                summary.sitesWithNoUsers++;
                log.debug("AttendanceSite, id: '{}' has no users or Site, id: '{}' no longer exists.", id, attendanceSite.getSiteID());
            }

            attendanceSite.setIsSyncing(false);
            attendanceLogic.updateAttendanceSite(attendanceSite);

            log.debug("AttendanceSite synced with id: {}", id);
            summary.sitesProcessed++;
        } else {
            log.debug("AttendanceSite not marked as in progress{}", id);
            summary.sitesNotMarked++;
        }
    }

    private void calculateEventStats(AttendanceEvent event, Map<String, int[]> userStats) {
        Set<AttendanceRecord> records = event.getRecords();
        AttendanceItemStats itemStats = attendanceLogic.getStatsForEvent(event);

        int present = 0, unexcused = 0, excused = 0, late = 0, leftEarly = 0;
        boolean recordsPresent = false;
        for(AttendanceRecord record : records) {
            recordsPresent = true;
            Status recordStatus = record.getStatus();
            int[] array = userStats.get(record.getUserID());
            if(array != null) {
                if (recordStatus == Status.PRESENT) {
                    present++;
                    array[0] = array[0] + 1;
                } else if (recordStatus == Status.UNEXCUSED_ABSENCE) {
                    unexcused++;
                    array[1] = array[1] + 1;
                } else if (recordStatus == Status.EXCUSED_ABSENCE) {
                    excused++;
                    array[2] = array[2] + 1;
                } else if (recordStatus == Status.LATE) {
                    late++;
                    array[3] = array[3] + 1;
                } else if (recordStatus == Status.LEFT_EARLY) {
                    leftEarly++;
                    array[4] = array[4] + 1;
                }

                userStats.put(record.getUserID(), array);
            } else {
                log.debug("AttendanceRecord user no longer present in course, record id: '{}' and userID: {}", record.getId(), record.getUserID());
            }
        }

        if(recordsPresent) {
            itemStats.setPresent(present);
            itemStats.setUnexcused(unexcused);
            itemStats.setExcused(excused);
            itemStats.setLate(late);
            itemStats.setLeftEarly(leftEarly);

            dao.updateAttendanceItemStats(itemStats);
        }
    }

    private String getOverallSummary() {
        List<Long> inProgress = dao.getAttendanceSitesInSync();
        if(!inProgress.isEmpty()) {
            String message = "%d AttendanceSite(s) currently marked in sync. IDs marked in sync: %s";
            return String.format(message, inProgress.size(), inProgress);
        }
        return "";
    }

    @Setter
    private AttendanceDao dao;

    @Setter
    private SakaiProxy sakaiProxy;

    @Setter
    private AttendanceLogic attendanceLogic;

    private TransactionTemplate txTemplate;

    // Injected via Spring to build a TransactionTemplate without proxies
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.txTemplate = new TransactionTemplate(transactionManager);
    }
}
