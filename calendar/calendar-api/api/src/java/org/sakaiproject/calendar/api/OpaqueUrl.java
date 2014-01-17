package org.sakaiproject.calendar.api;

/**
 * OpaqueURL stores the unique IDs used in calendar feeds. These unique URLs allow calendar clients to access
 * the calendar without supplying credentials.
 */
public interface OpaqueUrl {

	/**
	 * The user's ID to who this OpaqueUrl belongs.
	 * @return A User ID.
	 */
	public String getUserUUID();

	/**
	 * The calendar reference which this OpaqueUrl points to, it doesn't mean the user still has permission
	 * to view it.
	 * @return A Calendar reference.
	 */
	public String getCalendarRef();

	/**
	 * The ID which should be in the URL.
	 * @return An unguessable ID which should be part of the URL clients use to connect.
	 */
	public String getOpaqueUUID();

}
