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

package org.sakaiproject.search.journal.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.JournalStorage;
import org.sakaiproject.search.journal.api.JournalStorageState;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.util.FileUtils;

/**
 * @author ieb
 */
public class SharedFilesystemJournalStorage implements JournalStorage
{

	/**
	 * @author ieb
	 */
	public class JournalStorageStateImpl implements JournalStorageState
	{

		protected File tmpZip;

		protected File journalZip;

		/**
		 * @param tmpZip
		 * @param journalZip
		 */
		public JournalStorageStateImpl(File tmpZip, File journalZip)
		{
			this.tmpZip = tmpZip;
			this.journalZip = journalZip;
		}

	}

	private static final Log log = LogFactory
			.getLog(SharedFilesystemJournalStorage.class);

	private String journalLocation;

	/**
	 * @param transactionId
	 * @return
	 */
	private File getTransactionFile(long transactionId)
	{
		return new File(journalLocation, transactionId + ".zip");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournalStorage#prepareSave(java.lang.String,
	 *      long)
	 */
	public JournalStorageState prepareSave(String location, long transactionId)
			throws IOException
	{
		File indexLocation = new File(location);
		File tmpZip = new File(journalLocation, transactionId + ".zip."
				+ System.currentTimeMillis());
		tmpZip.getParentFile().mkdirs();
		FileOutputStream zout = new FileOutputStream(tmpZip);
		String basePath = indexLocation.getPath();
		String replacePath = String.valueOf(transactionId) + "/";
		FileUtils.pack(indexLocation, basePath, replacePath, zout);
		File journalZip = getTransactionFile(transactionId);

		return new JournalStorageStateImpl(tmpZip, journalZip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournalStorage#commitSave(org.sakaiproject.search.journal.api.JournalStorageState)
	 */
	public void commitSave(JournalStorageState jss) throws IOException
	{
		File journalZip = ((JournalStorageStateImpl) jss).journalZip;
		File tmpZip = ((JournalStorageStateImpl) jss).tmpZip;
		tmpZip.renameTo(journalZip);
		FileUtils.listDirectory(journalZip.getParentFile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournalStorage#rollbackSave(org.sakaiproject.search.journal.api.JournalStorageState)
	 */
	public void rollbackSave(JournalStorageState jss)
	{
		File journalZip = ((JournalStorageStateImpl) jss).journalZip;
		File tmpZip = ((JournalStorageStateImpl) jss).tmpZip;
		if (tmpZip != null && tmpZip.exists())
		{
			tmpZip.delete();
		}
		if (journalZip != null && journalZip.exists())
		{
			journalZip.delete();
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

	/**
	 * @throws IOException
	 * @throws IOException
	 * @see org.sakaiproject.search.maintanence.api.JournalStorage#retrieveLaterVersions(long[],
	 *      java.lang.String)
	 */
	public void retrieveVersion(long version, String workingSpace) throws IOException
	{
		File ws = new File(workingSpace);
		ws.mkdirs();
		File f = getTransactionFile(version);
		FileInputStream source = new FileInputStream(f);
		FileUtils.unpack(source, ws);
		source.close();

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
	}

}
