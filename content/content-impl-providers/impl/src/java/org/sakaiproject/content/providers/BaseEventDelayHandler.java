/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.providers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@Slf4j
public class BaseEventDelayHandler implements EventDelayHandler, ScheduledInvocationCommand
{
	private boolean autoDdl;

	private SqlService sqlService;
	private ScheduledInvocationManager schedInvocMgr;
	private UserDirectoryService userDirectoryService;
	private EventTrackingService eventService;
	private SecurityService securityService;
	

	/** contains a map of the database dependent handler. */
	protected Map<String, BaseEventDelayHandlerSql> databaseBeans;

	/** contains database dependent code. */
	protected BaseEventDelayHandlerSql baseEventDelayHandlerSql;

	public void setDatabaseBeans(Map<String, BaseEventDelayHandlerSql> databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	public BaseEventDelayHandlerSql getBaseEventDelayHandlerSql()
	{
		return baseEventDelayHandlerSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setBaseEventDelayHandlerSqlSql(String vendor)
	{
		this.baseEventDelayHandlerSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}
	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public void setEventService(EventTrackingService eventService)
	{
		this.eventService = eventService;
	}

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setSchedInvocMgr(ScheduledInvocationManager schedInvocMgr)
	{
		this.schedInvocMgr = schedInvocMgr;
	}

	public void setAutoDdl(boolean autoDdl)
	{
		this.autoDdl = autoDdl;
	}

	public void init()
	{
		setBaseEventDelayHandlerSqlSql(sqlService.getVendor());
		
		if (autoDdl)
		{
			// load the base ddl
			sqlService.ddl(this.getClass().getClassLoader(), "sakai_event_delay");
		}

		eventService.setEventDelayHandler(this);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#readDelay(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Event readDelay(String delayId)
	{
		List<Event> results = sqlService.dbRead(baseEventDelayHandlerSql.getDelayReadSql(), new Long[] { Long.parseLong(delayId) },
				new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
							throws SqlReaderFinishedException
					{
						Event e = null;
						try
						{
							// EVENT_DELAY_ID, EVENT, EVENT_CODE, PRIORITY, REF, USER_ID
							e = new ReEvent(result.getString(2), "m".equals(result.getString(3)), result
									.getInt(4), result.getString(5), result.getString(6));
						}
						catch (SQLException se)
						{
							log.error("Error trying to build event on read", se);
						}
						return e;
					}
				});
		Event e = null;
		if (results.size() > 0)
			e = results.get(0);
		return e;
	}

	/**
	 * Read an event delay and delete it from the db.
	 * 
	 * @param delayId
	 * @return The event found.
	 */
	public Event popEventDelay(String delayId)
	{
		Event e = readDelay(delayId);
		deleteDelayById(delayId);
		return e;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(org.sakaiproject.event.api.Event, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> findDelayIds(Event event, String userId)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify() ? "m" : "a", event.getPriority(),
				event.getResource(), userId };
		List<String> ids = sqlService.dbRead(baseEventDelayHandlerSql.getDelayFindFineSql(), fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(org.sakaiproject.event.api.Event)
	 */
	@SuppressWarnings("unchecked")
	public List<String> findDelayIds(Event event)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify() ? "m" : "a", event.getPriority(),
				event.getResource() };
		List<String> ids = sqlService.dbRead(baseEventDelayHandlerSql.getDelayFindEventSql(), fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> findDelayIds(String resource, String event)
	{
		Object[] fields = new Object[] { resource, event };
		List<String> ids = sqlService.dbRead(baseEventDelayHandlerSql.getDelayFindByRefEventSql(), fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<String> findDelayIds(String resource)
	{
		Object[] fields = new Object[] { resource };
		List<String> ids = sqlService.dbRead(baseEventDelayHandlerSql.getDelayFindByRefSql(), fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#createDelay(org.sakaiproject.event.api.Event)
	 */
	public String createDelay(Event event, Time fireTime)
	{
		return createDelay(event, event.getUserId(), fireTime);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#createDelay(org.sakaiproject.event.api.Event, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public String createDelay(Event event, String userId, Time fireTime)
	{
		// delete previous like delays
		deleteDelay(event);

		Object[] fields = new Object[] { event.getEvent(), event.getModify() ? "m" : "a", event.getPriority(),
				event.getResource(), userId };
		sqlService.dbWrite(baseEventDelayHandlerSql.getDelayWriteSql(), fields);
		List<String> ids = sqlService.dbRead(baseEventDelayHandlerSql.getDelayFindFineSql(), fields, null);
		String id = null;
		if (ids.size() > 0)
			id = (String) ids.get(0);

		// Schedule the new delayed invocation
		log.info("Creating new delayed event [" + id + "]");
		schedInvocMgr.createDelayedInvocation(fireTime, BaseEventDelayHandler.class.getName(), id);
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#deleteDelayById(java.lang.String)
	 */
	public boolean deleteDelayById(String delayId)
	{
		// Remove any existing notifications for this notification
		schedInvocMgr.deleteDelayedInvocation(BaseEventDelayHandler.class.getName(), delayId);
		boolean ret = sqlService
				.dbWrite(baseEventDelayHandlerSql.getDelayDeleteSql(), new Object[] { Long.parseLong(delayId) });
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#deleteDelay(org.sakaiproject.event.api.Event)
	 */
	public boolean deleteDelay(Event e)
	{
		boolean ret = true;
		List<String> ids = findDelayIds(e);
		for (String id : ids)
		{
			ret &= deleteDelayById(id);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#deleteDelay(java.lang.String, java.lang.String)
	 */
	public boolean deleteDelay(String resource, String event)
	{
		boolean ret = true;
		List<String> ids = findDelayIds(resource, event);
		for (String id : ids)
		{
			ret &= deleteDelayById(id);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#deleteDelay(java.lang.String)
	 */
	public boolean deleteDelay(String resource)
	{
		boolean ret = true;
		List<String> ids = findDelayIds(resource);
		for (String id : ids)
		{
			ret &= deleteDelayById(id);
		}
		return ret;
	}

	/**
	 * Deserializes the context into an event and refires the event.
	 */
	public void execute(String opaqueContext)
	{
		// need to instantiate components locally because this class is instantiated by the
		// scheduled invocation manager and not pulled from spring context.
		final Event event = popEventDelay(opaqueContext);

		if (event != null) {
			log.info("Refiring delayed event [" + opaqueContext + "]");

			// Set up security advisor
			try
			{
				User user = userDirectoryService.getUser(event.getUserId());
	
				securityService.pushAdvisor(new SecurityAdvisor() {
				    public SecurityAdvice isAllowed(String userId, String function, String reference) {
				            if (securityService.unlock(event.getUserId(), function, reference)) {
				                return SecurityAdvice.ALLOWED;
				             }
				            return SecurityAdvice.PASS;
				         }
				    });
	
				eventService.post(event, user);
			}
			catch (UserNotDefinedException unde)
			{
				// can't find the user so refire the event without user impersonation
				eventService.post(event);
			} finally {
				// Clear security advisor
				securityService.popAdvisor();
			}
		} else {
			log.warn("Delayed event not found [" + opaqueContext + "]");
		}
		
	}

	/**
	 * Local implementation of Event to allow the setting of all fields when refiring an event after
	 * it has been scheduled to run later than it was originally fired.
	 */
	protected static class ReEvent implements Event
	{
		private String event;
		private boolean modify;
		private int priority;
		private String resource;
		private String context;
		private String sessionId;
		private String userId;

		public ReEvent(String event, boolean modify, int priority, String resource, String userId)
		{
			this.event = event;
			this.modify = modify;
			this.priority = priority;
			this.resource = resource;
			this.userId = userId;
		}

		public String getEvent()
		{
			return event;
		}

		public boolean getModify()
		{
			return modify;
		}

		public int getPriority()
		{
			return priority;
		}

		public String getResource()
		{
			return resource;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public String getUserId()
		{
			return userId;
		}

		public String getContext()
		{
			return context;
		}

		public Date getEventTime() {
			return null;
		}

		@Override
		public LRS_Statement getLrsStatement() {
			//Don't do anything right now on a rerun
			return null;
		}
	}
}
