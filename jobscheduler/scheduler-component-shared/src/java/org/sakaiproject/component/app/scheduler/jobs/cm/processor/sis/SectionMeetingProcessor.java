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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
public class SectionMeetingProcessor extends AbstractCMProcessor {

    @AllArgsConstructor
    @Data
    class CourseMeeting {
        private String notes;
        private String location;
        private String sectionEid;
    }

    private final static Map<String, List<CourseMeeting>> getMeetings(ProcessorState state) {
        return (Map<String, List<CourseMeeting>>) state.getConfiguration().get("meetings");
    }

    public ProcessorState init(Map config) {
        ProcessorState state = super.init(config);

        state.getConfiguration().put("meetings", new HashMap<String, List<CourseMeeting>>());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        addMeeting(new CourseMeeting(data[2], data[1], data[0]), state);
    }

    protected void addMeeting(CourseMeeting meeting, ProcessorState state) {
        Map<String, List<CourseMeeting>> meetings = getMeetings(state);

        String sectionEid = meeting.getSectionEid();
        List<CourseMeeting> meetingList = meetings.get(sectionEid);
        if (meetingList == null) {
            meetingList = new ArrayList<>();
            meetings.put(sectionEid, meetingList);
        }
        meetingList.add(meeting);
    }

    public void preProcess(ProcessorState state) throws Exception {
        Map<String, List<CourseMeeting>> meetings = getMeetings(state);

        meetings.clear();
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map<String, List<CourseMeeting>> meetings = getMeetings(state);

        log.debug("postProcess() " + meetings.size() + " sections to inspect");

        for (String sectionEid : meetings.keySet()) {
            Section section = null;
            if (cmService.isSectionDefined(sectionEid)) {
                section = cmService.getSection(sectionEid);
            } else {
                log.error("can't add meeting no section with eid of {} found", sectionEid);
                continue;
            }

            List<CourseMeeting> currentMeetings = meetings.get(sectionEid);
            // clear existing sections
            cmAdmin.removeAllSectionMeetings(sectionEid);
            section.setMeetings(new HashSet<>());

            for (CourseMeeting courseMeeting : currentMeetings) {
                Meeting meeting = cmAdmin.newSectionMeeting(sectionEid, courseMeeting.getLocation(), null, null, courseMeeting.getNotes());
                section.getMeetings().add(meeting);
            }
            cmAdmin.updateSection(section);
        }
    }

    public String getProcessorTitle() { return "Section Meeting Processor"; }
}