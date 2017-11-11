/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.entity.api;

/**
 * Services which implement ContentExistsAware declare that they are able to determine if they contain content for the site they are in.
 * Note that services must also be registered EntityProducers.
 *
 * @since 11.0
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface ContentExistsAware {

	/**
	 * Does this tool contain content in this site?
	 * 
	 * @param siteId the siteId to use when checking if content exists
	 */
	public boolean hasContent(String siteId);
	
}
