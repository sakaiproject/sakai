package org.sakaiproject.citation.util.api;

import java.util.Set;

public interface InputStringParser {
	
	/**
	 * Parse keywords into a Set of Strings.  This method recognizes phrases 
	 * (marked using quotation marks) and drops common words (i.e. and, or, not,
	 * to, the, etc.) if they are not part of a phrase.  Each element of the
	 * resulting Set will be a single term or a phrase.
	 * 
	 * @param inputString the input a user has submitted (i.e. from an HTML 
	 * input field in a form)
	 * @return a Set containing individual search terms or phrases or null if
	 * inputString is null or empty.
	 */
	public Set<String> parseInputString( String inputString );
}
