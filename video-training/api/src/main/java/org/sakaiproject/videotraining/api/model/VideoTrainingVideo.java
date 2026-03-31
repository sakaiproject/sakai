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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.springframework.data.PersistableEntity;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;

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
@Table(name = "vtm_video")
public class VideoTrainingVideo implements PersistableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 99)
    private String siteId;

    @Column(nullable = false, length = 99)
    private String ownerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column
    private boolean inheritTitleMetadata;

    @Column
    private boolean inheritDescriptionMetadata;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VideoProviderType providerType = VideoProviderType.NATIVE;

    @Column(nullable = false, length = 1024)
    private String sourceReference;

    @Column(nullable = false)
    private boolean sourceDeleted;

    @Column
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VideoVisibilityScope visibilityScope = VideoVisibilityScope.COURSE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VideoPublicationStatus publicationStatus = VideoPublicationStatus.DRAFT;

    @Column
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant releaseDate;

    @Column
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant retractDate;

    @Column(nullable = false, length = 99)
    private String requiredViewPermission = VideoTrainingConstants.PERMISSION_VIEW;

    @Column(nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant createdOn = Instant.now();

    @Column(nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant modifiedOn = Instant.now();

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
        name = "vtm_video_category",
        joinColumns = @JoinColumn(name = "video_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<VideoTrainingCategory> categories = new HashSet<>();

    public VideoTrainingVideo(
        String siteId,
        String ownerId,
        String title,
        boolean inheritTitleMetadata,
        boolean inheritDescriptionMetadata,
        String description,
        VideoProviderType providerType,
        String sourceReference,
        Long fileSizeBytes,
        VideoVisibilityScope visibilityScope,
        VideoPublicationStatus publicationStatus,
        Instant releaseDate,
        Instant retractDate,
        String requiredViewPermission
    ) {
        this.siteId = siteId;
        this.ownerId = ownerId;
        this.title = title;
        this.inheritTitleMetadata = inheritTitleMetadata;
        this.inheritDescriptionMetadata = inheritDescriptionMetadata;
        this.description = description;
        this.providerType = providerType;
        this.sourceReference = sourceReference;
        this.fileSizeBytes = fileSizeBytes;
        this.visibilityScope = visibilityScope;
        this.publicationStatus = publicationStatus;
        this.releaseDate = releaseDate;
        this.retractDate = retractDate;
        this.requiredViewPermission = requiredViewPermission;

        this.createdOn = Instant.now();
        this.modifiedOn = Instant.now();
    }
}
