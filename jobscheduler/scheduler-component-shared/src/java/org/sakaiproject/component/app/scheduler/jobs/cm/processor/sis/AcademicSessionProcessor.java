/**
 * Copyright (c) 2003 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class AcademicSessionProcessor extends AbstractCMProcessor {

    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if (cmService.isAcademicSessionDefined(eid)) {
            updateAcademicSession(cmService.getAcademicSession(eid), data);
        } else {
            addAcademicSession(data);
        }
    }

    public String getProcessorTitle() {
        return "AcademicSession Processor";
    }

    public void addAcademicSession(String[] data) {
        String eid = data[0];
        log.debug("Adding AcademicSession {}", eid);
        String title = data[1];
        String description = data[2];
        Date startDate = getDate(data[3]);
        Date endDate = getDate(data[4]);
        AcademicSession session = cmAdmin.createAcademicSession(eid, title, description, startDate, endDate);
        setCurrentStatus(session);
    }

    public void updateAcademicSession(AcademicSession session, String[] data) {
        log.debug("Updating AcademicSession {}", session.getEid());
        session.setTitle(data[1]);
        session.setDescription(data[2]);
        session.setStartDate(getDate(data[3]));
        session.setEndDate(getDate(data[4]));
        cmAdmin.updateAcademicSession(session);
        setCurrentStatus(session);
    }

    private void setCurrentStatus(AcademicSession session) {
        List<AcademicSession> currentSessions = cmService.getCurrentAcademicSessions();
        List<String> currentTerms = new ArrayList<String>();

        // initialize the array with the current sessions
        for (AcademicSession s : currentSessions) {
            currentTerms.add(s.getEid());
        }

        // add this session if its end date is after today
        if (session.getEndDate().after(new Date())) {
            if (!currentTerms.contains(session.getEid().toString())) {
                currentTerms.add(session.getEid());
            }
            // otherwise remove this session
        } else {
            if (currentTerms.contains(session.getEid().toString())) {
                currentTerms.remove(session.getEid().toString());
            }
        }
        cmAdmin.setCurrentAcademicSessions(currentTerms);
    }
}