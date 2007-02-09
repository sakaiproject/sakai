/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;

import org.sakaiproject.content.api.ContentResource;
/**
 * A content digester converts  a content stream into a digested form
 * @author ieb
 *
 */
public interface ContentDigester
{


	/**
	 * Can the mime type be handled by this digester
	 * @param mimeType
	 * @return
	 */

	boolean accept(String mimeType);

	/**
	 * @param contentResource
	 * @param minWordLength minimum acceptable word length
	 * @return
	 */
	String getContent(ContentResource contentResource, int minWordLength);

	/**
	 * @param contentResource
	 * @param minWordlength
	 * @return
	 */
	Reader getContentReader(ContentResource contentResource, int minWordlength);

}
