package org.sakaiproject.content.migration;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

/*
 * This observer listens for content hosting events.  When it gets an event, it
 * appends it to the table of Content Resources and Collections that need to be
 * migrated.
 * 
 * Different Kinds of Content Events Include:
 * EVENT_RESOURCE_ADD, content.new
 * EVENT_RESOURCE_READ, content.read  <-- We don't really need to listen for this.
 * EVENT_RESOURCE_WRITE, content.revise
 * EVENT_RESOURCE_REMOVE, content.delete
 */
public class MigrationInProgressObserver implements Observer
{
	private static Log log = LogFactory.getLog(MigrationInProgressObserver.class);

	private static String APPEND_RECORD_SQL = "INSERT INTO MIGRATE_CHS_CONTENT_TO_JCR"
			+ " SET CONTENT_ID = ?, TIME_ADDED_TO_QUEUE = ?, EVENT_TYPE = ?";

	private static String AUTO_UPDATE_FIELD = "id";

	protected EventTrackingService eventTrackingService;

	protected SqlService sqlService;

	private boolean enabled = false;

	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
		enabled  = serverConfigurationService.getBoolean("jcr.experimental",false);
		if (!enabled ) {
			return;
		}

		log.info("init()");
		eventTrackingService.addObserver(this);
	}

	public void destroy()
	{
		if (!enabled ) {
			return;
		}
		log.info("destroy()");
		eventTrackingService.deleteObserver(this);
	}

	public void update(Observable obs, Object eventObj)
	{
		if (!enabled ) {
			return;
		}
		if (eventObj instanceof Event)
		{
			Event e = (Event) eventObj;
			if (e.getEvent().equals(ContentHostingService.EVENT_RESOURCE_ADD)
					|| e.getEvent().equals(ContentHostingService.EVENT_RESOURCE_WRITE)
					|| e.getEvent().equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				try
				{
					Connection conn = sqlService.borrowConnection();
					sqlService.dbInsert(conn, APPEND_RECORD_SQL, new Object[] {
							e.getResource(),
							new Date(GregorianCalendar.getInstance().getTimeInMillis()),
							e.getEvent() }, AUTO_UPDATE_FIELD);
					sqlService.returnConnection(conn);
				}
				catch (SQLException sqlException)
				{
					log
							.error(
									"Could not insert new record for JCR Migration from CHS Event",
									sqlException);
				}
			}

		}

	}

	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService()
	{
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

}
