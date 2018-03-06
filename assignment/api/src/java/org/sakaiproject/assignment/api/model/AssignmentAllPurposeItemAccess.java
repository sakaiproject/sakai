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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * the access string(role id or user id) for AssignmentAllPurposeItem
 */

@Entity
@Table(name = "ASN_AP_ITEM_ACCESS_T",
       indexes = {@Index(name = "uniqueAccessItem", columnList = "ITEM_ACCESS, ASN_AP_ITEM_ID", unique = true),
                  @Index(name = "ASN_AP_ITEM_I", columnList = "ASN_AP_ITEM_ID")})
@NamedQuery(name = "findAccessByAllPurposeItem",
            query = "select access from AssignmentAllPurposeItemAccess a where a.assignmentAllPurposeItem = :item")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AssignmentAllPurposeItemAccess {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "assignment_all_purpose_item_access_sequence")
    @SequenceGenerator(name = "assignment_all_purpose_item_access_sequence", sequenceName = "ASN_AP_ITEM_S")
    private Long id;

    @Column(name = "ITEM_ACCESS", nullable = false)
    private String access;

    @ManyToOne
    @JoinColumn(name = "ASN_AP_ITEM_ID", nullable = false)
    private AssignmentAllPurposeItem assignmentAllPurposeItem;
}
