package org.sakaiproject.content.migration.api;

/*
 * This interface is for migrating content from the Legacy
 * CHS to the JCR Implementation of ContentHosting.
 * 
 * Need to add some methods to find out the status of the migration.
 * 
 */
public interface CHStoJCRMigrator
{

	/*
	 * Start the Migration
	 */
	public void startMigrating();

	/*
	 * Stop/Pause the Migration. Will finish the current batch of files/folders
	 */
	public void stopMigrating();

	/*
	 * Find out if we're currently copying files in the background. @return Are
	 * we currently migrating?
	 */
	public boolean isCurrentlyMigrating();

	/*
	 * This is number of files/folders that are copied before taking a delay
	 * break, or checking to see if we stopped/paused the migration.
	 */
	public int getBatchSize();

	/*
	 * Set the number of files/folders to copy at a time.
	 */
	public void setBatchSize(int batchSize);

	/*
	 * Get the amount of time we wait between copying each batch of files.
	 */
	public int getDelayBetweenBatchesMilliSeconds();

	/*
	 * Set the number of milli seconds to delay between copying each batch of
	 * files.
	 */
	public void setDelayBetweenBatchesMilliSeconds(int milliseconds);
}
