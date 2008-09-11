package org.sakaiproject.sitestats.api;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ServerWideReportManager
{

    // ################################################################
    // Server-wide activity related methods
    // ################################################################
    
    
    /**
     * Get site login activity statistics grouped by month.
     * 
     * @return a list of login statistics. 
     * 		format: String yyyy-mm, Total Logins, Total Unique Logins
     */
    public List<StatsRecord> getMonthlyLogin ();
    
    /**
     * Get site login activity statistics grouped by week.
     * 
     * @return a list of login statistics where date is the Monday's of the
     *         week. format: Date, Total Logins, Total Unique Logins
     */
    public List<StatsRecord> getWeeklyLogin ();

    /**
     * Get site login activity statistics grouped by day.
     * 
     * @return a list of login statistics. format: Date, Total Logins, Total
     *         Unique Logins
     */
    public List<StatsRecord> getDailyLogin ();

    /**
     * Get site site created or deleted per time period.
     * 
     * @param period	string: daily, weekly, monthly
     * @return a list of login statistics. format: Date, number of site created, 
     *         number of site deleted
     */
    public List<StatsRecord> getSiteCreatedDeletedStats (String period);

    /**
     * Get number of new user login.
     * 
     * @param period	string: daily, weekly, monthly
     * @return a list of login statistics. format: Date, number of new user. 
     */
    public List<StatsRecord> getNewUserStats (String period);

    /**
     * Get top 20 activities in the last 7/30/365 daily average.
     * 
     * @return a list of activities. format: event, last 7, last 30, last 365
     *         average sorted by last 7
     */
    public List<StatsRecord> getTop20Activities ();

    /**
     * Get regular users by week
     * 
     * @return format: Date, number of users login 5+ in the week, 4, 3, 2, 1
     */
    public List<StatsRecord> getWeeklyRegularUsers ();

    /**
     * Get session start in the last 30 days
     * 
     * @return format: hour, number of logins
     */
    public List<StatsRecord> getHourlyUsagePattern ();

    
    /**
     * Get tool count
     * 
     * @return format: tool id, tool count
     */
    public List<StatsRecord> getToolCount ();    
    
    public BufferedImage generateReportChart(String reportType, int width, int height);
	
}
