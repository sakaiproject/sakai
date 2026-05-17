/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lti.api.model;

import java.io.Serializable;
import javax.persistence.*;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "lti_memberships_jobs")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class LtiMembershipsJob implements PersistableEntity<String>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "memberships_id", length = 256)
    private String membershipsId;

    @Lob
    @Column(name = "memberships_url")
    private String membershipsUrl;

    @Column(name = "consumerkey", length = 1024)
    private String consumerkey;

    @Column(name = "lti_version", length = 32)
    private String ltiVersion;

    @Override
    public String getId() {
        return siteId;
    }
}
