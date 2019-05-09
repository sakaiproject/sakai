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

/**
 * Data for a Lessons item which is presented on the page as an external content link as opposed
 * to having its content embedded in the page.
 */
public class ContentLinkItemData implements LessonsData
{
	public final String name;
	public final PageData parentPage;

	/**
	 * Constructor
	 * @param name the name of the item as it appears on the Lessons page
	 * @param parentPage the page the item appears on
	 */
	public ContentLinkItemData(String name, PageData parentPage)
	{
		this.name = name;
		this.parentPage = parentPage;
	}
}
