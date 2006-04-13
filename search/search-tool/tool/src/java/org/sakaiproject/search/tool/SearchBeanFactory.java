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

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.PermissionException;

/**
 * A factory bean to construct backing beans efficiently on a request basis. This is normally injected into the 
 * request attributes for each individual page to construct the backing beans that they require.
 * Ideally there should be a one to one mapping between backing beans and pages, and the page should be as simple as possible
 * @author ieb
 *
 */
public interface SearchBeanFactory {

	public final static String SEARCH_BEAN_FACTORY_ATTR = SearchBean.class.getName();

	/**
	 * create a search bean based on the request
	 * @param request
	 * @return
	 * @throws PermissionException 
	 */
	SearchBean newSearchBean(HttpServletRequest request) throws PermissionException;

	/**
	 * Create a search admin bean based ont he request
	 * @param request
	 * @return
	 * @throws PermissionException 
	 */
	SearchAdminBean newSearchAdminBean(HttpServletRequest request) throws PermissionException;

}
