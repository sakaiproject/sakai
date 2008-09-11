/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/ufp/usermembership/trunk/tool/src/java/org/sakaiproject/umem/tool/ui/UserListBean.java $
 * $Id: UserListBean.java 4381 2007-03-21 11:25:54Z nuno@ufp.pt $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
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

package org.sakaiproject.umem.tool.ui;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.umem.api.Authz;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
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

	/** Our log (commons). */
	private static Log						LOG					= LogFactory.getLog(UserListBean.class);

	/** Resource bundle */
	private transient ResourceLoader		msgs				= new ResourceLoader("org.sakaiproject.umem.tool.bundle.Messages");

	/** Controller fields */
	private List							userRows;

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
	private transient Collator				collator			= Collator.getInstance();																// use
	private String							message				= "";
	// system
	/** Sakai services vars */
	private transient UserDirectoryService	M_uds				= (UserDirectoryService) ComponentManager.get(UserDirectoryService.class.getName());
	private transient ToolManager			M_tm				= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private transient SqlService			M_sql				= (SqlService) ComponentManager.get(SqlService.class.getName());
	private transient Authz					authz				= (Authz) ComponentManager.get(Authz.class.getName());
	
	
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
		private String              createdOn;
		private String              modifiedOn;

		public UserRow() {
		}

		public UserRow(String userID, String userEID, String userDisplayId, String userName, 
				       String userEmail, String userType, String authority, String createdOn, 
				       String modifiedOn) {
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

		public String getCreatedOn() {
			return createdOn;
		}

		public String getModifiedOn() {
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

	public static final Comparator getUserRowComparator(final String fieldName, final boolean sortAscending, final Collator collator) {
		return new Comparator() {

			public int compare(Object o1, Object o2) {
				if(o1 instanceof UserRow && o2 instanceof UserRow){
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
							String s1 = r1.getCreatedOn();
							String s2 = r2.getCreatedOn();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER_MODIFIED_ON)){
							String s1 = r1.getModifiedOn();
							String s2 = r2.getModifiedOn();
							int res = collator.compare(s1!=null? s1.toLowerCase():"", s2!=null? s2.toLowerCase():"");
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
						LOG.warn("Error occurred while sorting by: "+fieldName, e);
					}
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

	private void doSearch() {
		/**
		 * 1. Query internal users from SAKAI_USER (filter by type and search)
		 * 2. Query external users from SAKAI_REALM_RL_GR
		 * 3. Get info and filter external users from UserDirectoryProvider
		 * 4. Sort resulting list
		 * 5. Update pager
		*/	
		LOG.debug("Refreshing query...");
		selectedUserType = newUserType;
		selectedAuthority = newAuthority;
		searchKeyword = searchKeyword.trim();
		boolean filtering = (selectedUserType != null && userTypes != null && !selectedUserType.equals(USER_TYPE_ALL));
		boolean searching = (searchKeyword != null && !searchKeyword.equals("") && !searchKeyword.equals(msgs.getString("bar_input_search_inst")) );
		userRows = new ArrayList();
			
		try{
			if(selectedAuthority.equals(USER_AUTH_ALL) || selectedAuthority.equals(USER_AUTH_INTERNAL)){
				// 1. Query internal users from SAKAI_USER
				String sql = "SELECT EID,EMAIL,FIRST_NAME,LAST_NAME,TYPE, CREATEDON, "
					        +"MODIFIEDON, SAKAI_USER_ID_MAP.USER_ID as USER_ID FROM SAKAI_USER LEFT JOIN SAKAI_USER_ID_MAP " 
					        +"ON SAKAI_USER.USER_ID=SAKAI_USER_ID_MAP.USER_ID";
				if(searching || filtering){
					sql += " WHERE ";
					if(searching)
						sql += " (EID LIKE '%"+searchKeyword+"%' OR SAKAI_USER.USER_ID LIKE '%"+searchKeyword+"%' OR FIRST_NAME LIKE '%"+searchKeyword+"%' OR LAST_NAME LIKE '%"+searchKeyword+"%' OR EMAIL LIKE '%"+searchKeyword+"%') ";
					if(filtering && searching)
						sql += " AND ";
					if(filtering){
						if(selectedUserType.equals(USER_TYPE_NONE))
							sql += " (TYPE='' or TYPE IS NULL) ";
						else{
							if(selectedUserType.indexOf(",") == -1)
								sql += " (TYPE='"+selectedUserType+"') ";
							else
								sql += " (TYPE in ("+selectedUserType+") ) ";
						}
					}
				}		

				Connection c = null;
				Statement st = null;
				ResultSet rs = null;
				try{
					c = M_sql.borrowConnection();
					st = c.createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()){
						String id = rs.getString("USER_ID");
						String eid = rs.getString("EID");
						eid = eid == null? id : eid;
						String e = rs.getString("EMAIL");
						String f = rs.getString("FIRST_NAME");
						String l = rs.getString("LAST_NAME");
						String t = rs.getString("TYPE");
						Timestamp createdOn = rs.getTimestamp("CREATEDON");
						String co = createdOn.toString();
						Timestamp modifiedOn = rs.getTimestamp("MODIFIEDON");
						String mo = modifiedOn.toString();
						// For internal users, making the assumption that eid will do for displayId
						userRows.add(new UserRow(id, eid, eid, getFullName(id, f, l), e, t, 
								     USER_AUTH_INTERNAL, co, mo));
					}
				}catch(SQLException e){
					LOG.error("SQL error occurred while retrieving list of internal users: "+e.getMessage());
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
			}
		}catch(Exception e){
			LOG.warn("Exception occurred while querying internal users: " + e.getMessage());
			e.printStackTrace();
		}
			
		// 2. Query external users from SAKAI_REALM_RL_GR
		try{
			if(selectedAuthority.equals(USER_AUTH_ALL) || selectedAuthority.equals(USER_AUTH_EXTERNAL)){
				List eUsers = new ArrayList();
				Connection c = null;
				Statement st = null;
				ResultSet rs = null;
				try{
					c = M_sql.borrowConnection();
					String sqlE = "SELECT DISTINCT USER_ID FROM SAKAI_REALM_RL_GR WHERE USER_ID NOT IN (SELECT USER_ID FROM SAKAI_USER)";
					st = c.createStatement();
					rs = st.executeQuery(sqlE);
					while (rs.next()){
						String id = rs.getString("USER_ID");
						eUsers.add(id);
					}
				}catch(SQLException e){
					LOG.error("SQL error occurred while retrieving list of external users: "+e.getMessage());
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
				
				// 3. Get info and filter external users from UserDirectoryProvider
				String id;
				String eid;
				String dispId; 
				String e;
				String n;
				String t;
				String regexp = null;
				String co;
				String mo;
				List pUsers = M_uds.getUsers(eUsers);
				Iterator it = pUsers.iterator();
				if(searching)
					regexp = ".*"+searchKeyword.toLowerCase()+".*";
				while(it.hasNext()){
					User u = (User) it.next();
					id = u.getId();
					eid = u.getEid();
					dispId = u.getDisplayId();
					e = u.getEmail();
					n = getFullName(id, u.getFirstName(), u.getLastName());
					t = u.getType();
					t = (t == null) ? "" : t;
					if(!"".equals(t)) addExtraUserType(t);
					co = (u.getCreatedTime() == null) ? "" : u.getCreatedTime().toStringLocalDate();
                    mo = (u.getModifiedTime() == null) ? "" : u.getModifiedTime().toStringLocalDate();
					boolean add = false;
					if(filtering && !searching){
						if((!selectedUserType.equals(USER_TYPE_NONE) && t.equals(selectedUserType)) || (selectedUserType.equals(USER_TYPE_NONE) && "".equals(t))) add = true;
					}else if(!filtering && searching){
						if(n.toLowerCase().matches(regexp) || e.toLowerCase().matches(regexp) || id.toLowerCase().matches(regexp)) add = true;
					}else if(filtering && searching){
						if((!selectedUserType.equals(USER_TYPE_NONE) && t.equals(selectedUserType)) || (selectedUserType.equals(USER_TYPE_NONE) && "".equals(t))){
							if(n.toLowerCase().matches(regexp) || e.toLowerCase().matches(regexp) || id.toLowerCase().matches(regexp)) add = true;
						}
					}else{
						add = true;
					}
					if(add) userRows.add(new UserRow(id, eid, dispId, n, e, t, USER_AUTH_EXTERNAL, co, mo));
				}
			}
		}catch(RuntimeException e){
			LOG.warn("Exception occurred while querying external users: " + e.getMessage(), e);
		}catch(SQLException e){
			LOG.warn("SQL error occurred while querying external users: " + e.getMessage(), e);
		}

		// 4. Update pager
		this.totalItems = userRows.size();
		if(totalItems > 0) 
			renderPager = true;
		else
			renderPager = false;
		firstItem = 0;
	}
	
	private String getFullName(String id, String firstName, String lastName){
		String fullName = "";
		try{
			fullName = M_uds.getUser(id).getDisplayName();
		}catch(UserNotDefinedException e){
			String _firstName = firstName == null ? "" : firstName;
			String _lastName = lastName == null ? "" : lastName;
			fullName = "".equals(_lastName) ? _firstName : ("".equals(_firstName) ? _lastName : _lastName+", "+_firstName);
		}
		return fullName;
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
				userTypes.add(0,new SelectItem(all.toString(),USER_TYPE_ALL));
			}else
			{					
				userTypes.add(new SelectItem(USER_TYPE_ALL));
				Connection c = null;
				Statement st = null;
				ResultSet rs = null;
				try{
					c = M_sql.borrowConnection();
					String vendor = M_sql.getVendor();
					String sql = null;
					if(vendor.equalsIgnoreCase("oracle")){
						sql = "select distinct TYPE from SAKAI_USER where TYPE is not null";
					}else{
						sql = "select distinct TYPE from SAKAI_USER where TYPE!='' and TYPE is not null;";
					}
					st = c.createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()){
						String type = rs.getString(1);
						userTypes.add(new SelectItem(type));
					}
				}catch(SQLException e){
					LOG.error("SQL error occurred while retrieving user types: " + e.getMessage(), e);
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
				userTypes.add(new SelectItem(USER_TYPE_NONE));
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
		userAuthorities.add(new SelectItem(USER_AUTH_ALL));
		userAuthorities.add(new SelectItem(USER_AUTH_INTERNAL));
		userAuthorities.add(new SelectItem(USER_AUTH_EXTERNAL));
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
		Export.writeAsCsv(getAsCsv(userRows), "UserListing");
	}

	private String getAsCsv(List list) {
		StringBuilder sb = new StringBuilder();

		// Add the headers
		Export.appendQuoted(sb, msgs.getString("user_id"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("internal_user_id"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_name"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_email"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_type"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_authority"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_created_on"));
		sb.append(",");
		Export.appendQuoted(sb, msgs.getString("user_modified_on"));
		sb.append("\n");

		// Add the data
		Iterator i = list.iterator();
		while (i.hasNext()){
			UserRow usr = (UserRow) i.next();
			// user name
			Export.appendQuoted(sb, usr.getUserEID());
			sb.append(",");
			Export.appendQuoted(sb, usr.getUserID());
			sb.append(",");
			Export.appendQuoted(sb, usr.getUserName());
			sb.append(",");
			Export.appendQuoted(sb, usr.getUserEmail());
			sb.append(",");
			Export.appendQuoted(sb, usr.getUserType());
			sb.append(",");
			Export.appendQuoted(sb, usr.getAuthority());
			sb.append(",");
			Export.appendQuoted(sb, usr.getCreatedOn());
			sb.append(",");
			Export.appendQuoted(sb, usr.getModifiedOn());
			sb.append("\n");
		}
		return sb.toString();
	}
}
