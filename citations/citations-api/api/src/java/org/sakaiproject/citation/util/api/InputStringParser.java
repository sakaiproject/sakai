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
