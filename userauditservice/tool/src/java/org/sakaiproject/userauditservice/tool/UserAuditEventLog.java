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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class UserAuditEventLog {
	protected List<EventLog> eventLog = new ArrayList<EventLog>();
	// Static comparators
	public static final Comparator<EventLog> userIdComparatorEL;
	public static final Comparator<EventLog> roleNameComparatorEL;
	public static final Comparator<EventLog> auditStampComparatorEL;
	public static final Comparator<EventLog> actionTextComparatorEL;
	public static final Comparator<EventLog> sourceTextComparatorEL;
	public static final String GET_EVENTS_SQL = "select user_id, role_name, action_taken, audit_stamp, source, action_user_id from user_audits_log where site_id=? order by audit_stamp desc";
	@Setter protected String sortColumn;
	@Getter @Setter protected boolean sortAscending;
	@Getter @Setter private int totalItems = -1;
	@Getter @Setter private int firstItem = 0;
	@Getter @Setter private int pageSize = 0;
	private transient SqlService sqlService = (SqlService) ComponentManager.get(SqlService.class.getName());
	private transient UserAuditRegistration userAuditRegistration = (UserAuditRegistration) ComponentManager.get(UserAuditRegistration.class.getName());
	private transient UserAuditService userAuditService = (UserAuditService) ComponentManager.get(UserAuditService.class.getName());
	private transient SiteService siteService = (SiteService) ComponentManager.get(SiteService.class.getName());
	private transient ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());

	private ResourceLoader rb = new ResourceLoader("UserAuditMessages");
	
	static {

		userIdComparatorEL = new Comparator<EventLog>() {
			public int compare(EventLog one, EventLog another) {
				return Collator.getInstance().compare(one.getUserEid(), another.getUserEid());
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
        if ("userId".equals(sortColumn))
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

    @Getter @Setter
	public class EventLog {
		protected String actionTaken;
		protected String actionText;
		protected String actionUserEid;
		protected Date auditStamp;
		protected String roleName;
		protected String source;
		protected String sourceText;
		protected String userEid;
		
		/**
		 * Constructs a EventLogImpl.
		 * 
		 * @param userEid - this is the userEid for who was add/dropped from a site
		 * @param roleName - the user's role in the site
		 * @param actionTaken - this interprets the A, D, and U and return the appropriate text from the bundle
		 * @param auditStamp - will return a String, although a Date object is passed in.  This is the date and time the user was added or dropped from the site
		 * @param source - interprets the letter key registered from a tool and returns the appropriate text from the bundle
		 * @param actionUserEid - User object for who performed the add/drop action
		 */
		public EventLog(String userEid, String roleName, String actionTaken, Date auditStamp, String source, String actionUserEid) {
			this.userEid = userEid;
			this.roleName = roleName;
			this.actionTaken = actionTaken;
			this.auditStamp = auditStamp;
			this.source = source;
			this.actionUserEid = actionUserEid;
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
	
		public String getAuditStamp() {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, rb.getLocale());
			return df.format(auditStamp);
		}
		
		public String getSourceText() {
			for(UserAuditRegistration uar : userAuditService.getRegisteredItems())
			{
				if (uar.getDatabaseSourceKey().equals(source))
				{
					String[] params = new String[] {actionUserEid};
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
			PreparedStatement statement = null;
			ResultSet result = null;
			String siteId = toolManager.getCurrentPlacement().getContext();
			try
			{
				conn = sqlService.borrowConnection();
				statement = conn.prepareStatement(GET_EVENTS_SQL);
				statement.setString(1, siteId);
				result = statement.executeQuery();
				while (result.next())
				{
					String userId = result.getString("user_id");
					String roleName = result.getString("role_name");
					String actionTaken = result.getString("action_taken");
					Timestamp auditStamp = result.getTimestamp("audit_stamp");
					String source = result.getString("source");
					String actionUserId = result.getString("action_user_id");

					eventLog.add(new EventLog(userId,roleName,actionTaken,auditStamp,source,actionUserId));
				}
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
	
	public int getRowsNumber() {
		if(totalItems <= pageSize){
			return totalItems;
		}
		return pageSize;
	}
}
