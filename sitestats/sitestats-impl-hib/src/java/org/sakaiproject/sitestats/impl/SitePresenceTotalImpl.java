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
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SitePresenceTotal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Like the SitePresence but it includes a total number of visits and when they last visited for a site/user.
 * @see SitePresence
 */
public class SitePresenceTotalImpl implements SitePresenceTotal, Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String siteId;
    private String userId;
    private int totalVisits;
    private Date lastVisitTime;

    public SitePresenceTotalImpl() {}

    public SitePresenceTotalImpl(SitePresence sp) {

        siteId = sp.getSiteId();
        userId = sp.getUserId();
        totalVisits = 1;
        setLastVisitTime(sp.getDate());
    }

    public void updateFrom(SitePresence sp) {
        incrementTotalVisits();
        setLastVisitTime(sp.getDate());
    }

    public void incrementTotalVisits() {
        totalVisits += 1;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof SitePresenceTotalImpl)) {
            return false;
        }

        SitePresenceTotalImpl other = (SitePresenceTotalImpl) o;

        return id == other.getId()
                && siteId.equals(other.getSiteId())
                && userId.equals(other.getUserId())
                && totalVisits == other.getTotalVisits()
                && lastVisitTime.equals(other.getLastVisitTime());
    }

    @Override
    public int hashCode() {

        if (siteId == null) {
            return Integer.MIN_VALUE;
        }

        String hashStr = this.getClass().getName() + ":"
                + id
                + siteId.hashCode()
                + userId.hashCode()
                + totalVisits
                + lastVisitTime.hashCode();
        return hashStr.hashCode();
    }

    public String toString(){
        return siteId + " : " + userId + " : " + totalVisits + " : " + lastVisitTime;
    }
}
