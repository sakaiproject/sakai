package org.sakaiproject.sitestats.api;

import java.util.Date;
import java.util.List;



public interface SummaryActivityChartData {

	public void setSiteActivity(List<SiteActivity> siteActivity);

	public void setSiteActivityByTool(List<SiteActivityByTool> siteActivityByTool);

	public long[] getActivity();
	
	public List<SiteActivityByTool> getActivityByTool();
	
	public int getActivityByToolTotal();

	public Date getFirstDay();

}