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
package org.sakaiproject.sitestats.api.event.detailed.podcasts;

import java.time.Instant;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data for a podcast
 * @author plukasew
 */
public class PodcastData implements ResolvedEventData
{
	public final String title;
	public final Instant publishTime;
	public final String parentUrl;

	/**
	 * Constructor
	 * @param title the title of the podcast
	 * @param publishTime the date/time the podcast was published
	 * @param parentUrl the url to the podcast's parent container
	 */
	public PodcastData(String title, Instant publishTime, String parentUrl)
	{
		this.title = title;
		this.publishTime = publishTime;
		this.parentUrl = parentUrl;
	}
}
