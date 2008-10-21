/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


public class PollVoteManagerDaoImpl extends HibernateDaoSupport implements PollVoteManager {

//  use commons logger
    private static Log log = LogFactory.getLog(PollListManagerDaoImpl.class);

    private EventTrackingService eventTrackingService;
    public void setEventTrackingService(EventTrackingService ets) {
        eventTrackingService = ets;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService ss) {
        securityService = ss;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager tm) {
        toolManager = tm;
    }


    public void saveVoteList(List<Vote> votes) {
        Long pollId = null;
        for (int i =0; i < votes.size(); i ++) {
            Vote vote = (Vote)votes.get(i);
            pollId = vote.getPollId();
            saveVote(vote);
        }
        eventTrackingService.post(eventTrackingService.newEvent("poll.vote", "poll/site/" + toolManager.getCurrentPlacement().getContext() +"/poll/" +  pollId, true));
    }

    public boolean saveVote(Vote vote)  {
        try {
            getHibernateTemplate().save(vote);

        } catch (DataAccessException e) {
            log.error("Hibernate could not save: " + e.toString());
            e.printStackTrace();
            return false;
        }

        log.debug(" Vote  " + vote.getId() + " successfuly saved");
        return true;
    }

    public List<Vote> getAllVotesForPoll(Poll poll) {
        DetachedCriteria d = DetachedCriteria.forClass(Vote.class)
        .add( Restrictions.eq("pollId",poll.getPollId()) );
        List<Vote> votes = getHibernateTemplate().findByCriteria(d);
        return votes;
    }

    public Map<Long, List<Vote>> getVotesForUser(String userId, Long[] pollIds) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        DetachedCriteria d = DetachedCriteria.forClass(Vote.class);
        d.add( Restrictions.eq("userId",userId) );
        if (pollIds != null) {
            if (pollIds.length > 0) {
                d.add( Restrictions.in("pollId",pollIds) );
            } else {
                // no polls to search so EXIT here
                return new HashMap<Long, List<Vote>>();
            }
        }
        Map<Long, List<Vote>> map = new HashMap<Long, List<Vote>>();
        if (pollIds != null && pollIds.length > 0) {
            List<Vote> votes = getHibernateTemplate().findByCriteria(d);
            // put the list of votes into a map
            for (Vote vote : votes) {
                Long pollId = vote.getPollId();
                if (! map.containsKey(pollId)) {
                    map.put(pollId, new ArrayList<Vote>() );
                }
                map.get(pollId).add(vote);
            }
        }
        return map;
    }

    public int getDisctinctVotersForPoll(Poll poll) {

        Query q = null;
        Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
        String statement = "SELECT DISTINCT VOTE_SUBMISSION_ID from POLL_VOTE where VOTE_POLL_ID = " + poll.getPollId().toString();
        q = session.createSQLQuery(statement);
        List results = q.list();
        if (results.size() > 0)
            return results.size();

        return 0; 
    }

    public boolean userHasVoted(Long pollid, String userID) {
        DetachedCriteria d = DetachedCriteria.forClass(Vote.class)
        .add( Restrictions.eq("userId",userID) )
        .add( Restrictions.eq("pollId",pollid) );

        List<Vote> votes = getHibernateTemplate().findByCriteria(d);		
        //System.out.println("got " + pollCollection.size() + "votes for this poll");
        if (votes.size() > 0)
            return true;
        else
            return false;
    }

    public boolean userHasVoted(Long pollId) {

        return userHasVoted(pollId, UserDirectoryService.getCurrentUser().getId());
    }

    public Vote getVoteById(Long voteId) {
        if (voteId == null) {
            throw new IllegalArgumentException("voteId cannot be null when getting vote");
        }
        Vote vote = (Vote) getHibernateTemplate().get(Vote.class, voteId);
        return vote;
    }

    public boolean isUserAllowedVote(String userId, Long pollId, boolean ignoreVoted) {
        boolean allowed = false;
        Poll poll = (Poll) getHibernateTemplate().get(Poll.class, pollId);
        if (poll == null) {
            throw new IllegalArgumentException("Invalid poll id ("+pollId+") when checking user can vote");
        }
        if (securityService.isSuperUser(userId)) {
            allowed = true;
        } else {
            String siteRef = "/site/" + poll.getSiteId();
            if (securityService.unlock(userId, PollListManager.PERMISSION_VOTE, siteRef)) {
                if (ignoreVoted) {
                    allowed = true;
                } else {
                    Map<Long, List<Vote>> m = getVotesForUser(userId, new Long[] {pollId});
                    if (m.isEmpty()) {
                        allowed = true;
                    }
                }
            }
        }
        return allowed;
    }

}
