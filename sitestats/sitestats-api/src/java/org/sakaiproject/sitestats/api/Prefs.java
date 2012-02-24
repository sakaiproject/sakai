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

public interface Prefs {
	/** Get the bd row id. */
	public long getId();
	
	/** Set the bd row id. */
	public void setId(long id);
	
	/** Get the preferences context (site id). */
	public String getSiteId();
	
	/** Set the preferences context (site id). */
	public void setSiteId(String siteId);

	/** Get the preferences XML string. */
	public String getPrefs();
	
	/** Set the preferences XML string. */
	public void setPrefs(String prefs);
}
