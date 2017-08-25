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
 * To provide sample answers to student
 */

@Entity
@Table(name = "ASN_MA_ITEM_T")
@PrimaryKeyJoinColumn(name = "ID")
@NamedQuery(name = "findModelAnswerByAssignmentId",
            query = "from AssignmentModelAnswerItem m where m.assignmentId = :id")
@Data
@NoArgsConstructor
public class AssignmentModelAnswerItem extends AssignmentSupplementItemWithAttachment {

    @Lob
    @Column(name = "TEXT")
    public String text;

    @Column(name = "ASSIGNMENT_ID", nullable = false)
    private String assignmentId;

    @Column(name = "SHOW_TO")
    private Integer showTo;
}
