/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.cover.CalendarService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;

/**
 * Provides a list of merged calendars by iterating through all available
 * calendars. Optionally, can exclude user hidden sites.
 */
public class EntryProvider extends MergedListEntryProviderBase
{
	/** Excluded sites from My Workspace preferences. */
	private final String TAB_EXCLUDED_SITES = "exclude";

	/** Calendar channels from hidden sites */
	private final List<String> excludedSites = new ArrayList<String>();

	/** Default constructor. */ 
	public EntryProvider()
	{
		this(false);
	}

	/** Exclude hidden sites from user preferences. */
	public EntryProvider(boolean excludeHiddenSites)
	{
		if (excludeHiddenSites)
		{
			List<String> excludedSiteIds = getExcludedSitesFromTabs();
			if (excludedSiteIds != null)
			{
				for (String siteId : excludedSiteIds)
				{
					excludedSites.add(CalendarService.calendarReference(siteId, SiteService.MAIN_CONTAINER));
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java.lang.String)
	 */
	public Object makeObjectFromSiteId(String id)
	{
		String calendarReference = CalendarService.calendarReference(id, SiteService.MAIN_CONTAINER);
		Object calendar = null;

		if (calendarReference != null)
		{
			try
			{
				calendar = CalendarService.getCalendar(calendarReference);
			}
			catch (IdUnusedException e)
			{
				// The channel isn't there.
			}
			catch (PermissionException e)
			{
				// We can't see the channel
			}
		}

		return calendar;
	}

	/*
	 * (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#allowGet(java.lang.Object)
	 */
	public boolean allowGet(String ref)
	{
		return CalendarService.allowGetCalendar(ref) &&
			(excludedSites ==  null || !excludedSites.contains(ref));
	}

	/*
	 * (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getContext(java.lang.Object)
	 */
	public String getContext(Object obj)
	{
		if (obj == null)
		{
			return "";
		}

		Calendar calendar = (Calendar) obj;
		return calendar.getContext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getReference(java.lang.Object)
	 */
	public String getReference(Object obj)
	{
		if (obj == null)
		{
			return "";
		}

		Calendar calendar = (Calendar) obj;
		return calendar.getReference();
	}

	/*
	 * (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getProperties(java.lang.Object)
	 */
	public ResourceProperties getProperties(Object obj)
	{
		if (obj == null)
		{
			return null;
		}

		Calendar calendar = (Calendar) obj;
		return calendar.getProperties();
	}
	
	/**
	 * Pulls excluded site ids from Tabs preferences
	 */
	private List<String> getExcludedSitesFromTabs()
	{
		List<String> list = null;
		try
		{
			String userId = SessionManager.getCurrentSessionUserId();
			Preferences prefs = PreferencesService.getPreferences(userId);
			ResourceProperties props = prefs.getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
			list = props.getPropertyList(TAB_EXCLUDED_SITES);
		}
		catch (Exception e)
		{
			list = new ArrayList<String>();
		}
		return list;
	}
}
