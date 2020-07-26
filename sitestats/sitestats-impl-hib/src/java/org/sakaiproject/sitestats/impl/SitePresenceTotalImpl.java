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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Like the SitePresence but it includes a total number of visits and when they last visited for a site/user.
 * @see SitePresence
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class SitePresenceTotalImpl implements SitePresenceTotal, Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private long id;
    private String siteId;
    private String userId;
    private int totalVisits;
    private Date lastVisitTime;

    public SitePresenceTotalImpl(SitePresence sp) {
        siteId = sp.getSiteId();
        userId = sp.getUserId();
        totalVisits = 1;
        lastVisitTime = sp.getLastVisitStartTime();
    }

    public void incrementTotalVisits() {
        totalVisits += 1;
    }
}
