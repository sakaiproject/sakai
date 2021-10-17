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
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_SETTINGS", indexes = @Index(columnList = "SITE_ID", unique = true))
@Getter
@Setter
public class Settings implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "ALLOW_BOOKMARKING")
    private Boolean allowBookmarking = Boolean.TRUE;

    @Column(name = "ALLOW_REACTIONS")
    private Boolean allowReactions = Boolean.TRUE;

    @Column(name = "ALLOW_UPVOTING")
    private Boolean allowUpvoting = Boolean.FALSE;

    @Column(name = "ALLOW_ANON_POSTING")
    private Boolean allowAnonPosting = Boolean.TRUE;

    @Column(name = "ALLOW_PINNING")
    private Boolean allowPinning = Boolean.TRUE;

    @Column(name = "SITE_LOCKED")
    private Boolean siteLocked = Boolean.FALSE;

    @Column(name = "REQUIRE_GUIDELINES_AGREEMENT")
    private Boolean requireGuidelinesAgreement = Boolean.FALSE;

    @Lob
    @Column(name = "GUIDELINES")
    private String guidelines = "";

    @Column(name = "DEFAULT_TOPIC_TYPE", length = 32)
    @Enumerated(EnumType.STRING)
    private TopicType defaultTopicType = TopicType.QUESTION;

    public Settings() {}

    public Settings(String siteId) {
        this.siteId = siteId;
    }
}
