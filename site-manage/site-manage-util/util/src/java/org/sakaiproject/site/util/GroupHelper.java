/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
