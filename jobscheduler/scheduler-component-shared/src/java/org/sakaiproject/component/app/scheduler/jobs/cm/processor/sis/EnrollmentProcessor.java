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
import org.sakaiproject.coursemanagement.api.Enrollment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class EnrollmentProcessor extends AbstractCMProcessor {

    private static Map<String, List<String[]>> getEnrollmentMap(final ProcessorState state) {
        return (Map<String, List<String[]>>) state.getConfiguration().get("enrollmentMap");
    }

    public ProcessorState init(Map<String, Object> config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("enrollmentMap", new HashMap<String, List<String[]>>());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map<String, List<String[]>> enrollmentMap = getEnrollmentMap(state);

        String enrollmentSetEid = data[0];
        List<String[]> members = enrollmentMap.get(enrollmentSetEid);
        if (members == null) {
            members = new ArrayList<>();
            enrollmentMap.put(enrollmentSetEid, members);
        }
        members.add(data);
    }

    public String getProcessorTitle() {
        return "Enrollment Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map<String, List<String[]>> enrollmentMap = getEnrollmentMap(state);

        for (String enrollmentSetEid : enrollmentMap.keySet()) {
            if (!cmService.isEnrollmentSetDefined(enrollmentSetEid)) {
                log.error("can't sync enrollment for non-existent enrollment set with eid {}", enrollmentSetEid);
                continue;
            }

            List<String[]> newEnrollmentElements = enrollmentMap.get(enrollmentSetEid);
            Set<Enrollment> existingEnrollments = cmService.getEnrollments(enrollmentSetEid);
            Set<String> newUserEids = new HashSet<>();

            for (String[] enrollmentElement : newEnrollmentElements) {
                newUserEids.add(enrollmentElement[1]);
                cmAdmin.addOrUpdateEnrollment(enrollmentElement[1], enrollmentSetEid, enrollmentElement[2], enrollmentElement[3], enrollmentElement[4]);
            }

            for (Enrollment existingEnr : existingEnrollments) {
                if (!newUserEids.contains(existingEnr.getUserId())) {
                    // Drop this enrollment
                    cmAdmin.removeEnrollment(existingEnr.getUserId(), enrollmentSetEid);
                }
            }
        }
    }

    public void preProcess(ProcessorState state) throws Exception {
        Map<String, List<String[]>> enrollmentMap = getEnrollmentMap(state);

        enrollmentMap.clear();
    }
}