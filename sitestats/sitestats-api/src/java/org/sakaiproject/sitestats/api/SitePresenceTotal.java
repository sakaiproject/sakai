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
package org.sakaiproject.sitestats.api;

import java.util.Date;

/**
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public interface SitePresenceTotal {

    public void incrementTotalVisits();

    /**
     * This increments the visits and updates the last visit time from the passed presence.
     * @param sp The SitePresence to update from.
     */
    public void updateFrom(SitePresence sp);

    public long getId();
    public void setId(long id);
    public String getUserId();
    public void setUserId(String userId);
    public String getSiteId();
    public void setSiteId(String siteId);
    public int getTotalVisits();
    public void setTotalVisits(int totalVisits);
    public Date getLastVisitTime();
    public void setLastVisitTime(Date lastVisitTime);
}
