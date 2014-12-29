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
