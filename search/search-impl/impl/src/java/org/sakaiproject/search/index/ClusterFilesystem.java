package org.sakaiproject.search.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ClusterFilesystem
{

	/**
	 * Update all the segments in the cluster file system, retruning a list of
	 * segment names with the current segment as the last in the list.
	 * 
	 * @return
	 */
	List updateSegments();

	/**
	 * get the total size of a segment
	 * 
	 * @param currentSegment
	 * @return
	 */
	long getTotalSize(File currentSegment);

	/**
	 * saves the segments returning a list of segments that were sent to the
	 * central store
	 * 
	 * @return
	 */
	List saveSegments();
	
	/**
	 * Forces all segments from this system into the DB, does not delete any inthe db.
	 * @return
	 */
	List saveAllSegments();

	/**
	 * create a new segment
	 * 
	 * @return
	 * @throws IOException
	 */
	File newSegment() throws IOException;

	/**
	 * set the location information for the cluster file store
	 * 
	 * @param location
	 */

	void setLocation(String location);

	/**
	 * Update the timestamp on the segment
	 * 
	 * @param currentSegment
	 * @throws IOException
	 */
	void touchSegment(File currentSegment) throws IOException;

	/**
	 * get a clean temporary index space for building a detached segment
	 * @param delete if true the temp index will be deleted first, there is only 1 temp index per location
	 * @return
	 */
	File getTemporarySegment(boolean delete);

	/**
	 * removes the temporary segment
	 *
	 */
	void removeTemporarySegment();

	/**
	 * Recover a dammaged segment from the DB
	 * @param segment
	 */
	void recoverSegment(String segment);

	/** 
	 * gets the segment name from a path
	 * @param segment
	 * @return
	 */
	String getSegmentName(String segment);

	/**
	 * Checks that a segment is valid
	 * @param segmentName
	 * @throws Exception 
	 */
	boolean checkSegmentValidity(String segmentName) throws Exception;

}
