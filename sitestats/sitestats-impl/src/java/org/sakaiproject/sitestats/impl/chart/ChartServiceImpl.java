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
package org.sakaiproject.sitestats.impl.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.SortOrder;

import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.event.EventRegistryServiceImpl;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class ChartServiceImpl implements ChartService {
	/** Static fields */
	private static ResourceLoader	msgs								= new ResourceLoader("Messages");
	private static final int		MIN_CHART_WIDTH_TO_DRAW_ALL_DAYS	= 640;

	/** Utility */
	private Map<Integer, String>	weekDaysMap							= null;
	private Map<Integer, String>	monthNamesMap						= null;

	/** Sakai services */
	private StatsManager			M_sm;
	private SiteService				M_ss;
	private UserDirectoryService	M_uds;
	private PreferencesService		M_ps;
	private EventRegistryService	M_ers;

	// ################################################################
	// Spring methods
	// ################################################################
	public void setStatsManager(StatsManager statsManager) {
		this.M_sm = statsManager;
	}
	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}
	public void setUserService(UserDirectoryService userDirectoryService) {
		this.M_uds = userDirectoryService;
	}
	public void setPreferencesService(PreferencesService preferencesService) {
		this.M_ps = preferencesService;
	}
	public void setEventRegistryService(EventRegistryService eventRegistryService) {
		this.M_ers = eventRegistryService;
	}
	

	// ################################################################
	// Chart Service methods 
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateVisitsChart(java.lang.String, java.lang.String, int, int, boolean, float, boolean)
	 */
	public byte[] generateVisitsChart(
			String siteId, String viewType,
			int width, int height, 
			boolean render3d, float transparency, boolean itemLabelsVisible) {
		
		CategoryDataset dataset = null;
		boolean smallFontInDomainAxis = false;
		render3d = false;
		if(StatsManager.VIEW_WEEK.equals(viewType)) {
			dataset = getVisitsWeekDataSet(siteId);
		}else if(StatsManager.VIEW_MONTH.equals(viewType)) {
			dataset = getVisitsMonthDataSet(siteId, width);
			smallFontInDomainAxis = true;
		}else{
			dataset = getVisitsYearDataSet(siteId);
		}
		if(dataset != null)
			return generateBarChart(siteId, dataset, width, height, render3d, transparency, itemLabelsVisible, smallFontInDomainAxis);
		else
			return generateNoDataChart(width, height);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateActivityChart(java.lang.String, java.lang.String, java.lang.String, int, int, boolean, float, boolean)
	 */
	public byte[] generateActivityChart(
			String siteId, String viewType, String chartType, 
			int width, int height, 
			boolean render3d, float transparency, boolean itemLabelsVisible) {
		
		boolean smallFontInDomainAxis = false;
		render3d = false;
		if(StatsManager.CHARTTYPE_PIE.equals(chartType)) {
			DefaultPieDataset dataset = null;
			if(StatsManager.VIEW_WEEK.equals(viewType))
				dataset = getActivityWeekPieDataSet(siteId);
			else if(StatsManager.VIEW_MONTH.equals(viewType))
				dataset = getActivityMonthPieDataSet(siteId);
			else 
				dataset = getActivityYearPieDataSet(siteId);
			if(dataset != null)
				return generatePieChart(siteId, dataset, width, height, render3d, transparency, smallFontInDomainAxis);
			else
				return generateNoDataChart(width, height);
		}else{
			CategoryDataset dataset = null;
			if(StatsManager.VIEW_WEEK.equals(viewType))
				dataset = getActivityWeekBarDataSet(siteId);
			else if(StatsManager.VIEW_MONTH.equals(viewType)){
				dataset = getActivityMonthBarDataSet(siteId, width);
				smallFontInDomainAxis = true;
			}else 
				dataset = getActivityYearBarDataSet(siteId);
			if(dataset != null)
				return generateBarChart(siteId, dataset, width, height, render3d, transparency, itemLabelsVisible, smallFontInDomainAxis);
			else
				return generateNoDataChart(width, height);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateChart(java.lang.String, java.lang.Object, java.lang.String, int, int, boolean, float, boolean, java.lang.String)
	 */
	public byte[] generateChart(
			String siteId, Object dataset, String chartType,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible, String timePeriod) {
		render3d = false;
		if(StatsManager.CHARTTYPE_BAR.equals(chartType)) {
			if(dataset instanceof CategoryDataset) {
				CategoryDataset ds = (CategoryDataset) dataset;
				return generateBarChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
			}else{
				log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
			}				
		}else if(StatsManager.CHARTTYPE_LINE.equals(chartType)) {
			if(dataset instanceof CategoryDataset) {
				CategoryDataset ds = (CategoryDataset) dataset;
				return generateLineChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
			}else{
				log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
			}
		}else if(StatsManager.CHARTTYPE_PIE.equals(chartType)) {
			if(dataset instanceof PieDataset) {
				PieDataset ds = (PieDataset) dataset;
				return generatePieChart(siteId, ds, width, height, render3d, transparency, false);
			}else{
				log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing PieDataset are supported.");
			}
		}else if(StatsManager.CHARTTYPE_TIMESERIES.equals(chartType)) {
			if(dataset instanceof IntervalXYDataset) {
				IntervalXYDataset ds = (IntervalXYDataset) dataset;
				return generateTimeSeriesChart(siteId, ds, width, height, false, transparency, itemLabelsVisible, false, timePeriod);
			}else{
				log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing XYDataset are supported.");
			}
		}else if(StatsManager.CHARTTYPE_TIMESERIESBAR.equals(chartType)) {
			if(dataset instanceof IntervalXYDataset) {
				IntervalXYDataset ds = (IntervalXYDataset) dataset;
				return generateTimeSeriesChart(siteId, ds, width, height, true, transparency, itemLabelsVisible, false, timePeriod);
			}else{
				log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing XYDataset are supported.");
			}
		}
		
		log.warn("Chart type "+chartType+" not supported: only line, bar, pie, timeseries are supported.");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateChart(org.sakaiproject.sitestats.api.report.ReportDef, int, int, boolean, float, boolean)
	 */
	public byte[] generateChart(
			Report report, 
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible) {
		// Data set
		AbstractDataset dataset = null;
		String chartType = report.getReportDefinition().getReportParams().getHowChartType();
		render3d = false;
		if(StatsManager.CHARTTYPE_BAR.equals(chartType)
			|| StatsManager.CHARTTYPE_LINE.equals(chartType)) {
			dataset = getCategoryDataset(report);			
		}else if(StatsManager.CHARTTYPE_TIMESERIES.equals(chartType)
				|| StatsManager.CHARTTYPE_TIMESERIESBAR.equals(chartType)) {
			dataset = getTimeSeriesCollectionDataset(report);			
		}else if(StatsManager.CHARTTYPE_PIE.equals(chartType)) {
			dataset = getPieDataset(report);			
		}
		
		// Report
		if(dataset != null) {
			/*return generateChart(
					report.getReportDefinition().getReportParams().getSiteId(), dataset, chartType,
					width, height,
					render3d, transparency,
					true,
					report.getReportDefinition().getReportParams().getHowChartSeriesPeriod()
					);*/
			String siteId = report.getReportDefinition().getReportParams().getSiteId();
			String timePeriod = report.getReportDefinition().getReportParams().getHowChartSeriesPeriod();
			Date firstDate = report.getReportDefinition().getReportParams().getWhenFrom();
			Date lastDate = report.getReportDefinition().getReportParams().getWhenTo();
			if(StatsManager.CHARTTYPE_BAR.equals(chartType)) {
				if(dataset instanceof CategoryDataset) {
					CategoryDataset ds = (CategoryDataset) dataset;
					return generateBarChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
				}else{
					log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
				}				
			}else if(StatsManager.CHARTTYPE_LINE.equals(chartType)) {
				if(dataset instanceof CategoryDataset) {
					CategoryDataset ds = (CategoryDataset) dataset;
					return generateLineChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
				}else{
					log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
				}
			}else if(StatsManager.CHARTTYPE_PIE.equals(chartType)) {
				if(dataset instanceof PieDataset) {
					PieDataset ds = (PieDataset) dataset;
					return generatePieChart(siteId, ds, width, height, render3d, transparency, false);
				}else{
					log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing PieDataset are supported.");
				}
			}else if(StatsManager.CHARTTYPE_TIMESERIES.equals(chartType)) {
				if(dataset instanceof IntervalXYDataset) {
					IntervalXYDataset ds = (IntervalXYDataset) dataset;
					return generateTimeSeriesChart(siteId, ds, width, height, false, transparency, itemLabelsVisible, false, timePeriod, firstDate, lastDate);
				}else{
					log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing XYDataset are supported.");
				}
			}else if(StatsManager.CHARTTYPE_TIMESERIESBAR.equals(chartType)) {
				if(dataset instanceof IntervalXYDataset) {
					IntervalXYDataset ds = (IntervalXYDataset) dataset;
					return generateTimeSeriesChart(siteId, ds, width, height, true, transparency, itemLabelsVisible, false, timePeriod, firstDate, lastDate);
				}else{
					log.warn("Dataset not supported for "+chartType+" chart type: only classes implementing XYDataset are supported.");
				}
			}
			
			log.warn("Chart type "+chartType+" not supported: only line, bar, pie, timeseries are supported.");
			return null;
		}else{		
			log.warn("Chart type "+chartType+" not supported: only line, bar, pie, timeseries are supported.");
			return null;
		}
	}
	
	
	// ######################################################################################
	// Chart Generation methods
	// ######################################################################################
	private byte[] generateBarChart(
			String siteId, CategoryDataset dataset, int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible, 
			boolean smallFontInDomainAxis) {
		JFreeChart chart = null;
		if(render3d)
			chart = ChartFactory.createBarChart3D(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		else
			chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		
		// set transparency
		plot.setForegroundAlpha(transparency);
		
		// set background
		chart.setBackgroundPaint(parseColor(M_sm.getChartBackgroundColor()));
		
		// set chart border
		chart.setPadding(new RectangleInsets(10,5,5,5));
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// allow longer legends (prevent truncation)
		plot.getDomainAxis().setMaximumCategoryLabelLines(50);
		plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(1.0f);
		
		// set antialias
		chart.setAntiAlias(true);
		
		// set domain axis font size
		if(smallFontInDomainAxis && !canUseNormalFontSize(width)) {
			plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			plot.getDomainAxis().setCategoryMargin(0.05);
		}

		// set bar outline
		BarRenderer barrenderer = (BarRenderer) plot.getRenderer();
		barrenderer.setDrawBarOutline(true);
		if(smallFontInDomainAxis && !canUseNormalFontSize(width)) 
			barrenderer.setItemMargin(0.05);
		else
			barrenderer.setItemMargin(0.10);
		
		// item labels
		if(itemLabelsVisible) {
			plot.getRangeAxis().setUpperMargin(0.2);
			barrenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
				private static final long	serialVersionUID	= 1L;

				@Override
				public String generateLabel(CategoryDataset dataset, int row, int column) {
					Number n = dataset.getValue(row, column);
					if(n.doubleValue() != 0) {
						if((double)n.intValue() == n.doubleValue()) return Integer.toString(n.intValue());
						else return Double.toString( Util.round(n.doubleValue(), 1) );
					}
					return "";
				}			
			});
			barrenderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			barrenderer.setItemLabelsVisible(true);
		}

		BufferedImage img = chart.createBufferedImage(width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}
	
	private byte[] generateLineChart(
			String siteId, CategoryDataset dataset, int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible, 
			boolean smallFontInDomainAxis) {
		JFreeChart chart = null;
		if(render3d)
			chart = ChartFactory.createLineChart3D(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		else
			chart = ChartFactory.createLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		
		// set transparency
		plot.setForegroundAlpha(transparency);
		
		// set background
		chart.setBackgroundPaint(parseColor(M_sm.getChartBackgroundColor()));
		
		// set chart border
		chart.setPadding(new RectangleInsets(10,5,5,5));
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// set antialias
		chart.setAntiAlias(true);
		
		// set domain axis font size
		if(smallFontInDomainAxis && !canUseNormalFontSize(width)) {
			plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			plot.getDomainAxis().setCategoryMargin(0.05);
		}

		// set outline
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();		
		renderer.setDrawOutlines(true);
		
		// item labels
		if(itemLabelsVisible) {
			plot.getRangeAxis().setUpperMargin(0.2);
			renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
				private static final long	serialVersionUID	= 1L;

				@Override
				public String generateLabel(CategoryDataset dataset, int row, int column) {
					Number n = dataset.getValue(row, column);
					if(n.intValue() != 0)
						//return n.intValue()+"";
						return n.toString();
					return "";
				}			
			});
			renderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			renderer.setItemLabelsVisible(true);
		}

		BufferedImage img = chart.createBufferedImage(width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}
	
	private byte[] generatePieChart(
			String siteId, PieDataset dataset, int width, int height,
			boolean render3d, float transparency,
			boolean smallFontInDomainAxis) {
		JFreeChart chart = null;
		if(render3d)
			chart = ChartFactory.createPieChart3D(null, dataset, false, false, false);
		else
			chart = ChartFactory.createPieChart(null, dataset, false, false, false);
		PiePlot plot = (PiePlot) chart.getPlot();
		
		// set start angle (135 or 150 deg so minor data has more space on the left)
		plot.setStartAngle(150D);
		
		// set transparency
		plot.setForegroundAlpha(transparency);
		
		// set background
		chart.setBackgroundPaint(parseColor(M_sm.getChartBackgroundColor()));
		plot.setBackgroundPaint(parseColor(M_sm.getChartBackgroundColor()));
		
		// fix border offset		
		chart.setPadding(new RectangleInsets(5,5,5,5));
		plot.setInsets(new RectangleInsets(1,1,1,1));
		// set chart border
		plot.setOutlinePaint(null);
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// set antialias
		chart.setAntiAlias(true);
		
		BufferedImage img = chart.createBufferedImage(width, height);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}
	
	private byte[] generateTimeSeriesChart(
			String siteId, IntervalXYDataset dataset, int width, int height,
			boolean renderBar, float transparency,
			boolean itemLabelsVisible, 
			boolean smallFontInDomainAxis,
			String timePeriod) {
		return generateTimeSeriesChart(siteId, dataset, width, height, 
				renderBar, transparency, 
				itemLabelsVisible,
				smallFontInDomainAxis,
				timePeriod, null, null);
	}
	
	private byte[] generateTimeSeriesChart(
			String siteId, IntervalXYDataset dataset, int width, int height,
			boolean renderBar, float transparency,
			boolean itemLabelsVisible, 
			boolean smallFontInDomainAxis,
			String timePeriod, Date firstDate, Date lastDate) {
		JFreeChart chart = null;
		if(!renderBar) {
			chart = ChartFactory.createTimeSeriesChart(null, null, null, dataset, true, false, false);
		}else {
			chart = ChartFactory.createXYBarChart(null, 
	                null, true, null, dataset, PlotOrientation.VERTICAL, 
	                true, false, false); 
		}
		XYPlot plot = (XYPlot) chart.getPlot();
		
		// set transparency
		plot.setForegroundAlpha(transparency);
		
		// set background
		chart.setBackgroundPaint(parseColor(M_sm.getChartBackgroundColor()));
		
		// set chart border
		chart.setPadding(new RectangleInsets(10,5,5,5));
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// set antialias
		chart.setAntiAlias(true);
		
		// set domain axis font size
		if(smallFontInDomainAxis && !canUseNormalFontSize(width)) {
			plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		}
		
		// configure date display (localized) in domain axis
		Locale locale = msgs.getLocale();		
		PeriodAxis periodaxis = new PeriodAxis(null);
		Class timePeriodClass = null;
		if(dataset instanceof TimeSeriesCollection) {
			TimeSeriesCollection tsc = (TimeSeriesCollection) dataset;
			if(tsc.getSeriesCount() > 0) {
				timePeriodClass = tsc.getSeries(0).getTimePeriodClass();
			}else{
				timePeriodClass = org.jfree.data.time.Day.class;
			}
			periodaxis.setAutoRangeTimePeriodClass(timePeriodClass);
		}
        PeriodAxisLabelInfo aperiodaxislabelinfo[] = null;
        if(StatsManager.CHARTTIMESERIES_WEEKDAY.equals(timePeriod)) {
        	aperiodaxislabelinfo = new PeriodAxisLabelInfo[2];
            aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(org.jfree.data.time.Day.class, new SimpleDateFormat("E", locale));
            aperiodaxislabelinfo[1] = new PeriodAxisLabelInfo(org.jfree.data.time.Day.class, new SimpleDateFormat("d", locale));            
        }else if(StatsManager.CHARTTIMESERIES_DAY.equals(timePeriod)) {
        	aperiodaxislabelinfo = new PeriodAxisLabelInfo[3];
            aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(org.jfree.data.time.Day.class, new SimpleDateFormat("d", locale));
            aperiodaxislabelinfo[1] = new PeriodAxisLabelInfo(org.jfree.data.time.Month.class, new SimpleDateFormat("MMM", locale));
            aperiodaxislabelinfo[2] = new PeriodAxisLabelInfo(org.jfree.data.time.Year.class, new SimpleDateFormat("yyyy", locale));
        }else if(StatsManager.CHARTTIMESERIES_MONTH.equals(timePeriod)) {
        	aperiodaxislabelinfo = new PeriodAxisLabelInfo[2];
            aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(org.jfree.data.time.Month.class, new SimpleDateFormat("MMM", locale));
            aperiodaxislabelinfo[1] = new PeriodAxisLabelInfo(org.jfree.data.time.Year.class, new SimpleDateFormat("yyyy", locale));
        }else if(StatsManager.CHARTTIMESERIES_YEAR.equals(timePeriod)) {
        	aperiodaxislabelinfo = new PeriodAxisLabelInfo[1];
            aperiodaxislabelinfo[0] = new PeriodAxisLabelInfo(org.jfree.data.time.Year.class, new SimpleDateFormat("yyyy", locale));
        }
        periodaxis.setLabelInfo(aperiodaxislabelinfo);
        // date range
        if(firstDate != null || lastDate != null) {
        	periodaxis.setAutoRange(false);
        	if(firstDate != null) {
        		if(StatsManager.CHARTTIMESERIES_MONTH.equals(timePeriod) || StatsManager.CHARTTIMESERIES_YEAR.equals(timePeriod)) {
        			periodaxis.setFirst(new org.jfree.data.time.Month(firstDate));
        		}else{
        			periodaxis.setFirst(new org.jfree.data.time.Day(firstDate));
        		}
        	}
        	if(lastDate != null) {
        		if(StatsManager.CHARTTIMESERIES_MONTH.equals(timePeriod) || StatsManager.CHARTTIMESERIES_YEAR.equals(timePeriod)) {
        			periodaxis.setLast(new org.jfree.data.time.Month(lastDate));
        		}else{
        			periodaxis.setLast(new org.jfree.data.time.Day(lastDate));
        		}
        	}        	
        }
		periodaxis.setTickMarkOutsideLength(0.0F);
        plot.setDomainAxis(periodaxis);
		
		// set outline
        AbstractXYItemRenderer renderer = (AbstractXYItemRenderer) plot.getRenderer();
        if(renderer instanceof XYLineAndShapeRenderer) {	
	        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) renderer;		
	        r.setDrawSeriesLineAsPath(true);
	        r.setShapesVisible(true);
	        r.setShapesFilled(true);
        }else if(renderer instanceof XYBarRenderer) {
        	//XYBarRenderer r = (XYBarRenderer) renderer;
        	ClusteredXYBarRenderer r = new ClusteredXYBarRenderer();
        	r.setDrawBarOutline(true);
    		if(smallFontInDomainAxis && !canUseNormalFontSize(width)) 
    			r.setMargin(0.05);
    		else
    			r.setMargin(0.10);
    		plot.setRenderer(r);
    		renderer = r;
        }
		
		// item labels
		if(itemLabelsVisible) {
			plot.getRangeAxis().setUpperMargin(0.2);
			renderer.setItemLabelGenerator(new XYItemLabelGenerator() {
				private static final long	serialVersionUID	= 1L;

				public String generateLabel(XYDataset dataset, int series, int item) {
					Number n = dataset.getY(series, item);
					if(n.doubleValue() != 0)
						return n.toString();
					return "";
				}	

						
			});
			renderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			renderer.setItemLabelsVisible(true);
		}

		BufferedImage img = chart.createBufferedImage(width, height);
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
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(img, "png", out);
		}catch(IOException e){
			log.warn("Error occurred while generating SiteStats chart image data", e);
		}
		return out.toByteArray();
	}
	
	// ######################################################################################
	// Chart DataSet methods
	// ######################################################################################
	private DefaultCategoryDataset getVisitsWeekDataSet(String siteId) {
//		log.info("Generating visitsWeekDataSet");
		SummaryVisitsChartData svc = M_sm.getSummaryVisitsChartData(siteId, StatsManager.VIEW_WEEK);
		if(svc == null) return null;
		DefaultCategoryDataset visitsWeekDataSet = new DefaultCategoryDataset();
		String visits = msgs.getString("legend_visits");
		String uniqueVisits = msgs.getString("legend_unique_visitors");

		Calendar cal = Calendar.getInstance();
		cal.setTime(svc.getFirstDay());
		Calendar currDay = (Calendar) cal.clone();

		long visitsData[] = svc.getVisits();
		long uniqueVisitsData[] = svc.getUniqueVisits();
		for(int i = 0; i < visitsData.length; i++){
			visitsWeekDataSet.addValue(visitsData[i], visits, getWeekDaysMap().get(currDay.get(Calendar.DAY_OF_WEEK)));
			visitsWeekDataSet.addValue(uniqueVisitsData[i], uniqueVisits, getWeekDaysMap().get(currDay.get(Calendar.DAY_OF_WEEK)));
			currDay.add(Calendar.DAY_OF_YEAR, 1);
		}
		return visitsWeekDataSet;
	}
	
	private DefaultCategoryDataset getVisitsMonthDataSet(String siteId, int width) {
//		log.info("Generating visitsMonthDataSet");
		SummaryVisitsChartData svc = M_sm.getSummaryVisitsChartData(siteId, StatsManager.VIEW_MONTH);
		if(svc == null) return null;
		DefaultCategoryDataset visitsMonthDataSet = new DefaultCategoryDataset();
		String visits = msgs.getString("legend_visits");
		String uniqueVisits = msgs.getString("legend_unique_visitors");
		Day day;

		Calendar cal = Calendar.getInstance();
		cal.setTime(svc.getFirstDay());
		Calendar currDay = (Calendar) cal.clone();

		long visitsData[] = svc.getVisits();
		long uniqueVisitsData[] = svc.getUniqueVisits();
		for(int i = 0; i < visitsData.length; i++){
			int dayOfMonth = currDay.get(Calendar.DAY_OF_MONTH);
			if(canUseNormalFontSize(width) || (i == 0 || i == 30 - 1 || i % 2 == 0)){
				day = new Day(dayOfMonth, Integer.toString(dayOfMonth));
			}else{
				day = new Day(dayOfMonth, "");
			}
			visitsMonthDataSet.addValue(visitsData[i], visits, day);
			visitsMonthDataSet.addValue(uniqueVisitsData[i], uniqueVisits, day);
			currDay.add(Calendar.DAY_OF_MONTH, 1);
		}
		return visitsMonthDataSet;
	}
	
	private DefaultCategoryDataset getVisitsYearDataSet(String siteId) {
//		log.info("Generating visitsYearDataSet");
		SummaryVisitsChartData svc = M_sm.getSummaryVisitsChartData(siteId, StatsManager.VIEW_YEAR);
		if(svc == null) return null;
		DefaultCategoryDataset visitsYearDataSet = new DefaultCategoryDataset();
		String visits = msgs.getString("legend_visits");
		String uniqueVisits = msgs.getString("legend_unique_visitors");

		Calendar cal = Calendar.getInstance();
		cal.setTime(svc.getFirstDay());
		Calendar currMonth = (Calendar) cal.clone();

		long visitsData[] = svc.getVisits();
		long uniqueVisitsData[] = svc.getUniqueVisits();
		for(int i = 0; i < visitsData.length; i++){
			visitsYearDataSet.addValue(visitsData[i], visits, getMonthNamesMap().get(currMonth.get(Calendar.MONTH)));
			visitsYearDataSet.addValue(uniqueVisitsData[i], uniqueVisits, getMonthNamesMap().get(currMonth.get(Calendar.MONTH)));
			currMonth.add(Calendar.MONTH, 1);
		}
		return visitsYearDataSet;
	}
	
	private DefaultPieDataset getActivityWeekPieDataSet(String siteId) {
//		log.info("Generating activityWeekPieDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_PIE);
		if(sac == null) return null;
		DefaultPieDataset activityWeekPieDataSet = fillActivityPieDataSet(sac);
		return activityWeekPieDataSet;
	}
	
	private DefaultPieDataset getActivityMonthPieDataSet(String siteId) {
//		log.info("Generating activityMonthPieDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_PIE);
		if(sac == null) return null;
		DefaultPieDataset activityMonthPieDataSet = fillActivityPieDataSet(sac);
		return activityMonthPieDataSet;
	}
	
	private DefaultPieDataset getActivityYearPieDataSet(String siteId) {
//		log.info("Generating activityYearPieDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_PIE);
		if(sac == null) return null;
		DefaultPieDataset activityYearPieDataSet = fillActivityPieDataSet(sac);
		return activityYearPieDataSet;
	}
	
	private DefaultPieDataset fillActivityPieDataSet(SummaryActivityChartData sac){
		DefaultPieDataset pieDataSet = new DefaultPieDataset();
		List<SiteActivityByTool> lsac = sac.getActivityByTool();			
		int total = sac.getActivityByToolTotal();		
		int showMax = 5;
		
		for(int i=0; i<lsac.size() && i<showMax; i++){
			SiteActivityByTool s = lsac.get(i);
			double percentage = (double) s.getCount() * 100 / total;
			int decimalPlaces = 1;
			if(percentage < 0.1)
				decimalPlaces = 2;
			String label = M_ers.getToolName(s.getTool().getToolId()) + " " + Util.round(percentage, decimalPlaces) + "%";
			pieDataSet.setValue(label, percentage );
		}
		if(lsac.size() > showMax){
			int acumulated = 0;	
			String otherTools = msgs.getString("label_activity_other_tools");
			for(int i=showMax; i<lsac.size(); i++){
				SiteActivityByTool s = lsac.get(i);
				acumulated += s.getCount();
			}
			double percentage = (double) acumulated * 100 / total;
			int decimalPlaces = 1;
			if(percentage < 0.1)
				decimalPlaces = 2;
			String label = otherTools + " " + Util.round(percentage, decimalPlaces) + "%";
			pieDataSet.setValue(label, percentage );
		}
		return pieDataSet;
	}
			
	
	private DefaultCategoryDataset getActivityWeekBarDataSet(String siteId) {
//		log.info("Generating activityWeekBarDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_BAR);
		if(sac == null) return null;
		DefaultCategoryDataset activityWeekBarDataSet = new DefaultCategoryDataset();
		String activity = msgs.getString("legend_activity");

		Calendar cal = Calendar.getInstance();
		cal.setTime(sac.getFirstDay());
		Calendar currDay = (Calendar) cal.clone();

		long activityData[] = sac.getActivity();
		for(int i = 0; i < activityData.length; i++){
			activityWeekBarDataSet.addValue(activityData[i], activity, getWeekDaysMap().get(currDay.get(Calendar.DAY_OF_WEEK)));
			currDay.add(Calendar.DAY_OF_YEAR, 1);
		}
		return activityWeekBarDataSet;
	}
	
	private DefaultCategoryDataset getActivityMonthBarDataSet(String siteId, int width) {
//		log.info("Generating activityMonthBarDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_BAR);
		if(sac == null) return null;
		DefaultCategoryDataset activityMonthBarDataSet = new DefaultCategoryDataset();
		String activity = msgs.getString("legend_activity");
		Day day;

		Calendar cal = Calendar.getInstance();
		cal.setTime(sac.getFirstDay());
		Calendar currDay = (Calendar) cal.clone();

		long activityData[] = sac.getActivity();
		for(int i = 0; i < activityData.length; i++){
			int dayOfMonth = currDay.get(Calendar.DAY_OF_MONTH);
			//if(params.areAllDaysDrawable() || (i == 0 || i == 30 - 1 || i % 2 == 0)){
			if(canUseNormalFontSize(width) || (i == 0 || i == 30 - 1 || i % 2 == 0)){
				day = new Day(dayOfMonth, Integer.toString(dayOfMonth));
			}else{
				day = new Day(dayOfMonth, "");
			}
			activityMonthBarDataSet.addValue(activityData[i], activity, day);
			currDay.add(Calendar.DAY_OF_MONTH, 1);
		}
		return activityMonthBarDataSet;
	}
	
	private DefaultCategoryDataset getActivityYearBarDataSet(String siteId) {
//		log.info("Generating activityYearBarDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_YEAR, StatsManager.CHARTTYPE_BAR);
		if(sac == null) return null;
		DefaultCategoryDataset activityYearBarDataSet = new DefaultCategoryDataset();
		String activity = msgs.getString("legend_activity");

		Calendar cal = Calendar.getInstance();
		cal.setTime(sac.getFirstDay());
		Calendar currMonth = (Calendar) cal.clone();

		long activityData[] = sac.getActivity();
		for(int i = 0; i < activityData.length; i++){
			activityYearBarDataSet.addValue(activityData[i], activity, getMonthNamesMap().get(currMonth.get(Calendar.MONTH)));
			currMonth.add(Calendar.MONTH, 1);
		}
		
		return activityYearBarDataSet;
	}
	
	private AbstractDataset getCategoryDataset(Report report) {
		List<? extends Stat> reportData = report.getReportData();
		
		// fill dataset
		DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
		String dataSource = report.getReportDefinition().getReportParams().getHowChartSource();
		String categorySource = report.getReportDefinition().getReportParams().getHowChartCategorySource();
		if(StatsManager.T_NONE.equals(categorySource)){
			categorySource = null;
		}
		if(categorySource == null){
			// without categories
			Set<Comparable> usedKeys = new HashSet<Comparable>();
			for(Stat s : reportData){
				Comparable key = getStatValue(s, dataSource);
				if(key != null) {
					if(usedKeys.contains(key)){
						dataSet.incrementValue(getTotalValue(s, report).doubleValue(), key, key);						
					}else{
						dataSet.addValue(getTotalValue(s, report), key, key);
						usedKeys.add(key);
					}
				}
			}
			
		}else{
			// with categories
			Map<Comparable, Comparable> usedKeys = new HashMap<Comparable, Comparable>();
			for(Stat s : reportData){
				Comparable key = getStatValue(s, dataSource);
				Comparable cat = getStatValue(s, categorySource);
				if(key != null && cat != null) {
					if(usedKeys.containsKey(cat) && key.equals(usedKeys.get(cat))){
						dataSet.incrementValue(getTotalValue(s, report).doubleValue(), key, cat);
					}else{
						dataSet.addValue(getTotalValue(s, report), key, cat);
						usedKeys.put(cat, key);
					}
				}
			}			
			// fill missing values with zeros
			for(Comparable c : usedKeys.keySet()) {			
				for(Comparable k : usedKeys.values()) {
					if(dataSet.getValue(k, c) == null) {
						dataSet.addValue(0, k, c);
					}
				}	
			}
		}
		
		return dataSet;
	}
	
	private AbstractDataset getPieDataset(Report report) {
		List<? extends Stat> reportData = report.getReportData();
		
		// fill dataset
		DefaultPieDataset dataSet = new DefaultPieDataset();
		String dataSource = report.getReportDefinition().getReportParams().getHowChartSource();
		//int total = 0;
		double total = 0;
		for(Stat s : reportData){
			Comparable key = getStatValue(s, dataSource);
			if(key != null) {
				try{
					Number existingValue = dataSet.getValue(key);
					dataSet.setValue(key, getTotalValue(existingValue, s, report));
					total += getTotalValue(s, report).doubleValue();
				}catch(UnknownKeyException e){
					dataSet.setValue(key, getTotalValue(s, report));
					total += getTotalValue(s, report).doubleValue();
				}
			}
		}

		// sort
		dataSet.sortByValues(SortOrder.DESCENDING);
		
		// fill in final key values in dataset
		// show only top values (aggregate remaining in 'others')
		int maxDisplayedItems = 10;
		int currItem = 1;
		Number othersValues = Integer.valueOf(0);
		List<Comparable> keys = dataSet.getKeys();
		for(Comparable key : keys) {
			Number existingValue = dataSet.getValue(key);
			if(currItem < maxDisplayedItems) {
				// re-compute values
				double percentage = (double) existingValue.doubleValue() * 100 / total;
				double valuePercentage = Util.round(percentage, (percentage > 0.1) ? 1 : 2 );
				// replace key with updated label
				StringBuilder keyStr = new StringBuilder(key.toString());
				keyStr.append(' ');
				keyStr.append(valuePercentage);
				keyStr.append("% (");
				if((double)existingValue.intValue() == existingValue.doubleValue()) keyStr.append(existingValue.intValue());
				else keyStr.append(existingValue.doubleValue());
				keyStr.append(")");
				dataSet.remove(key);
				dataSet.setValue(keyStr.toString(), existingValue);
			}else{
				othersValues = Integer.valueOf( othersValues.intValue() + existingValue.intValue() );
				dataSet.remove(key);
			}
			currItem++;
		}
		// compute "Others" value
		if(othersValues.intValue() > 0){
			double percentage = (double) othersValues.doubleValue() * 100 / total;
			double valuePercentage = Util.round(percentage, (percentage > 0.1) ? 1 : 2 );
			// replace key with updated label
			StringBuilder keyStr = new StringBuilder(msgs.getString("pie_chart_others"));
			keyStr.append(' ');
			keyStr.append(valuePercentage);
			keyStr.append("% (");
			if((double)othersValues.intValue() == othersValues.doubleValue()) keyStr.append(othersValues.intValue());
			else keyStr.append(othersValues.doubleValue());
			keyStr.append(")");
			dataSet.setValue(keyStr.toString(), othersValues);
		}
		
		return dataSet;
	}
	
	private AbstractDataset getTimeSeriesCollectionDataset(Report report) {
		List<? extends Stat> reportData = report.getReportData();
		
		// fill dataset
		TimeSeriesCollection dataSet = new TimeSeriesCollection();
		String dataSource = report.getReportDefinition().getReportParams().getHowChartSource();
		String seriesFrom = report.getReportDefinition().getReportParams().getHowChartSeriesSource();
		if(StatsManager.T_TOTAL.equals(seriesFrom) || StatsManager.T_NONE.equals(seriesFrom)){
			seriesFrom = null;
		}
		Class periodGrouping = null;
		if(StatsManager.CHARTTIMESERIES_DAY.equals(report.getReportDefinition().getReportParams().getHowChartSeriesPeriod())
			|| StatsManager.CHARTTIMESERIES_WEEKDAY.equals(report.getReportDefinition().getReportParams().getHowChartSeriesPeriod())) {
			periodGrouping = org.jfree.data.time.Day.class;
		}else if(StatsManager.CHARTTIMESERIES_MONTH.equals(report.getReportDefinition().getReportParams().getHowChartSeriesPeriod())) {
			periodGrouping = org.jfree.data.time.Month.class;
		}else if(StatsManager.CHARTTIMESERIES_YEAR.equals(report.getReportDefinition().getReportParams().getHowChartSeriesPeriod())) {
			periodGrouping = org.jfree.data.time.Year.class;
		}
		boolean visitsTotalsChart = 
			ReportManager.WHAT_VISITS_TOTALS.equals(report.getReportDefinition().getReportParams().getWhat())
			|| report.getReportDefinition().getReportParams().getHowTotalsBy().contains(StatsManager.T_VISITS)
			|| report.getReportDefinition().getReportParams().getHowTotalsBy().contains(StatsManager.T_UNIQUEVISITS);
		Set<RegularTimePeriod> keys = new HashSet<RegularTimePeriod>();
		if(!visitsTotalsChart && seriesFrom == null){
			// without additional series
			String name = msgs.getString("th_total"); 
			TimeSeries ts = new TimeSeries(name, periodGrouping);		
			for(Stat s : reportData){
				RegularTimePeriod key = (RegularTimePeriod) getStatValue(s, dataSource, periodGrouping);
				if(key != null) {
					Number existing = null;
					if((existing = ts.getValue(key)) == null) {
						ts.add(key, getTotalValue(s, report));
					}else{
						ts.addOrUpdate(key, getTotalValue(existing, s, report));
					}
					keys.add(key);
				}
			}
			dataSet.addSeries(ts);
		}else if(!visitsTotalsChart && seriesFrom != null){
			// with additional series
			Map<Comparable,TimeSeries> series = new HashMap<Comparable,TimeSeries>();
			//TimeSeries ts = new TimeSeries(dataSource, org.jfree.data.time.Day.class);
			for(Stat s : reportData){
				RegularTimePeriod key = (RegularTimePeriod) getStatValue(s, dataSource, periodGrouping);
				Comparable serie = (Comparable) getStatValue(s, seriesFrom);
				
				if(key != null && serie != null) {
					// determine appropriate serie
					TimeSeries ts = null;
					if(!series.containsKey(serie)) {
						ts = new TimeSeries(serie.toString(), periodGrouping);
						series.put(serie, ts);
					}else{
						ts = series.get(serie);
					}
					
					Number existing = null;
					if((existing = ts.getValue(key)) == null) {
						ts.add(key, getTotalValue(s, report));
					}else{
						ts.addOrUpdate(key, getTotalValue(existing, s, report));
					}
					keys.add(key);
				}
			}
			
			// add series
			for(TimeSeries ts : series.values()) {
				dataSet.addSeries(ts);
			}
		}else if(visitsTotalsChart){
			// 2 series: visits & unique visitors
			TimeSeries tsV = new TimeSeries(msgs.getString("th_visits"), periodGrouping);
			TimeSeries tsUV = new TimeSeries(msgs.getString("th_uniquevisitors"), periodGrouping);	
			for(Stat _s : reportData){
				SiteVisits s = (SiteVisits) _s;
				RegularTimePeriod key = (RegularTimePeriod) getStatValue(s, dataSource, periodGrouping);
				if(key != null) {
					Number existing = null;
					if((existing = tsV.getValue(key)) == null) {
						tsV.add(key, s.getTotalVisits());
						tsUV.add(key, s.getTotalUnique());
					}else{
						tsV.addOrUpdate(key, s.getTotalVisits() + existing.longValue());
						tsUV.addOrUpdate(key, s.getTotalVisits() + existing.longValue());
					}
					keys.add(key);
				}
			}
			dataSet.addSeries(tsV);
			dataSet.addSeries(tsUV);
		}
		
		// fill missing values with zeros
		/*for(TimeSeries ts : (List<TimeSeries>) dataSet.getSeries()) {
			for(RegularTimePeriod tp : keys) {
				if(ts.getValue(tp) == null) {
					ts.add(tp, 0.0);
				}
			}
		}*/
		dataSet.setXPosition(TimePeriodAnchor.MIDDLE);

		return dataSet;
	}
	

	// ################################################################
	// Utility Methods
	// ################################################################
	private static class Day implements Comparable<Object> {
		private String dayLabel;
		private int day;
		public Day(int day, String dayLabel) {
			this.day = day;
			this.dayLabel = dayLabel;
		}
		public int getDay() {
			return this.day;
		}
		public String toString() {
			return this.dayLabel;
		}
		public int compareTo(Object o) {
			if(!(o instanceof Day))
				return -1;
			int otherDay = ((Day) o).getDay();
			if(day < otherDay)
				return -1;
			else if(day > otherDay)
				return 1;
			else return 0;
		}
		public boolean equals(Object o) {
			if(!(o instanceof Day))
				return false;
			int otherDay = ((Day) o).getDay();
			if(day == otherDay)
				return true;
			else
				return false;
		}	
		public int hashCode() {
			return getDay();
		}
	}
	
	private Map<Integer, String> getWeekDaysMap(){
		weekDaysMap = new HashMap<Integer, String>();
		weekDaysMap.put(Calendar.SUNDAY, msgs.getString("day_sun"));
		weekDaysMap.put(Calendar.MONDAY, msgs.getString("day_mon"));
		weekDaysMap.put(Calendar.TUESDAY, msgs.getString("day_tue"));
		weekDaysMap.put(Calendar.WEDNESDAY, msgs.getString("day_wed"));
		weekDaysMap.put(Calendar.THURSDAY, msgs.getString("day_thu"));
		weekDaysMap.put(Calendar.FRIDAY, msgs.getString("day_fri"));
		weekDaysMap.put(Calendar.SATURDAY, msgs.getString("day_sat"));
		return weekDaysMap;
	}
	
	private Map<Integer, String> getMonthNamesMap(){
		monthNamesMap = new HashMap<Integer, String>();
		monthNamesMap.put(Calendar.JANUARY, msgs.getString("mo_jan"));
		monthNamesMap.put(Calendar.FEBRUARY, msgs.getString("mo_feb"));
		monthNamesMap.put(Calendar.MARCH, msgs.getString("mo_mar"));
		monthNamesMap.put(Calendar.APRIL, msgs.getString("mo_apr"));
		monthNamesMap.put(Calendar.MAY, msgs.getString("mo_may"));
		monthNamesMap.put(Calendar.JUNE, msgs.getString("mo_jun"));
		monthNamesMap.put(Calendar.JULY, msgs.getString("mo_jul"));
		monthNamesMap.put(Calendar.AUGUST, msgs.getString("mo_ago"));
		monthNamesMap.put(Calendar.SEPTEMBER, msgs.getString("mo_sep"));
		monthNamesMap.put(Calendar.OCTOBER, msgs.getString("mo_oct"));
		monthNamesMap.put(Calendar.NOVEMBER, msgs.getString("mo_nov"));
		monthNamesMap.put(Calendar.DECEMBER, msgs.getString("mo_dec"));
		return monthNamesMap;
	}
	
	private boolean canUseNormalFontSize(int chartWidth) {
		return chartWidth >= MIN_CHART_WIDTH_TO_DRAW_ALL_DAYS;
	}
	
	private static Color parseColor(String color) {
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
	//periodGrouping

	private Comparable getStatValue(Stat s, String fieldCode) {
		return getStatValue(s, fieldCode, org.jfree.data.time.Day.class);
	}
	private Comparable getStatValue(Stat s, String fieldCode, Class periodGrouping) {
		if(fieldCode == null) {
			return null;
		}
		try{
			if(fieldCode.equals(StatsManager.T_SITE)) {
				String siteId = s.getSiteId();
				String title = null;
				try{
					title = M_ss.getSite(siteId).getTitle();
				}catch(IdUnusedException e){
					title = siteId;
				}
				return title;
			}else if(fieldCode.equals(StatsManager.T_USER)) {
				String userId = s.getUserId();
				String name = null;
				if (userId != null) {
					if(("-").equals(userId)) {
						name = msgs.getString("user_anonymous");
					}else if(EventTrackingService.UNKNOWN_USER.equals(userId)) {
						name = msgs.getString("user_anonymous_access");
					}else{
						name = M_sm.getUserNameForDisplay(userId);
					}
				}else{
					name = msgs.getString("user_unknown");
				}
				return name;
			}else if(fieldCode.equals(StatsManager.T_EVENT)) {
				String eventName = "";
				if(s instanceof EventStat) {
					String eventId = ((EventStat) s).getEventId();
					if(!"".equals(eventId)){
						eventName = M_ers.getEventName(eventId);
					}
				}
				return eventName;
			}else if(fieldCode.equals(StatsManager.T_TOOL)) {
				String toolName = "";
				if(s instanceof EventStat) {
					String toolId = ((EventStat) s).getToolId();
					if(!"".equals(toolId)){
						toolName = M_ers.getToolName(toolId);
					}
				}
				return toolName;
			}else if(fieldCode.equals(StatsManager.T_RESOURCE)) {
				if(s instanceof ResourceStat) {
					String ref = ((ResourceStat) s).getResourceRef();
					String resName = M_sm.getResourceName(ref);
					return resName != null ? resName : msgs.getString("resource_unknown");
				}else{
					return "";
				}
			}else if(fieldCode.equals(StatsManager.T_RESOURCE_ACTION)) {
				String action = "";
				if(s instanceof ResourceStat) {
					String refAction = ((ResourceStat) s).getResourceAction();
					if(refAction == null){
						action = "";
					}else{
						if(!"".equals(refAction.trim()))
							action = msgs.getString("action_"+refAction);
					}
				}
				return action;
			}else if(fieldCode.equals(StatsManager.T_PAGE)) {
				if(s instanceof LessonBuilderStat) {
					return M_sm.getLessonPageTitle(((LessonBuilderStat)s).getPageId());
				}else{
					return "";
				}
            }else if(fieldCode.equals(StatsManager.T_PAGE_ACTION)) {
				String action = "";
				if(s instanceof LessonBuilderStat) {
					String refAction = ((LessonBuilderStat) s).getPageAction();
					if(refAction == null){
						action = "";
					}else{
						if(!"".equals(refAction.trim()))
							action = msgs.getString("action_"+refAction);
					}
				}
				return action;

			}else if(fieldCode.equals(StatsManager.T_DATE) || fieldCode.equals(StatsManager.T_LASTDATE)
					|| fieldCode.equals(StatsManager.T_DATEMONTH) || fieldCode.equals(StatsManager.T_DATEYEAR)) {
				Date d = s.getDate();
				if(d != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					if(org.jfree.data.time.Year.class.equals(periodGrouping)) {
						return new org.jfree.data.time.Year(c.getTime());
					}else if(org.jfree.data.time.Month.class.equals(periodGrouping)) {
						return new org.jfree.data.time.Month(c.getTime());
					}else if(org.jfree.data.time.Week.class.equals(periodGrouping)) {
						return new org.jfree.data.time.Week(c.getTime());
					}else{
						return new org.jfree.data.time.Day(c.getTime());
					}
				}else{
					return null;
				}
			}else if(fieldCode.equals(StatsManager.T_TOTAL)) {
				return s.getCount();
			}else if(fieldCode.equals(StatsManager.T_VISITS)) {
				if(s instanceof SiteVisits) {
					return ((SiteVisits) s).getTotalVisits();
				}
				return s.getCount();
			}else if(fieldCode.equals(StatsManager.T_UNIQUEVISITS)) {
				if(s instanceof SiteVisits) {
					return ((SiteVisits) s).getTotalUnique();
				}
				return s.getCount();
			}else if(fieldCode.equals(StatsManager.T_DURATION)) {
				if(s instanceof SitePresence) {
					double duration = (double) ((SitePresence) s).getDuration();
					return Util.round(duration / 1000 / 60, 1); // in minutes
				}
			}
		}catch(Exception e) {
			log.warn("Error occurred while getting value for chart", e);			
		}
		return null;
	}
	
	
	private Number getTotalValue(Stat s, Report r) {
		return getTotalValue(null, s, r);
	}
	private Number getTotalValue(Number existingValue, Stat s, Report r) {
		try{
			String what = r.getReportDefinition().getReportParams().getWhat();
			if(ReportManager.WHAT_VISITS_TOTALS.equals(what)) {
				if(s instanceof SiteVisits) {
					long totalVisits = ((SiteVisits) s).getTotalVisits();
					if(existingValue != null) {
						totalVisits += existingValue.longValue();
					}
					return totalVisits;
				}
				
			}else if(ReportManager.WHAT_PRESENCES.equals(what)) {
				if(s instanceof SitePresence) {
					double duration = (double) ((SitePresence) s).getDuration();
					duration = Util.round(duration / 1000 / 60, 1); // in minutes
					if(existingValue != null) {
						duration += existingValue.doubleValue();
					}
					return duration;
				}
			}
			long count = s.getCount();
			if(existingValue != null) {
				count += existingValue.longValue();
			}
			return count;
		}catch(Exception e) {
			log.warn("Error occurred while getting total value for chart", e);			
		}
		return null;
	}
	
}
