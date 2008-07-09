/**
 * 
 */
package org.sakaiproject.sitestats.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsRecord;

/**
 * @author u4330369
 * 
 */
public class ServerWideReportManagerImpl implements ServerWideReportManager
{
	/** Our log (commons). */
	private static Log LOG = LogFactory
			.getLog (ServerWideReportManagerImpl.class);

	/** Dependency: SqlService */
	private SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *                The SqlService.
	 */
	public void setSqlService (SqlService service)
	{
		m_sqlService = service;
	}

	/** Dependency: UsageSessionService */
	private UsageSessionService m_usageSessionService;

	public void setUsageSessionService (UsageSessionService usageSessionService)
	{
		this.m_usageSessionService = usageSessionService;
	}

	public void init ()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getMonthlyLogin()
	 */
	public List<StatsRecord> getMonthlyLogin ()
	{
		String mySql = "select STR_TO_DATE(date_format(SESSION_START, '%Y-%m-01'),'%Y-%m-%d') as period, "
				+ "count(*) as user_logins, "
				+ "count(distinct SESSION_USER) as unique_users "
				+ "from SAKAI_SESSION " + "group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
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
	 * 
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getWeeklyLogin()
	 */
	public List<StatsRecord> getWeeklyLogin ()
	{
		String mySql = "select STR_TO_DATE(concat(date_format(SESSION_START, '%x-%v'), ' Monday'),'%x-%v %W') as week_start,"
				+ " count(*) as user_logins, count(distinct SESSION_USER) as unique_users"
				+ " from SAKAI_SESSION" + " group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	public List<StatsRecord> getDailyLogin ()
	{
		String mySql = "select date(SESSION_START) as session_date,"
				+ " count(*) as user_logins,"
				+ " count(distinct SESSION_USER) as unique_users"
				+ " from SAKAI_SESSION" 
				+ " where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 90 DAY)"
				+ " group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	public List<StatsRecord> getSiteCreatedDeletedStats (String period)
	{
		String sqlPeriod = "";
		if (period.equals ("daily")) {
			sqlPeriod = "date(EVENT_DATE) as event_period";
		} else if (period.equals ("weekly")) {
			sqlPeriod = "STR_TO_DATE(date_format(EVENT_DATE, '%x-%v Monday'),'%x-%v %W') as event_period";
		} else {
			// monthly
			sqlPeriod = "STR_TO_DATE(date_format(EVENT_DATE, '%Y-%m-01'),'%Y-%m-%d') as event_period";
		}
		String mySql = "select " + sqlPeriod + ", "
				+ "sum(if(event = 'site.add' && ref not regexp '/site/[~!]',1,0)) as site_created, "
				+ "sum(if(event = 'site.del' && ref not regexp '/site/[~!]',1,0)) as site_deleted "
				+ "FROM SAKAI_EVENT ";
		
		if (period.equals ("daily")) {
			mySql = mySql + "where EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY) ";
		}
		
		mySql = mySql + "group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);
		return result;
	}

	public List<StatsRecord> getNewUserStats (String period)
	{
		String sqlPeriod = "";
		if (period.equals ("daily")) {
			sqlPeriod = "date(EVENT_DATE) as event_period";
		} else if (period.equals ("weekly")) {
			sqlPeriod = "STR_TO_DATE(date_format(EVENT_DATE, '%x-%v Monday'),'%x-%v %W') as event_period";
		} else {
			// monthly
			sqlPeriod = "STR_TO_DATE(date_format(EVENT_DATE, '%Y-%m-01'),'%Y-%m-%d') as event_period";
		}
		String mySql = "select " + sqlPeriod + ", "
				+ "sum(if(event = 'site.add' && ref regexp '/site/[~!]',1,0)) as new_user "
				+ "FROM SAKAI_EVENT ";

		if (period.equals ("daily")) {
			mySql = mySql + "where EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 90 DAY) ";
		}
		mySql = mySql + "group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	public List<StatsRecord> getTop20Activities ()
	{
		String mySql = "SELECT event, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 7 DAY),1,0))/7 as last7, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 30 DAY),1,0))/30 as last30, "
				+ "sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY),1,0))/365 as last365 "
				+ "FROM SAKAI_EVENT "
				+ "where event not in ('content.read', 'user.login', 'user.logout', 'pres.end', "
				+ "'realm.upd', 'realm.add', 'realm.del', 'realm.upd.own') "
				+ "and event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY) "
				+ "group by 1 " + "order by 2 desc, 3 desc, 4 desc "
				+ "LIMIT 20";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getString (1));
					info.add (result.getDouble (2));
					info.add (result.getDouble (3));
					info.add (result.getDouble (4));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		return result;
	}

	public List<StatsRecord> getWeeklyRegularUsers ()
	{
		String mySql = "select s.week_start, sum(if(s.user_logins >= 5,1,0)) as five_plus, "
				+ "sum(if(s.user_logins = 4,1,0)) as four, "
				+ "sum(if(s.user_logins = 3,1,0)) as three, "
				+ "sum(if(s.user_logins = 2,1,0)) as twice, "
				+ "sum(if(s.user_logins = 1,1,0)) as once "
				+ "from (select "
				+ "STR_TO_DATE(concat(date_format(session_start, '%x-%v'), ' Monday'),'%x-%v %W') as week_start, "
				+ "session_user, count(*) as user_logins "
				+ "from SAKAI_SESSION group by 1, 2) as s " + "group by 1";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getLong (2));
					info.add (result.getLong (3));
					info.add (result.getLong (4));
					info.add (result.getLong (5));
					info.add (result.getLong (6));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		// remove the last entry, as it might not be a complete period
		result.remove (result.size () - 1);

		return result;
	}

	public List<StatsRecord> getHourlyUsagePattern ()
	{
		String mySql = "select date(SESSION_START) as session_date, "
				+ "hour(session_start) as hour_start, "
				+ "count(distinct SESSION_USER) as unique_users "
				+ "from SAKAI_SESSION "
				+ "where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
				+ "group by 1, 2";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getDate (1));
					info.add (result.getInt (2));
					info.add (result.getLong (3));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		return result;
	}

	public List<StatsRecord> getToolCount ()
	{
		String mySql = "SELECT registration, count(*) as site_count " +
				"FROM SAKAI_SITE_TOOL " +
				"where site_id regexp '^[[:digit:]]' " +
				"group by 1 " +
				"order by 2 desc";

		List result = m_sqlService.dbRead (mySql, null, new SqlReader () {
			public Object readSqlResultRecord (ResultSet result)
			{
				StatsRecord info = new StatsRecordImpl ();
				try {
					info.add (result.getString (1));
					info.add (result.getInt (2));
				}
				catch (SQLException e) {
					return null;
				}
				return info;
			}
		});

		return result;
	}
}
