package org.sakaiproject.sitestats.impl;

import org.sakaiproject.sitestats.api.SummaryVisitsTotals;

public class SummaryVisitsTotalsImpl implements SummaryVisitsTotals {
	/** Visits */
	private long	totalVisits;
	private double last7DaysVisitsAverage;
	private double last30DaysVisitsAverage;
	private double last365DaysVisitsAverage;
	
	/** Unique visits */
	private long	totalUniqueVisits;
	private int		totalUsers;
	private double percentageOfUsersThatVisitedSite;
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getLast30DaysVisitsAverage()
	 */
	public double getLast30DaysVisitsAverage() {
		return last30DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setLast30DaysVisitsAverage(double)
	 */
	public void setLast30DaysVisitsAverage(double last30DaysVisitsAverage) {
		this.last30DaysVisitsAverage = last30DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getLast365DaysVisitsAverage()
	 */
	public double getLast365DaysVisitsAverage() {
		return last365DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setLast365DaysVisitsAverage(double)
	 */
	public void setLast365DaysVisitsAverage(double last365DaysVisitsAverage) {
		this.last365DaysVisitsAverage = last365DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getLast7DaysVisitsAverage()
	 */
	public double getLast7DaysVisitsAverage() {
		return last7DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setLast7DaysVisitsAverage(double)
	 */
	public void setLast7DaysVisitsAverage(double last7DaysVisitsAverage) {
		this.last7DaysVisitsAverage = last7DaysVisitsAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getPercentageOfUsersThatVisitedSite()
	 */
	public double getPercentageOfUsersThatVisitedSite() {
		return percentageOfUsersThatVisitedSite;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setPercentageOfUsersThatVisitedSite(double)
	 */
	public void setPercentageOfUsersThatVisitedSite(double percentageOfUsersThatVisitedSite) {
		this.percentageOfUsersThatVisitedSite = percentageOfUsersThatVisitedSite;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getTotalUniqueVisits()
	 */
	public long getTotalUniqueVisits() {
		return totalUniqueVisits;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setTotalUniqueVisits(int)
	 */
	public void setTotalUniqueVisits(long totalUniqueVisits) {
		this.totalUniqueVisits = totalUniqueVisits;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getTotalUsers()
	 */
	public int getTotalUsers() {
		return totalUsers;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setTotalUsers(int)
	 */
	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#getTotalVisits()
	 */
	public long getTotalVisits() {
		return totalVisits;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryVisitsTotals#setTotalVisits(int)
	 */
	public void setTotalVisits(long totalVisits) {
		this.totalVisits = totalVisits;
	}
	
	
}
