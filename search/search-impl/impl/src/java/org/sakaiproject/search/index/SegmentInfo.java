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

/**
 * Represents Information about a search segment
 * 
 * @author ieb
 */
public interface SegmentInfo
{

	/**
	 * Is the segment part of the cluster or just a stray directory.
	 * 
	 * @return
	 */
	boolean isClusterSegment();

	/**
	 * Get the name of the segment
	 * 
	 * @return
	 */
	String getName();

	/**
	 * get the current version of the segment
	 * 
	 * @return
	 */
	long getVersion();

	/**
	 * If the segment in a created state (ie not new and not deleted)
	 * 
	 * @return
	 */
	boolean isCreated();

	/**
	 * The File that is the segment location on the local filesystem (may not
	 * exist)
	 * 
	 * @return
	 */
	File getSegmentLocation();

	/**
	 * Mark the segment as deleted for later deletion
	 */
	void setDeleted();

	/**
	 * Is the segment in the DB
	 * 
	 * @return
	 */
	boolean isInDb();

	/**
	 * Set the version of the segment
	 * 
	 * @param newVersion
	 */
	void setVersion(long newVersion);

	/**
	 * Reset the internal checkum of the segment
	 * 
	 * @throws IOException
	 */
	// void setCheckSum() throws IOException;
	/**
	 * Was the segment deleted
	 * 
	 * @return
	 */
	boolean isDeleted();

	/**
	 * Set the timestamp on th segment
	 * 
	 * @param l
	 * @throws IOException
	 */
	void setTimeStamp(long l) throws IOException;

	/**
	 * Mark the segment as brand new
	 */
	void setNew();

	/**
	 * Get the time the segment was last modified
	 * 
	 * @return
	 */
	long getLocalSegmentLastModified();

	/**
	 * Get the size of the segment on disk
	 * 
	 * @return
	 */
	long getLocalSegmentSize();

	/**
	 * Mark the segment as created
	 */
	void setCreated();

	/**
	 * Check the validity of the segment
	 * 
	 * @throws Exception
	 */
	boolean checkSegmentValidity(boolean logging, String message) throws Exception;

	/**
	 * Check the validity of segment
	 * 
	 * @param force
	 * @param validate
	 * @return
	 * @throws Exception
	 */
	// boolean checkSegmentValidity(boolean force, boolean validate) throws
	// Exception;
	/**
	 * Get the size of the segment
	 * 
	 * @return
	 */
	long getTotalSize();

	/**
	 * make the segment as updated
	 * 
	 * @throws IOException
	 */
	void touchSegment() throws IOException;

	/**
	 * calculate the total size (this is expensive)
	 */
	void loadSize();

	/**
	 * get the size of the segment.
	 * 
	 * @return
	 */
	long getSize();

	/**
	 * Perform a final delete removing all files from the local disk
	 */
	void doFinalDelete();

	/**
	 * Perform a lock on the segment for local operations
	 */
	void lockLocalSegment();

	/**
	 * Does this thread have a local lock on the segment
	 * 
	 * @return
	 */
	boolean isLocalLock();

	/**
	 * Unlock the segment
	 */
	void unlockLocalSegment();

	/**
	 * @param string
	 */
	void debugSegment(String string);

	/**
	 * Compares this segment to the supplied segment, based on the in memory
	 * state of the live segment state
	 * 
	 * @param string
	 * @param recoverSegInfo
	 */
	void compareTo(String message, SegmentInfo recoverSegInfo);

}
