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
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_STATUS",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicStatus", columnNames = { "TOPIC_ID", "USER_ID" }) },
    indexes = { @Index(columnList = "TOPIC_ID, USER_ID") })
@Getter
@Setter
public class TopicStatus implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "SITE_ID", nullable = false)
    private String siteId;

    @Column(name = "TOPIC_ID", nullable = false)
    private String topicId;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "BOOKMARKED")
    private Boolean bookmarked = Boolean.FALSE;

    @Column(name = "UNREAD")
    private Integer unread = 0;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    public TopicStatus() {
    }

    public TopicStatus(String siteId, String topicId, String userId) {

        this.siteId = siteId;
        this.topicId = topicId;
        this.userId = userId;
    }
}
