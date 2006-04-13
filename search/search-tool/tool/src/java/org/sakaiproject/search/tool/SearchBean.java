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
package org.sakaiproject.search.tool;

import java.io.UnsupportedEncodingException;

/**
 * An interface describing the backing bean to the search page
 * @author ieb
 *
 */
public interface SearchBean {

	/**
	 * get an html fragmnent representing the search results
	 * 
	 * @param searchItemFormat
	 *            A Message format string {0} is the result index, {1} is the
	 *            item UR, {2} is the item title, {3} is the content fragment,
	 *            {4} is the score
	 * @return
	 */
	String getSearchResults(String searchItemFormat);

	/**
	 * get an html fragment representing a pager the
	 * 
	 * @param pagerFormat
	 *            A MessageFormat format string {0} is the page URL, {1} is the
	 *            page text, {2} is a css class id, 0 for first, 1 for middle, 2
	 *            for end
	 * @return
	 */
	String getPager(String pagerFormat) throws UnsupportedEncodingException;

	/**
	 * Title for the search page
	 * 
	 * @return
	 */
	String getSearchTitle();

	/**
	 * true if the user has admin rights
	 * 
	 * @return
	 */
	boolean hasAdmin();

	/**
	 * Gets the base url for the tool
	 * 
	 * @return
	 */
	String getToolUrl();

	/**
	 * The search text
	 * 
	 * @return
	 */
	String getSearch();

	/**
	 * Format the header, param {0} is the start doc on the page {1} is the end
	 * doc {2} is the total docs, {3} is the time taken.
	 * 
	 * @param headerFormat
	 * @return
	 */
	String getHeader(String headerFormat);
}
