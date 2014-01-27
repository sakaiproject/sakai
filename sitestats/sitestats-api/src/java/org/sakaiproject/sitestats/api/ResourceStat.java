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


/**
 * Represents a record from the SST_RESOURCES table.
 * This must be {@link java.lang.Comparable} so that the updates can be sorted before being inserted into the database
 * to avoid deadlocks.
 * @author Nuno Fernandes
 */
public interface ResourceStat extends Stat, Comparable<ResourceStat>{
	/** Get the the resource reference (eg. '/content/group/site_id/filename.txt') this record refers to. */
	public String getResourceRef();
	/** Set the the resource reference (eg. '/content/group/site_id/filename.txt') this record refers to. */
	public void setResourceRef(String resourceRef);

	/** Get the the resource action (one of 'new','read','revise','delete') this record refers to. */
	public String getResourceAction();
	/** Set the the resource action (one of 'new','read','revise','delete') this record refers to. */
	public void setResourceAction(String resourceAction);
}
