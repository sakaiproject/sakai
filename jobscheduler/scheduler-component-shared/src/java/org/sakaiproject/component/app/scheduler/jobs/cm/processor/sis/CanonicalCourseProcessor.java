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
import org.sakaiproject.coursemanagement.api.CanonicalCourse;

@Slf4j
public class CanonicalCourseProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        log.debug("Reconciling canonical course {}", data[0]);

        if (cmService.isCanonicalCourseDefined(data[0])) {
            CanonicalCourse canonicalCourse = cmService.getCanonicalCourse(data[0]);
            log.debug("Updating CanonicalCourse {}", canonicalCourse.getEid());

            canonicalCourse.setTitle(data[1]);
            canonicalCourse.setDescription(data[2]);
            cmAdmin.updateCanonicalCourse(canonicalCourse);
        } else {
            log.debug("Adding CanonicalCourse {}", data[0]);
            cmAdmin.createCanonicalCourse(data[0], data[1], data[2]);
        }

        if (data.length > 3) {
            String courseSet = data[3];
            if (courseSet != null && cmService.isCourseSetDefined(courseSet)) {
                cmAdmin.addCanonicalCourseToCourseSet(courseSet, data[0]);
            }
        }
    }

    public String getProcessorTitle() {
        return "Canonical Course Processor";
    }
}
