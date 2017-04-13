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
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.CourseSet;

@Slf4j
public class CourseSetProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        log.debug("Reconciling course set {}", data[0]);

        if (cmService.isCourseSetDefined(data[0])) {
            CourseSet courseSet = cmService.getCourseSet(data[0]);
            log.debug("Updating CourseSet {}", courseSet.getEid());

            courseSet.setTitle(data[1]);
            courseSet.setDescription(data[2]);
            courseSet.setCategory(data[3]);
            if (StringUtils.isNotBlank(data[4]) && cmService.isCourseSetDefined(data[4])) {
                CourseSet parent = cmService.getCourseSet(data[4]);
                courseSet.setParent(parent);
            }
            cmAdmin.updateCourseSet(courseSet);
        } else {
            String eid = data[0];
            log.debug("Adding CourseSet + " + eid);
            cmAdmin.createCourseSet(data[0], data[1], data[2], data[3], StringUtils.defaultIfEmpty(data[4], null));
        }

    }

    public String getProcessorTitle() {
        return "Course Set Processor";
    }
}