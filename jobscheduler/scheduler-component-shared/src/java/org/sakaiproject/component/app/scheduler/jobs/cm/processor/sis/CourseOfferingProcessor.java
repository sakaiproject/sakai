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
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseOffering;

@Slf4j
public class CourseOfferingProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        log.debug("Reconciling course offering {}", data[0]);

        if (cmService.isCourseOfferingDefined(data[0])) {
            CourseOffering courseOffering = cmService.getCourseOffering(data[0]);
            log.debug("Updating CourseOffering {}", courseOffering.getEid());

            AcademicSession newAcademicSession = cmService.getAcademicSession(data[1]);
            courseOffering.setTitle(data[2]);
            courseOffering.setDescription(data[3]);
            courseOffering.setStatus(data[4]);
            courseOffering.setAcademicSession(newAcademicSession);
            courseOffering.setStartDate(getDate(data[5]));
            courseOffering.setEndDate(getDate(data[6]));
            cmAdmin.updateCourseOffering(courseOffering);
        } else {
            String eid = data[0];
            log.debug("Adding CourseOffering {}", eid);
            cmAdmin.createCourseOffering(data[0], data[2], data[3], data[4], data[1], data[7], getDate(data[5]), getDate(data[6]));
        }
        if (data.length > 8) {
            String courseSet = data[8];
            if (courseSet != null && cmService.isCourseSetDefined(courseSet)) {
                cmAdmin.addCourseOfferingToCourseSet(courseSet, data[0]);
            }
        }
    }

    public String getProcessorTitle() { return "Course Offering Processor"; }
}