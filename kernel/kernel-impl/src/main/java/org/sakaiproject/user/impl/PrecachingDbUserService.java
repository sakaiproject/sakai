/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009, 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * This will refresh the user cache based on configuration options,
 * it handles the refresh by force clearing entries and then simply looking up all
 * users who are actively enrolled
 * 
 * this code is based on UserCacheRefresher.java by Aaron Zeckoski.
 *
 * Aaron's code extended JdbcDaoSupport.java and obtained the cache from spring component definitions.
 * That didn't work OOTB.
 * I simplied the spring definitions by extending DbUserService.java instead, 
 * and so getting access to the needed cache in the singleton object instance.
 * Java single inheritance requires a separate class DirectDbAccess.java to extend JdbcDaoSupport.java.
 * 
 * I also added a second timer and changed the scheduling so that the 2 timers provide both
 * an immediate boot-time pre-caching of users and followup pre-cache runs daily.
 *
 * created in UVa SAK-1382 (wdn5e 2010.09.22)
 *
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu)
 */
@Slf4j
public abstract class PrecachingDbUserService extends DbUserService
{
    boolean logUsersRemoved = false; 
    boolean logUsersNotRemoved = false; 
    boolean logUsersAccessed = false;
    boolean logUsersNotAccessed = true;

    /**
     * Query to retrieve all distinct userIds from all sites
     */
    protected String siteUserIdsQuery = "SELECT distinct(USER_ID) FROM SAKAI_SITE_USER where PERMISSION = 1 order by USER_ID";

    public Timer bootTimer = new Timer("boot precache users", true);
    public Timer dailyTimer = new Timer("daily precache users", true);

    /**
     * followup scheduled run, repeated daily at a given time
     */
    protected TimerTask scheduledTask;

    /**
     * 2nd task for an immediate initial run
     * this runs immediately (in a few minutes) and doesn't rerun
     */
    protected TimerTask onetimeTask;

    private DirectDbAccess directDbAccess;
    public void setDirectDbAccess (DirectDbAccess directDbAccess) {
        this.directDbAccess = directDbAccess;
    }

    /** UVa SAK-1382: replace Aaron's init() code to schedule one task to run at the same time each day
     * add a 2nd task "onetimeTask" to run immediately."
     */
    public void init() {
        super.init();
        if (log.isDebugEnabled()) {
            log.debug("init(): (grand-super) BaseUserDirectoryService includes this general cache just created in its code, m_callCache==" + m_callCache);
            log.debug("init(): (super) DbUserService includes this eid/id map, wired in user-components.xml, cache==" + cache);
        }

        // LOAD the various sakai config options
        Boolean runOnStartup = serverConfigurationService().getBoolean("precache.users.run.startup", false);
        Boolean runDaily = serverConfigurationService().getBoolean("precache.users.run.daily", false);
        String cacheTimeString = serverConfigurationService().getString("precache.users.refresh.time", "04:00");
        this.siteUserIdsQuery = serverConfigurationService().getString("precache.users.userlist.query", this.siteUserIdsQuery);

        this.logUsersRemoved = serverConfigurationService().getBoolean("precache.users.log.usersRemoved", this.logUsersRemoved);
        this.logUsersNotRemoved = serverConfigurationService().getBoolean("precache.users.log.usersNotRemoved", this.logUsersNotRemoved);
        this.logUsersAccessed = serverConfigurationService().getBoolean("precache.users.log.usersAccessed", this.logUsersAccessed);
        this.logUsersNotAccessed = serverConfigurationService().getBoolean("precache.users.log.usersNotAccessed", this.logUsersNotAccessed);

        Calendar cal = Calendar.getInstance();

        if (runOnStartup) {
            log.info("init() scheduling user precache for startup run");
            // set up onetime task to run after short delay
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, 5);
            Date onetimeTaskStart = cal.getTime();
            onetimeTask = new UserCacheTimerTask();
            bootTimer.schedule(onetimeTask, onetimeTaskStart);
            log.info("User precache refresh onetime task scheduled to run in 5 minutes without repetition.");
        } else {
            log.info("User precache not scheduled for startup run");
        }

        if (runDaily) {
            // set up recurring task
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 1); // start tomorrow
            long recurringTaskPeriod = 24l * 60l * 60l * 1000l;
            log.info("User precache will schedule recurring task every 24 hours, beginning tomorrow");

            try {
                String[] parts = cacheTimeString.trim().split(":");
                Integer hour = new Integer(parts[0]);
                if (hour < 12) {
                    cal.set(Calendar.AM_PM, Calendar.AM);
                } else {
                    cal.set(Calendar.AM_PM, Calendar.PM);
                }
                cal.set(Calendar.HOUR, hour);
                cal.set(Calendar.MINUTE, new Integer(parts[1]) );
                Date recurringTaskStart = cal.getTime();
                scheduledTask = new UserCacheTimerTask();
                dailyTimer.scheduleAtFixedRate(scheduledTask, recurringTaskStart, recurringTaskPeriod);
                log.info("User precache scheduled for daily run at " + cacheTimeString);
            } catch (RuntimeException e) {
                log.error("User precache: Didn't schedule user cache refresh: Bad config?, it should be like: 'precache.users.refresh.time = 04:00' : " + e.getMessage(), e);
            }
        } else {
            log.info("User precache not scheduled for daily run");
        }
    }

    public void doCacheRefresh(String siteUserIdsQuery) {
        if (log.isDebugEnabled()) {
            log.debug("USER PRECACHE BEGINNING");
            log.debug("doCacheRefresh(): using siteUserIdsQuery==" + siteUserIdsQuery);
        }
        //@SuppressWarnings("unchecked")
        List<Map<String, Object>> results = directDbAccess.getJdbcTemplate().queryForList(siteUserIdsQuery);
        List<String> userIds = new ArrayList<String>();
        for (Map<String, Object> row : results) {
            Object userId = row.get("USER_ID");
            if (userId != null) {
                userIds.add(userId.toString());
            }
        }

        if (userIds.isEmpty()) {
            log.warn("doCacheRefresh(): No userIds found as participants while trying to refresh all cache users, cannot refresh");
        } else {
            log.info("doCacheRefresh(): Found " + userIds.size() + " users to refresh, initiating user cache refreshing...");

            int removedCount = 0;
            int notRemovedCount = 0;
            int accessedCount = 0;
            int notAccessedCount = 0;

            List<String> removedUsers = new ArrayList<String>();
            List<String> notRemovedUsers = new ArrayList<String>();
            List<String> accessedUsers = new ArrayList<String>();
            List<String> notAccessedUsers = new ArrayList<String>();

            long totalTime = 0;
            for (String userId : userIds) {

                // clear existing cache entry
                String key = makeUserRef(userId);
                if (log.isDebugEnabled()) {
                    log.debug("doCacheRefresh(): NEW key==[" + key + "] in cache? before removing:  " + m_callCache.containsKey(key));
                }
                if (m_callCache.containsKey(key)) {
                    m_callCache.remove(key);
                    removedCount++;
                    if (logUsersRemoved) {
                        removedUsers.add(userId);
                    }
                } else {
                    notRemovedCount++;
                    if (logUsersNotRemoved) {
                        notRemovedUsers.add(userId);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("doCacheRefresh(): NEW key==[" + key + "] in cache? after removing:  " + m_callCache.containsKey(key));
                }

                // redo the lookup of this user which will reload the cache
                try {
                    long before = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        log.debug("doCacheRefresh(): key==[" + key + "] in cache? before accessing:  " + m_callCache.containsKey(key));
                    }
                    getUser(userId);
                    if (log.isDebugEnabled()) {
                        log.debug("doCacheRefresh(): key==[" + key + "] in cache? after accessing:  " + m_callCache.containsKey(key));
                    }
                    long after = System.currentTimeMillis();
                    totalTime += after - before;
                    accessedCount++;
                    if (logUsersAccessed) {
                        accessedUsers.add(userId);
                    }
                } catch (UserNotDefinedException e) {
                    notAccessedCount++;
                    if (logUsersNotAccessed) {
                        notAccessedUsers.add(userId);
                    }
                }
            }

            // now output the results of cache reset in the logs as configured
            String delimiter = "";
            if (logUsersRemoved) {
                delimiter = "";
                StringBuilder removedUserSB = new StringBuilder();
                for (String userId : removedUsers) {
                    removedUserSB.append(delimiter);
                    removedUserSB.append(userId);
                    delimiter = ":";
                }
                log.info("doCacheRefresh(): " + removedCount + " entries removed from cache");
                log.info("doCacheRefresh(): These users found in cache and so removed: [" + removedUserSB.toString() + "]");
            }
            if (logUsersNotRemoved) {
                delimiter = "";
                StringBuilder notRemovedUserSB = new StringBuilder();
                for (String userId : notRemovedUsers) {
                    notRemovedUserSB.append(delimiter);
                    notRemovedUserSB.append(userId);
                    delimiter = ":";
                }
                log.info("doCacheRefresh(): " + notRemovedCount + " entries not found in cache");
                log.info("doCacheRefresh(): These users not found in cache and so not removed: [" + notRemovedUserSB.toString() + "]");
            }
            if (logUsersAccessed) {
                delimiter = "";
                StringBuilder accessedUserSB = new StringBuilder();
                for (String userId : accessedUsers) {
                    accessedUserSB.append(delimiter);
                    accessedUserSB.append(userId);
                    delimiter = ":";
                }
                log.info("doCacheRefresh(): " + accessedCount + " users accessed and so recached");
                log.info("doCacheRefresh(): These users accessed and so newly cached: [" + accessedUserSB.toString() + "]");
            }
            if (logUsersNotAccessed) {
                delimiter = "";
                StringBuilder notAccessedUserSB = new StringBuilder();
                for (String userId : notAccessedUsers) {
                    notAccessedUserSB.append(delimiter);
                    notAccessedUserSB.append(userId);
                    delimiter = ":";
                }
                log.info("doCacheRefresh(): " + notAccessedCount + " users not found and so not recached");
                log.info("doCacheRefresh(): These users not found and so not newly cached: [" + notAccessedUserSB.toString() + "]");
            }

            if (log.isInfoEnabled()) {
                log.info("doCacheRefresh(): " + totalTime + " milliseconds to cache " + userIds.size() + " users, " 
                        + (totalTime / userIds.size()) + " milliseconds per user, while filling the cache");
            }
        }
        log.info("USER PRECACHE COMPLETED");
    }

    private String makeUserRef(String userId) {
        return "/user/" + userId;
    }

    protected class UserCacheTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                doCacheRefresh(siteUserIdsQuery);            
            } catch (Exception e) {
                log.error("run(): Failure attempting to refresh user cache: " + e.getMessage(), e);
            }
        }
    }

}
