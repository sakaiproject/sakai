/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
 * the attachment for the AssigmentSupplementItem object
 */

@Entity
@Table(name = "ASN_SUP_ATTACH_T",
       indexes = {@Index(name = "uniqueAttachmentItem", columnList = "ATTACHMENT_ID, ASN_SUP_ITEM_ID", unique = true),
                  @Index(name = "ASN_SUP_ITEM_I", columnList = "ASN_SUP_ITEM_ID")})
@NamedQuery(name = "findAttachmentBySupplementItem",
            query = "select attachmentId from AssignmentSupplementItemAttachment a where a.assignmentSupplementItemWithAttachment = :item")
@Data
@NoArgsConstructor
public class AssignmentSupplementItemAttachment {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "assignment_supplemental_item_attachment_sequence")
    @SequenceGenerator(name = "assignment_supplemental_item_attachment_sequence", sequenceName = "ASN_SUP_ITEM_ATT_S")
    private Long id;

    @Column(name = "ATTACHMENT_ID", nullable = false)
    private String attachmentId;

    @ManyToOne
    @JoinColumn(name = "ASN_SUP_ITEM_ID", nullable = false)
    private AssignmentSupplementItemWithAttachment assignmentSupplementItemWithAttachment;
}
