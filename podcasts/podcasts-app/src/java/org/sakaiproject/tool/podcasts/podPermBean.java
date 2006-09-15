/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/podcasts/trunk/podcasts-app/src/java/org/sakaiproject/tool/podcasts/podPermBean.java $
 * $Id: podPermBean.java 14691 2006-09-15 12:36:27Z josrodri@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.tool.podcasts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIColumn;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;


public class podPermBean {
	
	public class HtmlDynamicColumnCheckboxTable extends HtmlDataTable {

		private List dataTableContents;
		private int numColumns = -1;
		private String bindingVar;

		/**
		 * Returns the binding variable to be used in
		 * binding the Faces component to a Bean property
		 */
		public String getBindingVar() {
			return bindingVar;
		}

		/**
		 * Sets the binding variable to be used when
		 * binding the Bean property to the Faces component
		 */
		public void setBindingVar(String bindingVar) {
			this.bindingVar = bindingVar;
		}

		public HtmlDynamicColumnCheckboxTable() {

		}

		/**
		 * This constructs the Faces component structure of the
		 * table. It is created column by column since each
		 * column's type is homogeneous.
		 * 
		 * @ param headers
		 * 			A list of the header for each column
		 */
		public void prepareDCDataTable(List headers) {

			numColumns = headers.size();
			this.setNumColumns(numColumns);

			Iterator headerIter = headers.iterator();

			// Set columns.
			for (int i = 0; i < numColumns; i++) {

				// Set header (optional).
				UIOutput header = new UIOutput();
				header.setValue(headerIter.next());
				UIColumn column = new UIColumn();

				// Set output.
				if (i == 0) {
					UIOutput output = new UIOutput();
					ValueBinding aRow = FacesContext.getCurrentInstance()
							.getApplication().createValueBinding(
									"#{" + bindingVar + "[" + i + "]}");
					output.setValueBinding("value", aRow);

					// Set column.
					column = new UIColumn();
					column.getChildren().add(output);
					column.setHeader(header);

				} else {
					UISelectBoolean output = new UISelectBoolean();
					ValueBinding aRow = FacesContext.getCurrentInstance()
							.getApplication().createValueBinding(
									"#{" + bindingVar + "[" + i + "]}");
					output.setValueBinding("value", aRow);

					// Set column.
					column = new UIColumn();
					column.getChildren().add(output);
					column.setHeader(header);

				}

				// Add column.
				this.getChildren().add(column);
			}
		}

		/**
		 * This returns the actual Faces component for each cell.
		 * 
		 * @param rowId
		 * 			The Faces component for the first column of each row
		 * 
		 * @param colNumber
		 * 			Which column are we currently constructing
		 * 
		 * @return Object
		 * 			Either the component passed in (column = 0) or a
		 * 			HtmlSelectBooleanCheckbox (every other column)
		 **/
		private Object getRowCell(Object rowId, int colNumber) {

			if (colNumber == 0) {
				// first column of row, add columnId
				return rowId;
			
			}
			else {
				//Create a checkbox TODO: pull info to determine if checked
				HtmlSelectBooleanCheckbox checkboxCell = new HtmlSelectBooleanCheckbox();

				// TODO: from permissions, determine if should be checked
				checkboxCell.setSelected(false);
				checkboxCell.setSubmittedValue(((String) rowId) + "_"
						+ colNumber);
				checkboxCell.setRendererType("javax.faces.Checkbox");

				// create MethodBinding so when checkbox checked, can process it
				//	    	    		Class [] classArray = new Class[1];
				//    	    		classArray[0] = new ValueChangeEvent(checkboxCell, Boolean.FALSE, Boolean.FALSE);

				//	    	    		MethodBinding mb = FacesContext.getCurrentInstance().getApplication().createMethodBinding("processCheckboxStateChange", classArray);
				//    	    		checkboxCell.setValueChangeListener(mb);
				return checkboxCell;

			}

		}

		/**
		 * Returns the dataTableContents.
		 */
		public List getDataTableContents() {

			return dataTableContents;
		}

		/**
		 * Sets the actual contents of the data table
		 */
		public void setDataTableContents(List dataTableContents) {
			this.dataTableContents = dataTableContents;
		}

		/**
		 * This constructs the actual contents of the table
		 * 
		 * @param firstColumn
		 * 			List of the labels for the first column
		 * @param headerRow
		 * 			List of the labels for the headers for each column
		 */
		public void setDataTableContents(List firstColumn, List headerRow) {

			dataTableContents = new ArrayList();

			Iterator RIter = firstColumn.iterator();

			int rows = firstColumn.size();

			for (int i = 0; i < rows; i++) {
				List thisRow = new ArrayList();

				Object columnId = RIter.next();

				for (int colNumber = 0; colNumber < numColumns; colNumber++) {
					Object cell = getRowCell(columnId, colNumber);

					if (colNumber == 0) {
						thisRow.add(cell);

					} else {
						thisRow.add(cell);

					}
				}

				dataTableContents.add(thisRow);
			}

		}

		/**
		 * @param numColumns The numColumns to set.
		 */
		public void setNumColumns(int numColumns) {
			this.numColumns = numColumns;
		}

	}

	// constants
	private final String CONTENT = "content";
	private final String DOT = ".";

	private HtmlDynamicColumnCheckboxTable permTable;

	private String siteName;

	private List permTableDataList;

	private boolean[] maintainPerms = new boolean[4];

	private boolean[] accessPerms = new boolean[4];

	// injected beans
	private Log LOG = LogFactory.getLog(podPermBean.class);

	private PodcastService podcastService;

	public podPermBean() {
		for (int i = 0; i < 4; i++) {
			maintainPerms[i] = false;
			accessPerms[i] = false;
		}

		maintainPerms[1] = accessPerms[1] = true;
	}

	public podPermBean(boolean[] mPerms, boolean[] aPerms) {
		for (int i = 0; i < 4; i++) {
			maintainPerms[i] = mPerms[i];
			accessPerms[i] = mPerms[i];
		}
	}

	public boolean getmNew() {
		return maintainPerms[0];
	}

	public void setmNew(boolean newPerm) {
		maintainPerms[0] = newPerm;
	}

	public boolean getmRead() {
		return maintainPerms[1];
	}

	public void setmRead(boolean newPerm) {
		maintainPerms[1] = newPerm;
	}

	public boolean getmRevise() {
		return maintainPerms[2];
	}

	public void setmRevise(boolean newPerm) {
		maintainPerms[2] = newPerm;
	}

	public boolean getmDelete() {
		return maintainPerms[3];
	}

	public void setmDelete(boolean newPerm) {
		maintainPerms[3] = newPerm;
	}

	public boolean getaNew() {
		return accessPerms[0];
	}

	public void setaNew(boolean newPerm) {
		accessPerms[0] = newPerm;
	}

	public boolean getaRead() {
		return accessPerms[1];
	}

	public void setaRead(boolean newPerm) {
		accessPerms[1] = newPerm;
	}

	public boolean getaRevise() {
		return maintainPerms[2];
	}

	public void setaRevise(boolean newPerm) {
		accessPerms[2] = newPerm;
	}

	public boolean getaDelete() {
		return accessPerms[3];
	}

	public void setaDelete(boolean newPerm) {
		accessPerms[3] = newPerm;
	}

	public String processPermChange() {
		return "cancel";
	}

	public String processPermCancel() {
		return "cancel";
	}

	/**
	 * @return Returns the podcastService.
	 */
	public PodcastService getPodcastService() {
		return podcastService;
	}

	/**
	 * @param podcastService The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	/**
	 * Returns a list of user roles
	 * 
	 * @return List 
	 * 			List of user roles (String [])
	 */
	public List getRoleNames() {
		List rolesInfo = new ArrayList();

		String siteId = "/site/" + podcastService.getSiteId();

		try {
			String podcastFolderId = podcastService
					.retrievePodcastFolderId(podcastService.getSiteId());
			Collection podcasts = new ArrayList();
			podcasts.add(podcastFolderId);

			AuthzGroup realm = AuthzGroupService.getAuthzGroup(siteId);

			Set roles = realm.getRoles();

			Iterator iter = roles.iterator();

			while (iter.hasNext()) {
				Role role = (Role) iter.next();

				if (role != null)
					rolesInfo.add(role.getId());
			}

		} 
		catch (GroupNotDefinedException e) {
			LOG.error("GroupNotDefinedException trying to get roles for site " 
							+ podcastService.getSiteId() 	+ ". " + e.getMessage());

		} catch (PermissionException e) {
			LOG.warn("PermissionException trying to get roles for site "
							+ podcastService.getSiteId() + e.getMessage()); 

		}

		return rolesInfo;

	}

	/**
	 * Sets the permissions table
	 */
	public void setPermDataTable(HtmlDataTable permDataTable) {
		this.permTable = (HtmlDynamicColumnCheckboxTable) permDataTable;

	}

	/**
	 * Returns the permissions table
	 */
	public HtmlDataTable getPermDataTable() {
		List roleNames = getRoleNames();
		List permNames = getPermNames();
		List permNamesPlus = new ArrayList();

		permNamesPlus.add("Role");

		Iterator LIter = permNames.iterator();

		while (LIter.hasNext()) {
			permNamesPlus.add(LIter.next());
		}

		permTable = new HtmlDynamicColumnCheckboxTable();

		permTable.setStyleClass("listHier lines");
		permTable.setCellpadding("0");
		permTable.setCellspacing("0");
		permTable.setBorder(0);
		permTable.setBindingVar("permItem");
		permTable.setHeaderClass("navIntraTool");
		permTable.prepareDCDataTable(permNamesPlus);
		permTable.setDataTableContents(roleNames, permNamesPlus);

		return (HtmlDataTable) permTable;

	}

	/**
	 * Returns the names of the permissions (functions) available
	 * 
	 * @return List 
	 * 			List of permissions (functions) available (String[])
	 */
	public List getPermNames() {

		List permNames = new ArrayList();

		List allFunctions = FunctionManager.getRegisteredFunctions();

		Iterator fIter = allFunctions.iterator();

		while (fIter.hasNext()) {
			String permission = (String) fIter.next();

			// TODO: Determine correct way to filter which permissions to show
			if (permission.indexOf(CONTENT) != -1) {
				String actPermName = permission.substring(permission
						.indexOf(DOT) + 1);
				permNames.add(actPermName);

			}

		}

		return permNames;
	}

	/**
	 * @return Returns the permTableDataList.
	 */
	public List getPermDataTableList() {
		return permTable.getDataTableContents();

	}

	/**
	 * @param permTableDataList The permTableDataList to set.
	 */
	public void setPermTableDataList(List permTableDataList) {
		this.permTableDataList = permTableDataList;
	}

	/**
	 * Returns the site name if valid site or empty string if not.
	 * 
	 * @return Returns the siteName.
	 */
	public String getSiteName() {
		siteName = "";

		try {
			siteName = SiteService.getSite(podcastService.getSiteId())
					.getTitle();

		} catch (IdUnusedException e) {
			LOG.error("IdUnusedException attempting to get site name for site. "
					+ e.getMessage());

		}

		return siteName;
	}

	/**
	 * @param siteName The siteName to set.
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

}