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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_STATUS",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicStatus", columnNames = { "TOPIC_ID", "USER_ID" }) },
    indexes = { @Index(name = "conv_topic_status_topic_user_idx", columnList = "TOPIC_ID, USER_ID") })
@Getter
@Setter
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class TopicStatus implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "conv_topic_status_id_sequence")
    @SequenceGenerator(name = "conv_topic_status_id_sequence", sequenceName = "CONV_TOPIC_STATUS_S")
    private Long id;

    @EqualsAndHashCode.Include
    @JoinColumn(name = "TOPIC_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ConversationsTopic topic;

    @EqualsAndHashCode.Include
    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "BOOKMARKED")
    private Boolean bookmarked = Boolean.FALSE;

    @Column(name = "UNREAD")
    private Integer unread = 0;

    @Column(name = "POSTED")
    private Boolean posted = Boolean.FALSE;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    @Column(name = "UPVOTED")
    private Boolean upvoted = Boolean.FALSE;

    public TopicStatus(ConversationsTopic topic, String userId) {
        this.topic = topic;
        this.userId = userId;
        log.debug("NEW TopicStatus, topic={}, userId={}", topic.getId(), userId);
    }
}
