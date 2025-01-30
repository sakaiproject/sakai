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
package org.sakaiproject.tool.assessment.business.entity;

import org.sakaiproject.tool.assessment.util.StatisticsUtil;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemStatistics {


    private Long attemptedResponses;
    private Long correctResponses;
    private Long incorrectResponses;
    private Long blankResponses;
    private Integer difficulty;


    public static class ItemStatisticsBuilder {


        public ItemStatisticsBuilder calcDifficulty() {
            int calculatedDifficulty = StatisticsUtil.calcDifficulty(correctResponses, incorrectResponses, blankResponses);

            difficulty = calculatedDifficulty != -1 ? calculatedDifficulty : null;

            return this;
        }
    }
}
