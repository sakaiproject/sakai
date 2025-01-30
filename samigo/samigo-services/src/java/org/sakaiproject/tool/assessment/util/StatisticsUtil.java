/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.compare.ComparableUtils;

public class StatisticsUtil {


    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);


    //               incorrect + blank
    // difficulty = ------------------- * 100
    //                     total
    public static int calcDifficulty(long correctCount, long incorrectCount, long blankCount) {
        BigDecimal totalCount = BigDecimal.valueOf(correctCount + incorrectCount + blankCount);

        // Can not calculate difficulty with totalCount smaller than 1
        if (ComparableUtils.is(totalCount).lessThanOrEqualTo(BigDecimal.ZERO)) {
            return -1;
        }

        return BigDecimal.valueOf(incorrectCount + blankCount)
                .multiply(HUNDRED)
                .divide(totalCount, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
