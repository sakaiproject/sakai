package org.sakaiproject.sitestats.api;

public interface SummaryVisitsTotals {

	public double getLast30DaysVisitsAverage();

	public void setLast30DaysVisitsAverage(double last30DaysVisitsAverage);

	public double getLast365DaysVisitsAverage();

	public void setLast365DaysVisitsAverage(double last365DaysVisitsAverage);

	public double getLast7DaysVisitsAverage();

	public void setLast7DaysVisitsAverage(double last7DaysVisitsAverage);

	public double getPercentageOfUsersThatVisitedSite();

	public void setPercentageOfUsersThatVisitedSite(double percentageOfUsersThatVisitedSite);

	public long getTotalUniqueVisits();

	public void setTotalUniqueVisits(long totalUniqueVisits);

	public int getTotalUsers();

	public void setTotalUsers(int totalUsers);

	public long getTotalVisits();

	public void setTotalVisits(long totalVisits);

}