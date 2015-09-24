/**
 * 
 */
package org.sakaiproject.dash.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;

import org.apache.log4j.Logger;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.app.DashboardUserLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.dao.DashboardDao;
import org.sakaiproject.dash.entity.DashboardEntityInfo;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class DashboardUserLogicImpl implements DashboardUserLogic {

	private static Logger logger = Logger.getLogger(DashboardUserLogicImpl.class);
	
	/************************************************************************
	 * Spring-injected classes
	 ************************************************************************/
		
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	protected DashboardDao dao;
	public void setDao(DashboardDao dao) {
		this.dao = dao;
	}
	
	protected DashboardConfig dashboardConfig;
	public void setDashboardConfig(DashboardConfig dashboardConfig) {
		this.dashboardConfig = dashboardConfig;
	}

	protected Cache cache;

	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	/************************************************************************
	 * init() and destroy()
	 ************************************************************************/

	public void init() {
		logger.info("init()");
	
	}
	
	public void destroy() {
		logger.info("destroy()");
	}
		
	/************************************************************************
	 * DashboardUserLogic methods
	 ************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#countNewsLinksByGroupId(java.lang.String, java.lang.String)
	 */
	@Override
	public int countNewsLinksByGroupId(String sakaiUserId,
			String groupId) {
		return dao.countNewsLinksByGroupId(sakaiUserId,groupId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getCurrentNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getCurrentNewsLinks(String sakaiId, String siteId) {
		List<NewsLink> links = dao.getCurrentNewsLinks(sakaiId, siteId);

		if(links != null) {
			for(NewsLink link : links) {
				NewsItem item = link.getNewsItem();
				if(item != null && item.getItemCount() > 1) {
					int itemCount = item.getItemCount();
					SourceType sourceType = item.getSourceType();
					if(sourceType != null) {
						DashboardEntityInfo typeObj = dashboardLogic.getDashboardEntityInfo(sourceType.getIdentifier());
						if(typeObj == null) {
							ResourceLoader rl = new ResourceLoader("dash_entity");
							Object[] args = new Object[]{itemCount, sourceType.getIdentifier(), item.getContext().getContextTitle()};
							rl.getFormattedMessage("dash.grouped.title", args );
						} else {
							item.setTitle(typeObj.getGroupTitle(itemCount, item.getContext().getContextTitle(), item.getNewsTimeLabelKey()));
						}
					}
				}else{
					//When getItemCount() > 1 the infoLinkUrl is null and hence we don't need to run this call.
					if(item!=null){
						setItemInfoLinkUrl(item);
					}
				}
			}
		}

		return links;
	}

	private void setItemInfoLinkUrl(NewsItem item) {
		if(item!=null){
			SourceType sourceType = item.getSourceType();
			if(sourceType != null) {
				DashboardEntityInfo typeObj = dashboardLogic.getDashboardEntityInfo(sourceType.getIdentifier());
				if(typeObj != null) {
					//getValues() take one of the parameter as Locale code and this used for logging warn messages
					//so sending null should be fine here.
					Map<String, Object> values = typeObj.getValues(item.getEntityReference(), null);
					List<Map<String,String>> moreInfo=(ArrayList<Map<String, String>>) values.get(DashboardEntityInfo.VALUE_MORE_INFO);
					if(moreInfo!=null){
						for (Map<String, String> map : moreInfo) {
							item.setInfoLinkURL(map.get(DashboardEntityInfo.VALUE_INFO_LINK_URL));

						}
					}else{
						logger.debug("more.Info is null");
					}
				}else{
					logger.debug("DashboardEntityInfo is null");
				}
			}else{
				logger.debug("SourceType like is null");
			}

		}else{
			logger.debug("NewsItem is null");
		}
		
	}
	private void setItemInfoLinkUrl(CalendarItem item) {
		if(item!=null){
			SourceType sourceType = item.getSourceType();
			if(sourceType != null) {
				DashboardEntityInfo typeObj = dashboardLogic.getDashboardEntityInfo(sourceType.getIdentifier());
				if(typeObj != null) {
					//getValues() take one of the parameter as Locale code and this used for logging warn messages
					//so sending null should be fine here.
					Map<String, Object> values = typeObj.getValues(item.getEntityReference(), null);
					List<Map<String,String>> moreInfo=(ArrayList<Map<String, String>>) values.get(DashboardEntityInfo.VALUE_MORE_INFO);
					if(moreInfo!=null){
						for (Map<String, String> map : moreInfo) {
							item.setInfoLinkURL(map.get(DashboardEntityInfo.VALUE_INFO_LINK_URL));

						}
					}else{
						logger.debug("more.Info is null");
					}
				}else{
					logger.debug("DashboardEntityInfo is null");
				}
			}else{
				logger.debug("SourceType like is null");
			}

		}else{
			logger.debug("NewsItem is null");
		}
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getFutureCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		List<CalendarLink> futureCalendarLinks = dao.getFutureCalendarLinks(sakaiUserId, contextId, hidden);
		for (CalendarLink calendarLink : futureCalendarLinks) {
            		CalendarItem calendarItem = calendarLink.getCalendarItem();		
            		setItemInfoLinkUrl(calendarItem);
		}
		return futureCalendarLinks;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getHiddenNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getHiddenNewsLinks(String sakaiId, String siteId) {
		List<NewsLink> hiddenNewsLinks = dao.getHiddenNewsLinks(sakaiId, siteId);
		for (NewsLink newsLink : hiddenNewsLinks) {
			NewsItem item = newsLink.getNewsItem();
			setItemInfoLinkUrl(item);
		}
		return hiddenNewsLinks;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getNewsLinksByGroupId(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset) {
		return dao.getNewsLinksByGroupId(sakaiUserId, groupId, limit, offset);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getPastCalendarLinks(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden) {
		List<CalendarLink> pastCalendarLinks = dao.getPastCalendarLinks(sakaiUserId, contextId, hidden);
		for (CalendarLink calendarLink : pastCalendarLinks) {
            		CalendarItem calendarItem = calendarLink.getCalendarItem();		
            		setItemInfoLinkUrl(calendarItem);
		}
		return pastCalendarLinks;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getStarredCalendarLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId) {
		List<CalendarLink> starredCalendarLinks = dao.getStarredCalendarLinks(sakaiUserId, contextId);
		for (CalendarLink calendarLink : starredCalendarLinks) {
            		CalendarItem calendarItem = calendarLink.getCalendarItem();		
            		setItemInfoLinkUrl(calendarItem);
		}
		return starredCalendarLinks;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#getStarredNewsLinks(java.lang.String, java.lang.String)
	 */
	@Override
	public List<NewsLink> getStarredNewsLinks(String sakaiId, String siteId) {
		List<NewsLink> starredNewsLinks = dao.getStarredNewsLinks(sakaiId, siteId);
		for (NewsLink newsLink : starredNewsLinks) {
			NewsItem item = newsLink.getNewsItem();
			setItemInfoLinkUrl(item);
		}
		return starredNewsLinks;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#hideCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#hideNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean hideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(true);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#keepCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#keepNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean keepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(true);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unhideCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unhideNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean unhideNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setHidden(false);
		return dao.updateNewsLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unkeepCalendarItem(java.lang.String, long)
	 */
	@Override
	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		CalendarLink link = dao.getCalendarLink(calendarItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateCalendarLink(link);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.app.DashboardUserLogic#unkeepNewsItem(java.lang.String, long)
	 */
	@Override
	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId) {
		Person person = dao.getPersonBySakaiId(sakaiUserId);
		NewsLink link = dao.getNewsLink(newsItemId, person.getId().longValue());
		link.setSticky(false);
		return dao.updateNewsLink(link);
	}

}
