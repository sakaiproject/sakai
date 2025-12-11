/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "PROFILE_SOCIAL_INFO_T")
public class SocialNetworkingInfo implements PersistableEntity<String> {

	@EqualsAndHashCode.Include
    @Id
    @Column(name = "USER_ID", length = 99)
	private String userId;

    // Legacy column that must be populated - both columns contain the same value
    @Column(name = "USER_UUID", length = 99, nullable = false)
	private String userUuid;

    @Column(name = "FACEBOOK_URL", length = 255)
	private String facebookUrl;

    @Column(name = "LINKEDIN_URL", length = 255)
	private String linkedinUrl;

    @Column(name = "INSTAGRAM_URL", length = 255)
	private String instagramUrl;

	// additional constructor
	public SocialNetworkingInfo(String userId) {
		this.userId = userId;
		this.userUuid = userId;
	}

    public String getId() {
        return userId;
    }

    /**
     * JPA lifecycle callback to ensure both columns are always synchronized.
     *
     * <p>Architecture note: userId (USER_ID column) is the authoritative field and primary key.
     * userUuid (USER_UUID column) is a legacy column that must be kept in sync for backwards
     * compatibility. This callback ensures userUuid is synchronized from userId before any
     * database write operation.</p>
     *
     * <p>The null guard prevents violating the NOT NULL constraint on USER_UUID if an entity
     * is created using the no-arg constructor without setting userId.</p>
     */
    @PrePersist
    @PreUpdate
    private void syncUserIds() {
        if (this.userId != null) {
            this.userUuid = this.userId;
        }
    }
}
