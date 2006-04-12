/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.search;

import java.util.Map;

/**
 * @author ieb
 *
 */
public interface SearchResult {

	/**
	 * the result score
	 * @return
	 */
	float getScore();

	/**
	 * The result ID (entity id)
	 * @return
	 */
	String getId();

	/**
	 * All field names in the search record
	 * @return
	 */
	String[] getFieldNames();

	/**
	 * All values in a search field
	 * @param string
	 * @return
	 */
	String[] getValues(String string);
	
	/**
	 * Get a map of values in the result
	 * @return
	 */
	Map getValueMap();
	
	/**
	 * An absolute URL to the resource (no host, protocol or port)
	 * @return
	 */
	String getUrl();
	
	/**
	 * The title of the resource, usually including the type (eg Wiki Page, Resource)
	 * @return
	 */
	String getTitle();
	
	/**
	 * get the index of the search entry over the whole change set
	 * @return
	 */
	int getIndex();
	/**
	 * get the search result content for display
	 */
	String getSearchResult();
	
	

}
