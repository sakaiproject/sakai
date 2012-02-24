/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.search.api.rdf;

import java.util.List;

/**
 * @author ieb
 *
 */
public interface RDFSearchService
{
	
	/**
	 * adds a block of RDF expressed as XML-RDF to the store
	 * @param data
	 * @throws RDFIndexException
	 */
	void addData(String data) throws RDFIndexException;
	
	/**
	 * Performs a table based search using RQL on the RDF store returning a list of results. 
	 * Ideally the implementation should not force binding to the underlying provider in the list
	 * @param searchSpec
	 * @return
	 * @throws RDFSearchException
	 */
	List search(String searchSpec) throws RDFSearchException;

}
