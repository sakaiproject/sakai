package org.sakaiproject.sitestats.tool.wicket.providers;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.models.SiteModel;


public class StatisticableSitesDataProvider extends SortableSearchableDataProvider {
	private static final long		serialVersionUID	= 1L;
	public final static String		COL_TITLE			= "title";
	public final static String		COL_TYPE			= "type";
	public final static String		COL_STATUS			= "published";
	public final static String		SITE_TYPE_ALL		= "all";
	
	@SpringBean
	private transient SakaiFacade 	facade;
	
	private String					siteType			= SITE_TYPE_ALL;

	public StatisticableSitesDataProvider() {
		InjectorHolder.getInjector().inject(this);
		
        // set default sort
        setSort(COL_TITLE, true);
	}

	public Iterator iterator(int first, int count) {
		// pager
		int start = first + 1;
		int end = start + count - 1;
		PagingPosition pp = new PagingPosition(start, end);

		String type = SITE_TYPE_ALL.equals(getSiteType()) ? null : getSiteType();
		return getFacade().getSiteService().getSites(SelectionType.NON_USER, type, getSearchKeyword(), null, getSSSortType(), pp).iterator();
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

	public IModel model(Object object) {
		return new SiteModel((Site) object);
	}

	public int size() {
		String type = SITE_TYPE_ALL.equals(getSiteType()) ? null : getSiteType();
		return getFacade().getSiteService().countSites(SelectionType.NON_USER, type, getSearchKeyword(), null);
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}

	public String getSiteType() {
		return siteType;
	}
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}

}
