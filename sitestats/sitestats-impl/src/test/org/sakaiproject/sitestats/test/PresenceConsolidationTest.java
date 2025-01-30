/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.sitestats.api.presence.Presence;
import org.sakaiproject.sitestats.impl.PresenceConsolidation;
import org.sakaiproject.sitestats.impl.PresenceRecord;

import lombok.NonNull;

public class PresenceConsolidationTest {


    @Test
    public void testSerialPresences() {
        //     0   15   30        60        90       120
        // p1: b----e
        // p2:           b---------e
        // p3:                               b---------e
        Instant base = Instant.now();
        Presence presence1 = PresenceRecord.builder()
                .begin(base)
                .end(base.plus(15, ChronoUnit.MINUTES))
                .build();
        Presence presence2 = PresenceRecord.builder()
                .begin(base.plus(30, ChronoUnit.MINUTES))
                .end(base.plus(60, ChronoUnit.MINUTES))
                .build();
        Presence presence3 = PresenceRecord.builder()
                .begin(base.plus(90, ChronoUnit.MINUTES))
                .end(base.plus(120, ChronoUnit.MINUTES))
                .build();

        PresenceConsolidation presenceConsolidation = new PresenceConsolidation();

        presenceConsolidation.add(presence1);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(15));

        presenceConsolidation.add(presence2);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(45));

        presenceConsolidation.add(presence3);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(75));
    }

    @Test
    public void testOverlappingPresences() {
        //     0   15   30        60        90       120
        // p1: b-----------------------------e
        // p2:      b----------------------------------e
        // p3:           b---------e
        Instant base = Instant.now();
        Presence presence1 = PresenceRecord.builder()
                .begin(base)
                .end(base.plus(90, ChronoUnit.MINUTES))
                .build();
        Presence presence2 = PresenceRecord.builder()
                .begin(base.plus(15, ChronoUnit.MINUTES))
                .end(base.plus(120, ChronoUnit.MINUTES))
                .build();
        Presence presence3 = PresenceRecord.builder()
                .begin(base.plus(30, ChronoUnit.MINUTES))
                .end(base.plus(60, ChronoUnit.MINUTES))
                .build();

        PresenceConsolidation presenceConsolidation = new PresenceConsolidation();

        presenceConsolidation.add(presence1);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(90));

        presenceConsolidation.add(presence2);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(120));

        presenceConsolidation.add(presence3);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofMinutes(120));

        // Same, but in different order
        PresenceConsolidation presenceConsolidation2 = new PresenceConsolidation();

        presenceConsolidation2.add(presence3);
        assertEquals(presenceConsolidation2.getDuration(), Duration.ofMinutes(30));

        presenceConsolidation2.add(presence2);
        assertEquals(presenceConsolidation2.getDuration(), Duration.ofMinutes(105));

        presenceConsolidation2.add(presence1);
        assertEquals(presenceConsolidation2.getDuration(), Duration.ofMinutes(120));
    }

    @Test
    public void testCrossDayPresences() {
        // By day:  |d1  6   12         |d2  6    12   18   |d3   6
        // By hour:      6   12             30    36   42        54
        // p1:           b-------------------------e
        // p2:                b-----------------------------------e
        // p3:                               b----------e
        Instant base = PresenceConsolidation.toDay(Instant.now());
        Instant d1 = base;
        Instant d2 = base.plus(1, ChronoUnit.DAYS);
        Instant d3 = base.plus(2, ChronoUnit.DAYS);
        Presence presence1 = PresenceRecord.builder()
                .begin(base.plus(6, ChronoUnit.HOURS))
                .end(base.plus(36, ChronoUnit.HOURS))
                .build();
        Presence presence2 = PresenceRecord.builder()
                .begin(base.plus(12, ChronoUnit.HOURS))
                .end(base.plus(54, ChronoUnit.HOURS))
                .build();
        Presence presence3 = PresenceRecord.builder()
                .begin(base.plus(30, ChronoUnit.HOURS))
                .end(base.plus(42, ChronoUnit.HOURS))
                .build();

        // Assert (sub)total durations
        PresenceConsolidation presenceConsolidation = new PresenceConsolidation();

        presenceConsolidation.add(presence1);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofHours(30));

        presenceConsolidation.add(presence2);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofHours(48));

        presenceConsolidation.add(presence3);
        assertEquals(presenceConsolidation.getDuration(), Duration.ofHours(48));

        Map<Instant, PresenceConsolidation> consolidationByDay = presenceConsolidation.mapByDay();
        // Assert that wer have 3 days
        assertEquals(consolidationByDay.size(), 3);

        // Assert durations by day
        assertEquals(consolidationByDay.get(d1).getDuration(), Duration.ofHours(18));
        assertEquals(consolidationByDay.get(d2).getDuration(), Duration.ofHours(24));
        assertEquals(consolidationByDay.get(d3).getDuration(), Duration.ofHours(6));
    }
}
