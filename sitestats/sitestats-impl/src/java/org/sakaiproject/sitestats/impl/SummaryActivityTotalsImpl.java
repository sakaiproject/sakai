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
