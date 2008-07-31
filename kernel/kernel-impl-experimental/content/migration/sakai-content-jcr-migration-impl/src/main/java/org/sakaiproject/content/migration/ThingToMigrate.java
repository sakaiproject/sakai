package org.sakaiproject.content.migration;

import java.util.Date;


/**
 * A little class to model a single item in the queue that needs to be migrated.
 * 
 * @author sgithens
 */
public class ThingToMigrate
{
	public String contentId;

	public int status;

	public Date timeAdded;

	public String eventType;
}
