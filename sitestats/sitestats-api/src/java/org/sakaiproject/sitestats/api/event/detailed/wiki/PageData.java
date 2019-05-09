/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.wiki;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a wiki page
 * @author plukasew
 */
public class PageData implements ResolvedEventData
{
	public final String name;
	public final String url;

	/**
	 * Constructor
	 * @param name the name of the page
	 * @param url the url to the page
	 */
	public PageData(String name, String url)
	{
		this.name = name;
		this.url = url;
	}
}
