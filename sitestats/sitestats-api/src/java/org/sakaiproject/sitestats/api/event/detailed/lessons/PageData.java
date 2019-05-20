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

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Data for a Lessons page
 * @author plukasew
 */
public class PageData implements LessonsData
{
	public final String title;
	public final List<String> pageHierarchy;

	public static final DeletedPage DELETED_PAGE = new PageData.DeletedPage();
	public static final String DELETED_HIERARCHY_PAGE = "DHP";

	/**
	 * Constructor
	 * @param title the title of the page
	 * @param pageHierarchy the path to this page, as a list of page titles
	 */
	public PageData(String title, List<String> pageHierarchy)
	{
		this.title = StringUtils.trimToEmpty(title);
		this.pageHierarchy = Collections.unmodifiableList(pageHierarchy);
	}

	// a deleted page (no further info is available)
	public static final class DeletedPage implements LessonsData { }
}
