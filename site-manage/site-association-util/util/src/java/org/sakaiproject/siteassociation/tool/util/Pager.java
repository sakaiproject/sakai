/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.siteassociation.tool.util;

/**
 * This is a utility class for storing page properties of a list. Used with the
 * sakai 'pager' and myfaces tomahawk 'dataTable' jsf tags.
 * 
 * @author The Sakai Foundation.
 */
public class Pager {

	protected Integer totalItems;

	protected Integer firstItem;

	protected Integer pageSize;

	public Pager(Integer totalItems, Integer firstItem, Integer pageSize) {
		this.totalItems = totalItems;
		this.firstItem = firstItem;
		this.pageSize = pageSize;
	}

	public Integer getFirstItem() {
		return firstItem;
	}

	public void setFirstItem(Integer firstItem) {
		this.firstItem = firstItem;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}
}
