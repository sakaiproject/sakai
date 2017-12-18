/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;

@Slf4j
public class podPermBean {

	private final String CONTENT = "content";
	private final String DOT = ".";
	private final String SLASH = "/";
	private final String SITE = "site";
	
	public class DecoratedCheckboxTableRow {
		private String rowName;
		private List checkboxValues;
		private SelectItem [] checkboxSelectValues;
		
		public List getCheckboxValues() {
			return checkboxValues;
		}
		public void setCheckboxValues(List checkboxValues) {
			this.checkboxValues = checkboxValues;
		}
		public String getRowName() {
			return rowName;
		}
		public void setRowName(String rowName) {
			this.rowName = rowName;
		}
		public SelectItem[] getCheckboxSelectValues() {
			checkboxSelectValues = new SelectItem[checkboxValues.size()];
			
			Iterator ckboxValIter = checkboxValues.iterator();
			
			for  (int i = 0; i < checkboxValues.size(); i++) {
				checkboxSelectValues[i] = new SelectItem((Boolean) ckboxValIter.next(), " ");
			}
			
			return checkboxSelectValues;
		}
		public void setCheckboxSelectValues(SelectItem[] checkboxSelectValues) {
			this.checkboxSelectValues = checkboxSelectValues;
		}
	}

	public class HtmlDynamicColumnCheckboxTable extends HtmlDataTable {

		/** The faces cell values to be put in the table */
		private List dataTableContents;

		/** The number of columns to be put in the table */
		private int numColumns = -1;

		/** A String value to bind to each checkbox in the table to a bean property */
		private String checkboxBindingVar;
		
		/** A String value to bind the first column in the table to a bean property */
		private String firstColumnBindingVar;

		public String getCheckboxBindingVar() {
			return checkboxBindingVar;
		}

		public void setCheckboxBindingVar(String checkboxBindingVar) {
			this.checkboxBindingVar = checkboxBindingVar;
		}

		public String getFirstColumnBindingVar() {
			return firstColumnBindingVar;
		}

		public void setFirstColumnBindingVar(String firstColumnBindingVar) {
			this.firstColumnBindingVar = firstColumnBindingVar;
		}

		/**
		 * 
		 *
		 */
		public HtmlDynamicColumnCheckboxTable() {

		}

		/**
		 * This constructs the Faces component structure of the table. It is
		 * created column by column since each column's type is homogeneous.
		 *  @ param headers A list of the header for each column
		 */
		public void prepareDCDataTable(List headers) {

			numColumns = headers.size();
			this.setNumColumns(numColumns);

			Iterator headerIter = headers.iterator();

			// Set columns.
			for (int i = 0; i < numColumns; i++) {

				UIOutput header = new UIOutput();
				header.setValue(headerIter.next());

				UIColumn column = new UIColumn();

				// Set output.
				if (i == 0) {
					UIOutput output = new UIOutput();
					ValueBinding aRow = FacesContext.getCurrentInstance()
														.getApplication()
															.createValueBinding("#{cellItem[" + i + "]}");
					output.setValueBinding("value", aRow);

					// Create column FACES component and add label to it (column 0).
					column = new UIColumn();
					column.getChildren().add(output);
					column.setHeader(header);

				} 
				else {
					UISelectBoolean output = new UISelectBoolean();
					ValueBinding aRow = FacesContext.getCurrentInstance()
														.getApplication()
															.createValueBinding("#{cellItem[" + i + "]}");
					output.setValueBinding("value", aRow);

					// Create column FACES component and add checkbox to it.
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
		 *            The String name for the row being constructed (to be put
		 *            in column 0)
		 * 
		 * @param colNumber
		 *            Which column are we currently constructing
		 * 
		 * @param select
		 *            Whether the checkbox should be checked or not (ignored for
		 *            column 0)
		 * 
		 * @return Object Either the component passed in (column = 0) or a
		 *         HtmlSelectBooleanCheckbox (every other column)
		 */
		private Object getRowCell(String rowId, int colNumber, boolean select) {

			if (colNumber == 0) {
				// first column of row, add rowId
/*				HtmlOutputText labelCell = new HtmlOutputText();
				ValueBinding aRow = FacesContext.getCurrentInstance()
										.getApplication()
											.createValueBinding("#{" + firstColumnBindingVar + "}");
				labelCell.setValueBinding("value", aRow);

				return labelCell;
*/				return rowId;
			} 
			else {
				// Create a checkbox
				HtmlSelectBooleanCheckbox checkboxCell = new HtmlSelectBooleanCheckbox();

				checkboxCell.setSelected(select);
//				checkboxCell.setSubmittedValue(rowId + "_" + colNumber);
				checkboxCell.setRendererType("javax.faces.Checkbox");

				ValueBinding aRow = FacesContext.getCurrentInstance()
										.getApplication()
											.createValueBinding("#{" + checkboxBindingVar + "[" + (colNumber-1) + "]}");
				checkboxCell.setValueBinding("value", aRow);

				// create MethodBinding so when checkbox checked, can process it
				// Class [] classArray = new Class[1];
				// classArray[0] = new ValueChangeEvent(checkboxCell,
				// Boolean.FALSE, Boolean.FALSE);

				// MethodBinding mb =
				// FacesContext.getCurrentInstance().getApplication().createMethodBinding("processCheckboxStateChange",
				// classArray);
				// checkboxCell.setValueChangeListener(mb);
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
		 *            List of the labels for the first column
		 * @param headerRow
		 *            List of the labels for the headers for each column
		 */
		public void setDataTableContents(List firstColumn, List headerRow) {
			dataTableContents = new ArrayList();

			final int width = headerRow.size();
			final AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);
			
			final Iterator roleIter = firstColumn.iterator();
			int rows = firstColumn.size();

			for (int i = 0; i < rows; i++) {
				final List thisRow = new ArrayList();
				final String roleName = (String) roleIter.next();
				final Collection podcasts = new ArrayList();

				String podcastFolderRef = "";
				Iterator permIter = null;
				AuthzGroup podAuthzGroup = null;

				try {
					podcastFolderRef = SLASH + CONTENT  
											+ podcastService.retrievePodcastFolderId(podcastService.getSiteId());
					podAuthzGroup = authzGroupService.getAuthzGroup(podcastFolderRef);
				} 
				catch (PermissionException e) {
					log.warn("PermissionException trying to get roles for site "
								+ podcastService.getSiteId() + e.getMessage(), e);
				} 
				catch (GroupNotDefinedException e) {
					log.info("GroupNotDefinedException while constructing permission data table contents for site "
									+ podcastService.getSiteId() + ".", e);
				}

				// Create a list of azGroup ids to get permissions
				Collection podcastCollection = new ArrayList();
				if (podAuthzGroup != null) {
					podcastCollection.add(podAuthzGroup.getId());
				}
				
				podcastCollection.add(getSiteRef());

				// get functions (permissions) for this role
				Set rolePerms = authzGroupService.getAllowedFunctions(roleName,podcastCollection);
				permIter = rolePerms.iterator();

				Iterator headerIter = headerRow.iterator();

				for (int colNumber = 0; colNumber < numColumns; colNumber++) {
					Object cell = null;

					if (colNumber != 0) {
						final String permCheck = CONTENT + DOT + (String) headerIter.next();
						final boolean isChecked = rolePerms.contains(permCheck);

						cell = getRowCell(roleName, colNumber, isChecked);
					} 
					else {
						cell = getRowCell(roleName, colNumber, false);
						headerIter.next();
					}

					thisRow.add(cell);
				}

				dataTableContents.add(thisRow);
			}

		}

		/**
		 * @param numColumns
		 *            The numColumns to set.
		 */
		public void setNumColumns(int numColumns) {
			this.numColumns = numColumns;
		}

	}

	/** dataTable subclass dynamically created */
	private HtmlDynamicColumnCheckboxTable permTable;
	
	/** Name of the site for display purposes */
	private String siteName;
	
	/** List of UI components to populate table */
	private List permTableDataList;

	/** List of values to put in table */
	private List checkboxTableValues;

	// injected beans
	private PodcastService podcastService;

	/**
	 * 
	 *
	 */
	public podPermBean() {
	}

	/**
	 * 
	 * @return
	 */
	public String processPermChange() {
		return "cancel";
	}

	/**
	 * 
	 * @return
	 */
	public String processPermCancel() {
		return "cancel";
	}

	/**
	 * @param podcastService
	 *            The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	/**
	 * Returns a list of user roles
	 * 
	 * @return List List of user roles (String [])
	 */
	public List getRoleNames() {
		List rolesInfo = new ArrayList();

		String siteRef = getSiteRef();

		try {
			AuthzGroup realm = ComponentManager.get(AuthzGroupService.class).getAuthzGroup(siteRef);

			Set roles = realm.getRoles();
			Iterator iter = roles.iterator();

			while (iter.hasNext()) {
				Role role = (Role) iter.next();

				if (role != null)
					rolesInfo.add(role.getId());
			}

		} catch (GroupNotDefinedException e) {
			log.error("GroupNotDefinedException trying to get roles for site "
							+ podcastService.getSiteId() + ". " + e.getMessage(), e);
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
		permNamesPlus.addAll(permNames);

		permTable = new HtmlDynamicColumnCheckboxTable();

		permTable.setStyleClass("listHier lines");
		permTable.setCellpadding("0");
		permTable.setCellspacing("0");
		permTable.setBorder(0);
		permTable.setCheckboxBindingVar("podPerms.checkboxTableValues[1].checkboxValues");
		permTable.setHeaderClass("navIntraTool");
		permTable.prepareDCDataTable(permNamesPlus);
		permTable.setDataTableContents(roleNames, permNamesPlus);
		permTable.setValue("#{podPerms.checkboxTableValues}");
		permTable.setVar("permItem");

		setCheckboxTableValues();
		
		return (HtmlDataTable) permTable;

	}

	/**
	 * Returns the names of the permissions (functions) available
	 * 
	 * @return List List of permissions (functions) available (String[])
	 */
	public List getPermNames() {
		final List permNames = new ArrayList();
		final List allFunctions = FunctionManager.getRegisteredFunctions(CONTENT);

		final Iterator fIter = allFunctions.iterator();

		while (fIter.hasNext()) {
			final String permission = (String) fIter.next();

			// TODO: Determine correct way to filter which permissions to show
			if (permission.indexOf("all") == -1 && permission.indexOf("hidden") == -1) {
				final String actPermName = permission.substring(permission
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
	 * @param permTableDataList
	 *            The permTableDataList to set.
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
			siteName = SiteService.getSite(podcastService.getSiteId()).getTitle();

		} catch (IdUnusedException e) {
			log.error("IdUnusedException attempting to get site name for site. "
						+ e.getMessage(), e);
		}

		return siteName;
	}

	/**
	 * @param siteName
	 *            The siteName to set.
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	/**
	 * 
	 * @return
	 */
	private String getSiteRef() {
		return SLASH + SITE + SLASH + podcastService.getSiteId();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSiteId() {
		return podcastService.getSiteId();
	}

	public void setCheckboxTableValues() {
		final List roleNames = getRoleNames();
		final Iterator roleIter = roleNames.iterator();
		final AuthzGroupService authzGroupService = ComponentManager.get(AuthzGroupService.class);
		
		final List permNames = getPermNames();
		
		checkboxTableValues = new ArrayList();
		
		String podcastFolderRef = "";
		AuthzGroup podAuthzGroup = null;

		try {
			podcastFolderRef = SLASH + CONTENT  
									+ podcastService.retrievePodcastFolderId(podcastService.getSiteId());
			podAuthzGroup = authzGroupService.getAuthzGroup(podcastFolderRef);
		} 
		catch (PermissionException e) {
			log.warn("PermissionException trying to get roles for site "
						+ podcastService.getSiteId() + e.getMessage(), e);
		} 
		catch (GroupNotDefinedException e) {
			log.error("GroupNotDefinedException while constructing permission table for site "
							+ podcastService.getSiteId() + ".", e);
		}

		// Create a list of azGroup ids to get permissions
		Collection podcastCollection = new ArrayList();
		if (podAuthzGroup != null) {
			podcastCollection.add(podAuthzGroup.getId());
		}
		
		podcastCollection.add(getSiteRef());

		while (roleIter.hasNext()) {
			final DecoratedCheckboxTableRow tableRow = new DecoratedCheckboxTableRow();
			
			final String roleName = (String) roleIter.next();
			
			tableRow.setRowName(roleName);
			
			// get functions (permissions) for this role
			Set rolePerms = authzGroupService.getAllowedFunctions(roleName, podcastCollection);

			final Iterator permNameIter = permNames.iterator();
			final List checkVal = new ArrayList();

			for (int j=0; j < permNames.size(); j++) {
				final String testPerm = CONTENT + DOT + permNameIter.next();
		
				checkVal.add(new Boolean(rolePerms.contains(testPerm)));
			}
			tableRow.setCheckboxValues(checkVal);
			
			checkboxTableValues.add(tableRow);
		}		
	}

	public List getCheckboxTableValues() {
		return checkboxTableValues;
	}

	public void setCheckboxTableValues(List checkboxTableValues) {
		this.checkboxTableValues = checkboxTableValues;
	}

}
