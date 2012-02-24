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
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 * Collects common functionality between MergedListEntry providers.
 */
public abstract class MergedListEntryProviderBase implements MergedList.EntryProvider
{
    /* (non-Javadoc)
     * @see org.sakaiproject.util.MergedList.EntryProvider#isUserChannel(java.lang.Object)
     */
    public boolean isUserChannel(Object channel)
    {
        String context = getContext(channel);
        
        return (context == null ? false : SiteService.isUserSite(context));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.MergedList.EntryProvider#isSpecialSite(java.lang.Object)
     */
    public boolean isSpecialSite(Object channel)
    {
        String context = getContext(channel);
        
        return (context == null ? true : SiteService.isSpecialSite(context));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.MergedList.EntryProvider#getSiteUserId(java.lang.Object)
     */
    public String getSiteUserId(Object channel)
    {
		String context = getContext(channel);
		
        return (context == null ? "" : SiteService.getSiteUserId(context));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.MergedList.EntryProvider#getSite(java.lang.Object)
     */
    public Site getSite(Object channel)
    {
        try
        {
            String context = getContext(channel);
            
            return (context == null ? null : SiteService.getSite(context));
        }
        
        catch (IdUnusedException e)
        {
            return null;
        }
    }
    
	/* (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getIterator()
	 */
	public Iterator getIterator()
	{
		List siteList = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
						null, null, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);

		List objectList = new ArrayList();
		
		Iterator it = siteList.iterator();
		
		while ( it.hasNext() )
		{
		    Site curSite = (Site)it.next();
		    
		    if ( curSite != null )
		    {
				Object object = makeObjectFromSiteId(curSite.getId());

				if ( object != null )
				{
				    objectList.add(object);
				}
		    }
		}
		
		return objectList.iterator(); 
	}
	
	/**
	 * Make a channel/calendar/etc. in an generic way. 
	 */
	public abstract Object makeObjectFromSiteId(String siteId);
 }
