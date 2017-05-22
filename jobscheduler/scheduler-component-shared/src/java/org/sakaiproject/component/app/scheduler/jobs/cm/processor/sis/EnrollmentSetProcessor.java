/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;

@Slf4j
public class EnrollmentSetProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        log.debug("Reconciling enrollment set {}", data[0]);

        if (cmService.isEnrollmentSetDefined(data[0])) {
            EnrollmentSet enrollmentSet = cmService.getEnrollmentSet(data[0]);
            log.debug("Updating EnrollmentSet {}", enrollmentSet.getEid());

            enrollmentSet.setTitle(data[1]);
            enrollmentSet.setDescription(data[2]);
            enrollmentSet.setCategory(data[3]);
            enrollmentSet.setDefaultEnrollmentCredits(data[5]);
            cmAdmin.updateEnrollmentSet(enrollmentSet);
        } else {
            log.debug("Adding EnrollmentSet {}", data[0]);
            cmAdmin.createEnrollmentSet(data[0], data[1], data[2], data[3], data[5], data[4], null);
        }
    }

    public String getProcessorTitle() { return "Enrollment Set Processor"; }
}