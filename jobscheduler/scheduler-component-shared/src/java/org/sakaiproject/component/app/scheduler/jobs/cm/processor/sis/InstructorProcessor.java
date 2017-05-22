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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class InstructorProcessor extends AbstractCMProcessor {

    private final static Map<String, Set<String>> getInstructorMap(final ProcessorState state) {
        return (Map<String, Set<String>>) state.getConfiguration().get("instructorMap");
    }

    public ProcessorState init(Map<String, Object> config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("instructorMap", new HashMap<String, Set<String>>());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map<String, Set<String>> instructorMap = getInstructorMap(state);

        Set<String> enrollmentSet = instructorMap.get(data[0]);
        if (enrollmentSet == null) {
            enrollmentSet = new HashSet<>();
            instructorMap.put(data[0], enrollmentSet);
        }
        enrollmentSet.add(data[1]);
    }

    public String getProcessorTitle() {
        return "Instructor Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map<String, Set<String>> instructorMap = getInstructorMap(state);

        for (String enrollmentSetEid : instructorMap.keySet()) {
            if (!cmService.isEnrollmentSetDefined(enrollmentSetEid)) {
                log.error("can't sync instructors no enrollment set exists with eid: {}", enrollmentSetEid);
                continue;
            }

            Set<String> newInstructorElements = instructorMap.get(enrollmentSetEid);
            Set<String> newUserEids = new HashSet<>();
            newUserEids.addAll(newInstructorElements);

            EnrollmentSet enrollmentSet = cmService.getEnrollmentSet(enrollmentSetEid);

            Set<String> officialInstructors = enrollmentSet.getOfficialInstructors();
            if (officialInstructors == null) {
                officialInstructors = new HashSet<>();
                enrollmentSet.setOfficialInstructors(officialInstructors);
            }
            officialInstructors.clear();
            officialInstructors.addAll(newUserEids);
            try {
                cmAdmin.updateEnrollmentSet(enrollmentSet);
            } catch (Exception e) {
                log.error("can't save instructor enrollment set", e);
            }
        }
    }

}
