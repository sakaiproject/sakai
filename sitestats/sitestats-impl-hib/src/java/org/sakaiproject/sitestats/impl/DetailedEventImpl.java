package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;

/**
 * @author plukasew
 */
public class DetailedEventImpl implements DetailedEvent, Serializable
{
	private long id;
	private String siteId;
	private String userId;
	private String eventId;
	private String eventRef;
	private Date eventDate;

	public DetailedEventImpl()
	{
		this(0, "", "", "", "", new Date());
	}

	public DetailedEventImpl(long id, String siteId, String userId, String eventId, String eventRef, Date date)
	{
		this.id = id;
		this.siteId = siteId;
		this.userId = userId;
		this.eventId = eventId;
		this.eventRef = eventRef;
		this.eventDate = date;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getEventId()
	{
		return eventId;
	}

	public void setEventId(String eventId)
	{
		this.eventId = eventId;
	}

	public String getEventRef()
	{
		return eventRef;
	}

	public void setEventRef(String eventRef)
	{
		this.eventRef = eventRef;
	}

	public Date getEventDate()
	{
		return eventDate;
	}

	public void setEventDate(Date date)
	{
		this.eventDate = date;
	}
}
