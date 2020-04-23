/**
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
package org.sakaiproject.util.comparator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.user.api.User;

public class UserSortNameComparatorTest {

    @Test
    public void nullSafeCompare() {
        UserSortNameComparator nullsHighComparator = new UserSortNameComparator();
        UserSortNameComparator nullsLowComparator = new UserSortNameComparator(true);
        User userA = Mockito.mock(User.class);
        when(userA.getSortName()).thenReturn("One, One");
        User userB = Mockito.mock(User.class);
        when(userB.getSortName()).thenReturn("Two, Two");
        User userC = Mockito.mock(User.class);
        when(userC.getSortName()).thenReturn(null);

        // null high
        assertEquals(0, nullsHighComparator.compare(null, null));
        assertEquals(-1, nullsHighComparator.compare(userA, null));
        assertEquals(1, nullsHighComparator.compare(null, userA));
        assertEquals(1, nullsHighComparator.compare(null, userC));
        assertEquals(-1, nullsHighComparator.compare(userC, null));
        assertEquals(-1, nullsHighComparator.compare(userA, userC));
        assertEquals(1, nullsHighComparator.compare(userC, userA));

        // null low
        assertEquals(0, nullsLowComparator.compare(null, null));
        assertEquals(1, nullsLowComparator.compare(userA, null));
        assertEquals(-1, nullsLowComparator.compare(null, userA));
        assertEquals(1, nullsLowComparator.compare(userC, null));
        assertEquals(1, nullsLowComparator.compare(userA, userC));
        assertEquals(-1, nullsLowComparator.compare(userC, userA));

        // non null
        assertEquals(-1, nullsHighComparator.compare(userA, userB));
    }

    @Test
    public void underscoreCompare() {
        UserSortNameComparator comparator = new UserSortNameComparator();
        User userA = Mockito.mock(User.class);
        when(userA.getSortName()).thenReturn("One, One");
        User userB = Mockito.mock(User.class);
        when(userB.getSortName()).thenReturn("_Two, Two");

        assertEquals(1, comparator.compare(userA, userB));
        assertEquals(-1, comparator.compare(userB, userA));
        assertEquals(0, comparator.compare(userB, userB));
    }
}