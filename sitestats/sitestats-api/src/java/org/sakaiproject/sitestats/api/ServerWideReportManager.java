/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;

import java.util.List;

public interface ServerWideReportManager
{

    // ################################################################
    // Server-wide activity related methods
    // ################################################################
    
    
    /**
     * Get total login activity statistics grouped by month.
     * 
     * @return a list of login statistics. 
     * 		format: String yyyy-mm, Total Logins,
     */
    public List<ServerWideStatsRecord> getMonthlyTotalLogins();
    
    /**
     * Get unique login activity statistics grouped by month.
     * 
     * @return a list of login statistics. 
     * 		format: String yyyy-mm, Total Unique Logins
     */
    public List<ServerWideStatsRecord> getMonthlyUniqueLogins();
    
    /**
     * Get total login activity statistics grouped by week.
     * 
     * @return a list of login statistics where date is the Monday's of the
     *         week. format: Date, Total Logins, Total Unique Logins
     */
    public List<ServerWideStatsRecord> getWeeklyTotalLogins();
    
    /**
     * Get unique login activity statistics grouped by week.
     * 
     * @return a list of login statistics where date is the Monday's of the
     *         week. format: Date, Total Logins
     */
    public List<ServerWideStatsRecord> getWeeklyUniqueLogins();

    /**
     * Get total login activity statistics grouped by day.
     * 
     * @return a list of login statistics. format: Date, Total Logins
     */
    public List<ServerWideStatsRecord> getDailyTotalLogins();
    
    /**
     * Get unique login activity statistics grouped by day.
     * 
     * @return a list of login statistics. format: Date, Total Unique Logins
     */
    public List<ServerWideStatsRecord> getDailyUniqueLogins();

    /**
     * Get site site created or deleted per time period.
     * 
     * @param period	string: daily, weekly, monthly
     * @return a list of login statistics. format: Date, number of site created, 
     *         number of site deleted
     */
    public List<ServerWideStatsRecord> getSiteCreatedDeletedStats (String period);

    /**
     * Get number of new user login.
     * 
     * @param period	string: daily, weekly, monthly
     * @return a list of login statistics. format: Date, number of new user. 
     */
    public List<ServerWideStatsRecord> getNewUserStats (String period);

    /**
     * Get top 20 activities in the last 7/30/365 daily average.
     * 
     * @return a list of activities. format: event, last 7, last 30, last 365
     *         average sorted by last 7
     */
    public List<ServerWideStatsRecord> getTop20Activities ();

    /**
     * Get regular users by week
     * 
     * @return format: Date, number of users login 5+ in the week, 4, 3, 2, 1
     */
    public List<ServerWideStatsRecord> getWeeklyRegularUsers ();

    /**
     * Get session start in the last 30 days
     * 
     * @return format: hour, number of logins
     */
    public List<ServerWideStatsRecord> getHourlyUsagePattern ();

    
    /**
     * Get tool count
     * 
     * @return format: tool id, tool count
     */
    public List<ServerWideStatsRecord> getToolCount ();    
    
    public byte[] generateReportChart(String reportType, int width, int height);
	
}
