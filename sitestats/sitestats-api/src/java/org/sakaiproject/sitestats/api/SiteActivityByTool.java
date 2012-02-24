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

import org.sakaiproject.sitestats.api.event.ToolInfo;


public interface SiteActivityByTool {	
	/** Get the context (site id) this record refers to. */
	public String getSiteId();
	
	/** Set the context (site id) this record refers to. */
	public void setSiteId(String siteId);
	
	/** Get the tool this record refers to. */
	public ToolInfo getTool();
	
	/** Set the tool this record refers to. */
	public void setTool(ToolInfo toolInfo);
	
	/** Get the total tool events generated on this context and date. */
	public long getCount();
	
	/** Set the total tool events generated on this context and date. */
	public void setCount(long count);
}
