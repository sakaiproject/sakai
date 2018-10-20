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

@Data
public class Vote {

    private Long id;
    private String userId;
    private String ip;
    private Long pollId;
    private Date voteDate;
    private Long pollOption;
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

}
