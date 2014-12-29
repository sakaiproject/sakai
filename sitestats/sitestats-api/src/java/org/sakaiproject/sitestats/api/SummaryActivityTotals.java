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