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
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;

public class SummaryActivityChartDataImpl implements SummaryActivityChartData {
	private String						viewType			= null;
	private String						chartType			= null;
	private Date						firstDay			= null;
	private long[]						activity			= null;
	private int							activityByToolTotal	= 0;
	private List<SiteActivity>			siteActivity		= null;
	private List<SiteActivityByTool>	siteActivityByTool	= null;

	public SummaryActivityChartDataImpl(String viewType, String chartType) {
		this.viewType = viewType;
		this.chartType = chartType;
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityChartData#setSiteActivity(java.util.List)
	 */
	public void setSiteActivity(List<SiteActivity> siteActivity) {
		this.siteActivity = siteActivity;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SummaryActivityChartData#setSiteActivityByTool(java.util.List)
	 */
	public void setSiteActivityByTool(List<SiteActivityByTool> siteActivityByTool){
		this.siteActivityByTool = siteActivityByTool;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityChartData#getActivity()
	 */
	public long[] getActivity() {
		if(activity == null && siteActivity.size() > 0)
			fillBarChartData();
		return activity;
	}
	
	public List<SiteActivityByTool> getActivityByTool(){
		fillPieChartData();
		if(siteActivityByTool.size() > 0)
			return siteActivityByTool;
		else
			return null;
//		return siteActivityByTool;
	}
	
	public int getActivityByToolTotal(){
		fillPieChartData();
		return activityByToolTotal;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityChartData#getFirstDay()
	 */
	public Date getFirstDay() {
		if(firstDay == null && siteActivity.size() > 0)
			fillBarChartData();
		return firstDay;
	}
	
	private void fillPieChartData() {
		if(viewType == null || chartType == null || siteActivityByTool == null) return;
		
		// sort by count
		Collections.sort(siteActivityByTool, new SiteActivityByToolComparator());
		
		// get total activity count
		activityByToolTotal = 0;
		for(int i=0; i<siteActivityByTool.size(); i++){
			activityByToolTotal += siteActivityByTool.get(i).getCount();
		}
	}

	private void fillBarChartData() {
		if(viewType == null || chartType == null || siteActivity == null) return;
		
		// set first and last day data
		int timeSlots = 0;

		// fill arrays
		if(StatsManager.VIEW_WEEK.equals(viewType)){
			timeSlots = 7;

		}else if(StatsManager.VIEW_MONTH.equals(viewType)){
			timeSlots = 30;

		}else if(StatsManager.VIEW_YEAR.equals(viewType)){
			timeSlots = 12;
		}

		// fill with data, backwards
		Calendar cal = Calendar.getInstance();
		activity = new long[timeSlots];
		for(int i = timeSlots - 1; i >= 0; i--){
			SiteActivity sa = null;
			//log.debug("SummaryActivityChartDataImpl.fillData:  i="+i+"  date="+cal.getTime().toGMTString());
			
			if(StatsManager.VIEW_YEAR.equals(viewType))
				sa = getDataForMonth(cal);
			else
				sa = getDataForDay(cal);
			if(sa == null){
				activity[i] = 0;
			}else{
				activity[i] = sa.getCount();
			}
			if(i > 0) {
				if(StatsManager.VIEW_WEEK.equals(viewType) || StatsManager.VIEW_MONTH.equals(viewType)){
					cal.add(Calendar.DATE, -1);
				}else if(StatsManager.VIEW_YEAR.equals(viewType)){
					cal.add(Calendar.MONTH, -1);
				}			
			}
		}
		firstDay = cal.getTime();
		//log.debug("SummaryActivityChartDataImpl.firstDay:  "+firstDay.toGMTString());
	}
	
	private SiteActivity getDataForDay(Calendar cal_) {
		Calendar cal = (Calendar) cal_.clone();
		int desiredDay = cal.get(Calendar.DAY_OF_YEAR);

		for(int i = siteActivity.size() - 1; i >= 0; i--){
			SiteActivity sa = siteActivity.get(i);
			cal.setTime(sa.getDate());
			int thisDay = cal.get(Calendar.DAY_OF_YEAR);
			if(desiredDay == thisDay){
				//log.debug("SummaryActivityChartDataImpl.getDataForDay: "+(cal.getTime().toGMTString()) +" matches "+sa.getDate().toGMTString()+" => "+sa.getCount());
				return sa;
			}
		}
		return null;
	}
	
	private SiteActivity getDataForMonth(Calendar cal_) {
		Calendar cal = (Calendar) cal_.clone();
		int desiredMonth = cal.get(Calendar.MONTH);

		for(int i = siteActivity.size() - 1; i >= 0; i--){
			SiteActivity sa = siteActivity.get(i);
			cal.setTime(sa.getDate());
			int thisMonth = cal.get(Calendar.MONTH);
			if(desiredMonth == thisMonth){
				//log.debug("SummaryActivityChartDataImpl.getDataForMonth: "+(cal.getTime().toGMTString()) +" matches "+sa.getDate().toGMTString()+" => "+sa.getCount());
				return sa;
			}
		}
		return null;
	}
	
	static class SiteActivityByToolComparator implements Comparator<SiteActivityByTool>, Serializable {
		private static final long	serialVersionUID	= 1L;

		public int compare(SiteActivityByTool o1, SiteActivityByTool o2) {
			if(o1.getCount() < o2.getCount())
				return +1;
			else if(o1.getCount() == o2.getCount())
				return 0;
			else
				return -1;
		}		
	}
}
