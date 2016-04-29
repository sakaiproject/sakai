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
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.sakaiproject.sitestats.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.SortOrder;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.ServerWideStatsRecord;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * Server Wide Report Manager handles running the database queries for each of the server wide reports.
 * 
 * This currently provides limited support for the SST_ tables to be in a different database to the main Sakai database
 * so long as the credentials and URL are the same (except for the db name). It will do a cross database join onto that db.
 * 
 * Configure via sakai.properties:
 * sitestats.externalDb.name=DB_NAME
 * 
 * In addition to the normal settings for setting up external databases for the SST_ tables
 * 
 */
@Slf4j
public class ServerWideReportManagerImpl implements ServerWideReportManager
{
	
	/** Message bundle */
	private static ResourceLoader msgs = new ResourceLoader("Messages");
		
	@Setter
	private SqlService sqlService;
	
	@Setter
	private StatsManager statsManager;
	
	@Setter
	private UsageSessionService usageSessionService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;

	private String dbVendor;
	private String externalDbName;

	public void init (){
		//setup the vendor
		dbVendor = StringUtils.lowerCase(serverConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService", null));
		log.info("ServerWideReportManagerImpl SQL queries configured to use: " + dbVendor);
		
		//setup the external db name for our cross db queries
		externalDbName = serverConfigurationService.getString("sitestats.externalDb.name", null);
		if(StringUtils.isNotBlank(externalDbName)){
			log.info("ServerWideReportManagerImpl will query for Sitestats data in the external database: " + externalDbName);
		} else {
			log.info("ServerWideReportManagerImpl will query for Sitestats data in the main Sakai database");
		}
		
	}

	public void destroy (){}

	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getMonthlyTotalLogins()
	 */
	public List<ServerWideStatsRecord> getMonthlyTotalLogins() {
		
		String mysql = "select STR_TO_DATE(date_format(ACTIVITY_DATE, '%Y-%m-01'),'%Y-%m-%d') as period," +
				" sum(ACTIVITY_COUNT) as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login'" +
				" group by 1";
		
		String oracle = ("select TO_DATE(TO_CHAR(ACTIVITY_DATE, 'YYYY-MM-\"01\"'), 'YYYY-MM-DD') as period," +
				" sum(ACTIVITY_COUNT) as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login'" +
				" group by TO_DATE(TO_CHAR(ACTIVITY_DATE, 'YYYY-MM-\"01\"'), 'YYYY-MM-DD')");

		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getMonthlyTotalLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});
		
		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getMonthlyUniqueLogins()
	 */
	public List<ServerWideStatsRecord> getMonthlyUniqueLogins() {
		
		String mysql = "select STR_TO_DATE(date_format(LOGIN_DATE, '%Y-%m-01'),'%Y-%m-%d') as period," +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" group by 1";
		
		String oracle = "select TO_DATE(TO_CHAR(LOGIN_DATE, 'YYYY-MM-\"01\"'),'YYYY-MM-DD') as period," +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" group by TO_DATE(TO_CHAR(LOGIN_DATE, 'YYYY-MM-\"01\"'),'YYYY-MM-DD')";
			 	
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getMonthlyUniqueLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});
		
		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getWeeklyTotalLogins()
	 */
	public List<ServerWideStatsRecord> getWeeklyTotalLogins() {
		
		
		String mysql = "select STR_TO_DATE(concat(date_format(ACTIVITY_DATE, '%x-%v'), ' Monday'),'%x-%v %W') as week_start," +
				" sum(ACTIVITY_COUNT) as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login'" +
				" group by 1";
		
		String oracle = "select next_day(ACTIVITY_DATE - 7, 2) as week_start," +
				" sum(ACTIVITY_COUNT) as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login'" +
				" group by next_day(ACTIVITY_DATE - 7, 2)";
		
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getWeeklyTotalLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getWeeklyUniqueLogins()
	 */
	public List<ServerWideStatsRecord> getWeeklyUniqueLogins() {
		
		String mysql = "select STR_TO_DATE(concat(date_format(LOGIN_DATE, '%x-%v'), ' Monday'),'%x-%v %W') as week_start," +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" group by 1";
		
		String oracle = "select next_day(LOGIN_DATE - 7, 2) as week_start," +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" group by next_day(LOGIN_DATE - 7, 2)";
		
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getWeeklyUniqueLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getDailyTotalLogins()
	 */
	public List<ServerWideStatsRecord> getDailyTotalLogins() {
		
		String mysql = "select date(ACTIVITY_DATE) as session_date, " +
				" ACTIVITY_COUNT as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login' " +
				" and ACTIVITY_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY)" +
				" group by 1";
		
		String oracle = "select trunc(ACTIVITY_DATE, 'DDD') as session_date," +
				" sum(ACTIVITY_COUNT) as user_logins" +
				" from " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS" +
				" where EVENT_ID='user.login' " +
				" and ACTIVITY_DATE > (SYSDATE - 90)" +
				" group by trunc(ACTIVITY_DATE, 'DDD')";
		
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getDailyTotalLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getDailyUniqueLogins()
	 */
	public List<ServerWideStatsRecord> getDailyUniqueLogins() {
		
		String mysql = "select date(LOGIN_DATE) as session_date, " +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" where LOGIN_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY)" +
				" group by 1";
		
		String oracle = "select trunc(LOGIN_DATE, 'DDD') as session_date, " +
				" count(distinct user_id) as unique_users" +
				" from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				" where LOGIN_DATE > (SYSDATE - 90)" +
				" group by trunc(LOGIN_DATE, 'DDD')";
		
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getDailyUniqueLogins() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	public List<ServerWideStatsRecord> getSiteCreatedDeletedStats(String period) {
		String mysqlPeriod = "";
		if (period.equals ("daily")) {
			mysqlPeriod = "date(ACTIVITY_DATE) as event_period";
		} else if (period.equals ("weekly")) {
			mysqlPeriod = "STR_TO_DATE(date_format(ACTIVITY_DATE, '%x-%v Monday'),'%x-%v %W') as event_period";
		} else {
			// monthly
			mysqlPeriod = "STR_TO_DATE(date_format(ACTIVITY_DATE, '%Y-%m-01'),'%Y-%m-%d') as event_period";
		}
		String mysql = "select " + mysqlPeriod + ", "
				+ "sum(if(EVENT_ID = 'site.add',activity_count,0)) as site_created, "
				+ "sum(if(EVENT_ID = 'site.del',activity_count,0)) as site_deleted "
				+ "FROM " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS ";
		
		if (period.equals ("daily")) {
			mysql = mysql + "where ACTIVITY_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY) ";
		}
		
		mysql = mysql + "group by 1";
		
		
		String oraclePeriod = "";
		if (period.equals ("daily")) {
			oraclePeriod = "trunc(ACTIVITY_DATE, 'DDD')";
		} else if (period.equals ("weekly")) {
			oraclePeriod = "next_day(ACTIVITY_DATE - 7, 2)";
		} else {
			// monthly
			oraclePeriod = "TO_DATE(TO_CHAR(ACTIVITY_DATE, 'YYYY-MM-\"01\"'),'YYYY-MM-DD')";
		}
	
		String oracle = "select " + oraclePeriod + " as event_period, "
				+ "sum(decode(EVENT_ID, 'site.add',activity_count,0)) as site_created, "
				+ "sum(decode(EVENT_ID, 'site.del',activity_count,0)) as site_deleted "
				+ "FROM " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS ";
		
		if (period.equals ("daily")) {
			oracle = oracle + "where ACTIVITY_DATE > (SYSDATE - 90) ";
		}	
		oracle = oracle + "group by " + oraclePeriod;
		

		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					log.error("getSiteCreatedDeletedStats() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		if(result.size() > 0) {
			result.remove (result.size() - 1);
		}
		return result;
	}

	public List<ServerWideStatsRecord> getNewUserStats(String period)
	{
		String mysqlPeriod = "";
		if (period.equals ("daily")) {
			mysqlPeriod = "date(ACTIVITY_DATE) as event_period";
		} else if (period.equals ("weekly")) {
			mysqlPeriod = "STR_TO_DATE(date_format(ACTIVITY_DATE, '%x-%v Monday'),'%x-%v %W') as event_period";
		} else {
			// monthly
			mysqlPeriod = "STR_TO_DATE(date_format(ACTIVITY_DATE, '%Y-%m-01'),'%Y-%m-%d') as event_period";
		}
		String mysql = "select " + mysqlPeriod + ", "
				+ " ACTIVITY_COUNT as new_user"
				+ " FROM " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS"
				+ " where EVENT_ID='user.add'";
				

		if (period.equals ("daily")) {
			mysql = mysql + " and ACTIVITY_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY) ";
		}
		mysql = mysql + " group by 1";
		

		String oraclePeriod = "";
		if (period.equals ("daily")) {
			oraclePeriod = "trunc(ACTIVITY_DATE, 'DDD')";
		} else if (period.equals ("weekly")) {
			oraclePeriod = "next_day(ACTIVITY_DATE - 7, 2)";
		} else {
			// monthly
			oraclePeriod = "TO_DATE(TO_CHAR(ACTIVITY_DATE, 'YYYY-MM-\"01\"'),'YYYY-MM-DD')";
		}
		String oracle = "select " + oraclePeriod + " as event_period, "
				+ " sum(ACTIVITY_COUNT) as new_user"
				+ " FROM " + getExternalDbNameAsPrefix() + "SST_SERVERSTATS"
				+ " where EVENT_ID='user.add'";
	 	 
		if (period.equals ("daily")) {
			oracle = oracle + " AND ACTIVITY_DATE > (SYSDATE - 90) ";
		}
		oracle = oracle + " group by " + oraclePeriod;

		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					log.error("getNewUserStats() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		if(result.size () > 0){
			result.remove (result.size () - 1);
		}

		return result;
	}

	public List<ServerWideStatsRecord> getTop20Activities()
	{
		String mysql = "SELECT event_id, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 7 DAY),1,0))/7 as last7, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 30 DAY),1,0))/30 as last30, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY),1,0))/365 as last365 "
				+ "FROM " + getExternalDbNameAsPrefix() + "SST_EVENTS "
				+ "where event_id not in ('content.read', 'user.login', 'user.logout', 'pres.begin', 'pres.end', "
				+ "'realm.upd', 'realm.add', 'realm.del', 'realm.upd.own', 'site.add', 'site.del', 'user.add', 'user.del') "
				+ "and event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY) "
				+ "group by 1 " + "order by 2 desc, 3 desc, 4 desc "
				+ "LIMIT 20";
		
		String oracle = "select * from" +
				" (SELECT event_id," +
				" sum(decode(sign(event_date - (SYSDATE - 7)), 1, 1, 0)) / 7 as last7," +
				" sum(decode(sign(event_date - (SYSDATE - 30)), 1, 1, 0)) / 30 as last30," +
				" sum(decode(sign(event_date - (SYSDATE - 365)), 1, 1, 0)) / 365 as last365" +
				" FROM " + getExternalDbNameAsPrefix() + "SST_EVENTS" +
				" where event_id not in ('content.read', 'user.login', 'user.logout', 'pres.begin', 'pres.end', 'realm.upd', 'realm.add', 'realm.del', 'realm.upd.own', 'site.add', 'site.del', 'user.add', 'user.del')" +
				" and event_date > (SYSDATE - 365)" +
				" group by event_id" +
				" order by last7 desc, last30 desc, last365 desc)" +
				" where rownum <= 20";

		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getString (1));
					info.add (result.getDouble (2));
					info.add (result.getDouble (3));
					info.add (result.getDouble (4));
				}
				catch (SQLException e) {
					log.error("getTop20Activities() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		return result;
	}

	public List<ServerWideStatsRecord> getWeeklyRegularUsers ()
	{
		String mysql = "select s.week_start, sum(if(s.user_logins >= 5,1,0)) as five_plus, "
				+ "sum(if(s.user_logins = 4,1,0)) as four, "
				+ "sum(if(s.user_logins = 3,1,0)) as three, "
				+ "sum(if(s.user_logins = 2,1,0)) as twice, "
				+ "sum(if(s.user_logins = 1,1,0)) as once "
				+ "from (select "
				+ "STR_TO_DATE(concat(date_format(login_date, '%x-%v'), ' Monday'),'%x-%v %W') as week_start, "
				+ "user_id, login_count as user_logins "
				+ "from " + getExternalDbNameAsPrefix() + "SST_USERSTATS group by 1, 2) as s " + "group by 1";
		
		String oracle = "select s.week_start," +
				" sum(decode(sign(s.user_logins - 4), 1, 1, 0)) as five_plus," +
				" sum(decode(s.user_logins, 4, 1, 0)) as four, " +
				" sum(decode(s.user_logins, 3, 1, 0)) as three, " +
				" sum(decode(s.user_logins, 2, 1, 0)) as twice, " +
				" sum(decode(s.user_logins, 1, 1, 0)) as once" +
				" from (select next_day(LOGIN_DATE - 7, 2) as week_start," +
				"       user_id, login_count as user_logins" +
				"       from " + getExternalDbNameAsPrefix() + "SST_USERSTATS" +
				"       group by next_day(LOGIN_DATE - 7, 2), user_id, login_count) s" +
				" group by s.week_start";

		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
					info.add (result.getLong (4));
					info.add (result.getLong (5));
					info.add (result.getLong (6));
				}
				catch (SQLException e) {
					log.error("getWeeklyRegularUsers() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	// this has not been reimplemented in STAT-299 because the data is not tracked at an hourly level
	// in any case, the data is only shown for a 30 day period so you could think about retaining the data for 30 days, perhaps.
	public List<ServerWideStatsRecord> getHourlyUsagePattern ()
	{
		String mysql = "select date(SESSION_START) as session_date, "
				+ "hour(session_start) as hour_start, "
				+ "count(distinct SESSION_USER) as unique_users "
				+ "from SAKAI_SESSION "
				+ "where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
				+ "group by 1, 2";
		
		String oracle = "select trunc(SESSION_START, 'DDD') as session_date," +
				" to_number(to_char(session_start, 'HH24')) as hour_start," +
				" count(distinct SESSION_USER) as unique_users" +
				" from SAKAI_SESSION" +
				" where SESSION_START > (SYSDATE - 30)" +
				" group by trunc(SESSION_START, 'DDD'), to_number(to_char(session_start, 'HH24'))";

		// This query uses only the main Sakai database, so do not specify the connection as it might be external
		List result = sqlService.dbRead (getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getInt (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					log.error("getHourlyUsagePattern() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		return result;
	}

	public List<ServerWideStatsRecord> getToolCount ()
	{
		String mysql = "SELECT registration, count(*) as site_count " +
				"FROM SAKAI_SITE_TOOL " +
				"where site_id not like '~%' and site_id not like '!%' " +
				"group by 1 " +
				"order by 2 desc";
		
		String oracle = "SELECT registration, count(*) as site_count" +
				" FROM SAKAI_SITE_TOOL" +
				" where site_id not like '~%' and site_id not like '!%'" +
				" group by registration" +
				" order by site_count desc";

		// This query uses only the main Sakai database, so do not specify the connection as it might be external
		List result = sqlService.dbRead(getSqlForVendor(mysql, oracle), null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				ServerWideStatsRecord info = new ServerWideStatsRecordImpl ();
				try {
					info.add (result.getString (1));
					info.add (result.getInt (2));
				}
				catch (SQLException e) {
					log.error("getToolCount() exception: " + e.getClass() + ": " + e.getMessage());
					return null;
				}
				return info;
			}
		});

		return result;
	}
	
	public byte[] generateReportChart(String reportType, int width, int height)
	{

		if(reportType.equals(StatsManager.MONTHLY_LOGIN_REPORT)){
			return createMonthlyLoginChart(width, height);

		}else if(reportType.equals(StatsManager.WEEKLY_LOGIN_REPORT)){
			return createWeeklyLoginChart(width, height);

		}else if(reportType.equals(StatsManager.DAILY_LOGIN_REPORT)){
			return createDailyLoginChart(width, height);
			
		}else if(reportType.equals(StatsManager.REGULAR_USERS_REPORT)){
			CategoryDataset dataset = getRegularUsersDataSet();
			if(dataset != null){
				return generateStackedAreaChart(dataset, width, height);
			}else{
				return generateNoDataChart(width, height);
			}
			
		}else if(reportType.equals(StatsManager.HOURLY_USAGE_REPORT)){
			BoxAndWhiskerCategoryDataset dataset = getHourlyUsageDataSet();
			if(dataset != null){
				return generateBoxAndWhiskerChart(dataset, width, height);
			}else{
				return generateNoDataChart(width, height);
			}
			
		}else if(reportType.equals(StatsManager.TOP_ACTIVITIES_REPORT)){
			CategoryDataset dataset = getTopActivitiesDataSet();
			if(dataset != null){
				return generateLayeredBarChart(dataset, width, height);
			}else{
				return generateNoDataChart(width, height);
			}
			
		}else if(reportType.equals(StatsManager.TOOL_REPORT)){
			return createToolAnalysisChart(width, height);
			
		}else{
			return generateNoDataChart(width, height);
		}
	}
	


	
	private IntervalXYDataset getMonthlyLoginsDataSet() {
		
		List<ServerWideStatsRecord> totalLogins = getMonthlyTotalLogins();
		List<ServerWideStatsRecord> uniqueLogins = getMonthlyUniqueLogins();
		if (totalLogins == null || uniqueLogins == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"), Month.class);
		TimeSeries s2 = new TimeSeries (msgs.getString ("legend_unique_logins"), Month.class);
		for (ServerWideStatsRecord login : totalLogins) {
			Month month = new Month ((Date) login.get (0));
			s1.add (month, (Long) login.get (1));
		}
		for (ServerWideStatsRecord login : uniqueLogins) {
			Month month = new Month ((Date) login.get (0));
			s2.add (month, (Long) login.get (1));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s1);
		dataset.addSeries (s2);

		return dataset;
	}

	
	/*
	private IntervalXYDataset getMonthlyLoginsDataSet() {
		
		List<ServerWideStatsRecord> loginList = getMonthlyUniqueLogins();
		if (loginList == null) {
			return null;
		}

		TimeSeries s2 = new TimeSeries (msgs.getString ("legend_unique_logins"), Month.class);
		for (ServerWideStatsRecord login : loginList) {
			Month month = new Month ((Date) login.get (0));
			s2.add (month, (Long) login.get (1));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s2);

		return dataset;
	}
	*/
	
	private IntervalXYDataset getWeeklyLoginsDataSet() {

		List<ServerWideStatsRecord> totalLogins = getWeeklyTotalLogins();
		List<ServerWideStatsRecord> uniqueLogins = getWeeklyUniqueLogins();
		if (totalLogins == null || uniqueLogins == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),Week.class);
		TimeSeries s2 = new TimeSeries (msgs.getString ("legend_unique_logins"), Week.class);
		
		for (ServerWideStatsRecord login : totalLogins) {
			Week week = new Week ((Date) login.get (0));
			s1.add (week, (Long) login.get (1));
		}
		
		for (ServerWideStatsRecord login : uniqueLogins) {
			Week week = new Week ((Date) login.get (0));
			s2.add (week, (Long) login.get (1));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries (s1);
		dataset.addSeries (s2);

		return dataset;
	}
	

	private IntervalXYDataset getDailyLoginsDataSet() {
		
		List<ServerWideStatsRecord> totalLogins = getDailyTotalLogins();
		List<ServerWideStatsRecord> uniqueLogins = getDailyUniqueLogins();
		if (totalLogins == null || uniqueLogins == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),Day.class);
		TimeSeries s2 = new TimeSeries (msgs.getString ("legend_unique_logins"), Day.class);
		for (ServerWideStatsRecord login : totalLogins) {
			Day day = new Day ((Date) login.get (0));
			s1.add (day, (Long) login.get (1));
		}
		
		for (ServerWideStatsRecord login : uniqueLogins) {
			Day day = new Day ((Date) login.get (0));
			s2.add (day, (Long) login.get (1));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries (s1);
		dataset.addSeries (s2);

		TimeSeries mavS1 = MovingAverage.createMovingAverage (s1, "7 day login moving average", 7, 7);
		dataset.addSeries (mavS1);

		TimeSeries mavS2 = MovingAverage.createMovingAverage (s2, "7 day unique login moving average", 7, 7);
		dataset.addSeries (mavS2);

		return dataset;
	}
	

	private IntervalXYDataset getMonthlySiteUserDataSet ()
	{
		List<ServerWideStatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("monthly");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), Month.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), Month.class);
			
			for (ServerWideStatsRecord login : siteCreatedDeletedList) {
				Month month = new Month ((Date) login.get (0));
				s1.add (month, (Long) login.get (1));
				s2.add (month, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<ServerWideStatsRecord> newUserList = getNewUserStats ("monthly");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"), Month.class);
			
			for (ServerWideStatsRecord login : newUserList) {
				Month month = new Month ((Date) login.get (0));
				s3.add (month, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}


	private IntervalXYDataset getWeeklySiteUserDataSet ()
	{
		List<ServerWideStatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("weekly");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), Week.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), Week.class);
			
			for (ServerWideStatsRecord login : siteCreatedDeletedList) {
				Week week = new Week ((Date) login.get (0));
				s1.add (week, (Long) login.get (1));
				s2.add (week, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<ServerWideStatsRecord> newUserList = getNewUserStats ("weekly");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"), Week.class);
			
			for (ServerWideStatsRecord login : newUserList) {
				Week week = new Week ((Date) login.get (0));
				s3.add (week, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}

	private IntervalXYDataset getDailySiteUserDataSet ()
	{
		List<ServerWideStatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("daily");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), Day.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), Day.class);
			
			for (ServerWideStatsRecord login : siteCreatedDeletedList) {
				Day day = new Day ((Date) login.get (0));
				s1.add (day, (Long) login.get (1));
				s2.add (day, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<ServerWideStatsRecord> newUserList = getNewUserStats ("daily");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"), Day.class);
			
			for (ServerWideStatsRecord login : newUserList) {
				Day day = new Day ((Date) login.get (0));
				s3.add (day, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}

	private CategoryDataset getRegularUsersDataSet ()
	{
		List<ServerWideStatsRecord> regularUsersList = getWeeklyRegularUsers ();
		if (regularUsersList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();
		DateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");

		for (ServerWideStatsRecord regularUsers : regularUsersList) {
			Date weekStart = ((Date) regularUsers.get (0));
			dataset.addValue ((Long) regularUsers.get (1), "5+", formatter.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (2), "4", formatter.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (3), "3", formatter.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (4), "2", formatter.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (5), "1", formatter.format (weekStart));
		}

		return dataset;
	}

	private BoxAndWhiskerCategoryDataset getHourlyUsageDataSet ()
	{
		// log.info("Generating activityWeekBarDataSet");
		List<ServerWideStatsRecord> hourlyUsagePattern = getHourlyUsagePattern ();
		if (hourlyUsagePattern == null) {
			return null;
		}

		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset ();

		List[] hourList = new ArrayList[24];
		for (int ii = 0; ii < 24; ii++) {
			hourList[ii] = new ArrayList ();
		}

		int totalDays = 0;
		Date prevDate = null;
		for (ServerWideStatsRecord regularUsers : hourlyUsagePattern) {
			Date currDate = (Date) regularUsers.get (0);
			if (!currDate.equals (prevDate)) {
				prevDate = currDate;
				totalDays++;
			}
			hourList[(Integer) regularUsers.get (1)].add ((Long) regularUsers.get (2));
		}

		for (int ii = 0; ii < 24; ii++) {
			// add zero counts, when no data for the day
			for (int jj = hourList[ii].size (); jj < totalDays; jj++) {
				hourList[ii].add (Long.valueOf(0));
			}

			dataset.add (hourList[ii], "Last 30 days", "" + ii);
		}

		return dataset;
	}

	private CategoryDataset getTopActivitiesDataSet ()
	{
		List<ServerWideStatsRecord> topActivitiesList = getTop20Activities ();
		if (topActivitiesList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();

		for (ServerWideStatsRecord regularUsers : topActivitiesList) {
			String event = (String) regularUsers.get (0);
			dataset.addValue ((Double) regularUsers.get (1), "last 7 days", event);
			dataset.addValue ((Double) regularUsers.get (2), "last 30 days", event);
			dataset.addValue ((Double) regularUsers.get (3), "last 365 days", event);
		}

		return dataset;
	}

	
	private CategoryDataset getToolAnalysisDataSet ()
	{
		List<ServerWideStatsRecord> toolCountList = getToolCount ();
		if (toolCountList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();

		for (ServerWideStatsRecord regularUsers : toolCountList) {
			String toolId = (String) regularUsers.get (0);
			dataset.addValue ((Integer) regularUsers.get (1), "", toolId);
		}

		return dataset;
	}	
	
	
	private byte[] createMonthlyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getMonthlyLoginsDataSet();
        IntervalXYDataset dataset3 = getMonthlySiteUserDataSet();
		
		if ((dataset1 == null) || (dataset3 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer1.setSeriesPaint (0, Color.RED);
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.MONTH, 1, new SimpleDateFormat ("yyyy-MM")));
        domainAxis.setTickMarkPosition (DateTickMarkPosition.START);
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);
        
        NumberAxis axis1 = new NumberAxis("Total Logins");
		axis1.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());
		axis1.setLabelPaint(Color.RED);
		axis1.setTickLabelPaint(Color.RED);
		
        XYPlot plot1 = new XYPlot(dataset1, null, axis1, renderer1);
        plot1.setBackgroundPaint(Color.lightGray);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setRangeGridlinePaint(Color.white);
        
        // AXIS 2
        /*
        NumberAxis axis2 = new NumberAxis("Total Unique Users");
		axis2.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());
        axis2.setLabelPaint(Color.BLUE);
        axis2.setTickLabelPaint(Color.BLUE);
        plot1.setRangeAxis(1, axis2);

        plot1.setDataset(1, dataset2);
        plot1.mapDatasetToRangeAxis(1, 1);
        XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
        renderer2.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer2.setSeriesPaint (0, Color.BLUE);
        plot1.setRenderer(1, renderer2);
        */
        
        // add a third dataset and renderer...
        XYItemRenderer renderer3 = new XYLineAndShapeRenderer(true, false);
        renderer3.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer3.setSeriesStroke(1, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer3.setSeriesStroke(2, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer3.setSeriesPaint(0, Color.GREEN);
        renderer3.setSeriesPaint(1, Color.BLACK);
        renderer3.setSeriesPaint(2, Color.CYAN);
        
        axis1 = new NumberAxis("count");
		axis1.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		XYPlot plot2 = new XYPlot(dataset3, null, axis1, renderer3);
        plot2.setBackgroundPaint(Color.lightGray);
        plot2.setDomainGridlinePaint(Color.white);
        plot2.setRangeGridlinePaint(Color.white);
        
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
        cplot.add(plot1, 3);
        cplot.add(plot2, 2);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);

        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(null, 
                JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
        LegendTitle legend = new LegendTitle(cplot);
        chart.addSubtitle(legend);		
		
		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}


	private byte[] createWeeklyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getWeeklyLoginsDataSet ();
        IntervalXYDataset dataset2 = getWeeklySiteUserDataSet ();
		
		if ((dataset1 == null) || (dataset2 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer1.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer1.setSeriesPaint(0, Color.RED);
		renderer1.setSeriesPaint(0, Color.BLUE);
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.DAY, 7, new SimpleDateFormat ("yyyy-MM-dd")));
        domainAxis.setTickMarkPosition (DateTickMarkPosition.START);
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);
        
        NumberAxis rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());
		
        XYPlot plot1 = new XYPlot(dataset1, null, rangeAxis, renderer1);
        plot1.setBackgroundPaint(Color.lightGray);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setRangeGridlinePaint(Color.white);
        
        // add a second dataset and renderer...
        XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
        renderer2.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesPaint(0, Color.GREEN);
        renderer2.setSeriesPaint(1, Color.BLACK);
        renderer2.setSeriesPaint(2, Color.CYAN);
        
        rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		XYPlot plot2 = new XYPlot(dataset2, null, rangeAxis, renderer2);
        plot2.setBackgroundPaint(Color.lightGray);
        plot2.setDomainGridlinePaint(Color.white);
        plot2.setRangeGridlinePaint(Color.white);
        
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
        cplot.add(plot1, 3);
        cplot.add(plot2, 2);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);

        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
        LegendTitle legend = new LegendTitle(cplot);
        chart.addSubtitle(legend);		
		
		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] createDailyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getDailyLoginsDataSet();
        IntervalXYDataset dataset2 = getDailySiteUserDataSet();
		
		if ((dataset1 == null) || (dataset2 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
		renderer1.setSeriesPaint (0, Color.RED);
		renderer1.setSeriesPaint (1, Color.BLUE);
		renderer1.setSeriesPaint (2, Color.RED);
		renderer1.setSeriesPaint (3, Color.BLUE);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer1.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		BasicStroke dashLineStroke = new BasicStroke (2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[] { 4 }, 0);
		renderer1.setSeriesStroke (2, dashLineStroke);
		renderer1.setSeriesStroke (3, dashLineStroke);
		
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.DAY, 7, new SimpleDateFormat ("yyyy-MM-dd")));
        domainAxis.setTickMarkPosition (DateTickMarkPosition.START);
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);
        
        NumberAxis rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());
		
        XYPlot plot1 = new XYPlot(dataset1, null, rangeAxis, renderer1);
        plot1.setBackgroundPaint(Color.lightGray);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setRangeGridlinePaint(Color.white);
        
        // add a second dataset and renderer...
        XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
        renderer2.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesPaint(0, Color.GREEN);
        renderer2.setSeriesPaint(1, Color.BLACK);
        renderer2.setSeriesPaint(2, Color.CYAN);
        
        rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		XYPlot plot2 = new XYPlot(dataset2, null, rangeAxis, renderer2);
        plot2.setBackgroundPaint(Color.lightGray);
        plot2.setDomainGridlinePaint(Color.white);
        plot2.setRangeGridlinePaint(Color.white);
        
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
        cplot.add(plot1, 3);
        cplot.add(plot2, 2);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);

        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
        LegendTitle legend = new LegendTitle(cplot);
        chart.addSubtitle(legend);		
		
		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] generateStackedAreaChart (CategoryDataset dataset, int width, int height)
	{
		JFreeChart chart = ChartFactory.createStackedAreaChart (null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // the plot orientation
				true, // legend
				true, // tooltips
				false // urls
				);

		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		// set transparency
		plot.setForegroundAlpha (0.7f);
		plot.setAxisOffset (new RectangleInsets (5.0, 5.0, 5.0, 5.0));
		plot.setBackgroundPaint (Color.lightGray);
		plot.setDomainGridlinesVisible (true);
		plot.setDomainGridlinePaint (Color.white);
		plot.setRangeGridlinesVisible (true);
		plot.setRangeGridlinePaint (Color.white);

		// set colour of regular users using Karate belt colour: white, green, blue, brown, black/gold
		CategoryItemRenderer renderer = plot.getRenderer ();
		renderer.setSeriesPaint (0, new Color (205, 173, 0)); // gold users
		renderer.setSeriesPaint (1, new Color (139, 69, 19));
		renderer.setSeriesPaint (2, Color.BLUE);
		renderer.setSeriesPaint (3, Color.GREEN);
		renderer.setSeriesPaint (4, Color.WHITE);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis ();
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis ();
		domainAxis.setCategoryLabelPositions (CategoryLabelPositions.DOWN_45);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);

        BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] generateBoxAndWhiskerChart (BoxAndWhiskerCategoryDataset dataset, int width, int height)
	{
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart (null, null,
				null, dataset, false);

		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		plot.setDomainGridlinePaint (Color.white);
		plot.setDomainGridlinesVisible (true);
		plot.setRangeGridlinePaint (Color.white);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis ();
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis ();
		domainAxis.setLowerMargin (0.0);
		domainAxis.setUpperMargin (0.0);

		BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] generateLayeredBarChart (CategoryDataset dataset, int width, int height)
	{
		JFreeChart chart = ChartFactory.createBarChart (null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // the plot orientation
				true, // legend
				true, // tooltips
				false // urls
				);

		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		// disable bar outlines...
		LayeredBarRenderer renderer = new LayeredBarRenderer ();
		renderer.setDrawBarOutline (false);
		renderer.setSeriesBarWidth (0, .6);
		renderer.setSeriesBarWidth (1, .8);
		renderer.setSeriesBarWidth (2, 1.0);
		plot.setRenderer (renderer);

		// for this renderer, we need to draw the first series last...
		plot.setRowRenderingOrder (SortOrder.DESCENDING);

		// set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint (0.0f, 0.0f, Color.blue, 0.0f,
				0.0f, new Color (0, 0, 64));
		GradientPaint gp1 = new GradientPaint (0.0f, 0.0f, Color.green, 0.0f,
				0.0f, new Color (0, 64, 0));
		GradientPaint gp2 = new GradientPaint (0.0f, 0.0f, Color.red, 0.0f,
				0.0f, new Color (64, 0, 0));
		renderer.setSeriesPaint (0, gp0);
		renderer.setSeriesPaint (1, gp1);
		renderer.setSeriesPaint (2, gp2);

		CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis ();
		domainAxis.setCategoryLabelPositions (CategoryLabelPositions.DOWN_45);
		domainAxis.setLowerMargin (0.0);
		domainAxis.setUpperMargin (0.0);

		BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] createToolAnalysisChart (int width, int height)
	{
		CategoryDataset dataset = getToolAnalysisDataSet ();
		
		if (dataset == null) {
			return generateNoDataChart(width, height);
		}
				
		JFreeChart chart = ChartFactory.createBarChart (
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // the plot orientation
				false, // legend
				false, // tooltips
				false // urls
		);
		
		// set background
		chart.setBackgroundPaint (parseColor (statsManager.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		// set transparency
		plot.setForegroundAlpha (0.7f);
		plot.setAxisOffset (new RectangleInsets (5.0, 5.0, 5.0, 5.0));
		plot.setBackgroundPaint (Color.lightGray);
		plot.setDomainGridlinesVisible (false);
		plot.setRangeGridlinesVisible (true);
		plot.setRangeGridlinePaint (Color.white);
		
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);
        domainAxis.setUpperMargin (0);
        domainAxis.setLowerMargin (0);
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setUpperMargin(0.20);
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        CategoryItemLabelGenerator generator 
            = new StandardCategoryItemLabelGenerator("{1}", 
                    NumberFormat.getInstance(new ResourceLoader().getLocale()));
        renderer.setBaseItemLabelGenerator(generator);
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 9));
        renderer.setBaseItemLabelsVisible(true);
        renderer.setItemMargin (0);
        renderer.setSeriesPaint (0, Color.BLUE);
        
        BufferedImage img = chart.createBufferedImage (width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}

	private byte[] generateNoDataChart(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		
		g2d.setBackground(parseColor(statsManager.getChartBackgroundColor()));
		g2d.clearRect(0, 0, width-1, height-1);
		g2d.setColor(parseColor("#cccccc"));
		g2d.drawRect(0, 0, width-1, height-1);
		Font f = new Font("SansSerif", Font.PLAIN, 12);
		g2d.setFont(f);
		FontMetrics fm = g2d.getFontMetrics(f);
		String noData = msgs.getString("no_data");
		int noDataWidth = fm.stringWidth(noData);
		int noDataHeight = fm.getHeight();
		g2d.setColor(parseColor("#555555"));
		g2d.drawString(noData, width/2 - noDataWidth/2, height/2 - noDataHeight/2 + 2);		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}
	

	
	public static Color parseColor(String color) {
		if(color != null) {
			if(color.trim().startsWith("#")){
				// HTML colors (#FFFFFF format)
				return new Color(Integer.parseInt(color.substring(1), 16));
			}else if(color.trim().startsWith("rgb")){
				// HTML colors (rgb(255, 255, 255) format)
				String values = color.substring(color.indexOf("(") + 1, color.indexOf(")"));
				String rgb[] = values.split(",");
				return new Color(Integer.parseInt(rgb[0].trim()), Integer.parseInt(rgb[1].trim()), Integer.parseInt(rgb[2].trim()));
			}else{
				// Colors by name
				if(color.equalsIgnoreCase("black")) return Color.black;
				if(color.equalsIgnoreCase("grey")) return Color.gray;
				if(color.equalsIgnoreCase("yellow")) return Color.yellow;
				if(color.equalsIgnoreCase("green")) return Color.green;
				if(color.equalsIgnoreCase("blue")) return Color.blue;
				if(color.equalsIgnoreCase("red")) return Color.red;
				if(color.equalsIgnoreCase("orange")) return Color.orange;
				if(color.equalsIgnoreCase("cyan")) return Color.cyan;
				if(color.equalsIgnoreCase("magenta")) return Color.magenta;
				if(color.equalsIgnoreCase("darkgray")) return Color.darkGray;
				if(color.equalsIgnoreCase("lightgray")) return Color.lightGray;
				if(color.equalsIgnoreCase("pink")) return Color.pink;
				if(color.equalsIgnoreCase("white")) return Color.white;
			}
		}
		log.info("Unable to parse body background-color (color:" + color+"). Assuming white.");
		return Color.white;
	}
	
	/**
	 * Helper method to return the appropriate SQL for the DB vendor
	 * Everything should be lowercase.
	 * @return
	 */
	private String getSqlForVendor(String mysql, String oracle) {
		if(StringUtils.equals(dbVendor, "mysql")){
			return mysql;
		}
		if(StringUtils.equals(dbVendor, "oracle")){
			return oracle;
		}
		return null;
	}
	
	/**
	 * Helper to get the externalDbName as a prefix to be used directly in queries, . is appended
	 * If its not configured, then an empty string is returned so that whatever this returns can be used as-is
	 * @return
	 */
	private String getExternalDbNameAsPrefix() {
		if(StringUtils.isNotBlank(externalDbName)) {
			return externalDbName+".";
		} else {
			return "";
		}
	}
	
}
