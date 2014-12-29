/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/content/api/ContentEntity.java $
 * $Id: ContentEntity.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.content.api;


/**
 * Interface that allows modification of some content.
 * Originally the servlet output stream was wrapped, but wrapping the ContentResource allows ranged gets to still work.
 *
 * @author Matthew Buckett
 *
 */
public interface ContentFilter {

	/**
	 * Create a filter which will process the content.
	 * @param resource The content resource which to filter. If the filter doesn't want to do anything it should just
	 *                 return the same resource without modifying it.
	 * @return An content resource which might be wrapped to modify it's behaviour in some way.
	 */
	public ContentResource wrap(ContentResource resource);
	
}
