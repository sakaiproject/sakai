/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.poll.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;

public interface PollDao {

    // Persistence
    void save(Object entity);
    void delete(Object entity);
    void deleteSet(Collection<?> entities);

    // Polls
    List<Poll> findAllPolls();
    List<Poll> findPollsBySite(String siteId, boolean creationDateAsc);
    List<Poll> findPollsForSites(String[] siteIds, boolean creationDateAsc);
    List<Poll> findOpenPollsForSites(String[] siteIds, Date now, boolean creationDateAsc);
    Poll findPollById(Long pollId);
    Poll findPollByUuid(String uuid);

    // Options
    List<Option> findOptionsByPollId(Long pollId);
    Option findOptionById(Long optionId);

    // Votes
    List<Vote> findVotesByPollId(Long pollId);
    List<Vote> findVotesByPollIdAndOption(Long pollId, Long optionId);
    List<Vote> findVotesByUserAndPollIds(String userId, Long[] pollIds);
    List<Vote> findVotesByUserAndPollId(String userId, Long pollId);
    Vote findVoteById(Long voteId);

    // Aggregates
    int getDisctinctVotersForPoll(Poll poll);
}
