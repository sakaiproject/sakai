package org.sakaiproject.sitestats.impl.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.chart.ChartService;
import org.sakaiproject.sitestats.impl.event.EventRegistryServiceImpl;
import org.sakaiproject.util.ResourceLoader;


public class ChartServiceImpl implements ChartService {
	/** Static fields */
	private static Log				LOG									= LogFactory.getLog(EventRegistryServiceImpl.class);
	private static ResourceLoader	msgs								= new ResourceLoader("Messages");
	private static final int		MIN_CHART_WIDTH_TO_DRAW_ALL_DAYS	= 640;

	/** Utility */
	private Map<Integer, String>	weekDaysMap							= null;
	private Map<Integer, String>	monthNamesMap						= null;

	/** Sakai services */
	private StatsManager			M_sm;

	// ################################################################
	// Spring methods
	// ################################################################
	public void setStatsManager(StatsManager statsManager) {
		this.M_sm = statsManager;
	}
	

	// ################################################################
	// Chart Service methods 
	// ################################################################
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateVisitsChart(java.lang.String, java.lang.String, int, int, boolean, float, boolean)
	 */
	public BufferedImage generateVisitsChart(
			String siteId, String viewType,
			int width, int height, 
			boolean render3d, float transparency, boolean itemLabelsVisible) {
		
		CategoryDataset dataset = null;
		boolean smallFontInDomainAxis = false;
		if(viewType.equals(StatsManager.VIEW_WEEK)) {
			dataset = getVisitsWeekDataSet(siteId);
		}else if(viewType.equals(StatsManager.VIEW_MONTH)) {
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
	public BufferedImage generateActivityChart(
			String siteId, String viewType, String chartType, 
			int width, int height, 
			boolean render3d, float transparency, boolean itemLabelsVisible) {
		
		boolean smallFontInDomainAxis = false;
		if(chartType.equals(StatsManager.CHARTTYPE_PIE)) {
			DefaultPieDataset dataset = null;
			if(viewType.equals(StatsManager.VIEW_WEEK))
				dataset = getActivityWeekPieDataSet(siteId);
			else if(viewType.equals(StatsManager.VIEW_MONTH))
				dataset = getActivityMonthPieDataSet(siteId);
			else 
				dataset = getActivityYearPieDataSet(siteId);
			if(dataset != null)
				return generatePieChart(siteId, dataset, width, height, render3d, transparency, smallFontInDomainAxis);
			else
				return generateNoDataChart(width, height);
		}else{
			CategoryDataset dataset = null;
			if(viewType.equals(StatsManager.VIEW_WEEK))
				dataset = getActivityWeekBarDataSet(siteId);
			else if(viewType.equals(StatsManager.VIEW_MONTH)){
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
	 * @see org.sakaiproject.sitestats.api.chart.ChartService#generateChart(java.lang.String, java.lang.Object, java.lang.String, int, int, boolean, float, boolean)
	 */
	public BufferedImage generateChart(
			String siteId, Object dataset, String chartType,
			int width, int height,
			boolean render3d, float transparency,
			boolean itemLabelsVisible) {
		if(chartType.equals(StatsManager.CHARTTYPE_BAR)) {
			if(dataset instanceof CategoryDataset) {
				CategoryDataset ds = (CategoryDataset) dataset;
				return generateBarChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
			}else{
				LOG.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
			}				
		}else if(chartType.equals(StatsManager.CHARTTYPE_LINE)) {
			if(dataset instanceof CategoryDataset) {
				CategoryDataset ds = (CategoryDataset) dataset;
				return generateLineChart(siteId, ds, width, height, render3d, transparency, itemLabelsVisible, false);
			}else{
				LOG.warn("Dataset not supported for "+chartType+" chart type: only classes implementing CategoryDataset are supported.");
			}
		}else if(chartType.equals(StatsManager.CHARTTYPE_PIE)) {
			if(dataset instanceof PieDataset) {
				PieDataset ds = (PieDataset) dataset;
				return generatePieChart(siteId, ds, width, height, render3d, transparency, false);
			}else{
				LOG.warn("Dataset not supported for "+chartType+" chart type: only classes implementing PieDataset are supported.");
			}
		}
		
		LOG.warn("Chart type "+chartType+" not supprted: only line, bar, pie are suuported.");
		return null;
	}
	
	
	// ######################################################################################
	// Chart Generation methods
	// ######################################################################################
	private BufferedImage generateBarChart(
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
					if(n.intValue() != 0)
						return n.intValue()+"";
					return "";
				}			
			});
			barrenderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			barrenderer.setItemLabelsVisible(true);
		}

		BufferedImage img = chart.createBufferedImage(width, height);
		return img;
	}
	
	private BufferedImage generateLineChart(
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
					if(n.intValue() != 0)
						return n.intValue()+"";
					return "";
				}			
			});
			barrenderer.setItemLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			barrenderer.setItemLabelsVisible(true);
		}

		BufferedImage img = chart.createBufferedImage(width, height);
		return img;
	}
	
	private BufferedImage generatePieChart(
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
		return img;
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
	
	// ######################################################################################
	// Chart DataSet methods
	// ######################################################################################
	private DefaultCategoryDataset getVisitsWeekDataSet(String siteId) {
//		LOG.info("Generating visitsWeekDataSet");
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
//		LOG.info("Generating visitsMonthDataSet");
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
//		LOG.info("Generating visitsYearDataSet");
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
//		LOG.info("Generating activityWeekPieDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_WEEK, StatsManager.CHARTTYPE_PIE);
		if(sac == null) return null;
		DefaultPieDataset activityWeekPieDataSet = fillActivityPieDataSet(sac);
		return activityWeekPieDataSet;
	}
	
	private DefaultPieDataset getActivityMonthPieDataSet(String siteId) {
//		LOG.info("Generating activityMonthPieDataSet");
		SummaryActivityChartData sac = M_sm.getSummaryActivityChartData(siteId, StatsManager.VIEW_MONTH, StatsManager.CHARTTYPE_PIE);
		if(sac == null) return null;
		DefaultPieDataset activityMonthPieDataSet = fillActivityPieDataSet(sac);
		return activityMonthPieDataSet;
	}
	
	private DefaultPieDataset getActivityYearPieDataSet(String siteId) {
//		LOG.info("Generating activityYearPieDataSet");
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
			String label = s.getTool().getToolName() + " " + round(percentage, decimalPlaces) + "%";
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
			String label = otherTools + " " + round(percentage, decimalPlaces) + "%";
			pieDataSet.setValue(label, percentage );
		}
		return pieDataSet;
	}
			
	
	private DefaultCategoryDataset getActivityWeekBarDataSet(String siteId) {
//		LOG.info("Generating activityWeekBarDataSet");
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
//		LOG.info("Generating activityMonthBarDataSet");
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
//		LOG.info("Generating activityYearBarDataSet");
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
		LOG.info("Unable to parse body background-color (color:" + color+"). Assuming white.");
		return Color.white;
	}
	
	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		long tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return (double) tmp / factor;
	}
}
