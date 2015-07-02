/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.providers;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.SiteModel;


public class StatisticableSitesDataProvider extends SortableSearchableDataProvider {
	private static final long		serialVersionUID	= 1L;
	public final static String		COL_TITLE			= "title";
	public final static String		COL_TYPE			= "type";
	public final static String		COL_STATUS			= "published";
	public final static String		SITE_TYPE_ALL		= "all";
	
	private String					siteType			= SITE_TYPE_ALL;

	public StatisticableSitesDataProvider() {
		Injector.get().inject(this);
		
        // set default sort
        setSort(COL_TITLE, SortOrder.ASCENDING);
	}

	@Override
	public Iterator iterator(long first, long count) {
		// pager
		int start = (int) first + 1;
		int end = start + (int) count - 1;
		PagingPosition pp = new PagingPosition(start, end);

		String type = SITE_TYPE_ALL.equals(getSiteType()) ? null : getSiteType();
		return Locator.getFacade().getSiteService().getSites(SelectionType.NON_USER, type, getSearchKeyword(), null, getSSSortType(), pp).iterator();
	}
	
	private SortType getSSSortType() {
		SortParam sp = getSort();
		
		if(sp.getProperty().equals(COL_TITLE)){
			if(sp.isAscending()) {
				return SortType.TITLE_ASC;
			}else{
				return SortType.TITLE_DESC;
			}
		}else if(sp.getProperty().equals(COL_TYPE)){
			if(sp.isAscending()){
				return SortType.TYPE_ASC;
			}else{
				return SortType.TYPE_DESC;
			}
		}else if(sp.getProperty().equals(COL_STATUS)){
			if(sp.isAscending()){
				return SortType.PUBLISHED_ASC;
			}else{
				return SortType.PUBLISHED_DESC;
			}
		}else{
			return SortType.TITLE_ASC;
		}
	}

	@Override
	public IModel model(Object object) {
		return new SiteModel((Site) object);
	}

	@Override
	public long size() {
		String type = SITE_TYPE_ALL.equals(getSiteType()) ? null : getSiteType();
		return Locator.getFacade().getSiteService().countSites(SelectionType.NON_USER, type, getSearchKeyword(), null);
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}

	public String getSiteType() {
		return siteType;
	}

}
