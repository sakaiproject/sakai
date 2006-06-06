/**
 * 
 */
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
