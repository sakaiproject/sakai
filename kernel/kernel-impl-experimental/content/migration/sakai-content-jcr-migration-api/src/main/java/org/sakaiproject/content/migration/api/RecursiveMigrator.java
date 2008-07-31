package org.sakaiproject.content.migration.api;

/**
 * This Utility will copy a directory recursively from DB ContentHosting to
 * the JCR ContentHosting Area.  Currently this is just meant for sampling 
 * what the migrated data will look like, and allow moving over a single site or
 * something to view by swapping the Multiplexor. Not meant for a full migration.
 * 
 * @author sgithens
 */
public interface RecursiveMigrator 
{
	
	/**
	 * Recursively copy a directory from DB ContentHosting to it's appropriate
	 * spot in JCR.
	 * 
	 * @param startDirectory
	 */
	void runRecursiveMigration(String startDirectory); 
}
