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

import java.util.Date;

/**
 * Record with time spent in site, by date and user.
 * This must be {@link java.lang.Comparable} so that the updates can be sorted before being inserted into the database
 * to avoid deadlocks.
 * @author Nuno Fernandes
 */
public interface SitePresence extends Stat, Comparable<SitePresence> {

	/** Get time spent (in milliseconds) */
	public long getDuration();
	
	/** Set time spent (in milliseconds) */
	public void setDuration(long duration);
	
	/**
	 * Get (temporary) last visit start time. This is used when the begin event happens in one batch update and
	 * the end update happens in another batch update and so we need to persist the start time. */
	public Date getLastVisitStartTime();
	
	/** Set (temporary) last visit start time */
	public void setLastVisitStartTime(Date lastVisitStartTime);
}
