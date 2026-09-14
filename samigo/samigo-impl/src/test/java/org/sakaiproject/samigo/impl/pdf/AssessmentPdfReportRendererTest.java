/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.samigo.impl.pdf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;

public class AssessmentPdfReportRendererTest {

    @Test
    public void studentReportModelReportsGraderComments() {
        AssessmentStudentReportPdfModel model = AssessmentPdfTestDataFactory.studentReportWithComments();
        assertTrue(model.hasComments());
    }

    @Test
    public void studentReportModelWithoutComments() {
        AssessmentStudentReportPdfModel model = new AssessmentStudentReportPdfModel(
                "Student One", "Student", "student@example.com", null,
                "Quiz 1", "Site A", 8.0, 10.0, false, java.util.Collections.emptyList());
        assertFalse(model.hasComments());
    }
}
