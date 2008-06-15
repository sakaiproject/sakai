package org.sakaiproject.content.providers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.time.api.Time;

public class ResourceEventVoterHelper
{
	private static final Log LOG = LogFactory.getLog(ResourceEventVoterHelper.class);
	private static final String DELAY_WRITE_SQL = "insert into SAKAI_EVENT_DELAY (EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID) VALUES (?, ?, ?, ?, ?)";
	private static final String DELAY_READ_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";
	private static final String DELAY_READ_FINE_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT = ? and MODIFY = ? and PRIORITY = ? and RESOURCE = ? and USER_ID = ?";
	private static final String DELAY_READ_EVENT_SQL = "select EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID from SAKAI_EVENT_DELAY where EVENT = ? and MODIFY = ? and PRIORITY = ? and RESOURCE = ?";
	private static final String DELAY_FIND_BY_RESOURCE_SQL = "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where RESOURCE = ?";
	private static final String DELAY_DELETE_SQL = "delete from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";

	private SqlService sqlService;
	private ScheduledInvocationManager schedInvocMgr;

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public void setSchedInvocMgr(ScheduledInvocationManager schedInvocMgr)
	{
		this.schedInvocMgr = schedInvocMgr;
	}

	/**
	 * Find event delay IDs that are associated to a resource
	 * 
	 * @param resource
	 * @return
	 */
	public List<String> findDelayIds(String resource)
	{
		List<String> results = sqlService.dbRead(DELAY_FIND_BY_RESOURCE_SQL,
				new String[] { resource }, null);
		return results;
	}

	/**
	 * Read an event delay by a specified delay ID.
	 * 
	 * @param delayId
	 * @return
	 */
	public Event readEventDelay(String delayId)
	{
		List results = sqlService.dbRead(DELAY_READ_SQL, new Long[] { Long.parseLong(delayId) }, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result) throws SqlReaderFinishedException
			{
				Event e = null;
				try
				{
					// EVENT_DELAY_ID, EVENT, MODIFY, PRIORITY, RESOURCE, USER_ID
					e = new ReEvent(result.getString(2), result.getBoolean(3), result.getInt(4),
							result.getString(5), result.getString(6));

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
		Event e = readEventDelay(delayId);
		deleteDelay(delayId);
		return e;
	}

	/**
	 * Read a list of event delay IDs using the provided event and user ID.
	 * 
	 * @param event
	 * @param userId
	 * @return
	 */
	public List<String> readEventDelayIds(Event event, String userId)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource(), userId };
		List<String> ids = sqlService.dbRead(DELAY_READ_FINE_SQL, fields, null);
		return ids;
	}

	/**
	 * Read a list of event delay IDs using the provided event.  Does not look at the user ID.
	 * 
	 * @param event
	 * @return
	 */
	public List<String> readEventDelayIds(Event event)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource() };
		List<String> ids = sqlService.dbRead(DELAY_READ_EVENT_SQL, fields, null);
		return ids;
	}

	/**
	 * Schedules a delayed invocation of this notification to run at the requested time.
	 * 
	 * @param notification
	 * @param event
	 * @param runTime
	 * @return The ID of the delay
	 */
	public String createDelay(Event event, String userId, Time releaseDate)
	{
		Object[] fields = new Object[] { event.getEvent(), event.getModify(), event.getPriority(),
				event.getResource(), userId };
		sqlService.dbWrite(DELAY_WRITE_SQL, fields);
		List ids = sqlService.dbRead(DELAY_READ_FINE_SQL, fields, null);
		String id = null;
		if (ids.size() > 0)
			id = (String) ids.get(0);

		// Schedule the new delayed invocation
		LOG.info("Creating new scheduled invocation [" + id + "]");
		schedInvocMgr.createDelayedInvocation(releaseDate, ResourceEventVoter.class.getName(), id);
		return id;
	}

	/**
	 * Delete an event delay by referencing the delay ID.
	 * @param delayId
	 * @return
	 */
	public boolean deleteDelay(String delayId)
	{
		// Remove any existing notifications for this notification
		DelayedInvocation[] prevInvocs = schedInvocMgr.findDelayedInvocations(
				ResourceEventVoter.class.getName(), delayId);
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

	/**
	 * Delete an event delay by matching the event information.
	 * 
	 * @param e
	 * @return
	 */
	public boolean deleteDelay(Event e)
	{
		boolean ret = true;
		List<String> ids = readEventDelayIds(e);
		for (String id : ids)
		{
			ret &= deleteDelay(id);
		}
		return ret;
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
			this.context = null;
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
