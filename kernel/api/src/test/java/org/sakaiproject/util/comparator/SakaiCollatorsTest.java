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
package org.sakaiproject.util.comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.Locale;

import org.junit.Test;

public class SakaiCollatorsTest {

    @Test
    public void underscoreSortsImmediatelyAfterSpace() {
        Collator collator = SakaiCollators.getCollatorWithUnderscoreAfterSpace(Locale.US, Collator.TERTIARY);

        assertTrue(collator.compare(" a", "_a") < 0);
        assertTrue(collator.compare("_a", "a") < 0);
    }

    @Test
    public void sortNameCollatorUsesSecondaryStrength() {
        Collator collator = SakaiCollators.getSortNameCollator(Locale.US);

        assertEquals(0, collator.compare("Smith, John", "smith, john"));
    }
}
