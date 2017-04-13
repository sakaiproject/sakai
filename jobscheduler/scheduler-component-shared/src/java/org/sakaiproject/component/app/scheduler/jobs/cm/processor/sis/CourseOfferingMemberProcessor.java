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
import org.sakaiproject.coursemanagement.api.Membership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CourseOfferingMemberProcessor extends AbstractCMProcessor {

    public ProcessorState init(Map<String, Object> config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("membersMap", new HashMap<String, List<String[]>>());

        return state;
    }

    private static final Map<String, List<String[]>> getMembersMap(final ProcessorState state) {
        return (Map<String, List<String[]>>) state.getConfiguration().get("membersMap");
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map<String, List<String[]>> membersMap = getMembersMap(state);
        List<String[]> memberList = membersMap.get(data[0]);

        if (memberList == null) {
            memberList = new ArrayList<>();
            membersMap.put(data[0], memberList);
        }
        memberList.add(data);
    }

    public void postProcess(ProcessorState state) throws Exception {

        Map<String, List<String[]>> membersMap = getMembersMap(state);

        for (String courseOfferingEid : membersMap.keySet()) {
            if (!cmService.isCourseOfferingDefined(courseOfferingEid)) {
                log.error("can't find course offering with eid: {}", courseOfferingEid);
                continue;
            }
            Set<Membership> existingMembers = cmService.getCourseOfferingMemberships(courseOfferingEid);

            // Build a map of existing member userEids to Memberships
            Map<String, Membership> existingMemberMap = new HashMap<>(existingMembers.size());
            for (Membership member : existingMembers) {
                existingMemberMap.put(member.getUserId(), member);
            }

            // Keep track of the new members userEids, and add/update them
            Set<Membership> newMembers = new HashSet<>();
            List<String[]> memberElements = membersMap.get(courseOfferingEid);
            for (String[] member : memberElements) {
                newMembers.add(cmAdmin.addOrUpdateCourseOfferingMembership(member[1], member[2], courseOfferingEid, member[3]));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for (Membership member : existingMembers) {
                cmAdmin.removeCourseOfferingMembership(member.getUserId(), courseOfferingEid);
            }
        }
    }

    public String getProcessorTitle() {
        return "Course Offering Member Processor";
    }
}