/**
 * 
 */
package org.sakaiproject.dash.app;

import java.util.List;

import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.model.NewsLink;

/**
 * The DashboardUserLogic interface defines methods to access dashboard items in the 
 * tool layer.  These include accessors to retrieve records for a particular user and 
 * other methods intended to set/unset properties such as "starred" or "hidden" on 
 * particular items for a specified user.
 */
public interface DashboardUserLogic {

	/**
	 * Returns the number on NewsLink objects representing items in a "group" that the specified person 
	 * has permission to access and has not "hidden". A group is a set of items of the same source type 
	 * in the same context with the same label-key (indicating the last action on the entity) and last 
	 * modified on the same calendar date.
	 * @param sakaiUserId
	 * @param groupId
	 * @return
	 */
	public int countNewsLinksByGroupId(String sakaiUserId, String groupId);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has not hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId, String contextId);
	
	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has not hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param includeInfoLinkUrl
	 * @return
	 */
	public List<NewsLink> getCurrentNewsLinks(String sakaiUserId, String contextId,boolean includeInfoLinkUrl);
	
	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is in the current date or later (i.e. it will return items representing
	 * events that occurred earlier in the current day) and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden 
	 * @return
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden);
	
	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is in the current date or later (i.e. it will return items representing
	 * events that occurred earlier in the current day) and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null. variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden 
	 * @param includeInfoLinkUrl
	 * @return
	 */
	public List<CalendarLink> getFutureCalendarLinks(String sakaiUserId, String contextId, boolean hidden, boolean includeInfoLinkUrl);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getHiddenNewsLinks(String sakaiUserId, String siteId);
	
	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has hidden. 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param includeInfoLinkUrl
	 * @return
	 */
	
	public List<NewsLink> getHiddenNewsLinks(String userUuid, String siteId, boolean includeInfoLinkUrl);

	/**
	 * Returns a paged list of NewsLink objects representing items in a "group" that the specified person 
	 * has permission to access and has not "hidden". A group is a set of items of the same source type 
	 * in the same context with the same label-key (indicating the last action on the entity) and last 
	 * modified on the same calendar date. The list is selected in descending order by the time of the 
	 * last action.
	 * @param sakaiUserId
	 * @param groupId
	 * @param limit The maximum number of items to be returned.
	 * @param offset The zero-based index of the first item within the entire set.
	 * @return
	 */
	public List<NewsLink> getNewsLinksByGroupId(String sakaiUserId,
			String groupId, int limit, int offset);

	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is before the current instant and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden
	 * @return
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden);
	
	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * whose time attribute is before the current instant and whose "hidden" state matches the 
	 * specified value. Results will be limited to a particular site if the contextId parameter 
	 * is not null. variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param hidden
	 * @param includeInfoLinkUrl
	 * @return
	 */
	public List<CalendarLink> getPastCalendarLinks(String sakaiUserId, String contextId, boolean hidden, boolean includeInfoLinkUrl);

	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * that the specified user has "starred". Results will be limited to a particular site if 
	 * the contextId parameter is not null.
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId);
	/**
	 * Returns a list of CalendarLink objects linking a particular person to calendar items 
	 * that the specified user has "starred". Results will be limited to a particular site if 
	 * the contextId parameter is not null. variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param includeInfoLinkUrl
	 * @return
	 */
	public List<CalendarLink> getStarredCalendarLinks(String sakaiUserId, String contextId,boolean includeInfoLinkUrl);

	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has "starred". 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * @param sakaiUserId
	 * @param contextId
	 * @return
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String siteId);
	
	/**
	 * Returns a list of NewsLink objects which the specified person has permission to access and has "starred". 
	 * If the contextId is not null, the results will be limited to the site indicated by that value. 
	 * variable 'includeInfoLinkUrl' is used for method overloading purpose
	 * @param sakaiUserId
	 * @param contextId
	 * @param includeInfoLinkUrl
	 * @return
	 */
	public List<NewsLink> getStarredNewsLinks(String sakaiUserId, String siteId,boolean includeInfoLinkUrl);

	/**
	 * Hide a calendar item from views of calendar items for a particular user.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean hideCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Hide a news item from views of news items by a particular user.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean hideNewsItem(String sakaiUserId, long newsItemId);

	/**
	 * Mark a calendar item to be highlighted and kept in the calendar display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean keepCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Mark a news item to be highlighted and kept in the news display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean keepNewsItem(String sakaiUserId, long newsItemId);

	/**
	 * Restore a calendar item to views of calendar items by a particular user.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean unhideCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Restore a news item to views of news items by a particular user.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean unhideNewsItem(String sakaiUserId, long newsItemId);

	/**
	 * Remove the marking for a calendar item to be highlighted and kept in the calendar display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param calendarItemId
	 * @return
	 */
	public boolean unkeepCalendarItem(String sakaiUserId, long calendarItemId);

	/**
	 * Remove the marking for a news item to be highlighted and kept in the news display for a particular user even after it expires.
	 * @param sakaiUserId
	 * @param newsItemId
	 * @return
	 */
	public boolean unkeepNewsItem(String sakaiUserId, long newsItemId);

}
