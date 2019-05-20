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
package org.sakaiproject.sitestats.api.event.detailed.lessons;

import org.apache.commons.lang3.StringUtils;

/**
 * Data for a Lessons item which has its content embedded in the page as opposed to presented as an external content link.
 */
public class EmbeddedItemData implements LessonsData
{
	public final String desc;
	public final PageData parentPage;

	/**
	 * Constructor
	 * @param description an optional description for the items (as shown on the page in Lessons)
	 * @param parentPage the page the item is embedded into
	 */
	public EmbeddedItemData(String description, PageData parentPage)
	{
		desc = StringUtils.trimToEmpty(description);
		this.parentPage = parentPage;
	}
}
