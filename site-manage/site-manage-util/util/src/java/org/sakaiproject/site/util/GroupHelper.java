package org.sakaiproject.site.util;

import java.util.Collection;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;

/**
 * Helper methods for group management
 * @author plukasew
 */
public class GroupHelper
{
    /**
     * Finds a group in a particular site, by group title
     * @param site the site
     * @param groupTitle the group title
     * @return the id of the group with the matching title, or empty string if not found
     */
    public static String getSiteGroupByTitle(Site site, String groupTitle)
    {
        String groupId = "";
        Collection<Group> siteGroups = site.getGroups();
        if (groupTitle != null && siteGroups != null)
        {
            for (Group g : siteGroups)
            {
                if (g != null && groupTitle.equals(g.getTitle()))
                {
                    groupId = g.getId();
                    break;
                }
            }
        }

        return groupId;
    }
}
