package org.sakaiproject.sitestats.api.chart;

import java.awt.image.BufferedImage;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;

public interface ChartService {

	/**
	 * Generate a site visits chart.
	 * @param siteId The site id.
	 * @param viewType One of StatsManager.VIEW_WEEK, StatsManager.VIEW_MONTH, StatsManager.VIEW_YEAR
	 * @param width The chart width.
	 * @param height The chart height.
	 * @param render3d Render a 3D chart?
	 * @param transparency Set chart transparency (accept values between 0.0 and 1.0)
	 * @param itemLabelsVisible Render labels on top of bars
	 * @return The chart in a BufferedImage object.
	 * @see StatsManager
	 */
	public BufferedImage generateVisitsChart(
			String siteId, String viewType, 
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible); 
	
	/**
	 * Generate a site activity chart.
	 * @param siteId The site id.
	 * @param viewType One of StatsManager.VIEW_WEEK, StatsManager.VIEW_MONTH, StatsManager.VIEW_YEAR
	 * @param chartType One of StatsManager.CHARTTYPE_PIE, StatsManager.CHARTTYPE_BAR
	 * @param width The chart width.
	 * @param height The chart height.
	 * @param render3d Render a 3D chart?
	 * @param transparency Set chart transparency (accept values between 0.0 and 1.0)
	 * @param itemLabelsVisible Render labels on top of bars
	 * @return The chart in a BufferedImage object.
	 * @see StatsManager
	 */
	public BufferedImage generateActivityChart(
			String siteId, String viewType, String chartType,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible);  
	
	/**
	 * Generate a generic chart based on a know dataset.
	 * @param siteId The site id.
	 * @param dataset A JFreeChart Dataset object
	 * @param chartType One of StatsManager.CHARTTYPE_BAR, StatsManager.CHARTTYPE_LINE, StatsManager.CHARTTYPE_PIE
	 * @param width The chart width.
	 * @param height The chart height.
	 * @param render3d Render a 3D chart?
	 * @param transparency Set chart transparency (accept values between 0.0 and 1.0)
	 * @param itemLabelsVisible Render labels on top of bars
	 * @param timePeriod For TimeSeries charts, this indicates the time period unit: {@link StatsManager#CHARTTIMESERIES_DAY}, {@link StatsManager#CHARTTIMESERIES_WEEKDAY}, {@link StatsManager#CHARTTIMESERIES_MONTH}, {@link StatsManager#CHARTTIMESERIES_YEAR}
	 * @return The chart in a BufferedImage object.
	 * @see StatsManager
	 * @see org.jfree.data.general.Dataset
	 */
	public BufferedImage generateChart(
			String siteId, Object dataset, String chartType,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible, String timePeriod);   
	
	/**
	 * Generate a generic chart based on an existing report definition with report data.
	 * @param report A Report object.
	 * @param width The chart width.
	 * @param height The chart height.
	 * @param render3d Render a 3D chart?
	 * @param transparency Set chart transparency (accept values between 0.0 and 1.0)
	 * @param itemLabelsVisible Render labels on top of bars
	 * @return The chart in a BufferedImage object.
	 * @see StatsManager
	 * @see org.jfree.data.general.Dataset
	 */
	public BufferedImage generateChart(
			Report report,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible); 
}
