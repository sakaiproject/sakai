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
