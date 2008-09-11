package org.sakaiproject.sitestats.tool.bean;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.StatsManager;

public class ChartParamsBean implements Serializable {
	private static final long	serialVersionUID					= 1L;

	/** Our log (commons). */
	private static Log			LOG									= LogFactory.getLog(ChartParamsBean.class);

	public static final String	VIEW_WEEK							= StatsManager.VIEW_WEEK;
	public static final String	VIEW_MONTH							= StatsManager.VIEW_MONTH;
	public static final String	VIEW_YEAR							= StatsManager.VIEW_YEAR;
	public static final String	CHATTYPE_BAR						= StatsManager.CHARTTYPE_BAR;
	public static final String	CHATTYPE_PIE						= StatsManager.CHARTTYPE_PIE;

	// server wide stats
	public static final String MONTHLY_LOGIN_REPORT 				= StatsManager.MONTHLY_LOGIN_REPORT;
	public static final String WEEKLY_LOGIN_REPORT 					= StatsManager.WEEKLY_LOGIN_REPORT;
    public static final String DAILY_LOGIN_REPORT 					= StatsManager.DAILY_LOGIN_REPORT;
	public static final String REGULAR_USERS_REPORT 				= StatsManager.REGULAR_USERS_REPORT;
	public static final String HOURLY_USAGE_REPORT 					= StatsManager.HOURLY_USAGE_REPORT;
	public static final String TOP_ACTIVITIES_REPORT 				= StatsManager.TOP_ACTIVITIES_REPORT;
	public static final String TOOL_REPORT 							= StatsManager.TOOL_REPORT;
        
	private static final int	DEFAULT_CHART_WIDTH					= 400;
	private static final int	DEFAULT_CHART_HEIGHT				= 200;
	private static final int	MIN_CHART_WIDTH						= 240;
	private static final int	MIN_CHART_WIDTH_TO_DRAW_ALL_DAYS	= 440;
	private static final int	MAX_CHART_WIDTH						= 640;
	private static final int	MIN_CHART_HEIGHT					= 200;
	private static final int	MAX_CHART_HEIGHT					= 320;

	/** UI behavior vars */
	private String				selectedVisitsView					= VIEW_WEEK;
	private String				selectedActivityView				= VIEW_WEEK;
	private String				selectedActivityChartType			= CHATTYPE_PIE;
	private String				selectedReportChartType				= "";
	private int					mainAreaWidth						= 640;
	private int					chartWidth							= DEFAULT_CHART_WIDTH;
	private int					chartHeight							= DEFAULT_CHART_HEIGHT;

	/** Rendering control vars */
	private boolean				renderVisitsChart					= false;
	private boolean				renderActivityChart					= false;
	private boolean				renderReportChart					= false;
	private boolean				maximizedVisitsSelected				= false;
	private boolean				maximizedActivitySelected			= false;
	private boolean				maximizedReportSelected			= false;
	private float				foregroundAlpha						= 0.80f;
	private String				backgroundColor						= "white";

	private String				siteId								= null;

	// ######################################################################################
	// JFreeChart rendering parameters
	// ######################################################################################
	public float getForegroundAlpha() {
		return foregroundAlpha;
	}

	public void setForegroundAlpha(float foregroundAlpha) {
		this.foregroundAlpha = foregroundAlpha;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public boolean areAllDaysDrawable(){
		return (getChartWidth() >= MIN_CHART_WIDTH_TO_DRAW_ALL_DAYS);
	}

	// ######################################################################################
	// Chart selection methods
	// ######################################################################################
	public String getSelectedVisitsView(){
		return selectedVisitsView;
	}
	
	public String getSelectedActivityView(){
		return selectedActivityView;
	}
	
	public String getSelectedActivityChartType() {
		return selectedActivityChartType;
	}
	
	public void selectVisitsWeekView(ActionEvent e){
		this.selectedVisitsView = VIEW_WEEK;
		this.renderVisitsChart = true;
	}

	public void selectVisitsMonthView(ActionEvent e) {
		this.selectedVisitsView = VIEW_MONTH;
		this.renderVisitsChart = true;
	}

	public void selectVisitsYearView(ActionEvent e) {
		this.selectedVisitsView = VIEW_YEAR;
		this.renderVisitsChart = true;
	}

	
	public void selectActivityWeekView(ActionEvent e){
		this.selectedActivityView = VIEW_WEEK;
		this.renderActivityChart = true;
	}

	public void selectActivityMonthView(ActionEvent e) {
		this.selectedActivityView = VIEW_MONTH;
		this.renderActivityChart = true;
	}

	public void selectActivityYearView(ActionEvent e) {
		this.selectedActivityView = VIEW_YEAR;
		this.renderActivityChart = true;
	}

	public void selectActivityChart(ActionEvent e) {
		this.selectedActivityChartType = CHATTYPE_PIE;
		this.renderActivityChart = true;
	}

	public void selectActivityPieChart(ActionEvent e) {
		this.selectedActivityChartType = CHATTYPE_PIE;
		this.renderActivityChart = true;
	}

	public void selectActivityBarChart(ActionEvent e) {
		this.selectedActivityChartType = CHATTYPE_BAR;
		this.renderActivityChart = true;
	}
	
	public String getSelectedReportType() {
		return selectedReportChartType;
	}

	public void selectMonthlyLoginReportType (ActionEvent e){
		this.selectedReportChartType = MONTHLY_LOGIN_REPORT;
		this.renderReportChart = true;
	}

	public void selectWeeklyLoginReportType (ActionEvent e){
		this.selectedReportChartType = WEEKLY_LOGIN_REPORT;
		this.renderReportChart = true;
	}

	public void selectDailyLoginReportType (ActionEvent e){
		this.selectedReportChartType = DAILY_LOGIN_REPORT;
		this.renderReportChart = true;
	}

	public void selectRegularUsersReportType (ActionEvent e){
		this.selectedReportChartType = REGULAR_USERS_REPORT;
		this.renderReportChart = true;
	}

	public void selectHourlyUsageReportType (ActionEvent e){
		this.selectedReportChartType = HOURLY_USAGE_REPORT;
		this.renderReportChart = true;
	}

	public void selectTopActivitiesReportType (ActionEvent e){
		this.selectedReportChartType = TOP_ACTIVITIES_REPORT;
		this.renderReportChart = true;
	}

	public void selectToolReportType (ActionEvent e){
		this.selectedReportChartType = TOOL_REPORT;
		this.renderReportChart = true;
	}

	

	// ######################################################################################
	// Chart parameters (ActionEvent) methods
	// ######################################################################################	
	public void setChartParameters(ActionEvent e) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		Map paramMap = context.getRequestParameterMap();
		if(paramMap.containsKey("chartWidth")){
		try{
				String chartWidthStr = (String) paramMap.get("chartWidth");
				chartWidth = (int) Float.parseFloat(chartWidthStr);
				setChartWidth(chartWidth);
			}catch(Exception ex){
				chartWidth = DEFAULT_CHART_WIDTH;
			}
		}
		if(paramMap.containsKey("chartHeight")){
			try{
				String chartWidthStr = (String) paramMap.get("chartHeight");
				chartHeight = (int) Float.parseFloat(chartWidthStr);
				setChartHeight(chartHeight);
			}catch(Exception ex){
				chartHeight = DEFAULT_CHART_HEIGHT;
			}
		}
		if(paramMap.containsKey("backgroundColor")){
			try{
				String bgColor = (String) paramMap.get("backgroundColor");
				if(bgColor != null && !bgColor.equals("")) this.backgroundColor = bgColor;
			}catch(Exception ex){
				LOG.error("Failed to set backgroundColor. Using #ffffff.", ex);
				this.backgroundColor = "#ffffff";
			}
		}
		if(paramMap.containsKey("siteId")){
			try{
				String siteIdTmp = (String) paramMap.get("siteId");
				if(siteIdTmp != null && !siteIdTmp.equals("")) this.siteId = siteIdTmp;
			}catch(Exception ex){
				LOG.error("Failed to set siteId", ex);
			}
		}
	}
	
	public void setChartWidth(int chartWidth) {
		if(chartWidth < MIN_CHART_WIDTH)
			this.chartWidth = MIN_CHART_WIDTH;
		else
			this.chartWidth = chartWidth;
	}
	
	public int getChartWidth() {
		return chartWidth;
	}

	public void setChartHeight(int chartHeight) {
		if(chartHeight < MIN_CHART_HEIGHT)
			this.chartHeight = MIN_CHART_HEIGHT;
		else
			this.chartHeight = chartHeight;
	}
	
	public int getChartHeight() {
		return chartHeight;
	}

	// ######################################################################################
	// Render methods
	// ######################################################################################
	public boolean isRenderVisitsChart() {
		return renderVisitsChart;
	}
	
	public void renderVisitsChart(ActionEvent e) {
		setChartParameters(e);	
		this.renderVisitsChart = true;
	}
	
	public boolean isRenderActivityChart() {
		return renderActivityChart;
	}
	
	public void renderActivityChart(ActionEvent e) {
		setChartParameters(e);	
		this.renderActivityChart = true;
	}
	
	public boolean isRenderReportChart() {
		return renderReportChart;
	}
	
	public void renderReportChart(ActionEvent e) {
		setChartParameters(e);	
		this.renderReportChart = true;
	}
	
	public void setAllRenderFalse(ActionEvent e) {
		this.renderVisitsChart = false;
		this.renderActivityChart = false;
		this.renderReportChart = false;
	}
	
	public void setVisitsRenderFalse(ActionEvent e) {
		this.renderVisitsChart = false;	
	}
	
	public void setActivityRenderFalse(ActionEvent e) {
		this.renderActivityChart = false;	
	}
	
	public void setVisitsRenderTrue(ActionEvent e) {
		this.renderVisitsChart = true;	
	}
	
	public void setActivityRenderTrue(ActionEvent e) {
		this.renderActivityChart = true;	
	}
	
	public void setReportRenderFalse(ActionEvent e) {
		this.renderReportChart = false;	
	}
	
	public void setReportRenderTrue(ActionEvent e) {
		this.renderReportChart = true;	
	}
	
	public void selectMaximizedVisits(ActionEvent e) {
		this.maximizedVisitsSelected = true;	
		this.maximizedActivitySelected = false;	
		this.renderVisitsChart = false;	
		this.renderActivityChart = false;	
		this.maximizedReportSelected = false;	
		this.renderReportChart = false;	
	}
	
	public boolean isMaximizedVisits(){
		return this.maximizedVisitsSelected;
	}
	
	public void selectMaximizedActivity(ActionEvent e) {
		this.maximizedVisitsSelected = false;	
		this.maximizedActivitySelected = true;	
		this.renderVisitsChart = false;	
		this.renderActivityChart = false;	
		this.maximizedReportSelected = false;	
		this.renderReportChart = false;	
	}
	
	public void selectMaximizedReport(ActionEvent e) {
		this.maximizedVisitsSelected = false;	
		this.maximizedActivitySelected = false;	
		this.renderVisitsChart = false;	
		this.renderActivityChart = false;	
		this.maximizedReportSelected = true;	
		this.renderReportChart = false;	
	}
	
	public boolean isMaximizedReport(){
		return this.maximizedReportSelected;
	}
	
	public void restoreSize(ActionEvent e) {
		this.maximizedVisitsSelected = false;	
		this.maximizedActivitySelected = false;	
		this.maximizedReportSelected = false;	
	}
	
	public boolean isMaximizedActivity(){
		return this.maximizedActivitySelected;
	}
	
	public String getSiteId() {
		return siteId;
	}
}
