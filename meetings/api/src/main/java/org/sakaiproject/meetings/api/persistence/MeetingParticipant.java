/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.api.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.meetings.api.MeetingRole;
import org.sakaiproject.meetings.api.SelectionType;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;

@Entity
@Table(name = "MEETING_PARTICIPANTS", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "MEETING_ID", "SELECTION_TYPE", "SELECTION_ID" })
})
@Data
public class MeetingParticipant implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "meeting_participant_sequence")
    @SequenceGenerator(name = "meeting_participant_sequence", sequenceName = "MEETING_PARTICIPANT_S")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEETING_ID")
    private Meeting meeting;

    @Enumerated(EnumType.STRING)
    @Column(name = "SELECTION_TYPE", length = 16, nullable = false)
    private SelectionType selectionType;

    @Column(name = "SELECTION_ID", length = 99, nullable = false)
    private String selectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 32, nullable = false)
    private MeetingRole role;
}
