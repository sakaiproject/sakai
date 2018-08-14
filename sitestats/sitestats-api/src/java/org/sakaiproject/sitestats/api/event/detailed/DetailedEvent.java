package org.sakaiproject.sitestats.api.event.detailed;

import java.util.Date;

/**
 * Details about an event recorded by SiteStats, including event ref and timestamp
 * @author plukasew
 */
public interface DetailedEvent
{
	/**
	 * Gets the unique id of the event
	 * @return unique id
	 */
	public long getId();

	/**
	 * Sets the unique id of the event
	 * @param id unique id
	 */
	public void setId(long id);

	/**
	 * Gets the site id for the event
	 * @return the site id
	 */
	public String getSiteId();

	/**
	 * Sets the site id for the event
	 * @param siteId the site id
	 */
	public void setSiteId(String siteId);

	/**
	 * Gets the user id for the event
	 * @return the user id
	 */
	public String getUserId();

	/**
	 * Sets the user id for the event
	 * @param userId the user id
	 */
	public void setUserId(String userId);

	/**
	 * Gets the event id (type of event)
	 * @return the event id
	 */
	public String getEventId();

	/**
	 * Sets the event id (type of event)
	 * @param eventId the event id
	 */
	public void setEventId(String eventId);

	/**
	 * Gets the event reference
	 * @return the event reference
	 */
	public String getEventRef();

	/**
	 * Sets the event reference
	 * @param eventRef the event reference
	 */
	public void setEventRef(String eventRef);

	/**
	 * Gets the date (and time) of the event
	 * @return the date
	 */
	public Date getEventDate();

	/**
	 * Sets the date (and time) of the event
	 * @param date the date
	 */
	public void setEventDate(Date date);
}
