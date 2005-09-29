/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.section.jsf.backingbean;

/**
 * Stores user preferences for table sorting and paging.  These preferences are
 * currently implemented in session-scope, though this could be reimplemented
 * to store preferences across sessions.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class PreferencesBean {
	
	public PreferencesBean() {
		overviewSortColumn = "title";
		overviewSortAscending = true;

		rosterSortColumn = "studentName";
		rosterSortAscending = true;
		rosterMaxDisplayedRows = 10;
	}
	
	protected String overviewSortColumn;
	protected boolean overviewSortAscending;
	
	protected String rosterSortColumn;
	protected boolean rosterSortAscending;
	protected int rosterMaxDisplayedRows;

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


}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
