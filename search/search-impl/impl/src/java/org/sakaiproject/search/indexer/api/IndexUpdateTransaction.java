/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.indexer.api;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * This represents a transactional index operation
 * 
 * @author ieb
 */
public interface IndexUpdateTransaction
{
	public static final int STATUS_ACTIVE = 0;

	public static final int STATUS_COMMITTED = 3;

	public static final int STATUS_COMMITTING = 8;

	public static final int STATUS_MARKED_ROLLBACK = 1;

	public static final int STATUS_NO_TRANSACTION = 6;

	public static final int STATUS_PREPARED = 2;

	public static final int STATUS_PREPARING = 7;

	public static final int STATUS_ROLLEDBACK = 4;

	public static final int STATUS_ROLLING_BACK = 9;

	public static final int STATUS_UNKNOWN = 5;

	/**
	 * Prepare to commit this transaction, all the work is done, but all the
	 * listeners need to be ready and able to performa commit without failure
	 */
	void prepare() throws IndexTransactionException;

	/**
	 * Commit the transaction and make it available to others in the cluster
	 * 
	 * @throws IndexTransactionException
	 */
	void commit() throws IndexTransactionException;

	/**
	 * Get the index writer associated with this transaction
	 * 
	 * @return
	 * @throws IndexTransactionException
	 *         if the transaction is not open
	 */
	IndexWriter getIndexWriter() throws IndexTransactionException;

	/**
	 * Get a list of add items associated with this transaction
	 * 
	 * @return
	 * @throws IndexTransactionException
	 *         if the transaction is not open
	 */
	Iterator<SearchBuilderItem> addItemIterator() throws IndexTransactionException;

	/**
	 * Sets the items for the tansaction if the transaction is not open
	 * 
	 * @param items
	 * @throws IndexTransactionException
	 *         if the items has already been set, or the transaction is not open
	 */
	void setItems(List<SearchBuilderItem> items) throws IndexTransactionException;

	/**
	 * @throws IndexTransactionException
	 *         if the transaction is not open
	 */
	void rollback() throws IndexTransactionException;

	/**
	 * @return
	 */
	String getTempIndex();

	/**
	 * @return
	 */
	long getTransactionId();

	/**
	 * 
	 */
	void close();

	/**
	 * The list of items
	 * 
	 * @return
	 */
	List<SearchBuilderItem> getItems();
	
	/**
	 * get the transaction status
	 * @return
	 */
	int getStatus();

	/**
	 * get an Object from the transaction, that may have been placed in the transaction
	 * by earlier phases.
	 * @param key
	 * @return
	 */
	Object get(String key);

	/**
	 * Clear an object placed in the transaction
	 * @param key
	 */
	void clear(String key);

	/**
	 * Put an object into the transaction for use in later phases
	 * @param key
	 * @param obj
	 */
	void put(String key, Object obj); 

}
