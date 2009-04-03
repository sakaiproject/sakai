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
 * Represents the situation where a lock has failed and timeout has happened
 * @author ieb
 *
 */
public class LockTimeoutException extends IndexJournalException
{

	/**
	 * 
	 */
	public LockTimeoutException()
	{
	}

	/**
	 * @param arg0
	 */
	public LockTimeoutException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public LockTimeoutException(Throwable arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public LockTimeoutException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}
