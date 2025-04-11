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
package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TAGS", indexes = { @Index(name = "conv_tags_site_idx", columnList = "SITE_ID") })
@Getter
@Setter
public class Tag implements PersistableEntity<Long> {

    @Id
    @Column(name = "TAG_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "conv_tags_id_sequence")
    @SequenceGenerator(name = "conv_tags_id_sequence", sequenceName = "CONV_TAGS_S")
    private Long id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LABEL", nullable = false)
    private String label;
}
