/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RosterPreferences {
	private static final Log LOG = LogFactory.getLog(RosterPreferences.class);
	
	public enum Column {
		DISPLAY_NAME("sortName"), DISPLAY_ID("displayId"), ROLE("role"), EMAIL("email");
		
		private final String sortColumnName;
		
		Column(String sortColumnName) {
			this.sortColumnName = sortColumnName;
		}
		
		@Override public String toString() {
			return this.sortColumnName;
		}
	}

	private static final Set<String> SORT_COLUMNS = new HashSet<String>(
														Arrays.asList(
															Column.DISPLAY_NAME.toString(), Column.DISPLAY_ID.toString(),
															Column.ROLE.toString(), Column.EMAIL.toString()));
		
	protected String sortColumn;
	protected boolean sortAscending;
	protected boolean displayNames;
	protected boolean displayProfilePhotos;

	// Keep the "return page" here, since this is a session scoped bean.  Ugh, this is so nasty.
	protected String returnPage;
	
	protected ServicesBean services;
	public void setServices(ServicesBean services) {
		this.services = services;
	}

	public RosterPreferences() {
		sortAscending = true;
		displayNames = true;
	}

	public String getReturnPage() {
		return returnPage;
	}

	public void setReturnPage(String returnPage) {
		this.returnPage = returnPage;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
	public String getSortColumn() {
		if (this.sortColumn == null) {
			this.sortColumn = determineSortColumn();
		}
		return this.sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public boolean isDisplayNames() {
		return displayNames;
	}

	public void setDisplayNames(boolean displayNames) {
		this.displayNames = displayNames;
	}

	public boolean isDisplayProfilePhotos() {
		return displayProfilePhotos;
	}

	public void setDisplayProfilePhotos(boolean displayProfilePhotos) {
		this.displayProfilePhotos = displayProfilePhotos;
	}
	
	private String determineSortColumn() {
		String sortColumn;
		String defaultSortColumn = this.services.serverConfigurationService.getString("roster.defaultSortColumn");
		if (valid(defaultSortColumn)) {
			sortColumn = defaultSortColumn;
		}
		else {
			sortColumn = Column.DISPLAY_NAME.toString();
			LOG.warn("defaultSortColumn value = " + defaultSortColumn + " is invalid, must be one of " + SORT_COLUMNS.toString());
			LOG.warn("Check your default.sakai.preferences and sakai.preferences for a valid roster.defaultSortColumn value");				
			LOG.warn("Defaulting to sorting by " + sortColumn);	
		}
		return sortColumn;
	}

	private boolean valid(String defaultSortColumn) {
		boolean valid = false;
		if (defaultSortColumn != null) {
			if (SORT_COLUMNS.contains(defaultSortColumn.trim())) {
				valid = true;
			}
		}
		return valid;
	}
}
