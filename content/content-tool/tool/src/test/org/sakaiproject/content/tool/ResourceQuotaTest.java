/*
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
package org.sakaiproject.content.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;

public class ResourceQuotaTest {

    @Test
    public void convertsWholeDecimalAndScientificNotationQuotas() {
        assertEquals(0L, ResourceQuota.parseGigabytes("0"));
        assertEquals(2097152L, ResourceQuota.parseGigabytes("2"));
        assertEquals(1572864L, ResourceQuota.parseGigabytes("1.5"));
        assertEquals(10485760L, ResourceQuota.parseGigabytes("1e1"));
        assertEquals(1572864L, ResourceQuota.parseGigabytes(" 1.5 "));
    }

    @Test
    public void roundsUpBeforeConvertingToWholeKilobytes() {
        assertEquals(11072963L, ResourceQuota.parseGigabytes("10.555555"));
        assertEquals(3732931L, ResourceQuota.parseGigabytes("3.555"));
        assertEquals(3732931L, ResourceQuota.parseGigabytes("3.554"));
        assertEquals(1059062L, ResourceQuota.parseGigabytes("1.00000001"));
    }

    @Test
    public void appliesTheMinimumNonzeroQuota() {
        for (String value : List.of("0.000000001", "0.00000095367431640625", "0.01")) {
            assertEquals(value, 10486L, ResourceQuota.parseGigabytes(value));
        }
        assertEquals("0.01", ResourceQuota.formatGigabytes(1L));
        assertEquals("0.01", ResourceQuota.formatGigabytes(10486L));
    }

    @Test
    public void rejectsInvalidNegativeAndOverflowingQuotas() {
        for (String value : List.of("", " ", "-1", "-0.000000001", "8796093022208",
                "  not-a-number  ", "\"/><span id=\"quota-injected\">&amp;'</span>")) {
            assertThrows(value, NumberFormatException.class, () -> ResourceQuota.parseGigabytes(value));
        }
        // The unrounded value fits, but rounding to two decimal places overflows stored KB.
        assertThrows(ArithmeticException.class,
                () -> ResourceQuota.parseGigabytes("8796093022207.99999904632568359375"));
    }

    @Test
    public void formatsStoredQuotasAndKeepsRoundedValuesStable() {
        assertEquals("0", ResourceQuota.formatGigabytes(0L));
        assertEquals("2", ResourceQuota.formatGigabytes(2097152L));
        assertEquals("1.5", ResourceQuota.formatGigabytes(1572864L));
        assertEquals("3.56", ResourceQuota.formatGigabytes(3732931L));
        assertEquals("10.56", ResourceQuota.formatGigabytes(11072963L));
        assertEquals("1.01", ResourceQuota.formatGigabytes(1059062L));
        for (String value : List.of("10.555555", "3.555", "3.554", "1.00000001", "1e1",
                "0.000000001", "0.01", "1.5", "0.00000095367431640625", "0")) {
            long stored = ResourceQuota.parseGigabytes(value);
            assertEquals(value, stored, ResourceQuota.parseGigabytes(ResourceQuota.formatGigabytes(stored)));
        }
    }
}
