/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.search.elasticsearch.filter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.elasticsearch.Messages;
import org.sakaiproject.search.elasticsearch.filter.SearchItemFilter;

/**
 * @author ieb
 */
@Slf4j
public class SearchSecurityFilter implements SearchItemFilter
{

	private SearchIndexBuilder searchIndexBuilder = null;

	private EntityManager entityManager = null;

	private SearchItemFilter nextFilter = null;

	public void init()
	{
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	/**
	 * @return Returns the nextFilter.
	 */
	public SearchItemFilter getNextFilter()
	{
		return nextFilter;
	}

	/**
	 * @param nextFilter
	 *        The nextFilter to set.
	 */
	public void setNextFilter(SearchItemFilter nextFilter)
	{
		this.nextFilter = nextFilter;
	}

	/**
	 * @return Returns the searchIndexBuilder.
	 */
	public SearchIndexBuilder getSearchIndexBuilder()
	{
		return searchIndexBuilder;
	}

	/**
	 * @param searchIndexBuilder
	 *        The searchIndexBuilder to set.
	 */
	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
	{
		this.searchIndexBuilder = searchIndexBuilder;
	}

	public SearchResult filter(SearchResult result)
	{
		String reference = result.getReference();
		EntityContentProducer ecp = searchIndexBuilder
				.newEntityContentProducer(reference);

		if (ecp == null || !ecp.canRead(reference))
		{
			result = new CensoredSearchResult();
		}
		if (nextFilter == null)
		{
			return result;
		}
		return nextFilter.filter(result);
	}

	public class CensoredSearchResult implements SearchResult
	{

		public float getScore()
		{
			return 0;
		}

		public String getId()
		{
			return ""; //$NON-NLS-1$
		}

		public String[] getFieldNames()
		{
			return new String[0];
		}

		public String[] getValues(String string)
		{
			return new String[0];
		}

		public Map<String, String[]> getValueMap()
		{
			return new HashMap<String, String[]>();
		}

		public String getUrl()
		{
			return ""; //$NON-NLS-1$
		}

		public String getTitle()
		{
			return Messages.getString("SearchSecurityFilter.5"); //$NON-NLS-1$
		}

		public int getIndex()
		{
			return 0;
		}

		public String getSearchResult()
		{
			return ""; //$NON-NLS-1$
		}

		public String getReference()
		{
			return ""; //$NON-NLS-1$
		}

		public TermFrequency getTerms() throws IOException
		{
			return new TermFrequency()
			{
				int[] freq = new int[0];

				String[] terms = new String[0];

				public int[] getFrequencies()
				{
					return freq;
				}

				public String[] getTerms()
				{
					return terms;
				}

			};
		}

		public String getTool()
		{
			return ""; //$NON-NLS-1$
		}

		public void toXMLString(StringBuilder sb)
		{
			sb.append("<result"); //$NON-NLS-1$
			sb.append(" index=\"0\" "); //$NON-NLS-1$
			sb.append(" score=\"0\" "); //$NON-NLS-1$
			sb.append(" sid=\"\" "); //$NON-NLS-1$
			sb.append(" reference=\"\" "); //$NON-NLS-1$
			sb.append(" title=\"\" "); //$NON-NLS-1$
			sb.append(" tool=\"\" "); //$NON-NLS-1$
			sb.append(" url=\"\" />"); //$NON-NLS-1$
		}

		public String getSiteId() {
			return ""; //$NON-NLS-1$
		}

		public boolean isCensored() {
			return true;
		}

		public void setUrl(String newUrl) {
			// TODO Auto-generated method stub
			
		}

		public boolean hasPortalUrl() {
			log.debug("hasPortalUrl(" + getReference());
			return false;
		}

	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

}
