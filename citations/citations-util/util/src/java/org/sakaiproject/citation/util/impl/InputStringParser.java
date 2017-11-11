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
package org.sakaiproject.citation.util.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class InputStringParser
implements org.sakaiproject.citation.util.api.InputStringParser {
	private static final java.util.Set<String> COMMON_WORDS = new java.util.HashSet<String>();
	private static final String DOUBLE_QUOTE = "\"";

	// the parser switches between these two sets of delimiters
	private static final String WHITESPACE_AND_QUOTES = " \t\r\n\"";
	private static final String QUOTES_ONLY ="\"";

	// Common words against which searches will not be performed.
	static {
		COMMON_WORDS.add("a");
		COMMON_WORDS.add("and");
		COMMON_WORDS.add("be");
		COMMON_WORDS.add("for");
		COMMON_WORDS.add("from");
		COMMON_WORDS.add("has");
		COMMON_WORDS.add("i");
		COMMON_WORDS.add("in");
		COMMON_WORDS.add("is");
		COMMON_WORDS.add("it");
		COMMON_WORDS.add("of");
		COMMON_WORDS.add("on");
		COMMON_WORDS.add("to");
		COMMON_WORDS.add("the");
		COMMON_WORDS.add("not");
		COMMON_WORDS.add("or");
	}

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
	public Set<String> parseInputString( String inputString ) {
		if( inputString == null || inputString.trim().equals( "" ) ) {
			return null;
		}
		
		Set<String> result = new HashSet<String>();

		boolean returnTokens = true;
		String currentDelim = WHITESPACE_AND_QUOTES;
		StringTokenizer parser = new StringTokenizer( inputString, currentDelim,
				returnTokens );

		String token = null;
		while ( parser.hasMoreTokens() ) {
			token = parser.nextToken( currentDelim );
			if ( !isDoubleQuote(token) ){
				addNonTrivialWordToResult( token, result );
			}
			else {
				currentDelim = switchDelimiters( currentDelim );
			}
		}
		return result;
	}

	private static boolean isCommonWord( String searchTokenCandidate ) {
		return COMMON_WORDS.contains( searchTokenCandidate );
	}

	private static boolean textHasContent( String text ) {
		return ( text != null ) && ( !text.trim().equals( "" ) );
	}

	private static void addNonTrivialWordToResult( String token, Set<String> result ){
		if ( textHasContent( token ) && !isCommonWord( token.trim() ) ) {
			result.add( token.trim() );
		}
	}

	private static boolean isDoubleQuote( String token ){
		return token.equals( DOUBLE_QUOTE );
	}

	private static String switchDelimiters( String currentDelim ) {
		String result = null;
		if ( currentDelim.equals( WHITESPACE_AND_QUOTES ) ) {
			result = QUOTES_ONLY;
		}
		else {
			result = WHITESPACE_AND_QUOTES;
		}
		return result;
	}

}
