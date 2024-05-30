/**
 * Copyright (c) 2023 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.ui.model;

import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder(builderMethodName = "privateBuilder")
public class AssessmentReport {


    private Optional<String> title;
    private Optional<String> subject;
    private AssessmentReportType type;
    @Singular
    private List<AssessmentReportSection> sections;
    @Builder.Default
    private AssessmentReportOrientation orientation = AssessmentReportOrientation.PORTRAIT;


    // Builder with mandatory field "type"
    public static AssessmentReportBuilder type(AssessmentReportType type) {
        return privateBuilder().type(type);
    }

    // Make the default builder method private
    private static AssessmentReportBuilder privateBuilder() {
        return new AssessmentReportBuilder();
    }


    public static class AssessmentReportBuilder {


        private Optional<String> title = Optional.empty();
        private Optional<String> subject = Optional.empty();


        public AssessmentReportBuilder title(String title) {
            this.title = Optional.ofNullable(title);
            return this;
        }

        public AssessmentReportBuilder subject(String subject) {
            this.subject = Optional.ofNullable(subject);
            return this;
        }
    }


    public enum AssessmentReportType {
        ASSESSMENT_STATISTICS,
        ITEM_ANALYSIS,
    }

    public enum AssessmentReportOrientation {
        PORTRAIT,
        LANDSCAPE,
    }
}
