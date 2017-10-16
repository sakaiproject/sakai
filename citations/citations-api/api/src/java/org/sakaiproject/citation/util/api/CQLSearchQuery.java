/**
 * Copyright (c) 2003-2007 The Apereo Foundation
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
package org.sakaiproject.citation.util.api;

public interface CQLSearchQuery {

	/**
	 * Gets a CQL-formatted search query string by converting searchQuery.
	 * 
	 * @param searchQuery SearchQuery object to convert.
	 * @return the search query in CQL format or null if searchQuery is null.
	 */
	public String getCQLSearchQueryString( SearchQuery searchQuery );
}
