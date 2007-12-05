package org.sakaiproject.sitestats.impl;

import org.sakaiproject.sitestats.api.SummaryActivityTotals;

public class SummaryActivityTotalsImpl implements SummaryActivityTotals {
	/** Activity */
	private long	totalActivity;
	private double 	last7DaysActivityAverage;
	private double 	last30DaysActivityAverage;
	private double 	last365DaysActivityAverage;
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#getLast30DaysActivityAverage()
	 */
	public double getLast30DaysActivityAverage() {
		return last30DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#setLast30DaysActivityAverage(double)
	 */
	public void setLast30DaysActivityAverage(double last30DaysActivityAverage) {
		this.last30DaysActivityAverage = last30DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#getLast365DaysActivityAverage()
	 */
	public double getLast365DaysActivityAverage() {
		return last365DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#setLast365DaysActivityAverage(double)
	 */
	public void setLast365DaysActivityAverage(double last365DaysActivityAverage) {
		this.last365DaysActivityAverage = last365DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#getLast7DaysActivityAverage()
	 */
	public double getLast7DaysActivityAverage() {
		return last7DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#setLast7DaysActivityAverage(double)
	 */
	public void setLast7DaysActivityAverage(double last7DaysActivityAverage) {
		this.last7DaysActivityAverage = last7DaysActivityAverage;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#getTotalActivity()
	 */
	public long getTotalActivity() {
		return totalActivity;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.SummaryActivityTotals#setTotalActivity(long)
	 */
	public void setTotalActivity(long totalActivity) {
		this.totalActivity = totalActivity;
	}

	
}
