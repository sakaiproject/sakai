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

package org.sakaiproject.search.cluster.impl;

import java.io.File;
import java.io.FileOutputStream;

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexTransactionException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.TransactionListener;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class ClusterSharedFilesystem implements TransactionListener
{

	private String journalLocation;

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexUpdateTransaction transaction) throws IndexJournalException
	{
		try
		{
			String location = transaction.getTempIndex();
			File indexLocation = new File(location);
			File tmpZip = new File(journalLocation, transaction.getTransactionId()
					+ ".zip." + System.currentTimeMillis());
			tmpZip.getParentFile().mkdirs();
			FileOutputStream zout = new FileOutputStream(tmpZip);
			String basePath = indexLocation.getPath();
			String replacePath = String.valueOf(transaction.getTransactionId()) + "/";
			FileUtils.pack(indexLocation, basePath, replacePath, zout);
			File journalZip = new File(journalLocation, transaction.getTransactionId()
					+ ".zip");
			transaction.put(ClusterSharedFilesystem.class.getName() + ".journalZip", journalZip);
			transaction.put(ClusterSharedFilesystem.class.getName() + ".tmpZip", tmpZip);
			FileUtils.listDirectory(journalZip.getParentFile());
			

		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to transfer index ", ex);
		} finally {
		}

	}

	/**
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexUpdateTransaction transaction)
			throws IndexTransactionException
	{
		try
		{
			File journalZip = (File) transaction.get(ClusterSharedFilesystem.class.getName()
					+ ".journalZip");
			File tmpZip = (File) transaction.get(ClusterSharedFilesystem.class.getName()
					+ ".tmpZip");
			tmpZip.renameTo(journalZip);
			transaction.clear(ClusterSharedFilesystem.class.getName() + ".journalZip");
			transaction.clear(ClusterSharedFilesystem.class.getName() + ".tmpZip");
			FileUtils.listDirectory(journalZip.getParentFile());			
		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to commit index ", ex);
		}

	}

	/**
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexUpdateTransaction transaction)
	{
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexUpdateTransaction transaction)
	{
		try
		{
			File journalZip = (File) transaction.get(ClusterSharedFilesystem.class.getName()
					+ ".journalZip");
			File tmpZip = (File) transaction.get(ClusterSharedFilesystem.class.getName()
					+ ".tmpZip");
			if (tmpZip != null && tmpZip.exists())
			{
				tmpZip.delete();
			}
			if (journalZip != null && journalZip.exists())
			{
				journalZip.delete();
			}
			
			transaction.clear(ClusterSharedFilesystem.class.getName() + ".journalZip");
			transaction.clear(ClusterSharedFilesystem.class.getName() + ".tmpZip");
		}
		catch (Exception ex)
		{

		}
		finally
		{
		}
	}

	/**
	 * @return the journalLocation
	 */
	public String getJournalLocation()
	{
		return journalLocation;
	}

	/**
	 * @param journalLocation
	 *        the journalLocation to set
	 */
	public void setJournalLocation(String journalLocation)
	{
		this.journalLocation = journalLocation;
	}

}
