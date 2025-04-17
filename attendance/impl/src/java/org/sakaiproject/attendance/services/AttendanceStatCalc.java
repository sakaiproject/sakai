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
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.*;

import java.util.*;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AttendanceStatCalc {
    private int     sitesProcessed = 0;
    private int     sitesNotMarked = 0;
    private int     sitesInError = 0;
    private int     sitesWithNoUsers = 0;

    public void init() {
        log.debug("AttendanceStatCalc init()");
    }

    public void destroy() {
        log.debug("AttendanceStatCalc destroy()");
    }

    public void execute() {
        log.debug("AttendanceStatCalc execute()");
        Date syncTime = new Date();
        Long lastId = 0L;

        List<Long> ids = dao.getAttendanceSiteBatch(syncTime, lastId);
        if(ids.isEmpty()) {
            String summary = getOverallSummary();
            if("".equals(summary)) {
                log.info("AttendanceStatCalc no sites left to sync");
            } else {
                log.info("AttendanceStatCalc done, but there are errors\n" + summary);
            }
        } else {
            while(!ids.isEmpty()) {
                dao.markAttendanceSiteForSync(ids, syncTime);
                for (Long id : ids) {
                    calculateStats(id);
                    lastId = id > lastId ? id : lastId++; // never-ending loop protection
                }
                log.info("AttendanceStatCalc in progress " + getSummary());
                ids = dao.getAttendanceSiteBatch(syncTime, lastId);
            }
            log.info("AttendanceStatCalc finished " + getSummary());
            log.info(getOverallSummary());
        }

        resetCounters();
    }

    private void calculateStats(Long id) {
        try {
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
                    sitesWithNoUsers++;
                    log.debug("AttendanceSite, id: '" + id +"' has no users or Site, id: '"
                            + attendanceSite.getSiteID() +"' no longer exists.");
                }

                attendanceSite.setIsSyncing(false);
                attendanceLogic.updateAttendanceSite(attendanceSite);

                log.debug("AttendanceSite synced with id: " + id);
                sitesProcessed++;
            } else {
                log.debug("AttendanceSite not marked as in progress" + id);
                sitesNotMarked++;
            }
        } catch (Exception e) {
            sitesInError++;
            log.warn("Error syncing AttendanceSite id: " + id, e);
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
                log.debug("AttendanceRecord user no longer present in course, record id: '" + record.getId()
                        + "' and userID: " + record.getUserID());
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

    private String getSummary() {
        return String.format("%d Attendance Sites synced, %d Attendance Sites with no users, %d Attendance Sites unsuccessfully synced",
                sitesProcessed, sitesWithNoUsers, sitesInError + sitesNotMarked);
    }

    private String getOverallSummary() {
        List<Long> inProgress = dao.getAttendanceSitesInSync();
        if(inProgress.size() > 0) {
            String message = "%d AttendanceSite(s) currently marked in sync. IDs marked in sync: %s";
            return String.format(message, inProgress.size(), inProgress);
        }
        return "";
    }

    private void resetCounters() {
        this.sitesInError = 0;
        this.sitesNotMarked = 0;
        this.sitesProcessed = 0;
        this.sitesWithNoUsers = 0;
    }

    @Setter
    private AttendanceDao dao;

    @Setter
    private SakaiProxy sakaiProxy;

    @Setter
    private AttendanceLogic attendanceLogic;
}
