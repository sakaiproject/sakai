/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.springframework.data.PersistableEntity;

@Getter
@Setter
@Entity
@Table(name = "POLL_VOTE")
public class Vote implements PersistableEntity<Long> {

    @Id
    @SequenceGenerator(name = "poll_vote_id_sequence", sequenceName = "POLL_VOTE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "poll_vote_id_sequence")
    @Column(name = "VOTE_ID")
    private Long id;

    @Column(name = "USER_ID", nullable = false, length = 99)
    private String userId;

    @Column(name = "VOTE_IP", nullable = false, length = 99)
    private String ip;

    /**
     * Foreign key column for {@code VOTE_POLL_ID}. Persist changes by setting this identifier directly; the
     * {@link #poll} relationship is maintained by JPA but is not writeable.
     */
    @Column(name = "VOTE_POLL_ID", nullable = false)
    private Long pollId;

    /**
     * Read-only relationship for navigating to the parent poll. Refresh or reload the entity to populate this field;
     * it is not persisted because {@link #pollId} controls database writes.
     */
    @ManyToOne
    @JoinColumn(name = "VOTE_POLL_ID", nullable = false, insertable = false, updatable = false)
    private Poll poll;

    @Column(name = "VOTE_DATE", nullable = false)
    private Instant voteDate;

    /**
     * Foreign key column for {@code VOTE_OPTION}. Set this identifier to persist the selected option; the
     * {@link #option} relationship is read-only.
     */
    @Column(name = "VOTE_OPTION", nullable = false)
    private Long pollOption;

    /**
     * Read-only relationship for the selected {@link Option}. JPA populates this when the entity is reloaded; writes
     * must go through {@link #pollOption}.
     */
    @ManyToOne
    @JoinColumn(name = "VOTE_OPTION", insertable = false, updatable = false)
    private Option option;

    @Column(name = "VOTE_SUBMISSION_ID", nullable = false, length = 99)
    private String submissionId;

    public Vote() {
        // JPA requires a no-arg constructor
    }

    public Vote(Poll poll, Option option, String subId, Instant voteDate, String userId, String ip) {
        this.pollId = poll.getPollId();
        this.pollOption = option.getOptionId();
        this.poll = poll;
        this.option = option;
        this.submissionId = subId;
        this.voteDate = voteDate;
        this.userId = userId;
        this.ip = ip;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String toString() {
        return this.pollId + ":" + this.userId + ":" + this.ip + ":" + this.pollOption;
    }

}
