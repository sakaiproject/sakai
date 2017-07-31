/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ResourceLoaderHelperBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * RecentlyVisitedBean is a bean which stores in session a list of all the pages
 * you have recently visited.
 * 
 * @author andrew
 */
public class RecentlyVisitedBean
{

	private ViewBean viewBean;

	private List recentlyVisited = new ArrayList();

	private List uniqueRecentlyVisited = new LinkedList();

	private String defaultSpace;

	private Visit home;

	/**
	 * Create a new RecentlyVisitedBean in the given default space
	 * 
	 * @param defaultSpace
	 *        that links will be rendered against.
	 */
	public RecentlyVisitedBean(String defaultSpace)
	{
		this.defaultSpace = defaultSpace;
		this.viewBean = new ViewBean(null, defaultSpace);
		PageVisit temp = new PageVisit();
		temp.setPage(viewBean.getPageName());
		temp.setRealm(viewBean.getLocalSpace());
		this.home = temp;
	}

	private interface Visit
	{
		 String getLink();

		 String getPublicLink();
	}

	private class PageVisit implements Visit
	{
		private String page;

		private String realm;

		public String getPage()
		{
			return page;
		}

		public void setPage(String page)
		{
			this.page = page;
		}

		public String getRealm()
		{
			return realm;
		}

		public void setRealm(String realm)
		{
			this.realm = realm;
		}

		public String getLink()
		{
			viewBean.setLocalSpace(realm);
			viewBean.setPageName(page);
			return "<a href=\""
					+ XmlEscaper.xmlEscape(viewBean.getViewUrl())
					+ "\">"
					+ XmlEscaper.xmlEscape(NameHelper.localizeName(viewBean
							.getPageName(), defaultSpace)) + "</a>";
		}

		public String getPublicLink()
		{
			viewBean.setLocalSpace(realm);
			viewBean.setPageName(page);
			return "<a href=\""
					+ XmlEscaper.xmlEscape(viewBean.getPublicViewUrl(true))
					+ "\">"
					+ XmlEscaper.xmlEscape(NameHelper.localizeName(viewBean
							.getPageName(), defaultSpace)) + "</a>";
		}

		public boolean equals(Object other)
		{
			if (other instanceof PageVisit)
			{
				PageVisit castOther = (PageVisit) other;
				String thisGlobalName = NameHelper.globaliseName(page, realm);
				String otherGlobalName = NameHelper.globaliseName(
						castOther.page, castOther.realm);
				if (thisGlobalName.equals(otherGlobalName))
				{
					return true;
				}
			}

			return false;
		}

		public int hashCode()
		{
			return NameHelper.globaliseName(page, realm).hashCode();
		}
	}

	private class SearchVisit implements Visit
	{



		private String search;

		private String realm;

		public String getRealm()
		{
			return realm;
		}

		public void setRealm(String realm)
		{
			this.realm = realm;
		}

		public String getSearch()
		{
			return search;
		}

		public void setSearch(String search)
		{
			this.search = search;
		}

		public String getLink()
		{
			viewBean.setSearch(search);

			ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoaderBean();
			return "<a href=\"" + XmlEscaper.xmlEscape(viewBean.getSearchUrl())
					+ "\">"+rlb.getString("recentlyvisited.search","Search")+": " + XmlEscaper.xmlEscape(search) + "</a>";
		}

		public String getPublicLink()
		{
			return "";
		}

		private RecentlyVisitedBean getOuterType() {
			return RecentlyVisitedBean.this;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((realm == null) ? 0 : realm.hashCode());
			result = prime * result
					+ ((search == null) ? 0 : search.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SearchVisit other = (SearchVisit) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (realm == null) {
				if (other.realm != null)
					return false;
			} else if (!realm.equals(other.realm))
				return false;
			if (search == null) {
				if (other.search != null)
					return false;
			} else if (!search.equals(other.search))
				return false;
			return true;
		}


	}

	/**
	 * Set the most recently visited page as a View Page
	 * 
	 * @param vb
	 *        the current ViewBean
	 */
	public void setViewPage(ViewBean vb)
	{
		PageVisit pv = new PageVisit();
		pv.setPage(vb.getPageName());
		pv.setRealm(vb.getLocalSpace());

		// Proper RecentVisit List
		if (!pv.equals(this.getLastVisit()))
		{
			recentlyVisited.add(recentlyVisited.size(), pv);
		}
		// Unique BreadCrumb List
		uniqueRecentlyVisited.remove(pv);
		uniqueRecentlyVisited.add(pv);

	}

	/**
	 * Set the most recently visited page as a Search Page
	 * 
	 * @param sb
	 *        the current SearchBean
	 */
	public void setSearchPage(SearchBean sb)
	{
		SearchVisit sv = new SearchVisit();
		sv.setRealm(sb.getRealm());
		sv.setSearch(sb.getSearch());

		// Proper RecentVisit List
		if (!sv.equals(this.getLastVisit()))
		{
			recentlyVisited.add(recentlyVisited.size(), sv);
		}

		// Unique BreadCrumb List
		uniqueRecentlyVisited.remove(sv);
		uniqueRecentlyVisited.add(sv);

	}

	/**
	 * Set the recentlyVisited as a new list of visits
	 * 
	 * @param recentlyVisited
	 */
	public void setRecentlyVisited(List recentlyVisited)
	{
		this.recentlyVisited = recentlyVisited;
	}

	/**
	 * Get the recently visited pages as list of links
	 * 
	 * @return a list of recently visited pages as xhtml links (most recent
	 *         last)
	 */
	public List getRecentlyVisitedLinks()
	{
		List links = new ArrayList(recentlyVisited.size());
		for (Iterator it = recentlyVisited.iterator(); it.hasNext();)
		{
			links.add(((Visit) it.next()).getLink());
		}
		return links;
	}

	/**
	 * Get the recently visited pages as a list of links uniquely
	 * 
	 * @return a list of recently visited pages as xhtml links (most recent
	 *         last)
	 */
	public List getBreadcrumbLinks()
	{
		List links = new ArrayList(uniqueRecentlyVisited.size() + 1);

		for (Iterator it = uniqueRecentlyVisited.iterator(); it.hasNext();)
		{
			links.add(((Visit) it.next()).getLink());
		}
		return links;
	}

	/**
	 * Get the recently visited pages as a list of links uniquely
	 * 
	 * @return a list of recently visited pages as xhtml links (most recent
	 *         last)
	 */
	public List getPublicBreadcrumbLinks()
	{
		List links = new ArrayList(uniqueRecentlyVisited.size() + 1);

		for (Iterator it = uniqueRecentlyVisited.iterator(); it.hasNext();)
		{
			Visit v = (Visit) it.next();
			String publicLink = v.getPublicLink();
			if (publicLink.length() > 0)
			{
				links.add(publicLink);
			}
		}
		return new ArrayList(links);
	}

	public String getHomeLink()
	{
		return home.getLink();
	}

	/**
	 * Get the number of recently visited links
	 * 
	 * @return number of recently visited links
	 */
	public int getNumberOfRecentlyVisitedLinks()
	{
		return recentlyVisited.size();
	}

	private Visit getLastVisit()
	{
		if (recentlyVisited.size() > 0)
		{
			return (Visit) recentlyVisited.get(recentlyVisited.size() - 1);
		}
		else
		{
			return null;
		}
	}
}
