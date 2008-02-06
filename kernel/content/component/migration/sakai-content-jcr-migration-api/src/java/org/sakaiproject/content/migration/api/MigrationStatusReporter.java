package org.sakaiproject.content.migration.api;

public interface MigrationStatusReporter
{

	/*
	 * Returns number finished and total number to convert. example: There are
	 * 32 out of 3000 files remaining. [ 32 , 3000 ]
	 */
	public int[] filesRemaining();

	public boolean hasMigrationStarted();

	public boolean hasMigrationFinished();
}
