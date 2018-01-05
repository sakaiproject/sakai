/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.tool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class UserAuditEventLog {
	protected List<EventLog> eventLog = new ArrayList<EventLog>();
	// Static comparators
	public static final Comparator<EventLog> displayNameComparatorEL;
	public static final Comparator<EventLog> userIdComparatorEL;
	public static final Comparator<EventLog> roleNameComparatorEL;
	public static final Comparator<EventLog> auditStampComparatorEL;
	public static final Comparator<EventLog> actionTextComparatorEL;
	public static final Comparator<EventLog> sourceTextComparatorEL;
	protected String sortColumn;
	protected boolean sortAscending;
	private int totalItems = -1;
	private int firstItem = 0;
	private int pageSize = 0;
	private Map<String, User> userMap = new HashMap<String, User>();
	private transient SqlService sqlService = (SqlService) ComponentManager.get(SqlService.class.getName());
	private transient UserAuditRegistration userAuditRegistration = (UserAuditRegistration) ComponentManager.get(UserAuditRegistration.class.getName());
	private transient UserAuditService userAuditService = (UserAuditService) ComponentManager.get(UserAuditService.class.getName());
	private transient SiteService siteService = (SiteService) ComponentManager.get(SiteService.class.getName());
	private transient ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private transient UserDirectoryService userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class.getName());
	
	static {
		displayNameComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				int comparison = one.getUserDisplayName().compareToIgnoreCase(another.getUserDisplayName());
				return comparison == 0 ? userIdComparatorEL.compare(one,another) : comparison;
			}
		};

		userIdComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				return Collator.getInstance().compare(one.getUser().getEid(),another.getUser().getEid());
			}
		};
		
		roleNameComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				int comparison = Collator.getInstance().compare(one.getRoleName(),another.getRoleName());
				return comparison == 0 ? userIdComparatorEL.compare(one,another) : comparison;
			}
		};
		
		auditStampComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				// calling auditStamp directly so it does a comparison to the actual date versus a string style comparison, which isn't quite right.
				int comparison = (one.auditStamp.compareTo(another.auditStamp));
				return comparison == 0 ? userIdComparatorEL.compare(one,another) : comparison;
			}
		};

		actionTextComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				int comparison = Collator.getInstance().compare(one.getActionText(),another.getActionText());
				return comparison == 0 ? userIdComparatorEL.compare(one,another) : comparison;
			}
		};
		
		sourceTextComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				int comparison = Collator.getInstance().compare(one.getSourceText(),another.getSourceText());
				return comparison == 0 ? userIdComparatorEL.compare(one,another) : comparison;
			}
		};
	}
	
	protected Comparator<EventLog> getComparatorEL()
	{
    	String sortColumn = getSortColumn();
        Comparator<EventLog> comparator;
        if ("userDisplayName".equals(sortColumn))
        {
            comparator = displayNameComparatorEL;
        }
        else if ("userId".equals(sortColumn))
        {
            comparator = userIdComparatorEL;
        }
        else if("roleName".equals(sortColumn))
        {
            comparator = roleNameComparatorEL;
        }
        else if("auditStamp".equals(sortColumn))
        {
        	comparator = auditStampComparatorEL;
        }
        else if("actionText".equals(sortColumn))
        {
        	comparator = actionTextComparatorEL;
        }
        else if("sourceText".equals(sortColumn))
        {
        	comparator = sourceTextComparatorEL;
        }
        else
        {
            // Default to the sort name
            comparator = auditStampComparatorEL;
        }
        return comparator;
    }

	public class EventLog {
		protected String actionTaken;
		protected String actionText;
		protected User actionUser;
		protected Date auditStamp;
		protected String roleName;
		protected String source;
		protected String sourceText;
		protected User user;
		protected String userDisplayName;
		
		/**
		 * Constructs a EventLogImpl.
		 * 
		 * @param user - this is a User object for who was add/dropped from a site
		 * @param roleName - the user's role in the site
		 * @param actionTaken - this interprets the A, D, and U and return the appropriate text from the bundle
		 * @param auditStamp - will return a String, although a Date object is passed in.  This is the date and time the user was added or dropped from the site
		 * @param source - interprets the letter key registered from a tool and returns the appropriate text from the bundle
		 * @param actionUser - User object for who performed the add/drop action
		 * @param childSiteId - Used for logging something specific with parent/child sites
		 */
		public EventLog(User user, String roleName, String actionTaken, Date auditStamp, String source, User actionUser) {
			this.user = user;
			this.roleName = roleName;
			this.actionTaken = actionTaken;
			this.auditStamp = auditStamp;
			this.source = source;
			this.actionUser = actionUser;
		}
		
		public String getActionTaken() {
			return actionTaken;
		}
		
		public String getActionText() {
			if (userAuditService.USER_AUDIT_ACTION_ADD.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_add");
			}
			else if (userAuditService.USER_AUDIT_ACTION_REMOVE.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_remove");
			}
			else if (userAuditService.USER_AUDIT_ACTION_UPDATE.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_update");
			}
			return actionText;
		}
	
		public User getActionUser() {
			return actionUser;
		}
	
		public String getAuditStamp() {
			DateFormat df = DateFormat.getDateTimeInstance();
			return df.format(auditStamp);
		}
		
		public String getRoleName() {
			return roleName;
		}
	
		public String getSource() {
			return source;
		}
		
		public String getSourceText() {
			for(UserAuditRegistration uar : userAuditService.getRegisteredItems())
			{
				if (uar.getDatabaseSourceKey().equals(source))
				{
					String[] params = new String[] {actionUser.getSortName(), actionUser.getEid()};	
					sourceText = uar.getSourceText(params);
					break;
				}
				else
				{
					// if we didn't find an appropriate source, use the not available option
					sourceText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_not_available");
				}
			}

			return sourceText;
		}
	
		public User getUser() {
			return user;
		}
		
		public String getUserDisplayName() {
			userDisplayName = user.getSortName();
			return userDisplayName;
		}
	
		public void setActionTaken(String actionTaken) {
			this.actionTaken = actionTaken;
		}
		
		public void setActionText(String actionText) {
			this.actionText = actionText;
		}
	
		public void setActionUser(User actionUser) {
			this.actionUser = actionUser;
		}
	
		public void setAuditStamp(Date auditStamp) {
			this.auditStamp = auditStamp;
		}
	
		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}
		
		public void setSource(String source) {
			this.source = source;
		}
	
		public void setSourceText(String sourceText) {
			this.sourceText = sourceText;
		}
		
		public void setUser(User user) {
			this.user = user;
		}
	
		public void setUserDisplayName(String userDisplayName) {
			this.userDisplayName = userDisplayName;
		}
	}
	    
	public List<EventLog> getEventLog() {
		return eventLog;
	}
	
	private void getEvents()
	{
		if (this.eventLog == null || this.eventLog.isEmpty())
		{
			eventLog = new ArrayList<EventLog>();
			Connection conn = null;
			Statement statement = null;
			ResultSet result = null;
			String sql = "";
			String siteId = toolManager.getCurrentPlacement().getContext();
			try
			{
				conn = sqlService.borrowConnection();
				statement = conn.createStatement();
				sql = "select user_id, role_name, action_taken, audit_stamp, source, action_user_id from user_audits_log where site_id = '" + siteId + "' order by audit_stamp desc";
				result = statement.executeQuery(sql);
				while (result.next())
				{
					String userId = result.getString("user_id");
					String roleName = result.getString("role_name");
					String actionTaken = result.getString("action_taken");
					Timestamp auditStamp = result.getTimestamp("audit_stamp");
					String source = result.getString("source");
					String actionUserId = result.getString("action_user_id");
					
					User cachedUser;
					if (userMap.containsKey(userId))
					{
						cachedUser = userMap.get(userId);
					}
					else
					{
						cachedUser = userDirectoryService.getUserByEid(userId);
						userMap.put(userId, cachedUser);
					}
					
					if (actionUserId!=null && !"".equals(actionUserId))
					{
						User cachedActionUser;
						if (userMap.containsKey(actionUserId))
						{
							cachedActionUser = userMap.get(actionUserId);
						}
						else
						{
							cachedActionUser = userDirectoryService.getUserByEid(actionUserId);
							userMap.put(actionUserId, cachedActionUser);
						}
						eventLog.add(new EventLog(cachedUser,roleName,actionTaken,auditStamp,source,cachedActionUser));
					}
					else
					{
						eventLog.add(new EventLog(cachedUser,roleName,actionTaken,auditStamp,source,null));
					}
				}
			}
			catch (UserNotDefinedException e)
			{
				log.warn("ERROR getting the user audit logs!", e);
			}
			catch (SQLException e)
			{
				log.warn("ERROR getting the user audit logs!", e);
			}
			finally
			{
				try
				{
					if (result!=null)
					{
						result.close();
					}
				}
		 		catch (SQLException e)
		 		{
					log.warn("Error trying to close the result set in the Roster Event Logger!", e);
		 		}
				try
				{
					if (statement!=null)
					{
						statement.close();
					}
				}
		 		catch (SQLException e)
		 		{
					log.warn("Error trying to close the statement in the Roster Event Logger!", e);
		 		}
				try
				{
					if (conn!=null)
					{
						conn.close();
					}
				}
		 		catch (SQLException e)
		 		{
					log.warn("Error trying to close the database connection in the Roster Event Logger!", e);
		 		}
			}
		}
		this.totalItems = eventLog.size();
	}
	
	public String getInitValues() {
		getEvents();
		
		if (eventLog != null && eventLog.size() >= 1) {
			Collections.sort(eventLog, getComparatorEL());
			if(!isSortAscending()) {
				Collections.reverse(eventLog);
			}
	    }
		
		return "";
	}
	
	public String getPageTitle() {
	    return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				"UserAuditMessages", "title_event_log");
	}
	
	public String getSortColumn() {
		if (this.sortColumn == null) {
			this.sortColumn = "auditStamp";
		}
		return this.sortColumn;
	}
	
	public boolean isExportablePage() {
		return false;
	}
	
	public boolean isSortAscending() {
		return sortAscending;
	}
	
	public void setEventLog(List<EventLog> eventLog) {
		this.eventLog = eventLog;
	}
	
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
	
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
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
}
