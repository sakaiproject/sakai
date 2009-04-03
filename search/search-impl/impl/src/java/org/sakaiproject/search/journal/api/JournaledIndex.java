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

package org.sakaiproject.search.journal.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * A Journaled index is a in index that contains a number of journaled segments.
 * These segments may be merged into a local index periodically
 * 
 * @author ieb
 */
public interface JournaledIndex extends JournaledObject
{

	/**
	 * A file name that is used to mark if a index should be deleted when its closed.
	 * This MUST only be used on local files to the JVM.
	 */
	String DELETE_ON_CLOSE_FILE = "deleteOnClose";

	/**
	 * The workign space associated with the index
	 * 
	 * @return
	 */
	String getWorkingSpace();

	/**
	 * Add a transient segment to the journal index
	 * 
	 * @param f
	 */
	void addSegment(File f);

	/**
	 * get an index reader suitable for processing deletes
	 * The underlying reader will be bound to the thread, and must be closed, but if closed
	 * and annother thread is using it, it will not be closed immediately.
	 * Although this is implementation specific the implementation should be thread safe.
	 *
	 * 
	 * @return
	 * @throws IOException
	 */
	IndexReader getDeletionIndexReader() throws IOException;

	/**
	 * get a copy of the segments currently active
	 * 
	 * @return
	 */
	File[] getSegments();

	/**
	 * Get an index writer suitable for accessing the current permanent index
	 * 
	 * @return
	 * @throws IndexTransactionException
	 */
	IndexWriter getPermanentIndexWriter() throws IndexTransactionException;

	/**
	 * Set the list of segments
	 * 
	 * @param keep
	 */
	void setSegments(List<File> keep);

	/**
	 * @throws IOException
	 */
	void saveSegmentList() throws IOException;

	/**
	 * load the shared index reader, but dont return it or bind it to the current thread
	 * 
	 * @throws IOException
	 */
	void loadIndexReader() throws IOException;

}
