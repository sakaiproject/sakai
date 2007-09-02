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

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.IndexTransactionException;
import org.sakaiproject.search.indexer.api.IndexUpdateTransaction;
import org.sakaiproject.search.indexer.api.TransactionListener;

/**
 * @author ieb
 */
public class ClusterDbJournal implements TransactionListener
{

	private DataSource datasource;

	/**
	 * @throws IndexJournalException
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexUpdateTransaction transaction) throws IndexJournalException
	{
		PreparedStatement insertPst = null;
		try
		{
			
			Connection connection = datasource.getConnection();
			transaction.put(ClusterDbJournal.class.getName() + ".connection", connection);
			
			insertPst = connection.prepareStatement("insert into search_journal (txid, txts, indexwriter) values ( ?,?,?)");
			insertPst.clearParameters();
			insertPst.setLong(1, transaction.getTransactionId());
			insertPst.setLong(2, System.currentTimeMillis());
			insertPst.setString(3,String.valueOf(Thread.currentThread().getName()));
			if ( insertPst.executeUpdate() != 1 ) {
				throw new IndexJournalException("Failed to update index journal");
			}

		}
		catch ( IndexJournalException ijex ) {
			throw ijex;
		}
		catch (Exception ex)
		{
			throw new IndexJournalException("Failed to transfer index ", ex);
		} finally {
			try { insertPst.close(); } catch ( Exception ex ) {}
		}

	}

	/**
	 * @see org.sakaiproject.search.indexer.api.TransactionListener#commit(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void commit(IndexUpdateTransaction transaction)
			throws IndexTransactionException
	{
		Connection connection = (Connection) transaction.get(ClusterDbJournal.class
				.getName()
				+ ".connection");
		try
		{
			connection.commit();			
		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
			}
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
		Connection connection = (Connection) transaction.get(ClusterDbJournal.class
				.getName()
				+ ".connection");
		try
		{
			
			connection.commit();

			transaction.clear(ClusterDbJournal.class.getName() + ".connection");
		}
		catch (Exception ex)
		{
			try
			{
				connection.rollback();
			}
			catch (Exception ex2)
			{
			}

		}
		finally
		{
			try
			{
				connection.close();
			}
			catch (Exception ex)
			{
			}

		}
	}

	/**
	 * @return the datasource
	 */
	public DataSource getDatasource()
	{
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(DataSource datasource)
	{
		this.datasource = datasource;
	}

}
