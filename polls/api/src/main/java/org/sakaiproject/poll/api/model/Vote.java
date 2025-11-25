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

package org.sakaiproject.poll.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sakaiproject.springframework.data.PersistableEntity;

@Data
@Entity
@Table(name = "POLL_VOTE")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vote implements PersistableEntity<Long> {

    @Id
    @SequenceGenerator(name = "poll_vote_id_sequence", sequenceName = "POLL_VOTE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "poll_vote_id_sequence")
    @Column(name = "VOTE_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "USER_ID", nullable = false, length = 99)
    private String userId;

    @Column(name = "VOTE_IP", nullable = false, length = 99)
    private String ip;

    @Column(name = "VOTE_DATE", nullable = false)
    private Instant voteDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VOTE_OPTION", nullable = false)
    @ToString.Exclude
    private Option option;

    @Column(name = "VOTE_SUBMISSION_ID", nullable = false, length = 99)
    private String submissionId;

    public Vote(Option option, String subId, Instant voteDate, String userId, String ip) {
        this.option = option;
        this.submissionId = subId;
        this.voteDate = voteDate;
        this.userId = userId;
        this.ip = ip;
    }
}
