/*
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.sitestats;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.impl.SitePresenceImpl;

public class SitePresenceCompareTest {

    private final SitePresence presenceA;
    private final SitePresence presenceB;
    private final SitePresence presenceC;
    private final SitePresence presenceD;
    private final SitePresence presenceE;

    public SitePresenceCompareTest() {
        Instant now = Instant.now();
        Instant fiveFromNow = now.plus(5, ChronoUnit.MINUTES);
        presenceA = createPresence(1, "site-a", "user-a", Date.from(now), Date.from(now), 0);
        presenceB = createPresence(2, "site-a", "user-a", Date.from(fiveFromNow), Date.from(fiveFromNow), 0);
        presenceC = createPresence(3, "site-a", "user-a", null, null, 0);
        presenceD = createPresence(3, "site-a", "user-a", Date.from(fiveFromNow), null, 0);
        presenceE = createPresence(3, "site-a", "user-a", null, Date.from(fiveFromNow), 0);
    }

    @Test
    public void safeCompare() {
        Assert.assertThat(presenceA, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().comparesEqualTo(presenceA));
        Assert.assertThat(presenceB, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().comparesEqualTo(presenceB));
        Assert.assertThat(presenceA, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().lessThan(presenceB));
        Assert.assertThat(presenceB, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().greaterThan(presenceA));
    }

    @Test
    public void nullSafeCompare() {
        Assert.assertThat(presenceC, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().comparesEqualTo(presenceC));
        Assert.assertThat(presenceB, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().lessThan(presenceC));
        Assert.assertThat(presenceC, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().greaterThan(presenceB));

        Assert.assertThat(presenceD, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().comparesEqualTo(presenceD));
        Assert.assertThat(presenceB, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().lessThan(presenceD));
        Assert.assertThat(presenceD, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().greaterThan(presenceB));

        Assert.assertThat(presenceE, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().comparesEqualTo(presenceE));
        Assert.assertThat(presenceB, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().lessThan(presenceE));
        Assert.assertThat(presenceE, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().greaterThan(presenceB));

        Assert.assertThat(presenceD, ComparatorMatcherBuilder.<SitePresence>usingNaturalOrdering().lessThan(presenceE));
    }

    private SitePresence createPresence(long id, String siteId, String userId, Date date, Date lastVisit, long duration) {
        SitePresenceImpl presence = new SitePresenceImpl();
        presence.setId(id);
        presence.setSiteId(siteId);
        presence.setUserId(userId);
        presence.setDate(date);
        presence.setLastVisitStartTime(lastVisit);
        presence.setDuration(duration);
        return presence;
    }
}
