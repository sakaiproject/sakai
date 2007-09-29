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
 * @author ieb TODO Unit test
 */
public class SharedFilesystemJournalStorage implements JournalStorage
{

	public void init()
	{

	}

	public void destory()
	{

	}

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

	private JournalSettings journalSettings;

	/**
	 * @param transactionId
	 * @return
	 */
	private File getTransactionFile(long transactionId)
	{
		return new File(journalSettings.getJournalLocation(), transactionId + ".zip");
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
		File tmpZip = new File(journalSettings.getJournalLocation(), transactionId
				+ ".zip." + System.currentTimeMillis());
		tmpZip.getParentFile().mkdirs();
		String basePath = indexLocation.getPath();
		String replacePath = String.valueOf(transactionId);

		FileOutputStream zout = new FileOutputStream(tmpZip);
		FileUtils.pack(indexLocation, basePath, replacePath, zout);
		zout.close();

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.journal.api.JournalStorage#rollbackSave(org.sakaiproject.search.journal.api.JournalStorageState)
	 */
	public void rollbackSave(JournalStorageState jss)
	{
		if (jss != null)
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
	}

	/**
	 * @throws IOException
	 * @throws IOException
	 * @see org.sakaiproject.search.maintanence.api.JournalStorage#retrieveLaterSavePoints(long[],
	 *      java.lang.String)
	 */
	public void retrieveSavePoint(long savePoint, String workingSpace) throws IOException
	{
		File ws = new File(workingSpace);
		ws.mkdirs();
		File f = getTransactionFile(savePoint);
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

	/**
	 * @param savePoint
	 * @param workingSpace
	 * @return
	 */
	public File getLocalJournalLocation(long savePoint, String workingSpace)
	{
		return new File(workingSpace, String.valueOf(savePoint));
	}

	/**
	 * @param savePoint
	 * @throws IOException
	 */
	public void removeJournal(long savePoint) throws IOException
	{
		File f = getTransactionFile(savePoint);
		FileUtils.deleteAll(f);

	}

	/**
	 * @return the journalSettings
	 */
	public JournalSettings getJournalSettings()
	{
		return journalSettings;
	}

	/**
	 * @param journalSettings
	 *        the journalSettings to set
	 */
	public void setJournalSettings(JournalSettings journalSettings)
	{
		this.journalSettings = journalSettings;
	}

}
