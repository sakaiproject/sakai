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

import java.util.Date;

import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.cover.SessionManager;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.sakaiproject.springframework.data.PersistableEntity;

@Data
@Entity
@Table(name = "POLL_VOTE", indexes = {
        @Index(name = "POLLTOOL_VOTE_USERID_IDX", columnList = "USER_ID"),
        @Index(name = "POLLTOOL_VOTE_POLLID_IDX", columnList = "VOTE_POLL_ID")
})
public class Vote implements PersistableEntity<Long> {

    @Id
    @Column(name = "VOTE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "poll_vote_sequence")
    @SequenceGenerator(name = "poll_vote_sequence", sequenceName = "POLL_VOTE_ID_SEQ")
    private Long id;

    @Column(name = "USER_ID", length = 255, nullable = false)
    private String userId;

    @Column(name = "VOTE_IP", length = 255, nullable = false)
    private String ip;

    @Column(name = "VOTE_POLL_ID", nullable = false)
    private Long pollId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "VOTE_DATE", nullable = false)
    private Date voteDate;

    @Column(name = "VOTE_OPTION")
    private Long pollOption;

    @Column(name = "VOTE_SUBMISSION_ID", length = 255, nullable = false)
    private String submissionId;

    public Vote() {
        // needed by hibernate
    }

    public Vote(Poll poll, Option option, String subId) {
        this.pollId = poll.getPollId();
        this.pollOption = option.getOptionId();
        this.submissionId = subId;

        // the date can default to now
        voteDate = new Date();

        // TODO move this stuff to the service
        // user is current user
        userId = SessionManager.getCurrentSessionUserId();
        // set the Ip to the current sessions IP
        UsageSession usageSession = UsageSessionService.getSession();
        if (usageSession != null) {
            ip = usageSession.getIpAddress();
        }
    }

    public String toString() {
        return this.pollId + ":" + this.userId + ":" + this.ip + ":" + this.pollOption;
    }

    @Override
    public Long getId() {
        return id;
    }
}
