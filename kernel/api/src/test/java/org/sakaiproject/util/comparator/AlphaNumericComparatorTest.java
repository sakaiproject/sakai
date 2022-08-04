/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AlphaNumericComparatorTest {
    private AlphaNumericComparator alphaNumeric = new AlphaNumericComparator();

    @Test
    public void alphanumericCompare() {
        List<String> rawData = Arrays.asList("X19", "X10", "X25", "X111", "X2", "X1", "X", "0", "X242+141.55", "A11", "B12", "a", "11720217301000", "b", "9999372036854775807", null);
        List<String> expectedSort = Arrays.asList("0", "11720217301000", "9999372036854775807", "a", "A11", "b", "B12", null, "X", "X1", "X2", "X10", "X19", "X25", "X111", "X242+141.55");

        //rawData.stream().sorted(alphaNumeric).forEach(System.out::println);
        rawData.sort(alphaNumeric);

        assertEquals(expectedSort, rawData);
    }
}
