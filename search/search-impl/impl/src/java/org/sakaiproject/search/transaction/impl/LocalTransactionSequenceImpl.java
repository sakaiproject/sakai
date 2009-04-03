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

package org.sakaiproject.search.transaction.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.transaction.api.TransactionSequence;

/**
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.SequenceGeneratorDisabled
 */
public class LocalTransactionSequenceImpl implements TransactionSequence
{

	private static final Log log = LogFactory.getLog(LocalTransactionSequenceImpl.class);

	private long localId = System.currentTimeMillis();

	/**
	 * dependency
	 */
	private String name = "indexupdate";

	/**
	 * Loads the first transaction to initialize
	 */
	public void init()
	{

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.service.index.transactional.api.TransactionSequence#getNextId()
	 */
	public long getNextId()
	{
		throw new UnsupportedOperationException("Sequence is Local only");
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *        the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.indexer.api.TransactionSequence#getLocalId()
	 */
	public long getLocalId()
	{
		// this should be attomic
		long next = localId++;
		return next;
	}

}
