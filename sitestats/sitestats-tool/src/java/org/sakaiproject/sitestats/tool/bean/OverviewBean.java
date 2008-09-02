/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.tool.bean;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;

import org.ajax4jsf.framework.resource.ImageRenderer;
import org.ajax4jsf.framework.resource.PngRenderer;
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
import org.jfree.ui.RectangleInsets;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.util.ResourceLoader;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class OverviewBean {

	/** Our log (commons). */
	private static Log							LOG						= LogFactory.getLog(OverviewBean.class);

	/** Resource bundle */
	private static String						bundleName				= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private static ResourceLoader				msgs					= new ResourceLoader(bundleName);

	/** Rendering control vars */
	private boolean								renderVisitsTable		= false;
	private boolean								renderActivityTable		= false;

	/** Chart DataSets */
	private DefaultCategoryDataset				visitsWeekDataSet		= null;
	private DefaultCategoryDataset				visitsMonthDataSet		= null;
	private DefaultCategoryDataset				visitsYearDataSet		= null;
	private DefaultPieDataset					activityWeekPieDataSet	= null;
	private DefaultPieDataset					activityMonthPieDataSet	= null;
	private DefaultPieDataset					activityYearPieDataSet	= null;
	private DefaultCategoryDataset				activityWeekBarDataSet	= null;
	private DefaultCategoryDataset				activityMonthBarDataSet	= null;
	private DefaultCategoryDataset				activityYearBarDataSet	= null;

	/** Summary tables objects */
	private SummaryVisitsTotals					summaryVisitsTotals		= null;
	private SummaryActivityTotals				summaryActivityTotals	= null;

	/** Utility */
	private final Map<Integer, String>			weekDaysMap				= new HashMap<Integer, String>();
	private final Map<Integer, String>			monthNamesMap			= new HashMap<Integer, String>();

	/** Benas, Services */
	private transient ServiceBean				serviceBean				= null;

	/** Other */
	private String								previousSiteId			= "";
	private PrefsData							prefsdata				= null;
	private long								prefsLastModified		= 0;
	
	
	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################	
	public void setServiceBean(ServiceBean serviceBean){
		this.serviceBean = serviceBean;
	}	
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	public OverviewBean() {
	}
	
	private PrefsData getPrefsdata(String siteId) {
		if(siteId == null)
			siteId = serviceBean.getSiteId();
		if(prefsdata == null || prefsLastModified < serviceBean.getPreferencesLastModified() || !previousSiteId.equals(siteId)){
			previousSiteId = siteId;
			prefsdata = serviceBean.getSstStatsManager().getPreferences(siteId, false);
			prefsLastModified = serviceBean.getPreferencesLastModified();
		}
		return prefsdata;
	}
	
	// ######################################################################################
	// General Chart methods
	// ######################################################################################
	public void generateVisitsChart(OutputStream out, Object data) throws IOException {
		ChartParamsBean params = null;
		if(data instanceof ChartParamsBean)
			 params = (ChartParamsBean) data;
		else{
			LOG.warn("data NOT instanceof ChartParamsBean!");
			return;
		}
		
		boolean useSmallFontInDomainAxis = false;
		CategoryDataset dataset = null;
		if(params.getSelectedVisitsView().equals(ChartParamsBean.VIEW_WEEK))
			dataset = getVisitsWeekDataSet(params);
		else if(params.getSelectedVisitsView().equals(ChartParamsBean.VIEW_MONTH)){
			dataset = getVisitsMonthDataSet(params);
			useSmallFontInDomainAxis = true;
		}else 
			dataset = getVisitsYearDataSet(params);
		
		if(dataset != null)
			generateBarChart(dataset, params, useSmallFontInDomainAxis, out, data);
		else
			generateNoDataChart(params, out);
	}
	
	public void generateActivityChart(OutputStream out, Object data) throws IOException {
		ChartParamsBean params = null;
		if(data instanceof ChartParamsBean)
			 params = (ChartParamsBean) data;
		else{
			LOG.warn("data NOT instanceof ChartParamsBean!");
			return;
		}
		
		boolean useSmallFontInDomainAxis = false;
		if(params.getSelectedActivityChartType().equals(ChartParamsBean.CHATTYPE_PIE)) {
			DefaultPieDataset dataset = null;
			if(params.getSelectedActivityView().equals(ChartParamsBean.VIEW_WEEK))
				dataset = getActivityWeekPieDataSet(params);
			else if(params.getSelectedActivityView().equals(ChartParamsBean.VIEW_MONTH))
				dataset = getActivityMonthPieDataSet(params);
			else 
				dataset = getActivityYearPieDataSet(params);
			if(dataset != null)
				generatePieChart(dataset, params, out, data);
			else
				generateNoDataChart(params, out);
		}else{
			CategoryDataset dataset = null;
			if(params.getSelectedActivityView().equals(ChartParamsBean.VIEW_WEEK))
				dataset = getActivityWeekBarDataSet(params);
			else if(params.getSelectedActivityView().equals(ChartParamsBean.VIEW_MONTH)){
				dataset = getActivityMonthBarDataSet(params);
				useSmallFontInDomainAxis = true;
			}else 
				dataset = getActivityYearBarDataSet(params);
			if(dataset != null)
				generateBarChart(dataset, params, useSmallFontInDomainAxis, out, data);
			else
				generateNoDataChart(params, out);
		}
	}
	
	private void generateBarChart(CategoryDataset dataset, ChartParamsBean params, boolean useSmallFontInDomainAxis, OutputStream out, Object data) throws IOException {
		JFreeChart chart = null;
		if(getPrefsdata(params.getSiteId()).isChartIn3D())
			chart = ChartFactory.createBarChart3D(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);
		else
			chart = ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		
		// set transparency
		plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());
		
		// set background
		chart.setBackgroundPaint(parseColor(serviceBean.getSstStatsManager().getChartBackgroundColor()));
		
		// set chart border
		chart.setPadding(new RectangleInsets(10,5,5,5));
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// set antialias
		chart.setAntiAlias(true);
		
		// set domain axis font size
		if(useSmallFontInDomainAxis && (!params.isMaximizedVisits() && !params.isMaximizedActivity())){
			plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
			plot.getDomainAxis().setCategoryMargin(0.05);
		}

		// set bar outline
		BarRenderer barrenderer = (BarRenderer) plot.getRenderer();
		barrenderer.setDrawBarOutline(true);
		if(useSmallFontInDomainAxis && (!params.isMaximizedVisits() && !params.isMaximizedActivity()))
			barrenderer.setItemMargin(0.05);
		else
			barrenderer.setItemMargin(0.10);
		
		// item labels
		if(getPrefsdata(params.getSiteId()).isItemLabelsVisible()) {
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
		   
		
		BufferedImage img = chart.createBufferedImage(params.getChartWidth(), params.getChartHeight());
		try {
			ImageIO.write(img, "png", out);
		} catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn("Data transfer aborted by client.");
		}
	}
	
	private void generatePieChart(DefaultPieDataset dataset, ChartParamsBean params, OutputStream out, Object data) throws IOException {
		JFreeChart chart = null;
		if(getPrefsdata(params.getSiteId()).isChartIn3D())
			chart = ChartFactory.createPieChart3D(null, dataset, false, true, false);
		else
			chart = ChartFactory.createPieChart(null, dataset, false, true, false);
		PiePlot plot = (PiePlot) chart.getPlot();
		
		// set start angle (135 or 150 deg so minor data has more space on the left)
		plot.setStartAngle(150D);
		
		// set transparency
		plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());
		
		// set background
		chart.setBackgroundPaint(parseColor(serviceBean.getSstStatsManager().getChartBackgroundColor()));
		plot.setBackgroundPaint(parseColor(serviceBean.getSstStatsManager().getChartBackgroundColor()));
		
		// fix border offset		
		chart.setPadding(new RectangleInsets(5,5,5,5));
		plot.setInsets(new RectangleInsets(1,1,1,1));
		// set chart border
		plot.setOutlinePaint(null);
		chart.setBorderVisible(true);
		chart.setBorderPaint(parseColor("#cccccc"));
		
		// set antialias
		chart.setAntiAlias(true);
		
		BufferedImage img = chart.createBufferedImage(params.getChartWidth(), params.getChartHeight());
		try {
			ImageIO.write(img, "png", out);
		} catch (Exception e) {
			// Load cancelled by user
			// Do nothing.
			LOG.warn("Data transfer aborted by client.");
		}
	}
	
	private void generateNoDataChart(ChartParamsBean params, OutputStream out) throws IOException {
		ImageRenderer imgR = new PngRenderer();
		BufferedImage img = imgR.createImage(params.getChartWidth(), params.getChartHeight());
		Graphics2D g2d = img.createGraphics();
		
		g2d.setBackground(parseColor(serviceBean.getSstStatsManager().getChartBackgroundColor()));
		g2d.clearRect(0, 0, params.getChartWidth()-1, params.getChartHeight()-1);
		g2d.setColor(parseColor("#cccccc"));
		g2d.drawRect(0, 0, params.getChartWidth()-1, params.getChartHeight()-1);
		Font f = new Font("SansSerif", Font.PLAIN, 12);
		g2d.setFont(f);
		FontMetrics fm = g2d.getFontMetrics(f);
		String noData = msgs.getString("no_data");
		int noDataWidth = fm.stringWidth(noData);
		int noDataHeight = fm.getHeight();
		g2d.setColor(parseColor("#555555"));
		g2d.drawString(noData, params.getChartWidth()/2 - noDataWidth/2, params.getChartHeight()/2 - noDataHeight/2 + 2);
		
		try {
			ImageIO.write(img, "png", out);
		} catch (Exception e) {
			// Load canceled by user
			// Do nothing.
			LOG.warn("Data transfer aborted by client.");
		}
	}
	
	
	// ######################################################################################
	// Chart DataSet methods
	// ######################################################################################
	
	private DefaultCategoryDataset getVisitsWeekDataSet(ChartParamsBean params) {
//		LOG.info("Generating visitsWeekDataSet");
		SummaryVisitsChartData svc = serviceBean.getSstStatsManager().getSummaryVisitsChartData(params.getSiteId(), StatsManager.VIEW_WEEK);
		if(svc == null) return null;
		visitsWeekDataSet = new DefaultCategoryDataset();
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
	
	private DefaultCategoryDataset getVisitsMonthDataSet(ChartParamsBean params) {
//		LOG.info("Generating visitsMonthDataSet");
		SummaryVisitsChartData svc = serviceBean.getSstStatsManager().getSummaryVisitsChartData(params.getSiteId(), StatsManager.VIEW_MONTH);
		if(svc == null) return null;
		visitsMonthDataSet = new DefaultCategoryDataset();
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
			if(params.areAllDaysDrawable() || (i == 0 || i == 30 - 1 || i % 2 == 0)){
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
	
	private DefaultCategoryDataset getVisitsYearDataSet(ChartParamsBean params) {
//		LOG.info("Generating visitsYearDataSet");
		SummaryVisitsChartData svc = serviceBean.getSstStatsManager().getSummaryVisitsChartData(params.getSiteId(), StatsManager.VIEW_YEAR);
		if(svc == null) return null;
		visitsYearDataSet = new DefaultCategoryDataset();
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
	
	private DefaultPieDataset getActivityWeekPieDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityWeekPieDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_WEEK, StatsManager.CHATTYPE_PIE);
		if(sac == null) return null;
		activityWeekPieDataSet = fillActivityPieDataSet(sac);
		return activityWeekPieDataSet;
	}
	
	private DefaultPieDataset getActivityMonthPieDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityMonthPieDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_MONTH, StatsManager.CHATTYPE_PIE);
		if(sac == null) return null;
		activityMonthPieDataSet = fillActivityPieDataSet(sac);
		return activityMonthPieDataSet;
	}
	
	private DefaultPieDataset getActivityYearPieDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityYearPieDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_YEAR, StatsManager.CHATTYPE_PIE);
		if(sac == null) return null;
		activityYearPieDataSet = fillActivityPieDataSet(sac);
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
			
	
	private DefaultCategoryDataset getActivityWeekBarDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityWeekBarDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_WEEK, StatsManager.CHATTYPE_BAR);
		if(sac == null) return null;
		activityWeekBarDataSet = new DefaultCategoryDataset();
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
	
	private DefaultCategoryDataset getActivityMonthBarDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityMonthBarDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_MONTH, StatsManager.CHATTYPE_BAR);
		if(sac == null) return null;
		activityMonthBarDataSet = new DefaultCategoryDataset();
		String activity = msgs.getString("legend_activity");
		Day day;

		Calendar cal = Calendar.getInstance();
		cal.setTime(sac.getFirstDay());
		Calendar currDay = (Calendar) cal.clone();

		long activityData[] = sac.getActivity();
		for(int i = 0; i < activityData.length; i++){
			int dayOfMonth = currDay.get(Calendar.DAY_OF_MONTH);
			if(params.areAllDaysDrawable() || (i == 0 || i == 30 - 1 || i % 2 == 0)){
				day = new Day(dayOfMonth, Integer.toString(dayOfMonth));
			}else{
				day = new Day(dayOfMonth, "");
			}
			activityMonthBarDataSet.addValue(activityData[i], activity, day);
			currDay.add(Calendar.DAY_OF_MONTH, 1);
		}
		return activityMonthBarDataSet;
	}
	
	private DefaultCategoryDataset getActivityYearBarDataSet(ChartParamsBean params) {
//		LOG.info("Generating activityYearBarDataSet");
		SummaryActivityChartData sac = serviceBean.getSstStatsManager().getSummaryActivityChartData(params.getSiteId(), StatsManager.VIEW_YEAR, StatsManager.CHATTYPE_BAR);
		if(sac == null) return null;
		activityYearBarDataSet = new DefaultCategoryDataset();
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

	
	// ######################################################################################
	// Summary tables methods
	// ######################################################################################
	public SummaryVisitsTotals getSummaryVisitsTotals(){
		summaryVisitsTotals = serviceBean.getSstStatsManager().getSummaryVisitsTotals(serviceBean.getSiteId());
		return summaryVisitsTotals;
	}
	
	public SummaryActivityTotals getSummaryActivityTotals(){
		summaryActivityTotals = serviceBean.getSstStatsManager().getSummaryActivityTotals(serviceBean.getSiteId());//, getPrefsdata());
		return summaryActivityTotals;
	}
	
	// ######################################################################################
	// Table render methods
	// ######################################################################################
	public boolean isRenderVisitsTable() {
		return renderVisitsTable;
	}
	
	public void renderVisitsTable(ActionEvent e) {
		this.renderVisitsTable = true;
	}
	
	public boolean isRenderActivityTable() {
		return renderActivityTable;
	}
	
	public void renderActivityTable(ActionEvent e) {
		this.renderActivityTable = true;
	}
	
	public void setAllRenderFalse(ActionEvent e) {
		this.renderVisitsTable = false;
		this.renderActivityTable = false;		
	}
	

	// ######################################################################################
	// Utility methods
	// ######################################################################################
//	private ServiceBean getBaseBean() {
//		if(serviceBean == null){
//			FacesContext facesContext = FacesContext.getCurrentInstance();
//			serviceBean = (ServiceBean) facesContext.getApplication()
//				.createValueBinding("#{ServiceBean}")
//				.getValue(facesContext);
//		}
//		return serviceBean;
//	}
	
	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		long tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return (double) tmp / factor;
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
	
	static class Day implements Comparable<Object> {
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
		public boolean equals(Object obj) {
			return compareTo(obj) == 0;
		}	
		public int hashCode() {
			return getDay();
		}
	}
	
	private Map<Integer, String> getWeekDaysMap(){
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
	
}
