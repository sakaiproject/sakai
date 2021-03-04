/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.meetings.api.beans;

import org.sakaiproject.meetings.api.MeetingRole;
import org.sakaiproject.meetings.api.SelectionType;
import org.sakaiproject.meetings.api.persistence.MeetingParticipant;

public class MeetingParticipantTransferBean {

    public Long id;
    public MeetingRole role;
    public String selectionId;
    public SelectionType selectionType;

    public String displayString;

    public MeetingParticipantTransferBean() {}
    
    public MeetingParticipantTransferBean(MeetingParticipant participant) {

        this.id = participant.getId();
        this.role = participant.getRole();
        this.selectionId = participant.getSelectionId();
        this.selectionType = participant.getSelectionType();
    }

    public MeetingParticipant toMeetingParticipant() {

        MeetingParticipant participant = new MeetingParticipant();
        participant.setId(this.id);
        participant.setRole(this.role);
        participant.setSelectionId(this.selectionId);
        participant.setSelectionType(this.selectionType);
        return participant;
    }
}
