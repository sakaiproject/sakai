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

package org.sakaiproject.search.indexer.debug;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.indexer.api.IndexWorker;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;

/**
 * Debug listenwe to the Documents being indexed
 * 
 * @author ieb Unit test
 * @see org.sakaiproject.search.indexer.impl.test.TransactionalIndexWorkerTest
 */
public class DebugIndexWorkerDocumentListener implements IndexWorkerDocumentListener
{

	private static final Log log = LogFactory
			.getLog(DebugIndexWorkerDocumentListener.class);

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener#indexDocumentEnd(org.sakaiproject.search.indexer.api.IndexWorker,
	 *      java.lang.String)
	 */
	public void indexDocumentEnd(IndexWorker worker, String ref)
	{
		log.info("Worker [" + worker + "] Document End [" + ref + "]");

	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener#indexDocumentStart(org.sakaiproject.search.indexer.api.IndexWorker,
	 *      java.lang.String)
	 */
	public void indexDocumentStart(IndexWorker worker, String ref)
	{
		log.info("Worker [" + worker + "] Document Start [" + ref + "]");

	}

}
