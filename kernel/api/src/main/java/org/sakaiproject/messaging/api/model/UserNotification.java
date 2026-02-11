/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.messaging.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "USER_NOTIFICATIONS", indexes = {
    @Index(name = "IDX_USER_NOTIFICATIONS_TO_USER", columnList = "TO_USER"),
    @Index(name = "IDX_USER_NOTIFICATIONS_EVENT_REF", columnList = "EVENT, REF")
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserNotification implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_notification_id_sequence")
    @SequenceGenerator(name = "user_notification_id_sequence", sequenceName = "USER_NOTIFICATIONS_S")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "FROM_USER", length = 99, nullable = false)
    private String fromUser;

    @Column(name = "TO_USER", length = 99, nullable = false)
    private String toUser;

    @Column(name = "EVENT", length = 32, nullable = false)
    private String event;

    @Column(name = "REF", length = 255, nullable = false)
    private String ref;

    @Column(name="TITLE", length = 255)
    private String title;

    @Column(name="SITE_ID", length = 99)
    private String siteId;

    @Column(name="URL", length = 2048)
    private String url;

    @Column(name="EVENT_DATE", nullable = false)
    @Type(type = "org.hibernate.type.InstantType")
    private Instant eventDate;

    @Column(name="END_DATE")
    @Type(type = "org.hibernate.type.InstantType")
    private Instant endDate;

    @Column(name="DEFERRED", nullable = false)
    private Boolean deferred = Boolean.FALSE;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    @Column(name = "TOOL", length = 99)
    private String tool;

    @Column(name = "BROADCAST")
    private Boolean broadcast = Boolean.FALSE;
}
