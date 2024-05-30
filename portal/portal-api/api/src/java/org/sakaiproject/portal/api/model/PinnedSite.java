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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PINNED_SITES",
    uniqueConstraints = { @UniqueConstraint(name = "UniquePinning", columnNames = { "USER_ID", "SITE_ID" }) },
    indexes = { @Index(name = "pinned_sites_user_idx", columnList = "USER_ID"),
                @Index(name = "pinned_sites_site_idx", columnList = "SITE_ID") })
@Getter
@Setter
public class PinnedSite implements PersistableEntity<Long> {

    public static final int UNPINNED_POSITION = -1;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "POSITION", nullable = false)
    private int position;

    @Column(name = "HAS_BEEN_UNPINNED", nullable = false)
    private Boolean hasBeenUnpinned = false;

    public PinnedSite() {
    }

    public PinnedSite(String userId, String siteId) {

        super();

        this.userId = userId;
        this.siteId = siteId;
    }
}
