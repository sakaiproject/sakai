/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.assignment.api.model;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The AssignmentSupplementItem is to store additional information for the assignment. Candidates include model answers, instructor notes, grading guidelines, etc.
 */
@Entity
@Table(name = "ASN_NOTE_ITEM_T")
@NamedQuery(name = "findNoteItemByAssignmentId",
            query = "from AssignmentNoteItem m where m.assignmentId = :id")
@Data
@NoArgsConstructor
public class AssignmentNoteItem {

    @Lob
    @Column(name = "NOTE")
    public String note;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "assignment_note_item_sequence")
    @SequenceGenerator(name = "assignment_note_item_sequence", sequenceName = "ASN_NOTE_S")
    private Long id;

    @Column(name = "ASSIGNMENT_ID", nullable = false)
    private String assignmentId;

    @Column(name = "CREATOR_ID")
    private String creatorId;

    @Column(name = "SHARE_WITH")
    private Integer shareWith;
}
