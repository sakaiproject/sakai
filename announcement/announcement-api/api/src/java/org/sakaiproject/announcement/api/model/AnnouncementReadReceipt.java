/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.announcement.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Records that a user opened (read) a specific announcement message inside Sakai. One row is kept
 * per message reference and user, holding the earliest time the announcement was viewed.
 */
@Entity
@Table(name = "ANNC_READ_RECEIPT",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueAnncReadReceipt", columnNames = { "MESSAGE_REF", "USER_ID" }) },
    indexes = { @Index(name = "annc_read_receipt_message_ref_idx", columnList = "MESSAGE_REF") })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnnouncementReadReceipt implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "annc_read_receipt_id_sequence")
    @SequenceGenerator(name = "annc_read_receipt_id_sequence", sequenceName = "ANNC_READ_RECEIPT_S")
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "MESSAGE_REF", length = 255, nullable = false)
    private String messageRef;

    @EqualsAndHashCode.Include
    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "FIRST_VIEWED", nullable = false)
    private Instant firstViewed;

    public AnnouncementReadReceipt(String messageRef, String userId, Instant firstViewed) {
        this.messageRef = messageRef;
        this.userId = userId;
        this.firstViewed = firstViewed;
    }
}
