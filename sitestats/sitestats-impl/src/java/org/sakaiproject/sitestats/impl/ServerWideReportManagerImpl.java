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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.ServerWideStatsRecord;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
	
	@Setter
	private SqlService sqlService;
	
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
				" group by period";
		
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
		if (result.size() > 1) {
			result.remove (result.size () - 1);
		}

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
		if (result.size() > 1) {
			result.remove (result.size () - 1);
		}

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
				" group by session_date";
		
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
		if (result.size() > 1) {
			result.remove (result.size () - 1);
		}

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
		
		mysql = mysql + "group by event_period";
		
		
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
		if (result.size() > 1) {
			result.remove (result.size () - 1);
		}

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
