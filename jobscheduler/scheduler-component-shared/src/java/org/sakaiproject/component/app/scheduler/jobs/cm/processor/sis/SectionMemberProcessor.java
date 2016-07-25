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
public class SectionMemberProcessor extends AbstractCMProcessor {

    private final static Map<String, List<String[]>> getSectionMembers(ProcessorState state) {
        return (Map<String, List<String[]>>) state.getConfiguration().get("sectionMembers");
    }

    public ProcessorState init(Map<String, Object> config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("sectionMembers", new HashMap<String, List<String[]>>());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map<String, List<String[]>> sectionMembers = getSectionMembers(state);
        String sectionEid = data[0];
        List<String[]> members = sectionMembers.get(sectionEid);
        if (members == null) {
            members = new ArrayList<>();
            sectionMembers.put(sectionEid, members);
        }
        members.add(data);
    }

    public String getProcessorTitle() {
        return "Section Member Processor";
    }

    public void preProcess(ProcessorState state) throws Exception {
        Map<String, List<String[]>> sectionMembers = getSectionMembers(state);
        sectionMembers.clear();
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map<String, List<String[]>> sectionMembers = getSectionMembers(state);
        for (String sectionEid : sectionMembers.keySet()) {
            if (!cmService.isSectionDefined(sectionEid)) {
                log.error("can't sync section memberships, no section with eid of {} found", sectionEid);
                continue;
            }

            Set<Membership> existingMembers = cmService.getSectionMemberships(sectionEid);

            // Build a map of existing member userEids to Memberships
            Map<String, Membership> existingMemberMap = new HashMap(existingMembers.size());
            for (Membership member : existingMembers) {
                existingMemberMap.put(member.getUserId(), member);
            }

            List<String[]> memberElements = sectionMembers.get(sectionEid);

            // Keep track of the new members userEids, and add/update them
            Set<Membership> newMembers = new HashSet<>();
            for (String[] memberElement : memberElements) {
                newMembers.add(cmAdmin.addOrUpdateSectionMembership(memberElement[1], memberElement[2], sectionEid, memberElement[3]));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for (Membership member : existingMembers) {
                cmAdmin.removeSectionMembership(member.getUserId(), sectionEid);
            }
        }
    }
}