package org.sakaiproject.sitestats.api;

import java.util.Date;
import java.util.List;



public interface SummaryVisitsChartData {

	public List<SiteVisits> getSiteVisits();

	public void setSiteVisits(List<SiteVisits> siteVisits);

	public long[] getUniqueVisits();

	public long[] getVisits();

	public Date getFirstDay();

}