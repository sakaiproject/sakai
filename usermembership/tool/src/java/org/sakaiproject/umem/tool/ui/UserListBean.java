/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/ufp/usermembership/trunk/tool/src/java/org/sakaiproject/umem/tool/ui/UserListBean.java $
 * $Id: UserListBean.java 4381 2007-03-21 11:25:54Z nuno@ufp.pt $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.umem.tool.ui;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.umem.api.Authz;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
@Slf4j
public class UserListBean {
	private static final long				serialVersionUID	= 1L;
	private static final String				USER_TYPE_ALL		= "All";
	private static final String				USER_TYPE_NONE		= "(no type)";
	private static final String				USER_AUTH_ALL		= "All";
	private static final String				USER_AUTH_INTERNAL	= "Internal";
	private static final String				USER_AUTH_EXTERNAL	= "External";
	private static final String				SORT_USER_NAME		= "name";
	private static final String				SORT_USER_ID		= "id";
	private static final String				SORT_USER_EMAIL		= "email";
	private static final String				SORT_USER_TYPE		= "type";
	private static final String				SORT_USER_AUTHORITY	= "authority";
	private static final String				SORT_USER_CREATED_ON	= "createdOn";
	private static final String				SORT_USER_MODIFIED_ON	= "modifiedOn";
	private static final String				SORT_INTERNAL_USER_ID	= "internalUserId";
	private static final String				NO_NAME_USER		= "";
	private static final String				CFG_USER_TYPE_LIMIT_TO_SELF		= "userType.limitToSelf";
	private static final String				CFG_USER_TYPE_LIMIT_TO_LIST		= "userType.limitToList";

	/** Resource bundle */
	private transient ResourceLoader		msgs				= new ResourceLoader("org.sakaiproject.umem.tool.bundle.Messages");

	/** Controller fields */
	private List<UserRow>					userRows;

	/** Getter vars */
	private boolean							allowed				= false;
	private boolean							refreshQuery		= false;
	private boolean							renderTable			= false;
	private boolean							renderPager			= true;
	private boolean							renderClearSearch	= false;
	private int								totalItems			= -1;
	private int								firstItem			= 0;
	private int								pageSize			= 20;
	private String							searchKeyword		= null;
	private boolean							userSortAscending	= true;
	private String							userSortColumn		= SORT_USER_NAME;
	private List							userTypes			= null;
	private List							userAuthorities		= null;
	private String							selectedUserType	= null;
	private String							selectedAuthority	= USER_AUTH_ALL;
	private String							newUserType			= null;
	private String							newAuthority		= USER_AUTH_ALL;

	/** Private vars */
	private static RuleBasedCollator				collator;																// use
	private String							message				= "";
	// system
	/** Sakai services vars */
	private transient UserDirectoryService	M_uds				= (UserDirectoryService) ComponentManager.get(UserDirectoryService.class.getName());
	private transient ToolManager			M_tm				= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private transient SqlService			M_sql				= (SqlService) ComponentManager.get(SqlService.class.getName());
	private transient Authz					authz				= (Authz) ComponentManager.get(Authz.class.getName());
	private UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
	
	// ######################################################################################
	// UserRow, UserSitesRow CLASS
	// ######################################################################################
	public static class UserRow implements Serializable {
		private static final long	serialVersionUID	= 1L;
		private String				userID;
		private String				userEID;
		private String				userDisplayId; 
		private String				userName;
		private String				userEmail;
		private String				userType;
		private String				authority;
		private Date              createdOn;
		private Date              modifiedOn;

		static {
			try{
				collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
			}catch(ParseException e){
				collator = (RuleBasedCollator)Collator.getInstance();
			}
		}

		public UserRow() {
		}

		public UserRow(String userID, String userEID, String userDisplayId, String userName, 
				       String userEmail, String userType, String authority, Date createdOn, 
				       Date modifiedOn) {
			this.userID = userID;
			this.userEID = userEID;
			this.userDisplayId = userDisplayId;
			this.userName = userName;
			this.userEmail = userEmail;
			this.userType = userType;
			this.authority = authority;
			this.createdOn = createdOn;
			this.modifiedOn = modifiedOn;
		}

		public String getUserEmail() {
			return this.userEmail;
		}

		public String getUserID() {
			return this.userID;
		}

		public String getUserEID() {
			return this.userEID;
		}

		public String getUserDisplayId()
		{
			return this.userDisplayId;
		}
		
		public String getUserName() {
			return this.userName;
		}

		public String getUserType() {
			return (this.userType == null)? "" : this.userType;
		}
		
		public String getAuthority(){
			return this.authority;
		}

		public Date getCreatedOn() {
			return createdOn;
		}

		public Date getModifiedOn() {
			return modifiedOn;
		}
		
		public void setUserRow(UserRow row, String authority) {
			this.userID = row.getUserID();
			this.userEID = row.getUserEID();
			this.userDisplayId = row.getUserDisplayId();
			this.userName = row.getUserName();
			this.userEmail = row.getUserEmail();
			this.userType = row.getUserType();
			this.authority = authority;
			this.createdOn = row.createdOn;
			this.modifiedOn = row.modifiedOn;
		}

		public UserRow getUserRow() {
			return this;
		}
	}

	public static final Comparator<UserRow> getUserRowComparator(final String fieldName, final boolean sortAscending, final Collator collator) {
		return new Comparator<UserRow>() {

			public int compare(UserRow o1, UserRow o2) {
					UserRow r1 = (UserRow) o1;
					UserRow r2 = (UserRow) o2;
					try{
						if(fieldName.equals(SORT_USER_NAME)){
							String s1 = r1.getUserName();
							String s2 = r2.getUserName();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_ID)){
							String s1 = r1.getUserEID();
							String s2 = r2.getUserEID();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_EMAIL)){
							String s1 = r1.getUserEmail();
							String s2 = r2.getUserEmail();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_TYPE)){
							String s1 = r1.getUserType();
							String s2 = r2.getUserType();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_AUTHORITY)){
							String s1 = r1.getAuthority();
							String s2 = r2.getAuthority();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_CREATED_ON)){
							Date s1 = r1.getCreatedOn();
							Date s2 = r2.getCreatedOn();
							int res = collator.compare(s1!=null? s1.toString():"", s2!=null? s2.toString():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_MODIFIED_ON)){
							Date s1 = r1.getModifiedOn();
							Date s2 = r2.getModifiedOn();
							int res = collator.compare(s1!=null? s1.toString():"", s2!=null? s2.toString():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_INTERNAL_USER_ID)){
							String s1 = r1.getUserID();
							String s2 = r2.getUserID();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}
					}catch(Exception e){
						log.warn("Error occurred while sorting by: "+fieldName, e);
					}
				return 0;
			}
		};
	}

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public String getInitValues() {
		if(searchKeyword == null){
			renderClearSearch = false;
			searchKeyword = msgs.getString("bar_input_search_inst");
		}else
			renderClearSearch = true;
		
		if(isAllowed() && renderTable && refreshQuery){
			doSearch();
			refreshQuery = false;
		}
		
		if(userRows != null){
			// Sort resulting list
			Collections.sort(userRows, getUserRowComparator(userSortColumn, userSortAscending, collator));
		}
		
		return "";
	}
	
	public TimeZone getUserTimeZone() {
		return userTimeService.getLocalTimeZone();
	}

	private void doSearch() {
		log.debug("Refreshing query...");
		selectedUserType = newUserType;
		selectedAuthority = newAuthority;
		searchKeyword = searchKeyword.trim();
		userRows = new ArrayList<UserRow>();
		
		if(log.isDebugEnabled()){
			log.debug("selectedUserType: " + selectedUserType);
			log.debug("selectedAuthority: " + selectedAuthority);
			log.debug("searchKeyword: " + searchKeyword);
		}
		
		
		// 1. Search internal users (Sakai DB)
		if(selectedAuthority.equalsIgnoreCase(USER_AUTH_ALL) || selectedAuthority.equalsIgnoreCase(USER_AUTH_INTERNAL)){
			
			if(log.isDebugEnabled()){
				log.debug("Searching internal users...");
			}
			
			try{
				
				//SAK-20857 if empty search, return all users, otherwise only those that match.
				List<User> users;
				if(StringUtils.isBlank(searchKeyword)) {
					users = M_uds.getUsers();
				} else {
					users = M_uds.searchUsers(searchKeyword, 1, Integer.MAX_VALUE);
				}
				
				for(User u : users) {
					// filter user type
					if(userTypeMatches(u.getType())) {
						userRows.add(new UserRow(
								u.getId(), u.getEid(), u.getDisplayId(), 
								u.getDisplayName(), 
								u.getEmail(), 
								u.getType(), 
								msgs.getString("user_auth_internal"), 
								u.getCreatedDate(), 
								u.getModifiedDate()
								)
						);
					}
				}
				if(log.isDebugEnabled()){
					log.debug("Internal search results: " + users.size());
				}
			}catch(Exception e){
				log.warn("Exception occurred while searching internal users: " + e.getMessage());
			} 
		}
		
		
		// 2. Search users on external user providers
		if(selectedAuthority.equalsIgnoreCase(USER_AUTH_ALL) || selectedAuthority.equalsIgnoreCase(USER_AUTH_EXTERNAL)){
			
			if(log.isDebugEnabled()){
				log.debug("Searching external users...");
			}
			
			try{
				List<User> users = M_uds.searchExternalUsers(searchKeyword, -1, -1);
				for(User u : users) {
					// filter user type
					if(userTypeMatches(u.getType())) {
						userRows.add(new UserRow(
								u.getId(), u.getEid(), u.getDisplayId(), 
								u.getDisplayName(), 
								u.getEmail(), 
								u.getType(), 
								msgs.getString("user_auth_external"), 
								u.getCreatedDate(), 
								u.getModifiedDate()
								)
						);
					}
				}
				if(log.isDebugEnabled()){
					log.debug("External search results: " + users.size());
				}
			}catch(RuntimeException e){
				log.warn("Exception occurred while searching external users: " + e.getMessage(), e);
			} 
		}
		
		if(log.isDebugEnabled()){
			log.debug("Total results: " + userRows.size());
		}

		// 4. Update pager
		this.totalItems = userRows.size();
		renderPager = false;
		firstItem = 0;
		
		if(totalItems > 0) {
			renderPager = true;
		} 
		
	}
	
	private boolean userTypeMatches(String userType) {
		return 
			USER_TYPE_ALL.equals(selectedUserType)
			|| (!USER_TYPE_NONE.equals(selectedUserType) && StringUtils.equals(userType, selectedUserType))
			|| (USER_TYPE_NONE.equals(selectedUserType) && StringUtils.isEmpty(userType));
	}


	// ######################################################################################
	// ActionListener methods
	// ######################################################################################
	public String processActionSearch() {
		renderTable = true;
		refreshQuery = true;
		return "userlist";
	}

	public String processActionClearSearch() throws SQLException {
		this.selectedUserType = ((SelectItem) getUserTypes().get(0)).getLabel();
		this.selectedAuthority = ((SelectItem) getUserAuthorities().get(0)).getLabel();
		searchKeyword = null;
		renderTable = false;
		refreshQuery = false;
		return "userlist";
	}

	public void processActionSearchChangeListener(ValueChangeEvent event) {
		searchKeyword = (searchKeyword == null) ? "" : searchKeyword.trim();
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public boolean isAllowed() {
		allowed = authz.isUserAbleToViewUmem(M_tm.getCurrentPlacement().getContext());
	
		if(!allowed){
			FacesContext fc = FacesContext.getCurrentInstance();
			message = msgs.getString("unauthorized");
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
			allowed = false;
		}
		return allowed;
	}

	public boolean isEmptyUserList() {
		return renderTable && ((userRows == null) || (userRows.size() <= 0));
	}

	public List getUserRows() {		
		return userRows;
	}
	
	public int getRowsNumber() {
		if(totalItems <= pageSize){
			return totalItems;
		}
		return pageSize;
	}

	public int getFirstItem() {
		return firstItem;
	}

	public void setFirstItem(int firstItem) {
		this.firstItem = firstItem;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalItems() {
		return this.totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public boolean isRenderPager() {
		return renderTable && renderPager;
	}

	public boolean isRenderTable() {
		return renderTable && userRows != null && userRows.size() > 0;
	}
	
	public boolean isRenderClearSearch() {		
		return renderClearSearch;
	}

	public String getSearchKeyword() {		
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public void setUserSortAscending(boolean sortAscending) {
		this.userSortAscending = sortAscending;
	}

	public boolean getUserSortAscending() {
		return this.userSortAscending;
	}

	public String getUserSortColumn() {
		return this.userSortColumn;
	}

	public void setUserSortColumn(String sortColumn) {
		this.userSortColumn = sortColumn;
	}
	
	private void addExtraUserType(String newType) throws SQLException{
		boolean found = false;
		if(userTypes == null)
			userTypes = getUserTypes();
		Iterator it = userTypes.iterator();
		while(it.hasNext()){
			if(((SelectItem) it.next()).getLabel().equals(newType))
				found = true;
		}
		if(!found){
			userTypes.add(userTypes.size()-1,new SelectItem(newType));
		}
	}
	
	public List getUserTypes() throws SQLException {
		if(userTypes == null){
			Properties config = M_tm.getCurrentPlacement().getConfig();
			Boolean userTypeLimitToSelf = Boolean.parseBoolean(config.getProperty(CFG_USER_TYPE_LIMIT_TO_SELF, "false"));
			String userTypeLimitToListStr = config.getProperty(CFG_USER_TYPE_LIMIT_TO_LIST, "");
			String[] userTypeLimitToList = null;
			userTypes = new ArrayList();
			
			if(userTypeLimitToSelf){
				userTypes.add(new SelectItem(M_uds.getCurrentUser().getType()));				
			}else if(!"".equals(userTypeLimitToListStr)){
				userTypeLimitToList = userTypeLimitToListStr.split(",");
				StringBuilder all = new StringBuilder();
				for(int i=0; i<userTypeLimitToList.length; i++){
					userTypes.add(new SelectItem(userTypeLimitToList[i]));
					all.append("'");
					all.append(userTypeLimitToList[i]);
					all.append("'");
					if(i<userTypeLimitToList.length-1)
						all.append(",");
				}
				userTypes.add(0,new SelectItem(all.toString(), msgs.getString("user_type_all")));
			}else
			{					
				userTypes.add(new SelectItem(USER_TYPE_ALL, msgs.getString("user_type_all")));
				Connection c = null;
				Statement st = null;
				ResultSet rs = null;
				try{
					c = M_sql.borrowConnection();
					String vendor = M_sql.getVendor();
					String sql = null;
					if(vendor.equalsIgnoreCase("oracle")){
						sql = "select distinct TYPE from SAKAI_USER where TYPE is not null order by TYPE";
					}else{
						sql = "select distinct TYPE from SAKAI_USER where TYPE!='' and TYPE is not null order by TYPE;";
					}
					st = c.createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()){
						String type = rs.getString(1);
						userTypes.add(new SelectItem(type));
					}
				}catch(SQLException e){
					log.error("SQL error occurred while retrieving user types: " + e.getMessage(), e);
				}finally{
					try{
						if(rs != null)
							rs.close();
					}finally{
						try{
							if(st != null)
								st.close();
						}finally{
							if(c != null)
								M_sql.returnConnection(c);
						}
					}
				}
				userTypes.add(new SelectItem(USER_TYPE_NONE, msgs.getString("user_type_none")));
			}
		}
		
		return userTypes;
	}

	public String getSelectedUserType() throws SQLException {
		if(this.selectedUserType != null) return this.selectedUserType;
		else{
			this.selectedUserType = ((SelectItem) getUserTypes().get(0)).getLabel();
			return this.selectedUserType;
		}
	}

	public void setSelectedUserType(String type) {
		this.newUserType = type;
	}

	public List getUserAuthorities() {
		userAuthorities = new ArrayList();
		userAuthorities.add(new SelectItem(USER_AUTH_ALL, msgs.getString("user_auth_all")));
		userAuthorities.add(new SelectItem(USER_AUTH_INTERNAL, msgs.getString("user_auth_internal")));
		userAuthorities.add(new SelectItem(USER_AUTH_EXTERNAL, msgs.getString("user_auth_external")));
		return userAuthorities;
	}

	public String getSelectedAuthority() {
		if(this.selectedAuthority != null) return this.selectedAuthority;
		else{
			this.selectedAuthority = ((SelectItem) getUserAuthorities().get(0)).getLabel();
			return this.selectedAuthority;
		}
	}

	public void setSelectedAuthority(String type) {
		this.newAuthority = type;
	}
	
	public String getNoName(){
		return NO_NAME_USER;
	}

	// ######################################################################################
	// CSV export
	// ######################################################################################
	public void exportAsCsv(ActionEvent event) {
		Export.writeAsCsv(buildDataTable(userRows), "UserListing");
	}
	
    /**
     * Export the data in this user list to the response stream as an Excel workbook
     * @param event
     */
    public void exportAsXls(ActionEvent event) {
		Export.writeAsXls(buildDataTable(userRows), "UserListing");
	}

	/**
	 * Build a generic tabular representation of the user membership data export.
	 * 
	 * @param userRows The content of the table
	 * @return
	 * 	A table of data suitable to be exported
	 */
	private List<List<Object>> buildDataTable(List<UserRow> userRows) {
		List<List<Object>> table = new LinkedList<List<Object>>();
		
		//add header row
		List<Object> header = new ArrayList<Object>();
		header.add(msgs.getString("user_id"));
		header.add(msgs.getString("internal_user_id"));
		header.add(msgs.getString("user_name"));
		header.add(msgs.getString("user_email"));
		header.add(msgs.getString("user_type"));
		header.add(msgs.getString("user_authority"));
		header.add(msgs.getString("user_created_on"));
		header.add(msgs.getString("user_modified_on"));
		table.add(header);
		
		//add data rows
		for (UserRow userRow : userRows) {
			List<Object> currentRow = new ArrayList<Object>();
			currentRow.add(userRow.getUserEID());
			currentRow.add(userRow.getUserID());
			currentRow.add(userRow.getUserName());
			currentRow.add(userRow.getUserEmail());
			currentRow.add(userRow.getUserType());
			currentRow.add(userRow.getAuthority());
			currentRow.add(userRow.getCreatedOn());
			currentRow.add(userRow.getModifiedOn());
			table.add(currentRow);
		}
		
		return table;
	}
	
	

}
