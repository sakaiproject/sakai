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

package org.sakaiproject.search.indexer.api;

/**
 * An index worker performs index operations
 * 
 * @author ieb
 */
public interface IndexWorker
{

	/**
	 * Perform an index cycle. If the indexing strategy requires locking this
	 * should be performed before invoking this method, as the implementation is
	 * not required to perform locking. Ideally the implementation should not
	 * require locking to happen and operation in a transactionally safe way.
	 * 
	 * @param batchSize
	 *        the maximum number of items to take from the queue
	 * @return the number of items processed
	 */
	int process(int batchSize);

	/**
	 * @param indexWorkerListener
	 */
	void addIndexWorkerListener(IndexWorkerListener indexWorkerListener);

	/**
	 * @param indexWorkerListener
	 */
	void removeIndexWorkerListener(IndexWorkerListener indexWorkerListener);

	/**
	 * @param indexWorkerDocumentListener
	 */
	void addIndexWorkerDocumentListener(IndexWorkerDocumentListener indexWorkerDocumentListener);

	/**
	 * @param indexWorkerDocumentListener
	 */
	void removeIndexWorkerDocumentListener(IndexWorkerDocumentListener indexWorkerDocumentListener);

}
