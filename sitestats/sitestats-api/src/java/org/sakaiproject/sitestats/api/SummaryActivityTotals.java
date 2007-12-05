package org.sakaiproject.sitestats.api;

public interface SummaryActivityTotals {

	public double getLast30DaysActivityAverage();

	public void setLast30DaysActivityAverage(double last30DaysActivityAverage);

	public double getLast365DaysActivityAverage();

	public void setLast365DaysActivityAverage(double last365DaysActivityAverage);

	public double getLast7DaysActivityAverage();

	public void setLast7DaysActivityAverage(double last7DaysActivityAverage);

	public long getTotalActivity();

	public void setTotalActivity(long totalActivity);

}