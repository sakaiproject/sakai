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
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MembershipProcessor extends AbstractCMProcessor {

    private final static Map<String, Set<String>> getInstructorMap(final ProcessorState state) {
        return (Map<String, Set<String>>) state.getConfiguration().get("instructorMap");
    }

    private final static Map<String, List<String[]>> getCourseSetMembershipMap(final ProcessorState state) {
        return (Map<String, List<String[]>>) state.getConfiguration().get("courseSetMembersMap");
    }

    private final static Map<String, List<String[]>> getMembersMap(final ProcessorState state) {
        return (Map<String, List<String[]>>) state.getConfiguration().get("membersMap");
    }

    public ProcessorState init(Map<String, Object> config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("instructorMap", new HashMap<String, Set<String>>());
        state.getConfiguration().put("membersMap", new HashMap<String, List<String[]>>());
        state.getConfiguration().put("courseSetMembersMap", new HashMap<String, List<String[]>>());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map<String, Set<String>> instructorMap = getInstructorMap(state);
        Map<String, List<String[]>> membersMap = getMembersMap(state);
        Map<String, List<String[]>> courseSetMembersMap = getCourseSetMembershipMap(state);

        String mode = data[0];

        if (StringUtils.equalsIgnoreCase(data[0], "instructor")) {
            Set<String> enrollmentSet = instructorMap.get(data[1]);
            if (enrollmentSet == null) {
                enrollmentSet = new HashSet<>();
                instructorMap.put(data[1], enrollmentSet);
            }
            enrollmentSet.add(data[2]);
        }
        if (StringUtils.equalsIgnoreCase(data[0], "courseoffering")) {

            List<String[]> memberList = membersMap.get(data[1]);
            if (memberList == null) {
                memberList = new ArrayList<>();
                membersMap.put(data[1], memberList);
            }
            memberList.add(data);
        }

        if (StringUtils.equalsIgnoreCase(data[0], "courseset")) {

            List<String[]> courseSetMemberList = courseSetMembersMap.get(data[1]);
            if (courseSetMemberList == null) {
                courseSetMemberList = new ArrayList<>();
                courseSetMembersMap.put(data[1], courseSetMemberList);
            }
            courseSetMemberList.add(data);
        }
    }

    public String getProcessorTitle() {
        return "Membership Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map<String, Set<String>> instructorMap = getInstructorMap(state);
        Map<String, List<String[]>> membersMap = getMembersMap(state);
        Map<String, List<String[]>> courseSetMembersMap = getCourseSetMembershipMap(state);

        for (String enrollmentSetEid : instructorMap.keySet()) {
            if (!cmService.isEnrollmentSetDefined(enrollmentSetEid)) {
                log.error("can't sync instructors no enrollment set exists with eid: {}", enrollmentSetEid);
                continue;
            }

            Set<String> newInstructorElements = instructorMap.get(enrollmentSetEid);
            EnrollmentSet enrollmentSet = cmService.getEnrollmentSet(enrollmentSetEid);
            enrollmentSet.setOfficialInstructors(new HashSet<>(newInstructorElements));

            try {
                cmAdmin.updateEnrollmentSet(enrollmentSet);
            } catch (Exception e) {
                log.error("can't save instructor enrollment set", e);
            }
        }

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
            for (String[] member : membersMap.get(courseOfferingEid)) {
                newMembers.add(cmAdmin.addOrUpdateCourseOfferingMembership(member[2], member[3], courseOfferingEid, member[4]));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for (Membership member : existingMembers) {
                cmAdmin.removeCourseOfferingMembership(member.getUserId(), courseOfferingEid);
            }
        }

        for (String courseSetEid : courseSetMembersMap.keySet()) {
            if (!cmService.isCourseSetDefined(courseSetEid)) {
                log.error("can't find course set with eid: {}", courseSetEid);
                continue;
            }
            Set<Membership> existingMembers = cmService.getCourseSetMemberships(courseSetEid);

            // Build a map of existing member userEids to Memberships
            Map<String, Membership> existingMemberMap = new HashMap<>(existingMembers.size());
            for (Membership member : existingMembers) {
                existingMemberMap.put(member.getUserId(), member);
            }

            // Keep track of the new members userEids, and add/update them
            Set<Membership> newMembers = new HashSet<>();
            for (String[] member : courseSetMembersMap.get(courseSetEid)) {
                newMembers.add(cmAdmin.addOrUpdateCourseSetMembership(member[2], member[3], courseSetEid, member[4]));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for (Membership member : existingMembers) {
                cmAdmin.removeCourseSetMembership(member.getUserId(), courseSetEid);
            }
        }
    }

    public void preProcess(ProcessorState state) throws Exception {
        Map<String, Set<String>> instructorMap = getInstructorMap(state);
        Map<String, List<String[]>> membersMap = getMembersMap(state);
        Map<String, List<String[]>> courseSetMembersMap = getCourseSetMembershipMap(state);

        instructorMap.clear();
        membersMap.clear();
        courseSetMembersMap.clear();
    }
}
