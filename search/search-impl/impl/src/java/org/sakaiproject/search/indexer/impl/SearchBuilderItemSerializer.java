/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.search.indexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;

/**
 * A utility class to serialise update lists to and from disk
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 * @see org.sakaiproject.search.indexer.impl.test.SearchBuilderItemSerializerTest
 */
public class SearchBuilderItemSerializer
{
	private static final Log log = LogFactory.getLog(SearchBuilderItemSerializer.class);
	
	protected static final String TRANSACTION_LIST = "sakai_tx";

	public void init()
	{

	}

	public void destroy()
	{

	}

	public void saveTransactionList(File indexDirectoy, List<SearchBuilderItem> txList)
			throws IOException
	{
		File transactionList = new File(indexDirectoy, TRANSACTION_LIST);
		DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(
				transactionList));
		for (Iterator<SearchBuilderItem> isbi = txList.iterator(); isbi.hasNext();)
		{
			SearchBuilderItem sbi = isbi.next();
			sbi.output(dataOutputStream);
		}
		dataOutputStream.close();

	}

	public List<SearchBuilderItem> loadTransactionList(File indexDirectoy)
			throws IOException
	{
		File transactionList = new File(indexDirectoy, TRANSACTION_LIST);
		List<SearchBuilderItem> itemList = new ArrayList<SearchBuilderItem>();
		if (transactionList.exists())
		{
			DataInputStream dataInputStream = new DataInputStream(new FileInputStream(
					transactionList));
			try
			{
				while (true)
				{
					SearchBuilderItem sbi = new SearchBuilderItemImpl();
					sbi.input(dataInputStream);
					itemList.add(sbi);
				}
			}
			catch (IOException ioex)
			{
				log.debug("Exception reading from input stream, normal behaviour at the end of a list, readInt 210");
			}
			dataInputStream.close();
		}
		return itemList;
	}

	/**
	 * @param targetSegment
	 */
	public void removeTransactionList(File indexDirectory)
	{
		File transactionList = new File(indexDirectory, TRANSACTION_LIST);
		if (transactionList.exists())
		{
			transactionList.delete();
		}
	}

}
