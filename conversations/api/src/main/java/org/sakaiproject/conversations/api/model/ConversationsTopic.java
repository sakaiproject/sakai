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

import java.util.HashSet;
import java.util.Set;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPICS", indexes = {
                        @Index(name = "conv_topics_site_idx", columnList = "SITE_ID"),
                        @Index(name = "conv_topics_site_creator_idx", columnList = "SITE_ID, CREATOR")
                    }
)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConversationsTopic implements PersistableEntity<String> {

    @Id
    @Column(name = "TOPIC_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "ABOUT_REFERENCE", length = 255)
    private String aboutReference = "";

    @Column(name = "TITLE", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "MESSAGE")
    private String message = "";

    @ElementCollection
    @CollectionTable(name = "CONV_TOPIC_TAGS",
                     joinColumns = @JoinColumn(name = "TOPIC_ID"),
                     uniqueConstraints = @UniqueConstraint(name="UK_TOPIC_TAG", columnNames = {"TOPIC_ID", "TAG"}))
    @Column(name = "TAG")
    private Set<Long> tagIds = new HashSet<>();

    @Column(name = "TOPIC_TYPE", length = 32)
    @Enumerated(EnumType.STRING)
    private TopicType type = TopicType.QUESTION;

    @Column(name = "RESOLVED")
    private Boolean resolved = Boolean.FALSE;

    @Column(name = "HOW_ACTIVE")
    private Integer howActive = 0;

    @Column(name = "LAST_ACTIVITY")
    private Instant lastActivity;

    @Column(name = "PINNED")
    private Boolean pinned = Boolean.FALSE;

    @Column(name = "DRAFT")
    private Boolean draft = Boolean.FALSE;

    // This is our soft delete type flag
    @Column(name = "HIDDEN")
    private Boolean hidden = Boolean.FALSE;

    @Column(name = "LOCKED")
    private Boolean locked = Boolean.FALSE;

    @Column(name = "MUST_POST_BEFORE_VIEWING")
    private Boolean mustPostBeforeViewing = Boolean.FALSE;

    @Column(name = "ANONYMOUS")
    private Boolean anonymous = Boolean.FALSE;

    @Column(name = "ALLOW_ANONYMOUS_POSTS")
    private Boolean allowAnonymousPosts = Boolean.FALSE;

    @ElementCollection
    @CollectionTable(name = "CONV_TOPIC_GROUPS", joinColumns = @JoinColumn(name = "TOPIC_ID"))
    @Column(name = "GROUP_ID")
    private Set<String> groups = new HashSet<>();

    @Column(name = "VISIBILITY", length = 32)
    @Enumerated(EnumType.STRING)
    private TopicVisibility visibility = TopicVisibility.SITE;

    @Column(name = "SHOW_DATE")
    private Instant showDate;

    @Column(name = "HIDE_DATE")
    private Instant hideDate;

    @Column(name = "LOCK_DATE")
    private Instant lockDate;

    @Column(name = "DUE_DATE")
    private Instant dueDate;

    @Column(name = "DUE_DATE_CALENDAR_EVENT_ID", length = 36)
    private String dueDateCalendarEventId;

    @Column(name = "SHOW_MESSAGE_SCHEDULE_ID", length = 36)
    private String showMessageScheduleId;

    @Column(name = "UPVOTES")
    private Integer upvotes = 0;

    @Column(name = "GRADED")
    private Boolean graded = Boolean.FALSE;

    @Column(name = "GRADING_ITEM_ID")
    private Long gradingItemId;

    @Embedded
    private Metadata metadata;
}
