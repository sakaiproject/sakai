/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.portal.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

import org.hibernate.annotations.Type;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "RECENT_SITES",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueRecentSite", columnNames = { "USER_ID", "SITE_ID" }) },
    indexes = { @Index(name = "recent_sites_user_idx", columnList = "USER_ID"),
                @Index(name = "recent_sites_site_idx", columnList = "SITE_ID") })
@Getter
@Setter
public class RecentSite implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "recent_sites_id_sequence")
    @SequenceGenerator(name = "recent_sites_id_sequence", sequenceName = "RECENT_SITES_S")
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "CREATED", nullable = false)
    private Instant created;
}
