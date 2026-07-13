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
import java.time.Instant;
import javax.persistence.*;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lti_tool_site")
@Data
@NoArgsConstructor
public class LtiToolSite implements PersistableEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "lti_tool_site_sequence")
    @SequenceGenerator(name = "lti_tool_site_sequence", sequenceName = "lti_tool_site_S")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private LtiTool tool;

    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "notes", length = 1024)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deployment_group", length = 128)
    private String deploymentGroup;
}
