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
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_REACTION_TOTALS",
    indexes = { @Index(name = "conv_topic_reaction_totals_topic_idx", columnList = "TOPIC_ID") },
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicReactionTotals", columnNames = { "TOPIC_ID", "REACTION" }) })
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TopicReactionTotal implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @EqualsAndHashCode.Include
    @JoinColumn(name = "TOPIC_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ConversationsTopic topic;

    @EqualsAndHashCode.Include
    @Column(name = "REACTION", nullable = false)
    private Reaction reaction;

    @Column(name = "TOTAL", nullable = false)
    private Integer total;
}
