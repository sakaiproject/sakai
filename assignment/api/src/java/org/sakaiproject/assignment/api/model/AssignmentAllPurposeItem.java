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

import java.util.Date;
import java.util.Set;
import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The AssignmentSupplementItem is to store additional information for the assignment. Candidates include model answers, instructor notes, grading guidelines, etc.
 */

@Entity
@Table(name = "ASN_AP_ITEM_T")
@PrimaryKeyJoinColumn(name = "ID")
@NamedQuery(name = "findAllPurposeItemByAssignmentId",
            query = "from AssignmentAllPurposeItem m where m.assignmentId = :id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AssignmentAllPurposeItem extends AssignmentSupplementItemWithAttachment {

    @Column(name = "ASSIGNMENT_ID", nullable = false)
    private String assignmentId;

    @Column(name = "TITLE")
    private String title;

    @Lob
    @Column(name = "TEXT")
    private String text;

    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseDate;

    @Column(name = "RETRACT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date retractDate;

    @Column(name = "HIDE", nullable = false)
    private Boolean hide;

    @OneToMany(mappedBy = "assignmentAllPurposeItem")
    private Set<AssignmentAllPurposeItemAccess> accessSet;
}
