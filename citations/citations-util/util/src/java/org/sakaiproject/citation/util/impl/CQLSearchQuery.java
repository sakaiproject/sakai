package org.sakaiproject.citation.util.impl;

import org.sakaiproject.citation.util.api.SearchQuery;

public class CQLSearchQuery
implements org.sakaiproject.citation.util.api.CQLSearchQuery {
	
	private static final org.apache.commons.logging.Log LOG =
		org.apache.commons.logging.LogFactory.getLog(
				"org.sakaibrary.common.search.impl.CQLSearchQuery" ); 

	private org.sakaiproject.citation.util.api.SearchQuery searchQuery;
	private String cqlQuery;

	/**
	 * Gets a CQL-formatted search query by converting searchQuery.
	 * 
	 * @param searchQuery SearchQuery object to convert.
	 * @return the search query in CQL format or null if searchQuery is null.
	 */
	public String getCQLSearchQueryString(
			org.sakaiproject.citation.util.api.SearchQuery searchQuery ) {
		
		if( searchQuery == null ) {
			return null;
		}
		
		this.searchQuery = searchQuery;
		
		// get criteria strings
		String keywordString = getKeywordString();
		String titleString = getTitleString();
		String authorString = getAuthorString();
		String subjectString = getSubjectString();
		String yearString = getYearString();
		
		// debugging
		LOG.debug( "going to parse - keyword: " + keywordString +
        "; title: " + titleString + "; author: " + authorString + "; subject: "
        + subjectString + "; year: " + yearString );
    
		// concatenate criteria strings
		StringBuilder searchStringBuilder = new StringBuilder();
		
		if( keywordString != null ) {
			searchStringBuilder.append( keywordString + " " );
		}
		if( titleString != null ) {
			searchStringBuilder.append( titleString + " ");
		}
		if( authorString != null ) {
			searchStringBuilder.append( authorString + " ");
		}
		if( subjectString != null ) {
			searchStringBuilder.append( subjectString + " ");
		}
		if( yearString != null ) {
			searchStringBuilder.append( yearString + " ");
		}
		
		// manipulate the buffer
		String searchString = searchStringBuilder.toString();
		searchString = searchString.substring( 0, searchString.length() - 1 );
		searchString = searchString.replaceAll( "\\s", " and " );
		
		// debugging
		LOG.debug( "full search string to parse: " + searchString );
		
		// convert the searchString to CQL
		org.z3950.zing.cql.CQLParser parser = new org.z3950.zing.cql.CQLParser();
		org.z3950.zing.cql.CQLNode root = null;
		
		try {
			// parse the criteria
			root = parser.parse( searchString );
		} catch( java.io.IOException ioe ) {
			LOG.warn( "CQLSearchQuery.getCQLSearchQueryString() IO " +
					"exception while parsing: " + ioe.getMessage() ); 
		} catch( org.z3950.zing.cql.CQLParseException e ) {
			LOG.warn( "CQLSearchQuery.getCQLSearchQueryString() CQL " +
					"parsing exception while parsing: " + e.getMessage() ); 
		}
		
		cqlQuery = ( root == null ) ? null : root.toCQL();
		
		return cqlQuery;
	}
	
	private String getKeywordString() {
		java.util.Set<String> keywords = searchQuery.getKeywords();
		String keywordString = null;
		
		if( keywords != null ) {
			keywordString = criteriaCleanup( keywords, "keyword" );
		}
		
		return keywordString;
	}
	
	private String getTitleString() {
		java.util.Set<String> titles = searchQuery.getTitles();
		String titleString = null;
		
		if( titles != null ) {
			titleString = criteriaCleanup( titles, "title" );
		}
		
		return titleString;
	}
	
	private String getAuthorString() {
		String authorString = null;
		java.util.Set<String> authors = searchQuery.getAuthors();
		
		if( authors != null ) {
			authorString = criteriaCleanup( authors, "author" );
		}
		
		return authorString;
	}
	
	private String getSubjectString() {
		String subjectString = null;
		java.util.Set<String> subjects = searchQuery.getSubjects();
		
		if( subjects != null ) {
			subjectString = criteriaCleanup( subjects, "subject" );
		}
		
		return subjectString;
	}
	
	private String getYearString() {
		java.util.Set<String> years = searchQuery.getYears();
		String yearString = null;
		
		if( years != null ) {
			yearString = criteriaCleanup( years, "year" );
		}
		
		return yearString;
	}
	
	private String criteriaCleanup( java.util.Set<String> criteriaSet,
			String criteriaName ) {
		java.util.Iterator criteria = criteriaSet.iterator();
		
		StringBuilder result = new StringBuilder();
		while( criteria.hasNext() ) {
			result.append( " " + criteriaName + "=" );
			String criterion = ( String ) criteria.next();
			
			// remove any punctuation
			criterion = criterion.replaceAll( "\\p{Punct}", " " );
			criterion = criterion.trim();
			
			// take care of any adjacent spaces
			criterion = criterion.replaceAll( "\\s+", " " );
			
			// replace spaces with +
			criterion = criterion.replaceAll( "\\s", "+" );
			
			// append this keyword
			result.append( criterion );
		}
		
		return ( result.toString().trim().equals("") ) ? null :
			result.toString().trim();
	}

}
