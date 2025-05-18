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
package org.sakaiproject.lti.impl.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Simplified JPA mapping of the LTI tool site model.
 */
@Entity
@Table(name = "LTI_TOOL_SITE")
@Getter
@Setter
public class LtiToolSite implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "lti_tool_site_id_sequence")
    @SequenceGenerator(name = "lti_tool_site_id_sequence", sequenceName = "LTI_TOOL_SITE_S")
    private Long id;

    @Column(name = "TOOL_ID")
    private Long toolId;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "NOTES", length = 1024)
    private String notes;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
}
