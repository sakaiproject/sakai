/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "vtm_category")
public class VideoTrainingCategory implements PersistableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 99)
    private String siteId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 36)
    private String parentCategoryId;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant createdOn = Instant.now();

    @Column(length = 99)
    private String createdBy;

    @Column(nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant modifiedOn = Instant.now();

    @Column(length = 99)
    private String modifiedBy;

    @Transient
    private List<VideoTrainingCategory> children = new ArrayList<>();

    @Transient
    private Long videoCount = 0L;

    @Transient
    private boolean hasChildren;

    @ToString.Exclude
    @ManyToMany(mappedBy = "categories")
    private Set<VideoTrainingVideo> videos = new HashSet<>();

    public VideoTrainingCategory(String siteId, String name, String parentCategoryId, Integer sortOrder) {
        this.siteId = siteId;
        this.name = name;
        this.parentCategoryId = parentCategoryId;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
