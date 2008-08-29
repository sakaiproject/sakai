package org.sakaiproject.sitestats.tool.bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import org.ajax4jsf.framework.resource.ImageRenderer;
import org.ajax4jsf.framework.resource.PngRenderer;
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
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsRecord;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class ServerWideReportBean
{
	/** Our log (commons). */
	private static Log LOG = LogFactory.getLog (ServerWideReportBean.class);

	/** Resource bundle */
	private static String bundleName = FacesContext.getCurrentInstance ()
			.getApplication ().getMessageBundle ();
	private static ResourceLoader msgs = new ResourceLoader (bundleName);

	/** Manager APIs */
	private transient ServiceBean serviceBean = null;
	private transient StatsManager SST_sm = null;
	private SiteService M_ss = null;
	private StatsAuthz SST_authz = null;
	private ServerWideReportManager serverWideReportManager = null;

	/** Other */
	private PrefsData prefsdata = null;
	private long prefsLastModified = 0;

	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################
	public void setServiceBean (ServiceBean serviceBean)
	{
		this.serviceBean = serviceBean;
		this.SST_sm = serviceBean.getSstStatsManager ();
		this.SST_authz = serviceBean.getSstAuthz ();
		this.M_ss = serviceBean.getSiteService ();
	}

	public void setServerWideReportManager (
			ServerWideReportManager serverWideReportManager)
	{
		this.serverWideReportManager = serverWideReportManager;
	}

	public boolean isAllowed ()
	{
		boolean allowed = SST_authz.isUserAbleToViewSiteStatsAdmin (ToolManager
				.getCurrentPlacement ().getContext ());

		if (!allowed) {
			FacesContext fc = FacesContext.getCurrentInstance ();
			fc.addMessage ("allowed", new FacesMessage (
					FacesMessage.SEVERITY_FATAL, msgs
							.getString ("unauthorized"), null));
		}
		return allowed;
	}

	public void generateReportChart (OutputStream out, Object data)
			throws IOException
	{
		ChartParamsBean params = null;
		if (data instanceof ChartParamsBean) {
			params = (ChartParamsBean) data;
		} else {
			LOG.warn ("data NOT instanceof ChartParamsBean!");
			return;
		}

		boolean useSmallFontInDomainAxis = false;
		
		if (params.getSelectedReportType ().equals (
				ChartParamsBean.MONTHLY_LOGIN_REPORT))
		{
			createMonthlyLoginChart (params, useSmallFontInDomainAxis, out);

		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.WEEKLY_LOGIN_REPORT))
		{
			createWeeklyLoginChart (params, useSmallFontInDomainAxis, out);

		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.DAILY_LOGIN_REPORT))
		{
			createDailyLoginChart (params,
					useSmallFontInDomainAxis, out);
		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.REGULAR_USERS_REPORT))
		{
			CategoryDataset dataset = getRegularUsersDataSet (params);
			if (dataset != null) {
				generateStackedAreaChart (dataset, params,
						useSmallFontInDomainAxis, out);
			} else {
				generateNoDataChart (params, out);
			}
		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.HOURLY_USAGE_REPORT))
		{
			BoxAndWhiskerCategoryDataset dataset = getHourlyUsageDataSet (params);
			if (dataset != null) {
				generateBoxAndWhiskerChart (dataset, params,
						useSmallFontInDomainAxis, out);
			} else {
				generateNoDataChart (params, out);
			}
		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.TOP_ACTIVITIES_REPORT))
		{
			CategoryDataset dataset = getTopActivitiesDataSet (params);
			if (dataset != null) {
				generateLayeredBarChart (dataset, params,
						useSmallFontInDomainAxis, out);
			} else {
				generateNoDataChart (params, out);
			}
		} else if (params.getSelectedReportType ().equals (
				ChartParamsBean.TOOL_REPORT))
		{
			createToolAnalysisChart (params, useSmallFontInDomainAxis, out);
		} else {
			generateNoDataChart (params, out);
		}
	}

	
	private IntervalXYDataset getMonthlyLoginsDataSet (ChartParamsBean params)
	{
		List<StatsRecord> loginList = serverWideReportManager.getMonthlyLogin ();
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

	
	private IntervalXYDataset getMonthlyUniqueLoginsDataSet (ChartParamsBean params)
	{
		List<StatsRecord> loginList = serverWideReportManager.getMonthlyLogin ();
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
	
	private IntervalXYDataset getWeeklyLoginsDataSet (ChartParamsBean params)
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> loginList = serverWideReportManager.getWeeklyLogin ();
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

	private IntervalXYDataset getDailyLoginsDataSet (ChartParamsBean params)
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> loginList = serverWideReportManager.getDailyLogin ();
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

	private IntervalXYDataset getMonthlySiteUserDataSet (ChartParamsBean params)
	{
		List<StatsRecord> siteCreatedDeletedList = serverWideReportManager
				.getSiteCreatedDeletedStats ("monthly");
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

		List<StatsRecord> newUserList = serverWideReportManager
				.getNewUserStats ("monthly");
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


	private IntervalXYDataset getWeeklySiteUserDataSet (ChartParamsBean params)
	{
		List<StatsRecord> siteCreatedDeletedList = serverWideReportManager
				.getSiteCreatedDeletedStats ("weekly");
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

		List<StatsRecord> newUserList = serverWideReportManager
				.getNewUserStats ("weekly");
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

	private IntervalXYDataset getDailySiteUserDataSet (ChartParamsBean params)
	{
		List<StatsRecord> siteCreatedDeletedList = serverWideReportManager
				.getSiteCreatedDeletedStats ("daily");
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

		List<StatsRecord> newUserList = serverWideReportManager
				.getNewUserStats ("daily");
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

	private CategoryDataset getRegularUsersDataSet (ChartParamsBean params)
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> regularUsersList = serverWideReportManager
				.getWeeklyRegularUsers ();
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

	private BoxAndWhiskerCategoryDataset getHourlyUsageDataSet (
			ChartParamsBean params)
	{
		// LOG.info("Generating activityWeekBarDataSet");
		List<StatsRecord> hourlyUsagePattern = serverWideReportManager
				.getHourlyUsagePattern ();
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
				hourList[ii].add (new Long (0));
			}

			dataset.add (hourList[ii], "Last 30 days", "" + ii);
		}

		return dataset;
	}

	private CategoryDataset getTopActivitiesDataSet (ChartParamsBean params)
	{
		List<StatsRecord> topActivitiesList = serverWideReportManager
				.getTop20Activities ();
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

	
	private CategoryDataset getToolAnalysisDataSet (ChartParamsBean params)
	{
		List<StatsRecord> toolCountList = serverWideReportManager
				.getToolCount ();
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
	
	
	private void createMonthlyLoginChart (ChartParamsBean params,
			boolean useSmallFontInDomainAxis, OutputStream out)
			throws IOException
	{
		IntervalXYDataset dataset1 = getMonthlyLoginsDataSet (params);
		IntervalXYDataset dataset2 = getMonthlyUniqueLoginsDataSet (params);
        IntervalXYDataset dataset3 = getMonthlySiteUserDataSet (params);
		
		if ((dataset1 == null) || (dataset3 == null)) {
			generateNoDataChart (params, out);
			return;
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
        //domainAxis.setDateFormatOverride (new SimpleDateFormat ("yyyy-MM-dd"));
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);

		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}
        
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
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}


	private void createWeeklyLoginChart (ChartParamsBean params,
			boolean useSmallFontInDomainAxis, OutputStream out)
			throws IOException
	{
		IntervalXYDataset dataset1 = getWeeklyLoginsDataSet (params);
        IntervalXYDataset dataset2 = getWeeklySiteUserDataSet (params);
		
		if ((dataset1 == null) || (dataset2 == null)) {
			generateNoDataChart (params, out);
			return;
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
        //domainAxis.setDateFormatOverride (new SimpleDateFormat ("yyyy-MM-dd"));
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);

		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}
        
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
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void createDailyLoginChart (ChartParamsBean params, 
			boolean useSmallFontInDomainAxis,
			OutputStream out) throws IOException
	{
		IntervalXYDataset dataset1 = getDailyLoginsDataSet (params);
        IntervalXYDataset dataset2 = getDailySiteUserDataSet (params);
		
		if ((dataset1 == null) || (dataset2 == null)) {
			generateNoDataChart (params, out);
			return;
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
        //domainAxis.setDateFormatOverride (new SimpleDateFormat ("yyyy-MM-dd"));
        domainAxis.setVerticalTickLabels (true);
		domainAxis.setLowerMargin (0.01);
		domainAxis.setUpperMargin (0.01);

		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}
        
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
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void generateStackedAreaChart (CategoryDataset dataset,
			ChartParamsBean params, boolean useSmallFontInDomainAxis,
			OutputStream out) throws IOException
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
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		// set transparency
		// plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());
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
		
		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void generateBoxAndWhiskerChart (
			BoxAndWhiskerCategoryDataset dataset, ChartParamsBean params,
			boolean useSmallFontInDomainAxis, OutputStream out)
			throws IOException
	{
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart (null, null,
				null, dataset, false);

		// set background
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

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

		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void generateLayeredBarChart (CategoryDataset dataset,
			ChartParamsBean params, boolean useSmallFontInDomainAxis,
			OutputStream out) throws IOException
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

		// set transparency
		// plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());

		// set background
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

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

		// set domain axis font size
		if (useSmallFontInDomainAxis) {
			domainAxis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
		}

		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void createToolAnalysisChart (ChartParamsBean params, 
			boolean useSmallFontInDomainAxis,
			OutputStream out) throws IOException
	{
		CategoryDataset dataset = getToolAnalysisDataSet (params);
		
		if (dataset == null) {
			generateNoDataChart (params, out);
			return;
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
		chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));

		// set chart border
		chart.setPadding (new RectangleInsets (10, 5, 5, 5));
		chart.setBorderVisible (true);
		chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

		// set anti alias
		chart.setAntiAlias (true);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();

		// set transparency
		// plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());
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
        
		BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
				params.getChartHeight ());
		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}

	private void generateNoDataChart (ChartParamsBean params, OutputStream out)
			throws IOException
	{
		ImageRenderer imgR = new PngRenderer ();
		BufferedImage img = imgR.createImage (params.getChartWidth (), params
				.getChartHeight ());
		Graphics2D g2d = img.createGraphics ();

		g2d.setBackground (OverviewBean.parseColor (SST_sm
				.getChartBackgroundColor ()));
		g2d.clearRect (0, 0, params.getChartWidth () - 1, params
				.getChartHeight () - 1);
		g2d.setColor (OverviewBean.parseColor ("#cccccc"));
		g2d.drawRect (0, 0, params.getChartWidth () - 1, params
				.getChartHeight () - 1);
		Font f = new Font ("SansSerif", Font.PLAIN, 12);
		g2d.setFont (f);
		FontMetrics fm = g2d.getFontMetrics (f);
		String noData = msgs.getString ("no_data");
		int noDataWidth = fm.stringWidth (noData);
		int noDataHeight = fm.getHeight ();
		g2d.setColor (OverviewBean.parseColor ("#555555"));
		g2d.drawString (noData, params.getChartWidth () / 2 - noDataWidth / 2,
				params.getChartHeight () / 2 - noDataHeight / 2 + 2);

		try {
			ImageIO.write (img, "png", out);
		}
		catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn ("Data transfer aborted by client.");
		}
	}
}
