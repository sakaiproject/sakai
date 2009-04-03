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

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#destroy()
	 */
	public void destroy()
	{

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getAllSearchItems()
	 */
	public List getAllSearchItems()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getContentProducers()
	 */
	public List getContentProducers()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentDocument()
	 */
	public String getCurrentDocument()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentElapsed()
	 */
	public String getCurrentElapsed()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getCurrentLock()
	 */
	public SearchWriterLock getCurrentLock()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getGlobalMasterSearchItems()
	 */
	public List getGlobalMasterSearchItems()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getLastDocument()
	 */
	public String getLastDocument()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getLastElapsed()
	 */
	public String getLastElapsed()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getNodeStatus()
	 */
	public List getNodeStatus()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getPendingDocuments()
	 */
	public int getPendingDocuments()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#getSiteMasterSearchItems()
	 */
	public List getSiteMasterSearchItems()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isBuildQueueEmpty()
	 */
	public boolean isBuildQueueEmpty()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isLocalLock()
	 */
	public boolean isLocalLock()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isOnlyIndexSearchToolSites()
	 */
	public boolean isOnlyIndexSearchToolSites()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#newEntityContentProducer(org.sakaiproject.event.api.Event)
	 */
	public EntityContentProducer newEntityContentProducer(Event event)
	{
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
				return null;
			}

			public List getAllContent()
			{
				return null;
			}

			public String getContainer(String ref)
			{
				return ref.substring(0,ref.lastIndexOf("/"));
			}

			public String getContent(String reference)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("  Node is ").append(reference).append(" ");
				for ( int i = 0;  i < 100; i++ ) {
					sb.append(reference.replace('/', ' ')).append(" ").append(System.currentTimeMillis()).append(" ");
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
				return null;
			}

			public String getId(String ref)
			{
				return ref;
			}

			public List getSiteContent(String context)
			{
				return null;
			}

			public Iterator getSiteContentIterator(String context)
			{
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
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#rebuildIndex(java.lang.String)
	 */
	public void rebuildIndex(String currentSiteId)
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#refreshIndex()
	 */
	public void refreshIndex()
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#refreshIndex(java.lang.String)
	 */
	public void refreshIndex(String currentSiteId)
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#registerEntityContentProducer(org.sakaiproject.search.api.EntityContentProducer)
	 */
	public void registerEntityContentProducer(EntityContentProducer ecp)
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#removeWorkerLock()
	 */
	public boolean removeWorkerLock()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchIndexBuilder#isExcludeUserSites()
	 */
	public boolean isExcludeUserSites()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
