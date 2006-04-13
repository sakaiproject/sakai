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

package org.sakaiproject.search.dao;

import java.util.List;

import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public interface SearchBuilderItemDao
{

	/**
	 * create a new item
	 * 
	 * @return
	 */
	SearchBuilderItem create();

	/**
	 * Update a single item
	 * 
	 * @param sb
	 */
	void update(SearchBuilderItem sb);

	/**
	 * Locate the resource entry
	 * 
	 * @param resourceName
	 * @return
	 */
	SearchBuilderItem findByName(String resourceName);

	/**
	 * count the number of entries pending
	 * 
	 * @return
	 */
	int countPending();

	List getAll();

}
