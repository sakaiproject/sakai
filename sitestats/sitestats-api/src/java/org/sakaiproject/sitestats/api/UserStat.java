/**
 * Copyright (c) 2006-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
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
 * This must be {@link java.lang.Comparable} so that the updates can be sorted before being inserted into the database
 * to avoid deadlocks.
 */
public interface UserStat extends Comparable<UserStat> {
	
	/** Get the db row id. */
	public long getId();
	
	/** Set the db row id. */
	public void setId(long id);
	
	/** Get the date this record refers to. */
	public Date getDate();
	
	/** Set the date this record refers to. */
	public void setDate(Date date);

	/** Get the user we are tracking. */
	public String getUserId();
	
	/** Set the user we are tracking */
	public void setUserId(String userId);
	
	/** Get the total times this event was generated on this context and date. */
	public long getCount();
	
	/** Set the total times this event was generated on this context and date. */
	public void setCount(long count);
}
