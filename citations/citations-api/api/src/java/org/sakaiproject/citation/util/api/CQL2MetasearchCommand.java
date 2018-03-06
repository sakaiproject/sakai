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

public interface CQL2MetasearchCommand {

	/**
	 * Converts a CQL-formatted search query into a format that a metasearch
	 * engine can understand.  Usually this involves converting the CQL query
	 * into an XML structure and then mapping that XML into a search command the
	 * metasearch engine can understand.
	 * 
	 * @param cqlSearchQuery CQL-formatted search query.
	 * @return search command formatted for a specific metasearch engine.
	 * @see org.z3950.zing.cql.CQLNode.toXCQL()
	 */
	public String doCQL2MetasearchCommand( String cqlSearchQuery );
}
