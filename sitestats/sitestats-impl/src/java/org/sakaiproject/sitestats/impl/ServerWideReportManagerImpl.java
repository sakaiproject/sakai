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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsRecord;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author u4330369
 * 
 */
public class ServerWideReportManagerImpl implements ServerWideReportManager
{
	/** Our log (commons). */
	private static Log LOG = LogFactory
			.getLog (ServerWideReportManagerImpl.class);

	/** Message bundle */
	private static ResourceLoader	msgs								= new ResourceLoader("Messages");
		
	/** Dependency: SqlService */
	private SqlService m_sqlService = null;
	
	/** Dependence: StatsManager */
	private StatsManager M_sm = null;
	
	public void setStatsManager (StatsManager statsManager)
	{
		this.M_sm = statsManager;
	}

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

	public void destroy ()
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
	
	public BufferedImage generateReportChart(String reportType, int width, int height)
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
	


	
	private IntervalXYDataset getMonthlyLoginsDataSet ()
	{
		List<StatsRecord> loginList = getMonthlyLogin ();
		if (loginList == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),
				Month.class);
		for (StatsRecord login : loginList) {
			Month month = new Month ((Date) login.get (0));
			s1.add (month, (Long) login.get (1));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s1);

		return dataset;
	}

	
	private IntervalXYDataset getMonthlyUniqueLoginsDataSet ()
	{
		List<StatsRecord> loginList = getMonthlyLogin ();
		if (loginList == null) {
			return null;
		}

		TimeSeries s2 = new TimeSeries (
				msgs.getString ("legend_unique_logins"), Month.class);
		for (StatsRecord login : loginList) {
			Month month = new Month ((Date) login.get (0));
			s2.add (month, (Long) login.get (2));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s2);

		return dataset;
	}
	
	private IntervalXYDataset getWeeklyLoginsDataSet ()
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> loginList = getWeeklyLogin ();
		if (loginList == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),
				Week.class);
		TimeSeries s2 = new TimeSeries (
				msgs.getString ("legend_unique_logins"), Week.class);
		for (StatsRecord login : loginList) {
			Week week = new Week ((Date) login.get (0));
			s1.add (week, (Long) login.get (1));
			s2.add (week, (Long) login.get (2));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s1);
		dataset.addSeries (s2);

		return dataset;
	}

	private IntervalXYDataset getDailyLoginsDataSet ()
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> loginList = getDailyLogin ();
		if (loginList == null) {
			return null;
		}

		TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),
				Day.class);
		TimeSeries s2 = new TimeSeries (
				msgs.getString ("legend_unique_logins"), Day.class);
		for (StatsRecord login : loginList) {
			Day day = new Day ((Date) login.get (0));
			s1.add (day, (Long) login.get (1));
			s2.add (day, (Long) login.get (2));
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		dataset.addSeries (s1);
		dataset.addSeries (s2);

		TimeSeries mavS1 = MovingAverage.createMovingAverage (s1,
				"7 day login moving average", 7, 7);
		dataset.addSeries (mavS1);

		TimeSeries mavS2 = MovingAverage.createMovingAverage (s2,
				"7 day unique login moving average", 7, 7);
		dataset.addSeries (mavS2);

		return dataset;
	}

	private IntervalXYDataset getMonthlySiteUserDataSet ()
	{
		List<StatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("monthly");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), 
					Month.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), 
					Month.class);
			
			for (StatsRecord login : siteCreatedDeletedList) {
				Month month = new Month ((Date) login.get (0));
				s1.add (month, (Long) login.get (1));
				s2.add (month, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<StatsRecord> newUserList = getNewUserStats ("monthly");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"),
					Month.class);
			
			for (StatsRecord login : newUserList) {
				Month month = new Month ((Date) login.get (0));
				s3.add (month, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}


	private IntervalXYDataset getWeeklySiteUserDataSet ()
	{
		List<StatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("weekly");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), 
					Week.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), 
					Week.class);
			
			for (StatsRecord login : siteCreatedDeletedList) {
				Week week = new Week ((Date) login.get (0));
				s1.add (week, (Long) login.get (1));
				s2.add (week, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<StatsRecord> newUserList = getNewUserStats ("weekly");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"),
					Week.class);
			
			for (StatsRecord login : newUserList) {
				Week week = new Week ((Date) login.get (0));
				s3.add (week, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}

	private IntervalXYDataset getDailySiteUserDataSet ()
	{
		List<StatsRecord> siteCreatedDeletedList = getSiteCreatedDeletedStats ("daily");
		TimeSeriesCollection dataset = new TimeSeriesCollection ();
		if (siteCreatedDeletedList != null) {
			TimeSeries s1 = new TimeSeries (msgs.getString ("legend_site_created"), 
					Day.class);
			TimeSeries s2 = new TimeSeries (msgs.getString ("legend_site_deleted"), 
					Day.class);
			
			for (StatsRecord login : siteCreatedDeletedList) {
				Day day = new Day ((Date) login.get (0));
				s1.add (day, (Long) login.get (1));
				s2.add (day, (Long) login.get (2));
			}

			dataset.addSeries (s1);
			dataset.addSeries (s2);
		}

		List<StatsRecord> newUserList = getNewUserStats ("daily");
		if (newUserList != null) {
			TimeSeries s3 = new TimeSeries (msgs.getString ("legend_new_user"),
					Day.class);
			
			for (StatsRecord login : newUserList) {
				Day day = new Day ((Date) login.get (0));
				s3.add (day, (Long) login.get (1));
			}

			dataset.addSeries (s3);
		}

		return dataset;
	}

	private CategoryDataset getRegularUsersDataSet ()
	{
		List<StatsRecord> regularUsersList = getWeeklyRegularUsers ();
		if (regularUsersList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();
		DateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");

		for (StatsRecord regularUsers : regularUsersList) {
			Date weekStart = ((Date) regularUsers.get (0));
			dataset.addValue ((Long) regularUsers.get (1), "5+", formatter
					.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (2), "4", formatter
					.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (3), "3", formatter
					.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (4), "2", formatter
					.format (weekStart));
			dataset.addValue ((Long) regularUsers.get (5), "1", formatter
					.format (weekStart));
		}

		return dataset;
	}

	private BoxAndWhiskerCategoryDataset getHourlyUsageDataSet ()
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> hourlyUsagePattern = getHourlyUsagePattern ();
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
		for (StatsRecord regularUsers : hourlyUsagePattern) {
			Date currDate = (Date) regularUsers.get (0);
			if (!currDate.equals (prevDate)) {
				prevDate = currDate;
				totalDays++;
			}
			hourList[(Integer) regularUsers.get (1)].add ((Long) regularUsers
					.get (2));
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
		List<StatsRecord> topActivitiesList = getTop20Activities ();
		if (topActivitiesList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();

		for (StatsRecord regularUsers : topActivitiesList) {
			String event = (String) regularUsers.get (0);
			dataset.addValue ((Double) regularUsers.get (1), "last 7 days",
					event);
			dataset.addValue ((Double) regularUsers.get (2), "last 30 days",
					event);
			dataset.addValue ((Double) regularUsers.get (3), "last 365 days",
					event);
		}

		return dataset;
	}

	
	private CategoryDataset getToolAnalysisDataSet ()
	{
		List<StatsRecord> toolCountList = getToolCount ();
		if (toolCountList == null) {
			return null;
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();

		for (StatsRecord regularUsers : toolCountList) {
			String toolId = (String) regularUsers.get (0);
			dataset.addValue ((Integer) regularUsers.get (1), "",
					toolId);
		}

		return dataset;
	}	
	
	
	private BufferedImage createMonthlyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getMonthlyLoginsDataSet ();
		IntervalXYDataset dataset2 = getMonthlyUniqueLoginsDataSet ();
        IntervalXYDataset dataset3 = getMonthlySiteUserDataSet ();
		
		if ((dataset1 == null) || (dataset3 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer1.setSeriesPaint (0, Color.RED);
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.MONTH, 1, 
        		new SimpleDateFormat ("yyyy-MM")));
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

		XYPlot plot2 = new XYPlot(dataset3, null, axis1, 
                renderer3);
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		return chart.createBufferedImage (width, height);
	}


	private BufferedImage createWeeklyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getWeeklyLoginsDataSet ();
        IntervalXYDataset dataset2 = getWeeklySiteUserDataSet ();
		
		if ((dataset1 == null) || (dataset2 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer1.setSeriesStroke(1, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		renderer1.setSeriesPaint (0, Color.RED);
		renderer1.setSeriesPaint (0, Color.BLUE);
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.DAY, 7, 
        		new SimpleDateFormat ("yyyy-MM-dd")));
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
        renderer2.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(1, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(2, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesPaint(0, Color.GREEN);
        renderer2.setSeriesPaint(1, Color.BLACK);
        renderer2.setSeriesPaint(2, Color.CYAN);
        
        rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		XYPlot plot2 = new XYPlot(dataset2, null, rangeAxis, 
                renderer2);
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		return chart.createBufferedImage (width, height);
	}

	private BufferedImage createDailyLoginChart (int width, int height)
	{
		IntervalXYDataset dataset1 = getDailyLoginsDataSet ();
        IntervalXYDataset dataset2 = getDailySiteUserDataSet ();
		
		if ((dataset1 == null) || (dataset2 == null)) {
			return generateNoDataChart(width, height);
		}
		
        // create plot ...
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
		renderer1.setSeriesPaint (0, Color.RED);
		renderer1.setSeriesPaint (1, Color.BLUE);
		renderer1.setSeriesPaint (2, Color.RED);
		renderer1.setSeriesPaint (3, Color.BLUE);
        renderer1.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer1.setSeriesStroke(1, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		BasicStroke dashLineStroke = new BasicStroke (2, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_ROUND, 0, new float[] { 4 }, 0);
		renderer1.setSeriesStroke (2, dashLineStroke);
		renderer1.setSeriesStroke (3, dashLineStroke);
		
        
        DateAxis domainAxis = new DateAxis("");
        domainAxis.setTickUnit (new DateTickUnit (DateTickUnit.DAY, 7, 
        		new SimpleDateFormat ("yyyy-MM-dd")));
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
        renderer2.setSeriesStroke(0, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(1, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesStroke(2, new BasicStroke(2.0f, 
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer2.setSeriesPaint(0, Color.GREEN);
        renderer2.setSeriesPaint(1, Color.BLACK);
        renderer2.setSeriesPaint(2, Color.CYAN);
        
        rangeAxis = new NumberAxis("count");
		rangeAxis.setStandardTickUnits (NumberAxis.createIntegerTickUnits ());

		XYPlot plot2 = new XYPlot(dataset2, null, rangeAxis, 
                renderer2);
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		return chart.createBufferedImage (width, height);
	}

	private BufferedImage generateStackedAreaChart (CategoryDataset dataset, int width, int height)
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

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

		return chart.createBufferedImage (width, height);
	}

	private BufferedImage generateBoxAndWhiskerChart (BoxAndWhiskerCategoryDataset dataset, int width, int height)
	{
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart (null, null,
				null, dataset, false);

		// set background
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

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

		return chart.createBufferedImage (width, height);
	}

	private BufferedImage generateLayeredBarChart (CategoryDataset dataset, int width, int height)
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

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

		return chart.createBufferedImage (width, height);
	}

	private BufferedImage createToolAnalysisChart (int width, int height)
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
		chart.setBackgroundPaint (parseColor (M_sm.getChartBackgroundColor ()));

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
                    NumberFormat.getInstance());
        renderer.setBaseItemLabelGenerator(generator);
        renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 9));
        renderer.setBaseItemLabelsVisible(true);
        renderer.setItemMargin (0);
        renderer.setSeriesPaint (0, Color.BLUE);
        
		return chart.createBufferedImage (width, height);
	}

	private BufferedImage generateNoDataChart(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		
		g2d.setBackground(parseColor(M_sm.getChartBackgroundColor()));
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
		return img;
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
		LOG.info("Unable to parse body background-color (color:" + color+"). Assuming white.");
		return Color.white;
	}
}
