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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class OverviewBean extends InitializableBean implements Serializable {
	private static final long	serialVersionUID				= -3521524574495782529L;
	private static final String	VIEW_WEEK						= "week";
	private static final String	VIEW_MONTH						= "month";
	private static final String	VIEW_YEAR						= "year";

	/** Our log (commons). */
	private static Log			LOG								= LogFactory.getLog(OverviewBean.class);

	/** Bean members */
	private String				selectedView					= VIEW_WEEK;
	private long				totalVisits						= -1;
	private double				lastWeekVisitsAverage			= -1;
	private double				lastMonthVisitsAverage			= -1;
	private double				lastYearVisitsAverage			= -1;
	private long				totalUniqueVisits				= -1;
	private double				lastWeekUniqueVisitsAverage		= -1;
	private double				lastMonthUniqueVisitsAverage	= -1;
	private double				lastYearUniqueVisitsAverage		= -1;
	private int					totalSiteUsers					= -1;
	private long				totalActivity					= -1;
	private double				lastWeekActivityAverage			= -1;
	private double				lastMonthActivityAverage		= -1;
	private double				lastYearActivityAverage			= -1;

	private List				weekSiteStatsVisits				= null;
	private List				weekSiteStatsActivity			= null;
	private List				weekVisits						= null;
	private List				weekUniqueVisitors				= null;
	private List				weekActivity					= null;
	private List				monthSiteStatsVisits			= null;
	private List				monthSiteStatsActivity			= null;
	private List				monthVisits						= null;
	private List				monthUniqueVisitors				= null;
	private List				monthActivity					= null;
	private List				yearSiteStatsVisits				= null;
	private List				yearSiteStatsActivity			= null;
	private List				yearVisits						= null;
	private List				yearUniqueVisitors				= null;
	private List				yearActivity					= null;

	private List				eventIds						= null;
	private boolean				eventIdsChanged					= true;

	/** Statistics Manager object */
	private BaseBean			baseBean						= null;
	private StatsManager		sm								= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	
	
	public void init() {
		LOG.debug("OverviewBean.init()");
		initializeBaseBean();
		
		if(baseBean.isAllowed()){	
			totalVisits = -1;
			lastWeekVisitsAverage = -1;
			lastMonthVisitsAverage = -1;
			lastYearVisitsAverage = -1;
			totalUniqueVisits = -1;
			lastWeekUniqueVisitsAverage = -1;
			lastMonthUniqueVisitsAverage = -1;
			lastYearUniqueVisitsAverage = -1;
			totalActivity = -1;
			lastWeekActivityAverage = -1;
			lastMonthActivityAverage = -1;
			lastYearActivityAverage = -1;
			totalVisits = getTotalVisits();
			lastWeekVisitsAverage = getLastWeekVisitsAverage();
			lastMonthVisitsAverage = getLastMonthVisitsAverage();
			lastYearVisitsAverage = getLastYearVisitsAverage();
			totalUniqueVisits = getTotalUniqueVisits();
			totalSiteUsers = sm.getTotalSiteUsers(baseBean.getSiteId());
			totalUniqueVisits = sm.getTotalSiteUniqueVisits(baseBean.getSiteId());
			lastWeekUniqueVisitsAverage = getLastWeekUniqueVisitsAverage();
			lastMonthUniqueVisitsAverage = getLastMonthUniqueVisitsAverage();
			lastYearUniqueVisitsAverage = getLastYearUniqueVisitsAverage();
			totalActivity = getTotalActivity();
			lastWeekActivityAverage = getLastWeekActivityAverage();
			lastMonthActivityAverage = getLastMonthActivityAverage();
			lastYearActivityAverage = getLastYearActivityAverage();
			
			if(selectedView.equals(VIEW_WEEK)){
				weekVisits = null;
				weekActivity = null;
				weekUniqueVisitors = null;
				weekSiteStatsVisits = null;
				weekSiteStatsActivity = null;
				weekSiteStatsVisits = getWeekSiteStatsVisits();
				weekSiteStatsActivity = getWeekSiteStatsActivity();
			}else if(selectedView.equals(VIEW_MONTH)){
				monthVisits = null;
				monthActivity = null;
				monthUniqueVisitors = null;
				monthSiteStatsVisits = null;
				monthSiteStatsActivity = null;
				monthSiteStatsVisits = getMonthSiteStatsVisits();
				monthSiteStatsActivity = getMonthSiteStatsActivity();
			}else if(selectedView.equals(VIEW_YEAR)){
				yearVisits = null;
				yearActivity = null;
				yearUniqueVisitors = null;
				yearSiteStatsVisits = null;
				yearSiteStatsActivity = null;
				yearSiteStatsVisits = getYearSiteStatsVisits();
				yearSiteStatsActivity = getYearSiteStatsActivity();
			}
		}
	}
	
	private void initializeBaseBean(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		baseBean = (BaseBean) facesContext.getApplication()
			.createValueBinding("#{BaseBean}")
			.getValue(facesContext);
	}

	public List getEventIds() {
		eventIds = sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE);
		return eventIds;
	}

	public boolean isEventIdsChanged(){
		List l = sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE);
		eventIdsChanged = !l.equals(eventIds);	
		eventIds = l;
		return this.eventIdsChanged;
	}


	// ######################################################################################
	// JFreeChart testing
	// ######################################################################################
//	public DefaultPieDataset getPieDataSet() {
//		DefaultPieDataset pieDataSet = new DefaultPieDataset();
//		pieDataSet.setValue("A",52);
//		pieDataSet.setValue("B", 18);
//		pieDataSet.setValue("C", 30);
//		return pieDataSet;
//	}
//	
//	public String getChartURL(){
//		//String context = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
//		//return context + "/chartServlet";
//		return "/chartservlet?id=1&type=2";
//	}
	
	// ######################################################################################
	// Chart selection methods
	// ######################################################################################
	public String getSelectedView(){
		return selectedView;
	}
	
	public void selectWeekView(ActionEvent e){
		this.selectedView = VIEW_WEEK;
	}

	public void selectMonthView(ActionEvent e) {
		this.selectedView = VIEW_MONTH;
	}

	public void selectYearView(ActionEvent e) {
		this.selectedView = VIEW_YEAR;
	}
	

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	
	
	
	// ######################################################################################
	// SUMMARY: VISITS
	// ######################################################################################
	public long getTotalVisits() {
		if(totalVisits == -1){
			totalVisits = sm.getTotalSiteVisits(baseBean.getSiteId());
		}
		return totalVisits;
	}

	public String getVisitsAverage() {
		if(lastWeekVisitsAverage == -1)
			getLastWeekVisitsAverage();
		if(lastMonthVisitsAverage == -1)
			getLastMonthVisitsAverage();
		if(lastYearVisitsAverage == -1)
			getLastYearVisitsAverage();
		return lastWeekVisitsAverage+"/"+lastMonthVisitsAverage+"/"+lastYearVisitsAverage;
	}

	private double getLastWeekVisitsAverage() {
		if(lastWeekVisitsAverage == -1){
			Date now = new Date();
			// week = 1000ms * 60s * 60m * 24h * 7d;
			long weekDiff = 604800000l;
			Date lastWeek = new Date(now.getTime() - weekDiff);
			double weekVisitors = (double) sm.getTotalSiteVisits(baseBean.getSiteId(), lastWeek, now);
			lastWeekVisitsAverage = round(weekVisitors / 7.0, 1);
		}
		return lastWeekVisitsAverage;
	}

	private double getLastMonthVisitsAverage() {
		if(lastMonthVisitsAverage == -1){
			Date now = new Date();
			// month = 1000ms * 60s * 60m * 24h * 30d;
			long monthDiff = 2592000000l;
			Date lastMonth = new Date(now.getTime() - monthDiff);
			double monthVisitors = (double) sm.getTotalSiteVisits(baseBean.getSiteId(), lastMonth, now);
			lastMonthVisitsAverage = round(monthVisitors / 30.0, 1);
		}
		return lastMonthVisitsAverage;
	}

	private double getLastYearVisitsAverage() {
		if(lastYearVisitsAverage == -1){
			Date now = new Date();
			// year = 1000ms * 60s * 60m * 24h * 365d;
			long yearDiff = 31536000000l;
			Date lastYear = new Date(now.getTime() - yearDiff);
			double yearVisitors = (double) sm.getTotalSiteVisits(baseBean.getSiteId(), lastYear, now);
			lastYearVisitsAverage = round(yearVisitors / 365.0, 1);
		}
		return lastYearVisitsAverage;
	}
	
	
	// ######################################################################################
	// SUMMARY: VISITS
	// ######################################################################################
	public long getTotalUniqueVisits() {
		if(totalUniqueVisits == -1){
			totalUniqueVisits = sm.getTotalSiteUniqueVisits(baseBean.getSiteId());
		}
		return totalUniqueVisits;
	}
	
	public String getLoggedTotalUsersRelation() {
		int totalUsers = getTotalSiteUsers();
		long loggedUsers = getTotalUniqueVisits();
		if(totalUsers == 0)
			return new String("0");
		else{
			double percentage = (100 * loggedUsers) / totalUsers;
			return loggedUsers + "/" + totalUsers + " (" + percentage + "%)";
		}
	}
	
	private int getTotalSiteUsers() {
		if(totalSiteUsers == -1){
			totalSiteUsers = sm.getTotalSiteUsers(baseBean.getSiteId());
		}
		return totalSiteUsers;
	}

	public String getUniqueVisitsAverage() {
		if(lastWeekUniqueVisitsAverage == -1)
			getLastWeekUniqueVisitsAverage();
		if(lastMonthUniqueVisitsAverage == -1)
			getLastMonthUniqueVisitsAverage();
		if(lastYearUniqueVisitsAverage == -1)
			getLastYearUniqueVisitsAverage();
		return lastWeekUniqueVisitsAverage+"/"+lastMonthUniqueVisitsAverage+"/"+lastYearUniqueVisitsAverage;
	}

	private double getLastWeekUniqueVisitsAverage() {
		if(lastWeekUniqueVisitsAverage == -1){
			Date now = new Date();
			// week = 1000ms * 60s * 60m * 24h * 7d;
			long weekDiff = 604800000l;
			Date lastWeek = new Date(now.getTime() - weekDiff);
			double weekUniqueVisitors = (double) sm.getTotalSiteUniqueVisits(baseBean.getSiteId(), lastWeek, now);
			lastWeekUniqueVisitsAverage = round(weekUniqueVisitors / 7.0, 1);
		}
		return lastWeekUniqueVisitsAverage;
	}

	private double getLastMonthUniqueVisitsAverage() {
		if(lastMonthUniqueVisitsAverage == -1){
			Date now = new Date();
			// month = 1000ms * 60s * 60m * 24h * 30d;
			long monthDiff = 2592000000l;
			Date lastMonth = new Date(now.getTime() - monthDiff);
			double monthUniqueVisitors = (double) sm.getTotalSiteUniqueVisits(baseBean.getSiteId(), lastMonth, now);
			lastMonthUniqueVisitsAverage = round(monthUniqueVisitors / 30.0, 1);
		}
		return lastMonthUniqueVisitsAverage;
	}

	private double getLastYearUniqueVisitsAverage() {
		if(lastYearUniqueVisitsAverage == -1){
			Date now = new Date();
			// year = 1000ms * 60s * 60m * 24h * 365d;
			long yearDiff = 31536000000l;
			Date lastYear = new Date(now.getTime() - yearDiff);
			double yearUniqueVisitors = (double) sm.getTotalSiteUniqueVisits(baseBean.getSiteId(), lastYear, now);
			lastYearUniqueVisitsAverage = round(yearUniqueVisitors / 365.0, 1);
		}
		return lastYearUniqueVisitsAverage;
	}
	
	
	// ######################################################################################
	// SUMMARY: ACTIVITY
	// ######################################################################################
	public long getTotalActivity() {
		if(totalActivity == -1){
			totalActivity = sm.getTotalSiteActivity(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE));
		}
		return totalActivity;
	}

	public String getActivityAverage() {
		if(lastWeekActivityAverage == -1)
			getLastWeekActivityAverage();
		if(lastMonthActivityAverage == -1)
			getLastMonthActivityAverage();
		if(lastYearActivityAverage == -1)
			getLastYearActivityAverage();
		return lastWeekActivityAverage+"/"+lastMonthActivityAverage+"/"+lastYearActivityAverage;
	}

	private double getLastWeekActivityAverage() {
		if(lastWeekActivityAverage == -1){
			Date now = new Date();
			// week = 1000ms * 60s * 60m * 24h * 7d;
			long weekDiff = 604800000l;
			Date lastWeek = new Date(now.getTime() - weekDiff);
			double weekActivity = (double) sm.getTotalSiteActivity(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), lastWeek, now);
			lastWeekActivityAverage = round(weekActivity / 7.0, 1);
		}
		return lastWeekActivityAverage;
	}

	private double getLastMonthActivityAverage() {
		if(lastMonthActivityAverage == -1){
			Date now = new Date();
			// month = 1000ms * 60s * 60m * 24h * 30d;
			long monthDiff = 2592000000l;
			Date lastMonth = new Date(now.getTime() - monthDiff);
			double monthActivity = (double) sm.getTotalSiteActivity(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), lastMonth, now);
			lastMonthActivityAverage = round(monthActivity / 30.0, 1);
		}
		return lastMonthActivityAverage;
	}

	private double getLastYearActivityAverage() {
		if(lastYearActivityAverage == -1){
			Date now = new Date();
			// year = 1000ms * 60s * 60m * 24h * 365d;
			long yearDiff = 31536000000l;
			Date lastYear = new Date(now.getTime() - yearDiff);
			double yearActivity = (double) sm.getTotalSiteActivity(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), lastYear, now);
			lastYearActivityAverage = round(yearActivity / 365.0, 1);
		}
		return lastYearActivityAverage;
	}		


	// ######################################################################################
	// CHART: STATS
	// ######################################################################################
	public List getVisits() {
		List list = null;
		if(selectedView.equals(VIEW_WEEK)){
			if(weekVisits == null)
				fillWeekData();
			list = weekVisits;
		}else if(selectedView.equals(VIEW_MONTH)){
			if(monthVisits == null)
				fillMonthData();
			list = monthVisits;
		}else if(selectedView.equals(VIEW_YEAR)){
			if(yearVisits == null)
				fillYearData();
			list = yearVisits;
		}
		return list;
	}

	public List getActivity() {
		List list = null;
		if(selectedView.equals(VIEW_WEEK)){
			if(weekActivity == null)
				fillWeekData();
			list = weekActivity;
		}else if(selectedView.equals(VIEW_MONTH)){
			if(monthActivity == null)
				fillMonthData();
			list = monthActivity;
		}else if(selectedView.equals(VIEW_YEAR)){
			if(yearActivity == null)
				fillYearData();
			list = yearActivity;
		}
		return list;
	}

	public List getUniqueVisitors() {
		List list = null;
		if(selectedView.equals(VIEW_WEEK)){
			if(weekUniqueVisitors == null)
				fillWeekData();
			list = weekUniqueVisitors;
		}else if(selectedView.equals(VIEW_MONTH)){
			if(monthUniqueVisitors == null)
				fillMonthData();
			list = monthUniqueVisitors;
		}else if(selectedView.equals(VIEW_YEAR)){
			if(yearUniqueVisitors == null)
				fillYearData();
			list = yearUniqueVisitors;
		}
		return list;
	}
	
	public Long getTodayInMillis() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return new Long(c.getTimeInMillis());
	}
	
	// ######################################################################################
	// WEEK CHART
	// ######################################################################################
	private List getWeekSiteStatsVisits(){
		if(weekSiteStatsVisits == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -6);
			Date initialDate = c.getTime();
			weekSiteStatsVisits = sm.getSiteVisits(baseBean.getSiteId(), initialDate, finalDate);
		}
		return weekSiteStatsVisits;
	}
	
	private List getWeekSiteStatsActivity(){
		if(weekSiteStatsActivity == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -6);
			Date initialDate = c.getTime();
			weekSiteStatsActivity = sm.getSiteActivityByDay(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), initialDate, finalDate);
		}
		return weekSiteStatsActivity;
	}

	
	// ######################################################################################
	// MONTH CHART
	// ######################################################################################
	private List getMonthSiteStatsVisits(){
		if(monthSiteStatsVisits == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -29);
			Date initialDate = c.getTime();
			monthSiteStatsVisits = sm.getSiteVisits(baseBean.getSiteId(), initialDate, finalDate);
		}
		return monthSiteStatsVisits;
	}
	
	private List getMonthSiteStatsActivity(){
		if(monthSiteStatsActivity == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -29);
			Date initialDate = c.getTime();
			monthSiteStatsActivity = sm.getSiteActivityByDay(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), initialDate, finalDate);
		}
		return monthSiteStatsActivity;
	}

	
	// ######################################################################################
	// YEAR CHART
	// ######################################################################################
	private List getYearSiteStatsVisits(){
		if(yearSiteStatsVisits == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -11);
			Date initialDate = c.getTime();
			yearSiteStatsVisits = sm.getSiteVisitsByMonth(baseBean.getSiteId(), initialDate, finalDate);
		}
		return yearSiteStatsVisits;
	}
	
	private List getYearSiteStatsActivity(){
		if(yearSiteStatsActivity == null){
			Date finalDate = getToday();
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -11);
			Date initialDate = c.getTime();
			yearSiteStatsActivity = sm.getSiteActivityByMonth(baseBean.getSiteId(), sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE), initialDate, finalDate);
		}
		return yearSiteStatsActivity;
	}

	
	// ######################################################################################
	// STATS UTILITY FUNCTIONS
	// ######################################################################################
	private void fillWeekData(){
		weekVisits = new ArrayList();
		weekUniqueVisitors = new ArrayList();	
		weekActivity = new ArrayList();		
		List weekSiteStatsVisits = getWeekSiteStatsVisits();
		List weekSiteStatsActivity = getWeekSiteStatsActivity();

		// fill w/ 0
		for(int i = 0; i < 7; i++){
			weekVisits.add(new Integer(0));
			weekUniqueVisitors.add(new Integer(0));
			weekActivity.add(new Integer(0));
		}

		// add site vists data
		Iterator wi = weekSiteStatsVisits.iterator();
		Calendar curr = Calendar.getInstance();
		int nDaysOfPreviousMonth = -1;
		int currDay = curr.get(Calendar.DATE);
		int currMonth = curr.get(Calendar.MONTH);
		int day = 0, month = 0;
		int pos = 0;
		while (wi.hasNext()){
			SiteVisits ss = (SiteVisits) wi.next();
			Date key = ss.getDate();
			curr.setTime(key);
			month = curr.get(Calendar.MONTH);
			day = curr.get(Calendar.DATE);
			if(month != currMonth){
				if(nDaysOfPreviousMonth == -1){
					Calendar c = (Calendar) curr.clone();
					c.set(Calendar.MONTH, month);
					nDaysOfPreviousMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH); 
				}
				day = day - nDaysOfPreviousMonth;
			}
			pos = 6 - currDay + day;
			weekVisits.set(pos, new Integer((int)ss.getTotalVisits()));
			weekUniqueVisitors.set(pos, new Integer((int)ss.getTotalUnique()));
		}

		// add site activity data
		Iterator wa = weekSiteStatsActivity.iterator();
		while (wa.hasNext()){
			SiteActivity ss = (SiteActivity) wa.next();
			Date key = ss.getDate();
			curr.setTime(key);
			month = curr.get(Calendar.MONTH);
			day = curr.get(Calendar.DATE);
			if(month != currMonth){
				if(nDaysOfPreviousMonth == -1){
					Calendar c = (Calendar) curr.clone();
					c.set(Calendar.MONTH, month);
					nDaysOfPreviousMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH); 
				}
				day = day - nDaysOfPreviousMonth;
			}
			pos = 6 - currDay + day;
			weekActivity.set(pos , new Integer((int)ss.getCount()));
		}
	}
	
	private void fillMonthData(){
		monthVisits = new ArrayList();
		monthUniqueVisitors = new ArrayList();	
		monthActivity = new ArrayList();		
		List monthSiteStatsVisits = getMonthSiteStatsVisits();
		List monthSiteStatsActivity = getMonthSiteStatsActivity();

		// fill w/ 0
		for(int i = 0; i < 30; i++){
			monthVisits.add(new Integer(0));
			monthUniqueVisitors.add(new Integer(0));
			monthActivity.add(new Integer(0));
		}

		// add site vists data
		Iterator wi = monthSiteStatsVisits.iterator();
		// find no. of days of previous 2 months
		Calendar curr = Calendar.getInstance();
		Calendar c = (Calendar) curr.clone();
		c.add(Calendar.MONTH, -1);
		int previousMonth = c.get(Calendar.MONTH);
		int nDaysOfPreviousMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		c.add(Calendar.MONTH, -1);
		int prePreviousMonth = c.get(Calendar.MONTH);
		int nDaysOfPrePreviousMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		int currDay = curr.get(Calendar.DATE);
		int day = 0, month = 0;
		int pos = 0;
		// iterate
		while (wi.hasNext()){
			SiteVisits ss = (SiteVisits) wi.next();
			Date key = ss.getDate();
			curr.setTime(key);
			month = curr.get(Calendar.MONTH);
			day = curr.get(Calendar.DATE);
			if(month == previousMonth)
				day -= nDaysOfPreviousMonth;
			else if(month == prePreviousMonth)
				day -= nDaysOfPreviousMonth + nDaysOfPrePreviousMonth;
			pos = 29 - currDay + day;
			monthVisits.set(pos, new Integer((int)ss.getTotalVisits()));
			monthUniqueVisitors.set(pos, new Integer((int)ss.getTotalUnique()));
		}

		// add site activity data
		Iterator wa = monthSiteStatsActivity.iterator();
		curr = Calendar.getInstance();
		while (wa.hasNext()){
			SiteActivity ss = (SiteActivity) wa.next();
			Date key = ss.getDate();
			curr.setTime(key);
			month = curr.get(Calendar.MONTH);
			day = curr.get(Calendar.DATE);
			if(month == previousMonth)
				day -= nDaysOfPreviousMonth;
			else if(month == prePreviousMonth)
				day -= nDaysOfPreviousMonth + nDaysOfPrePreviousMonth;
			pos = 29 - currDay + day;
			monthActivity.set(pos, new Integer((int)ss.getCount()));
		}
	}
	
	private void fillYearData(){
		yearVisits = new ArrayList();
		yearUniqueVisitors = new ArrayList();	
		yearActivity = new ArrayList();		
		List yearSiteStatsVisits = getYearSiteStatsVisits();
		List yearSiteStatsActivity = getYearSiteStatsActivity();

		// fill w/ 0
		for(int i = 0; i < 12; i++){
			yearVisits.add(new Integer(0));
			yearUniqueVisitors.add(new Integer(0));
			yearActivity.add(new Integer(0));
		}

		// add site vists data
		Iterator wi = yearSiteStatsVisits.iterator();
		Calendar curr = Calendar.getInstance();
		int currMonth = curr.get(Calendar.MONTH);
		int currYear = curr.get(Calendar.YEAR);
		int month = 0, year = 0;
		int pos = 0;
		while (wi.hasNext()){
			SiteVisits ss = (SiteVisits) wi.next();
			Date key = ss.getDate();
			curr.setTime(key);
			year = curr.get(Calendar.YEAR);
			month = curr.get(Calendar.MONTH);
			month = (year == currYear)? month : month - 12;
			pos = 11 - currMonth + month;
			yearVisits.set(pos, new Integer((int)ss.getTotalVisits()));
			yearUniqueVisitors.set(pos, new Integer((int)ss.getTotalUnique()));
		}

		// add site activity data
		Iterator wa = yearSiteStatsActivity.iterator();
		curr = Calendar.getInstance();
		while (wa.hasNext()){
			SiteActivity ss = (SiteActivity) wa.next();
			Date key = ss.getDate();
			curr.setTime(key);
			year = curr.get(Calendar.YEAR);
			month = curr.get(Calendar.MONTH);
			month = (year == currYear)? month : month - 12;
			pos = 11 - currMonth + month;
			yearActivity.set(pos, new Integer((int)ss.getCount()));
		}
	}
	
	
	// ######################################################################################
	// Utility methods
	// ######################################################################################
	private Date getToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
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
