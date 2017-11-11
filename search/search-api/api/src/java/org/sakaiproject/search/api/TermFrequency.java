/**
 * Copyright (c) 2003-2006 The Apereo Foundation
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
package org.sakaiproject.search.api;

public interface TermFrequency
{

	/**
	 * get the terms, ordered by frequency 
	 * @return
	 */
	String[] getTerms();

	/**
	 * get the frequencies
	 * @return
	 */
	int[] getFrequencies();
	

}
