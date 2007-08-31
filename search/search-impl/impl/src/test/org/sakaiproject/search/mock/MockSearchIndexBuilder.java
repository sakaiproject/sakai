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

package org.sakaiproject.search.mock;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchWriterLock;

/**
 * @author ieb
 *
 */
public class MockSearchIndexBuilder implements SearchIndexBuilder
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#addResource(org.sakaiproject.event.api.Notification, org.sakaiproject.event.api.Event)
	 */
	public void addResource(Notification notification, Event event)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#destroy()
	 */
	public void destroy()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getAllSearchItems()
	 */
	public List getAllSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getContentProducers()
	 */
	public List getContentProducers()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentDocument()
	 */
	public String getCurrentDocument()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentElapsed()
	 */
	public String getCurrentElapsed()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentLock()
	 */
	public SearchWriterLock getCurrentLock()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getGlobalMasterSearchItems()
	 */
	public List getGlobalMasterSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getLastDocument()
	 */
	public String getLastDocument()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getLastElapsed()
	 */
	public String getLastElapsed()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getNodeStatus()
	 */
	public List getNodeStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getPendingDocuments()
	 */
	public int getPendingDocuments()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getSiteMasterSearchItems()
	 */
	public List getSiteMasterSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isBuildQueueEmpty()
	 */
	public boolean isBuildQueueEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isLocalLock()
	 */
	public boolean isLocalLock()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isOnlyIndexSearchToolSites()
	 */
	public boolean isOnlyIndexSearchToolSites()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#newEntityContentProducer(org.sakaiproject.event.api.Event)
	 */
	public EntityContentProducer newEntityContentProducer(Event event)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#newEntityContentProducer(java.lang.String)
	 */
	public EntityContentProducer newEntityContentProducer(String ref)
	{
		
		return new EntityContentProducer() {

			public boolean canRead(String reference)
			{
				return true;
			}

			public Integer getAction(Event event)
			{
				// TODO Auto-generated method stub
				return null;
			}

			public List getAllContent()
			{
				// TODO Auto-generated method stub
				return null;
			}

			public String getContainer(String ref)
			{
				// TODO Auto-generated method stub
				return ref.substring(0,ref.lastIndexOf("/"));
			}

			public String getContent(String reference)
			{
				StringBuilder sb = new StringBuilder();
				for ( int i = 0;  i < 100; i++ ) {
					sb.append(reference).append(" ").append(System.currentTimeMillis()).append(" ");
				}
				return sb.toString();
			}

			public Reader getContentReader(String reference)
			{
				return new StringReader(getContainer(reference));
			}

			public Map getCustomProperties(String ref)
			{
				return null;
			}

			public String getCustomRDF(String ref)
			{
				// TODO Auto-generated method stub
				return null;
			}

			public String getId(String ref)
			{
				return ref;
			}

			public List getSiteContent(String context)
			{
				// TODO Auto-generated method stub
				return null;
			}

			public Iterator getSiteContentIterator(String context)
			{
				// TODO Auto-generated method stub
				return null;
			}

			public String getSiteId(String reference)
			{
				return getContainer(reference);
			}

			public String getSubType(String ref)
			{
				return "TEST";
			}

			public String getTitle(String reference)
			{
				return reference;
			}

			public String getTool()
			{
				return "TESTTOOL";
			}

			public String getType(String ref)
			{
				return "TESTTYPE";
			}

			public String getUrl(String reference)
			{
				return "ref";
			}

			public boolean isContentFromReader(String reference)
			{
				return false;
			}

			public boolean isForIndex(String reference)
			{
				return true;
			}

			public boolean matches(String reference)
			{
				return true;
			}

			public boolean matches(Event event)
			{
				return true;
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#rebuildIndex()
	 */
	public void rebuildIndex()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#rebuildIndex(java.lang.String)
	 */
	public void rebuildIndex(String currentSiteId)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#refreshIndex()
	 */
	public void refreshIndex()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#refreshIndex(java.lang.String)
	 */
	public void refreshIndex(String currentSiteId)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#registerEntityContentProducer(org.sakaiproject.search.api.EntityContentProducer)
	 */
	public void registerEntityContentProducer(EntityContentProducer ecp)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#removeWorkerLock()
	 */
	public boolean removeWorkerLock()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
