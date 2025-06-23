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
 * Simplified JPA mapping of the LTI tool model.
 */
@Entity
@Table(name = "LTI_TOOLS")
@Getter
@Setter
public class LtiTool implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "lti_tools_id_sequence")
    @SequenceGenerator(name = "lti_tools_id_sequence", sequenceName = "LTI_TOOLS_S")
    private Long id;

    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "TITLE", length = 1024, nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", length = 4096)
    private String description;

    @Column(name = "LAUNCH", length = 1024, nullable = false)
    private String launch;

    @Column(name = "VISIBLE")
    private Integer visible;

    @Column(name = "CONSUMERKEY", length = 1024)
    private String consumerKey;

    @Column(name = "SECRET", length = 1024)
    private String secret;

    @Column(name = "FRAMEHEIGHT")
    private Integer frameHeight;

    @Column(name = "ALLOWOUTCOMES")
    private Boolean allowOutcomes;

    @Column(name = "ALLOWLINEITEMS")
    private Boolean allowLineitems;

    @Column(name = "ALLOWROSTER")
    private Boolean allowRoster;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Type(type = "org.hibernate.type.InstantType")
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
}