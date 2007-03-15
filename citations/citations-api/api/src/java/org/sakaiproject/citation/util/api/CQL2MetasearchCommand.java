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
