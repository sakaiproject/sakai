package org.sakaiproject.content.providers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class BaseEventDelayHandler implements EventDelayHandler, ScheduledInvocationCommand
{
	private boolean autoDdl;

	private static final Log LOG = LogFactory.getLog(BaseEventDelayHandler.class);
	private static final String DELAY_WRITE_SQL = "insert into SAKAI_EVENT_DELAY (EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID) VALUES (?, ?, ?, ?, ?)";
	private static final String DELAY_READ_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";
	private static final String DELAY_FIND_FINE_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT = ? and MODIFY = ? and PRIORITY = ? and RESOURCE = ? and USER_ID = ?";
	private static final String DELAY_FIND_EVENT_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT = ? and MODIFY = ? and PRIORITY = ? and RESOURCE = ?";
	private static final String DELAY_FIND_BY_RESOURCE_SQL = "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where RESOURCE = ?";
	private static final String DELAY_FIND_BY_RESOURCE_EVENT_SQL = "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where RESOURCE = ? and EVENT = ?";
	private static final String DELAY_DELETE_SQL = "delete from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";

	private SqlService sqlService;
	private ScheduledInvocationManager schedInvocMgr;
	private UserDirectoryService userDirectoryService;
	private EventTrackingService eventService;

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
	public Event readDelay(String delayId)
	{
		List results = sqlService.dbRead(DELAY_READ_SQL, new Long[] { Long.parseLong(delayId) },
				new SqlReader()
				{
					public Object readSqlResultRecord(ResultSet result)
							throws SqlReaderFinishedException
					{
						Event e = null;
						try
						{
							// EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID
							e = new ReEvent(result.getString(2), result.getBoolean(3), result
									.getInt(4), result.getString(5), result.getString(6));
						}
						catch (SQLException se)
						{
							LOG.warn("Error trying to build event on read", se);
						}
						return e;
					}
				});
		Event e = null;
		if (results.size() > 0)
			e = (Event) results.get(0);
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
	public List<String> findDelayIds(Event event, String userId)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource(), userId };
		List<String> ids = sqlService.dbRead(DELAY_FIND_FINE_SQL, fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(org.sakaiproject.event.api.Event)
	 */
	public List<String> findDelayIds(Event event)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource() };
		List<String> ids = sqlService.dbRead(DELAY_FIND_EVENT_SQL, fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(java.lang.String, java.lang.String)
	 */
	public List<String> findDelayIds(String resource, String event)
	{
		Object[] fields = new Object[] { resource, event };
		List<String> ids = sqlService.dbRead(DELAY_FIND_BY_RESOURCE_EVENT_SQL, fields, null);
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#findDelayIds(java.lang.String)
	 */
	public List<String> findDelayIds(String resource)
	{
		Object[] fields = new Object[] { resource };
		List<String> ids = sqlService.dbRead(DELAY_FIND_BY_RESOURCE_SQL, fields, null);
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
	public String createDelay(Event event, String userId, Time fireTime)
	{
		// delete previous like delays
		deleteDelay(event);

		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource(), userId };
		sqlService.dbWrite(DELAY_WRITE_SQL, fields);
		List ids = sqlService.dbRead(DELAY_FIND_FINE_SQL, fields, null);
		String id = null;
		if (ids.size() > 0)
			id = (String) ids.get(0);

		// Schedule the new delayed invocation
		LOG.info("Creating new scheduled invocation [" + id + "]");
		schedInvocMgr.createDelayedInvocation(fireTime, BaseEventDelayHandler.class.getName(), id);
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.providers.EventDelayHandler#deleteDelayById(java.lang.String)
	 */
	public boolean deleteDelayById(String delayId)
	{
		// Remove any existing notifications for this notification
		DelayedInvocation[] prevInvocs = schedInvocMgr.findDelayedInvocations(
				BaseEventDelayHandler.class.getName(), delayId);
		if (prevInvocs != null && prevInvocs.length > 0)
		{
			for (DelayedInvocation invoc : prevInvocs)
			{
				LOG.info("Deleting previously scheduled invocation to create new one ["
						+ invoc.contextId + "]");
				schedInvocMgr.deleteDelayedInvocation(invoc.uuid);
			}
		}
		boolean ret = sqlService
				.dbWrite(DELAY_DELETE_SQL, new Object[] { Long.parseLong(delayId) });
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
		LOG.info("Refiring event that was delayed until resource is available: delay id " + opaqueContext);
		final Event event = popEventDelay(opaqueContext);
		
		// Set up security advisor
		try
		{
			User user = userDirectoryService.getUser(event.getUserId());

			SecurityService.pushAdvisor(new SecurityAdvisor() {
			    public SecurityAdvice isAllowed(String userId, String function, String reference) {
			            if (SecurityService.unlock(event.getUserId(), function, reference)) {
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
			SecurityService.clearAdvisors();
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
	}
}
