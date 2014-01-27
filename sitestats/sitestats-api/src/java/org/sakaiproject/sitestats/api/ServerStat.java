package org.sakaiproject.sitestats.api;

import java.util.Date;

/**
 * This must be {@link java.lang.Comparable} so that the updates can be sorted before being inserted into the database
 * to avoid deadlocks.
 */
public interface ServerStat extends Comparable<ServerStat>{
	
	/** Get the db row id. */
	public long getId();
	
	/** Set the db row id. */
	public void setId(long id);
	
	/** Get the date this record refers to. */
	public Date getDate();
	
	/** Set the date this record refers to. */
	public void setDate(Date date);

	/** Get the event this record refers to. */
	public String getEventId();
	
	/** Set the event this record refers to. */
	public void setEventId(String eventId);
	
	/** Get the total times this event was generated on this context and date. */
	public long getCount();
	
	/** Set the total times this event was generated on this context and date. */
	public void setCount(long count);
}
