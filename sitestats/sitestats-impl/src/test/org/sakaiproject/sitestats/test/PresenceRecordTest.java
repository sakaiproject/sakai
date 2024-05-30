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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.impl.PresenceRecord;

public class PresenceRecordTest {


    private Instant base;
    private PresenceRecord beginningRecord;
    private PresenceRecord endingRecord;
    private PresenceRecord oneHourRecord;
    private PresenceRecord emptyRecord;


    @Before
    public void setup() {
        base = Instant.now();
        beginningRecord = PresenceRecord.builder().begin(base).end(null).build();
        endingRecord = PresenceRecord.builder().begin(null).end(base).build();
        oneHourRecord = PresenceRecord.builder().begin(base).end(base.plus(1, ChronoUnit.HOURS)).build();
        emptyRecord = PresenceRecord.builder().begin(null).end(null).build();
    }

    @Test
    public void testIsEnding() {
        assertTrue(endingRecord.isEnding());
        assertFalse(oneHourRecord.isEnding());
        assertFalse(beginningRecord.isEnding());
        assertFalse(emptyRecord.isEnding());
    }

    @Test
    public void testIsBeginning() {
        assertTrue(beginningRecord.isBeginning());
        assertFalse(oneHourRecord.isBeginning());
        assertFalse(emptyRecord.isBeginning());
        assertFalse(endingRecord.isBeginning());
    }

    @Test
    public void testIsComplete() {
        assertTrue(oneHourRecord.isComplete());
        assertFalse(emptyRecord.isComplete());
        assertFalse(beginningRecord.isComplete());
        assertFalse(endingRecord.isComplete());
    }

    @Test
    public void testDuration() {
        assertEquals(oneHourRecord.getDuration(), Duration.ofHours(1));
        assertEquals(emptyRecord.getDuration(), Duration.ZERO);
        assertEquals(beginningRecord.getDuration(), Duration.ZERO);
        assertEquals(endingRecord.getDuration(), Duration.ZERO);
    }

    @Test
    public void testOverlapsWith() {
        // t0     t1  t2 t3t4 t5  t6     t7
        //            b-------e             complete test record
        //            b------------------>  beginning test record
        // <------------------e             ending test record
        //        b--------------e          complete record surrounding overlap
        //               b-e                complete record within overlap
        //                 b-----e          complete record begin overlap
        //       b-------e                  complete record end overlap
        // <-------------e                  ending record end overlap
        //                 b------------->  beginning record begin overlap 
        // b-------e                        complete record before
        //                       b-------e  complete record after
        // <-------e                        beginning record before
        //                       b------->  ending record after

        Instant t0 = base;
        Instant t1 = base.plus(30, ChronoUnit.MINUTES);
        Instant t2 = base.plus(50, ChronoUnit.MINUTES);
        Instant t3 = base.plus(60, ChronoUnit.MINUTES);
        Instant t4 = base.plus(70, ChronoUnit.MINUTES);
        Instant t5 = base.plus(80, ChronoUnit.MINUTES);
        Instant t6 = base.plus(100, ChronoUnit.MINUTES);
        Instant t7 = base.plus(130, ChronoUnit.MINUTES);

        PresenceRecord completeTestRecord = PresenceRecord.builder().begin(t2).end(t5).build();
        PresenceRecord beginningTestRecord = PresenceRecord.builder().begin(t2).end(null).build();
        PresenceRecord endingTestRecord = PresenceRecord.builder().begin(null).end(t5).build();

        PresenceRecord completeSurroundingRecord = PresenceRecord.builder().begin(t1).end(t6).build();
        PresenceRecord completeWithinRecord = PresenceRecord.builder().begin(t3).end(t4).build();
        PresenceRecord completeBeginOverlapRecord = PresenceRecord.builder().begin(t4).end(t6).build();
        PresenceRecord completeEndOverlapRecord = PresenceRecord.builder().begin(t1).end(t3).build();
        PresenceRecord endingOverlapRecord = PresenceRecord.builder().begin(null).end(t3).build();
        PresenceRecord beginningOverlapRecord = PresenceRecord.builder().begin(t4).end(null).build();

        PresenceRecord completeBeforeRecord = PresenceRecord.builder().begin(t0).end(t1).build();
        PresenceRecord completeAfterRecord = PresenceRecord.builder().begin(t6).end(t7).build();
        PresenceRecord endingBeforeRecord = PresenceRecord.builder().begin(null).end(t1).build();
        PresenceRecord beginningAfterRecord = PresenceRecord.builder().begin(t6).end(null).build();

        assertTrue(completeTestRecord.overlapsWith(completeSurroundingRecord));
        assertTrue(completeTestRecord.overlapsWith(completeWithinRecord));
        assertTrue(completeTestRecord.overlapsWith(completeBeginOverlapRecord));
        assertTrue(completeTestRecord.overlapsWith(completeEndOverlapRecord));
        assertTrue(completeTestRecord.overlapsWith(endingOverlapRecord));
        assertTrue(completeTestRecord.overlapsWith(beginningOverlapRecord));
        assertFalse(completeTestRecord.overlapsWith(completeBeforeRecord));
        assertFalse(completeTestRecord.overlapsWith(completeAfterRecord));
        assertFalse(completeTestRecord.overlapsWith(endingBeforeRecord));
        assertFalse(completeTestRecord.overlapsWith(beginningAfterRecord));

        assertTrue(beginningTestRecord.overlapsWith(completeSurroundingRecord));
        assertTrue(beginningTestRecord.overlapsWith(completeWithinRecord));
        assertTrue(beginningTestRecord.overlapsWith(completeBeginOverlapRecord));
        assertTrue(beginningTestRecord.overlapsWith(completeEndOverlapRecord));
        assertTrue(beginningTestRecord.overlapsWith(endingOverlapRecord));
        assertTrue(beginningTestRecord.overlapsWith(beginningOverlapRecord));
        assertFalse(beginningTestRecord.overlapsWith(completeBeforeRecord));
        assertTrue(beginningTestRecord.overlapsWith(completeAfterRecord));
        assertFalse(beginningTestRecord.overlapsWith(endingBeforeRecord));
        assertTrue(beginningTestRecord.overlapsWith(beginningAfterRecord));

        assertTrue(endingTestRecord.overlapsWith(completeSurroundingRecord));
        assertTrue(endingTestRecord.overlapsWith(completeWithinRecord));
        assertTrue(endingTestRecord.overlapsWith(completeBeginOverlapRecord));
        assertTrue(endingTestRecord.overlapsWith(completeEndOverlapRecord));
        assertTrue(endingTestRecord.overlapsWith(endingOverlapRecord));
        assertTrue(endingTestRecord.overlapsWith(beginningOverlapRecord));
        assertTrue(endingTestRecord.overlapsWith(completeBeforeRecord));
        assertFalse(endingTestRecord.overlapsWith(completeAfterRecord));
        assertTrue(endingTestRecord.overlapsWith(endingBeforeRecord));
        assertFalse(endingTestRecord.overlapsWith(beginningAfterRecord));
    }
}