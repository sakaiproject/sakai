package org.sakaiproject.citation.api;

public interface SearchDatabase
{
	/**
	 * Returns the display name for this database
	 * 
	 * @return display name for this database
	 */
	public String getDisplayName();
	
	/**
	 * Returns the description for this database
	 * 
	 * @return description for this database
	 */
	public String getDescription();
	
	/**
	 * Returns the id for this database
	 * 
	 * @return id for this database
	 */
	public String getId();
	
	/**
	 * Determines whether or not this database belongs to the given group
	 * 
	 * @param groupId group identifier (name, id, etc...) to check
	 * @return true if this database is a member of the group, false otherwise
	 */
	public boolean isGroupMember( String groupId );
}
