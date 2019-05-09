package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;

/**
 * @author plukasew
 */
public class DetailedEventImpl implements DetailedEvent, Serializable
{
	@Getter @Setter private long id;
	@Getter @Setter private String siteId;
	@Getter @Setter private String userId;
	@Getter @Setter private String eventId;
	@Getter @Setter private String eventRef;
	@Getter @Setter private Date eventDate;

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
}
