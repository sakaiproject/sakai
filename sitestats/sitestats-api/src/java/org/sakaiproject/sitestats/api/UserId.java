package org.sakaiproject.sitestats.api;

/**
 * Immutable class representing a Sakai internal user id
 * @author plukasew
 */
public class UserId
{
	public final String uuid;

	public UserId(String uuid)
	{
		this.uuid = uuid;
	}
}
