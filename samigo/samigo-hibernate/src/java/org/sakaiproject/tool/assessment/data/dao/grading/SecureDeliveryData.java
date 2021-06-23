/*
 * Copyright (c) 2020, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.data.dao.grading;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecureDeliveryData implements Serializable {
    private static final    long                    serialVersionUID = 1L;

    private                 Long                    id;
    private                 Long                    publishedAssessmentId;
    private                 String                  agentId;
    private                 Long                    assessmentGradingId;
    private                 Date                    createdDate;
    private                 String                  instructorUrl;
    private                 String                  studentUrl;

}
