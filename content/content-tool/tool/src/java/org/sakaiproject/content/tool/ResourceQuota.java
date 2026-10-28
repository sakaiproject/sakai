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

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Converts the Resources form's GB quotas to and from stored whole KB. */
public final class ResourceQuota {

    private static final BigDecimal KILOBYTES_PER_GIGABYTE = BigDecimal.valueOf(1024L * 1024L);
    private static final BigDecimal MINIMUM_GIGABYTES = BigDecimal.valueOf(1L, 2);

    private ResourceQuota() {
    }

    /** Rounds GB up to two decimal places with a 0.01 minimum; zero remains unlimited. */
    public static long parseGigabytes(String value) {
        BigDecimal gigabytes = new BigDecimal(value.trim());
        BigDecimal kilobytes = gigabytes.multiply(KILOBYTES_PER_GIGABYTE);
        if (kilobytes.signum() < 0 || kilobytes.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            throw new NumberFormatException("Quota must be non-negative and within the supported KB range");
        }
        BigDecimal roundedGigabytes = gigabytes.signum() == 0 ? BigDecimal.ZERO
                : gigabytes.max(MINIMUM_GIGABYTES).setScale(2, RoundingMode.CEILING);
        return roundedGigabytes.multiply(KILOBYTES_PER_GIGABYTE)
                .setScale(0, RoundingMode.CEILING).longValueExact();
    }

    public static String formatGigabytes(long kilobytes) {
        BigDecimal gigabytes = BigDecimal.valueOf(kilobytes).divide(KILOBYTES_PER_GIGABYTE);
        BigDecimal rounded = gigabytes.setScale(2, RoundingMode.HALF_UP);
        // Existing positive quotas below 0.01 GB must not display as unlimited (0).
        return (gigabytes.signum() > 0 ? rounded.max(MINIMUM_GIGABYTES) : rounded)
                .stripTrailingZeros().toPlainString();
    }
}
