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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.content.api;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * Interface that allows modification of some content.
 * We don't have access to the response headers so can't do things like zip compression.
 * @author buckett
 *
 */
public interface ContentFilter {

	/**
	 * Check if this content filter should be applied to the resource. This should be a fast 
	 * check.
	 * @param resource The resource being requested.
	 * @return <code>true</code> if a filter should be retrieved using {@link #wrap(OutputStream)}.
	 */
	public boolean isFiltered(ContentResource resource);
	
	/**
	 * Create a filter which will process the content.
	 * @param content A stream to which the output will be written.
	 * @return An output stream which is contains the modified output.
	 */
	public HttpServletResponse wrap(HttpServletResponse response, ContentResource resource);
	
}
