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
@Table(name = "PUSH_SUBSCRIPTIONS",
    indexes = {
        @Index(name = "IDX_PUSH_SUBSCRIPTIONS_USER", columnList = "USER_ID")
    }
)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PushSubscription implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_subscription_id_sequence")
    @SequenceGenerator(name = "push_subscription_id_sequence", sequenceName = "PUSH_SUBSCRIPTION_S")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "USER_KEY", length = 255, nullable = false)
    private String userKey;

    @Column(name = "AUTH", length = 255, nullable = false)
    private String auth;

    @Column(name = "ENDPOINT", length = 2048, nullable = false)
    private String endpoint;

    @Column(name = "FINGERPRINT", length = 255, nullable = false, unique = true)
    private String fingerprint;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "CREATED", nullable = false)
    private Instant created;
}
