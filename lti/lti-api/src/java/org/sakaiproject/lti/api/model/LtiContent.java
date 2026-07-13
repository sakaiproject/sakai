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

import org.hibernate.annotations.ColumnDefault;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lti_content")
@Data
@NoArgsConstructor
public class LtiContent implements PersistableEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "lti_content_sequence")
    @SequenceGenerator(name = "lti_content_sequence", sequenceName = "lti_content_S")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private LtiTool tool;

    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "title", length = 1024)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "frameheight")
    private Integer frameheight;

    @Column(name = "newpage")
    @ColumnDefault("0")
    private Integer newpage = 0;

    @Column(name = "protect")
    @ColumnDefault("0")
    private Integer protect = 0;

    @Column(name = "debug")
    @ColumnDefault("0")
    private Integer debug = 0;

    @Lob
    @Column(name = "custom")
    private String custom;

    @Column(name = "launch", length = 1024)
    private String launch;

    @Lob
    @Column(name = "xmlimport")
    private String xmlimport;

    @Lob
    @Column(name = "settings")
    private String settings;

    @Lob
    @Column(name = "contentitem")
    private String contentitem;

    @Column(name = "placement", length = 256)
    private String placement;

    @Column(name = "placementsecret", length = 512)
    private String placementsecret;

    @Column(name = "oldplacementsecret", length = 512)
    private String oldplacementsecret;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
