/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ClusterFilesystem provides the mechanism by which the local search index is
 * syncronsed with the clustered file system
 * 
 * @author ieb
 */
public interface ClusterFilesystem
{

	/**
	 * Update all the segments in the cluster file system, retruning a list of
	 * segment names with the current segment as the last in the list.
	 * 
	 * @return
	 */
	List<SegmentInfo> updateSegments();

	/**
	 * get the total size of a segment
	 * 
	 * @param currentSegment
	 * @return
	 */
	// long getTotalSize(File currentSegment);
	/**
	 * saves the segments returning a list of segments that were sent to the
	 * central store
	 * 
	 * @return
	 */
	List<SegmentInfo> saveSegments();

	/**
	 * Forces all segments from this system into the DB, does not delete any
	 * inthe db.
	 * 
	 * @return
	 */
	List<SegmentInfo> saveAllSegments();

	/**
	 * create a new segment
	 * 
	 * @return
	 * @throws IOException
	 */
	SegmentInfo newSegment() throws IOException;

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
	// void touchSegment(File currentSegment) throws IOException;
	/**
	 * get a clean temporary index space for building a detached segment
	 * 
	 * @param delete
	 *        if true the temp index will be deleted first, there is only 1 temp
	 *        index per location
	 * @return
	 */
	File getTemporarySegment(boolean delete);

	/**
	 * removes the temporary segment
	 */
	void removeTemporarySegment();

	/**
	 * Recover a dammaged segment from the DB
	 * 
	 * @param segment
	 */
	void recoverSegment(SegmentInfo segment);

	/**
	 * gets the segment name from a path
	 * 
	 * @param segment
	 * @return
	 */
	// String getSegmentName(String segment);
	/**
	 * Checks that a segment is valid
	 * 
	 * @param segmentName
	 * @throws Exception
	 */
	// boolean checkSegmentValidity(String segmentName) throws Exception;
	/**
	 * Remove a segment from the index.
	 * 
	 * @param mergeSegment
	 */
	void removeLocalSegment(SegmentInfo mergeSegment);

	long getLastUpdate();

	List getSegmentInfoList();

	/**
	 * if the thread already has a lock ignore get a lock on the index so that
	 * it can be updated this should block untill a lock becomes free
	 * 
	 * @throws IOException
	 */
	void getLock() throws IOException;

	/**
	 * release the lock, only if there is one this should block untill a lock
	 * becomes free
	 */
	void releaseLock();

	/**
	 * can the Cluster Filesystem cope with multiple indexers running at the
	 * same time
	 * 
	 * @return
	 */
	boolean isMultipleIndexers();

	/**
	 * Save the temporary segment into a permanent segment
	 * 
	 * @return
	 * @throws IOException
	 */
	SegmentInfo saveTemporarySegment() throws IOException;

	/**
	 * A low cost reliable mechanism for determining if an index exists in the
	 * cluster
	 * 
	 * @return
	 */
	boolean centralIndexExists();

}
