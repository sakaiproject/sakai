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

import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.model.impl.SearchBuilderItemImpl;

/**
 * @author ieb
 */
public class SearchBuilderItemSerializer
{
	
	protected static final String TRANSACTION_LIST = "sakai_tx";

	public void saveTransactionList(File indexDirectoy, List<SearchBuilderItem> txList) throws IOException
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

	public List<SearchBuilderItem> loadTransactionList(File indexDirectoy) throws IOException
	{
		File transactionList = new File(indexDirectoy, TRANSACTION_LIST);
		DataInputStream dataInputStream = new DataInputStream(new FileInputStream(
				transactionList));
		List<SearchBuilderItem> itemList = new ArrayList<SearchBuilderItem>();
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

		}
		dataInputStream.close();
		return itemList;
	}

}
