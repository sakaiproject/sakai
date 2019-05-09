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
 * Data for a comments section. There are two variations on a comments section, normal comments a user adds,
 * and "forced comments" for student pages which are added automatically.
 * 
 * @author plukasew
 */
public class CommentsSectionItemData implements LessonsData
{
	public static final ForcedComments FORCED = new ForcedComments();

	public final PageData parent;

	/**
	 * Constructor
	 * @param parent the page the comments section is on
	 */
	public CommentsSectionItemData(final PageData parent)
	{
		this.parent = parent;
	}

	public static class ForcedComments implements LessonsData
	{
		// Due to Lessons design choices, forced comments cannot be tied back to their student page and therefore will have no page hierarchy
	}
}
