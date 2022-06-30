 /**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/announcement/trunk/announcement-tool/tool/src/java/org/sakaiproject/announcement/entityprovider/AnnouncementEntityProviderImpl.java $
 * $Id: AnnouncementEntityProviderImpl.java 87813 2011-01-28 13:42:17Z savithap@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.announcement.api;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.time.api.Time;

/**
 * Allows some basic functions on announcements.
 * Due to limitations of EntityBroker the internal URLs of the announcements service can't be exposed
 * directly, so we have to map them, with assumptions about characters used in IDs. Basically we pack together
 * the {siteId}:{channelId}:{announcementId} into the ID.
 *
 */
@Slf4j
public class ViewableFilter implements Filter {

    @Setter
    private Filter filter = null;
    private Time t;
    private int numberOfAnnouncements;
    private AnnouncementService announcementService;

    private int accepted = 0;

    /**
     * Show viewable announcements and limit the result
     * @param filter The other filter we check with.
     * @param t Min Time to be showed
     * @param numberOfAnnouncements Limited to latest numberOfAnnouncements
     */
    public ViewableFilter(Filter filter, Time t, int numberOfAnnouncements, AnnouncementService announcementService) {

        this.filter = filter;
        this.numberOfAnnouncements = numberOfAnnouncements;
        this.t = t;
        this.announcementService = announcementService;
    }

    /**
     * Does this object satisfy the criteria of the filter?
     * @param o The object
     * @return true if the object is accepted by the filter, false if not.
     */
    public boolean accept(Object o) {

        if (accepted >= numberOfAnnouncements){
            return false;
        }

        if (o instanceof AnnouncementMessage) {
            AnnouncementMessage msg = (AnnouncementMessage) o;

            if (t != null) {
                ResourceProperties msgProperties = msg.getProperties();
                String releaseDate = msgProperties.getProperty(AnnouncementService.RELEASE_DATE);
                if (releaseDate != null) {
                    long release = Long.parseLong(releaseDate);
                    long limitDate = Long.parseLong(t.toString());
                    if (release < limitDate) {
                        return false;
                    }
                }
            }

            if (msg.getHeader().getDraft() || !announcementService.isMessageViewable(msg)) {
                return false;
            }
        }

        if (filter != null) return filter.accept(o);

        accepted++;
        return true;
    }
}
