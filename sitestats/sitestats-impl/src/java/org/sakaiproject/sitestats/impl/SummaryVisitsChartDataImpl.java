package org.sakaiproject.sitestats.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;


public class SummaryVisitsChartDataImpl implements SummaryVisitsChartData {
	private String				viewType		= null;
	private Date				firstDay		= null;
	private long[]				visits			= null;
	private long[]				uniqueVisits	= null;
	private List<SiteVisits>	siteVisits		= null;

	public SummaryVisitsChartDataImpl(String viewType) {
		this.viewType = viewType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsChartData#getSiteVisits()
	 */
	public List<SiteVisits> getSiteVisits() {
		return siteVisits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsChartData#setSiteVisits(java.util.List)
	 */
	public void setSiteVisits(List<SiteVisits> siteVisits) {
		this.siteVisits = siteVisits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsChartData#getUniqueVisits()
	 */
	public long[] getUniqueVisits() {
		if(uniqueVisits == null && siteVisits.size() > 0)
			fillData();
		return uniqueVisits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsChartData#getVisits()
	 */
	public long[] getVisits() {
		if(visits == null && siteVisits.size() > 0)
			fillData();
		return visits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsChartData#getFirstDay()
	 */
	public Date getFirstDay() {
		if(firstDay == null && siteVisits.size() > 0)
			fillData();
		return firstDay;
	}

	private void fillData() {
		if(viewType == null || siteVisits == null) return;
		
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
		visits = new long[timeSlots];
		uniqueVisits = new long[timeSlots];
		for(int i = timeSlots - 1; i >= 0; i--){
			SiteVisits sv = null;
			if(StatsManager.VIEW_YEAR.equals(viewType))
				sv = getDataForMonth(cal);
			else
				sv = getDataForDay(cal);
			if(sv == null){
				visits[i] = 0;
				uniqueVisits[i] = 0;
			}else{
				visits[i] = sv.getTotalVisits();
				uniqueVisits[i] = sv.getTotalUnique();
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
	}
	
	private SiteVisits getDataForDay(Calendar cal_) {
		Calendar cal = (Calendar) cal_.clone();
		int desiredDay = cal.get(Calendar.DAY_OF_YEAR);

		for(int i = siteVisits.size() - 1; i >= 0; i--){
			SiteVisits sv = siteVisits.get(i);
			cal.setTime(sv.getDate());
			int thisDay = cal.get(Calendar.DAY_OF_YEAR);
			if(desiredDay == thisDay){
				//System.out.println("SummaryVisitsChartDataImpl.getDataForDay: "+(new Date(dateInMs).toGMTString()) +" matches "+sv.getDate().toGMTString()+" => "+sv.getTotalVisits()+" / "+sv.getTotalUnique());
				return sv;
			}
		}
		return null;
	}
	
	private SiteVisits getDataForMonth(Calendar cal_) {
		Calendar cal = (Calendar) cal_.clone();
		int desiredMonth = cal.get(Calendar.MONTH);

		for(int i = siteVisits.size() - 1; i >= 0; i--){
			SiteVisits sv = siteVisits.get(i);
			cal.setTime(sv.getDate());
			int thisMonth = cal.get(Calendar.MONTH);
			if(desiredMonth == thisMonth){
				//System.out.println("SummaryVisitsChartDataImpl.getDataForMonth: "+(new Date(dateInMs).toGMTString()) +" matches "+sv.getDate().toGMTString()+" => "+sv.getTotalVisits()+" / "+sv.getTotalUnique());
				return sv;
			}
		}
		return null;
	}
}
