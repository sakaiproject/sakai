/*
 * Copyright (c) 2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Simplified JPA mapping of the LTI memberships jobs model.
 */
@Entity
@Table(name = "LTI_MEMBERSHIPS_JOBS")
@Getter
@Setter
public class LtiMembershipJob implements PersistableEntity<String> {

    @Id
    @Column(name = "SITE_ID", length = 99)
    private String id;  // This is both the ID and the site ID

    /**
     * Gets the site ID (same as the ID).
     * @return the site ID
     */
    public String getSiteId() {
        return id;
    }

    /**
     * Sets the site ID (same as the ID).
     * @param siteId the site ID
     */
    public void setSiteId(String siteId) {
        this.id = siteId;
    }

    @Column(name = "MEMBERSHIPS_ID", length = 256, nullable = false)
    private String membershipsId;

    @Column(name = "MEMBERSHIPS_URL", length = 4000, nullable = false)
    private String membershipsUrl;

    @Column(name = "CONSUMERKEY", length = 1024)
    private String consumerKey;

    @Column(name = "LTI_VERSION", length = 32, nullable = false)
    private String ltiVersion;
}