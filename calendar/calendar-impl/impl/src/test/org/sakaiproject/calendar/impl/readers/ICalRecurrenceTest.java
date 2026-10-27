/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.calendar.impl.readers;

import java.time.Instant;

import net.fortuna.ical4j.model.property.RRule;
import org.junit.Test;
import org.sakaiproject.calendar.impl.MWFRecurrenceRule;
import org.sakaiproject.calendar.impl.WeeklyRecurrenceRule;

import static org.junit.Assert.*;

public class ICalRecurrenceTest {
    @Test
    public void preservesWeekdayMapping() throws Exception {
        ICalRecurrence imported = new ICalRecurrence("FREQ=WEEKLY;BYDAY=MO,WE,FR;COUNT=3");
        assertEquals(MWFRecurrenceRule.FREQ, imported.getFrequency());
        assertEquals(Integer.valueOf(3), imported.getREPEAT());
        RRule<Instant> exported = ICalRecurrence.toRRule(new MWFRecurrenceRule(1, 3));
        assertEquals("FREQ=WEEKLY;COUNT=3;BYDAY=MO,WE,FR", exported.getValue());
    }

    @Test
    public void preservesUntilInstantOnImport() throws Exception {
        ICalRecurrence rule = new ICalRecurrence("FREQ=WEEKLY;UNTIL=20261225T120000Z");
        assertEquals(Instant.parse("2026-12-25T12:00:00Z"), rule.getEND_TIME().toInstant());
    }

    @Test
    public void preservesCountAndIntervalOnExport() {
        RRule<Instant> rule = ICalRecurrence.toRRule(new WeeklyRecurrenceRule(2, 4));
        assertEquals(4, rule.getRecur().getCount());
        assertEquals(2, rule.getRecur().getInterval());
    }
}
