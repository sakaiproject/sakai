/**
 * $URL$
 * $Id$
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
package org.sakaiproject.sitestats.api.chart;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;

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
	 * @return The chart image in a byte array.
	 * @see StatsManager
	 */
	public byte[] generateVisitsChart(
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
	 * @return The chart image in a byte array.
	 * @see StatsManager
	 */
	public byte[] generateActivityChart(
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
	 * @return The chart image in a byte array.
	 * @see StatsManager
	 * @see org.jfree.data.general.Dataset
	 */
	public byte[] generateChart(
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
	 * @return The chart image in a byte array.
	 * @see StatsManager
	 * @see org.jfree.data.general.Dataset
	 */
	public byte[] generateChart(
			Report report,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible); 
}
