/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf.backingbean;

import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Stores user preferences for table sorting and paging.  These preferences are
 * currently implemented in session-scope, though this could be reimplemented
 * to store preferences across sessions.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class PreferencesBean extends CourseDependentBean {

	private static final long serialVersionUID = 1L;

	public PreferencesBean() {
		overviewSortColumn = "title";
		overviewSortAscending = true;

		rosterSortColumn = "studentName";
		rosterSortAscending = true;
		rosterMaxDisplayedRows = 50;
	}


	protected int maxNameLength;
	protected String overviewSortColumn;
	protected boolean overviewSortAscending;
	protected String overviewMyFilter;
	protected String overviewCategoryFilter;

	protected String rosterSortColumn;
	protected boolean rosterSortAscending;
	protected int rosterMaxDisplayedRows;
	protected String rosterFilter;

	protected String editStudentSectionsSortColumn;
	protected boolean editStudentSectionsSortAscending;
	protected String editStudentSectionsMyFilter;
	protected String editStudentSectionsCategoryFilter;

	public boolean isOverviewSortAscending() {
		return overviewSortAscending;
	}
	public void setOverviewSortAscending(boolean overviewSortAscending) {
		this.overviewSortAscending = overviewSortAscending;
	}
	public String getOverviewSortColumn() {
		return overviewSortColumn;
	}
	public void setOverviewSortColumn(String overviewSortColumn) {
		this.overviewSortColumn = overviewSortColumn;
	}
	public int getRosterMaxDisplayedRows() {
		return rosterMaxDisplayedRows;
	}
	public void setRosterMaxDisplayedRows(int rosterMaxDisplayedRows) {
		this.rosterMaxDisplayedRows = rosterMaxDisplayedRows;
	}
	public boolean isRosterSortAscending() {
		return rosterSortAscending;
	}
	public void setRosterSortAscending(boolean rosterSortAscending) {
		this.rosterSortAscending = rosterSortAscending;
	}
	public String getRosterSortColumn() {
		return rosterSortColumn;
	}
	public void setRosterSortColumn(String rosterSortColumn) {
		this.rosterSortColumn = rosterSortColumn;
	}
	public int getMaxNameLength() {
		return maxNameLength;
	}
	public void setMaxNameLength(int l) {
		this.maxNameLength = l;
	}

	public boolean isEditStudentSectionsSortAscending() {
		return editStudentSectionsSortAscending;
	}

	public void setEditStudentSectionsSortAscending(
			boolean editStudentSectionsSortAscending) {
		this.editStudentSectionsSortAscending = editStudentSectionsSortAscending;
	}

	public String getEditStudentSectionsSortColumn() {
		return editStudentSectionsSortColumn;
	}

	public void setEditStudentSectionsSortColumn(
			String editStudentSectionsSortColumn) {
		this.editStudentSectionsSortColumn = editStudentSectionsSortColumn;
	}

	public String getEditStudentSectionsCategoryFilter() {
		return editStudentSectionsCategoryFilter;
	}

	public void setEditStudentSectionsCategoryFilter(
			String editStudentSectionsCategoryFilter) {
		this.editStudentSectionsCategoryFilter = editStudentSectionsCategoryFilter;
	}

	public String getEditStudentSectionsMyFilter() {
		return editStudentSectionsMyFilter;
	}

	public void setEditStudentSectionsMyFilter(String editStudentSectionsMyFilter) {
		this.editStudentSectionsMyFilter = editStudentSectionsMyFilter;
	}

	public String getOverviewCategoryFilter() {
		return overviewCategoryFilter;
	}

	public void setOverviewCategoryFilter(String overviewCategoryFilter) {
		this.overviewCategoryFilter = overviewCategoryFilter;
	}

	public String getOverviewMyFilter() {
		return overviewMyFilter;
	}

	public void setOverviewMyFilter(String overviewMyFilter) {
		this.overviewMyFilter = overviewMyFilter;
	}

	public void setRosterFilter(String rosterFilter) {
		this.rosterFilter = rosterFilter;
	}

	public String getRosterFilter() {
		return rosterFilter;
	}
}
