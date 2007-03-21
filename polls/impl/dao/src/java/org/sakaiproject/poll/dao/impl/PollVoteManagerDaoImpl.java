package org.sakaiproject.poll.dao.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Query;
import org.hibernate.criterion.Distinct;
import org.hibernate.Session;

import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.cover.ToolManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class PollVoteManagerDaoImpl extends HibernateDaoSupport implements PollVoteManager {

//	 use commons logger
	private static Log log = LogFactory.getLog(PollListManagerDaoImpl.class);
	
	
	public boolean saveVote(Vote vote)  {
		try {
			getHibernateTemplate().save(vote);
			
		} catch (DataAccessException e) {
			log.error("Hibernate could not save: " + e.toString());
			e.printStackTrace();
			return false;
		}
		//Session sess = ;
		//we need the siteID
		
		log.info(" Vote  " + vote.getId() + " successfuly saved");
		EventTrackingService.post(EventTrackingService.newEvent("poll.vote", "poll/site/" + ToolManager.getCurrentPlacement() +"/poll/" +  vote.getPollId(), false));
		return true;
	}

	public void deleteVote(Vote Vote) {
		// TODO Auto-generated method stub

	}

	public List getAllVotesForPoll(Poll poll) {
		DetachedCriteria d = DetachedCriteria.forClass(Vote.class)
		.add( Restrictions.eq("pollId",poll.getPollId()) );
		Collection pollCollection = getHibernateTemplate().findByCriteria(d);
		List votes = new ArrayList();
		for (Iterator tit = pollCollection.iterator(); tit.hasNext();) {
			Vote vote = (Vote) tit.next();
			votes.add(vote);
		}
		return votes;
	}
	
	public int getDisctinctVotersForPoll(Poll poll) {
		
		Query q = null;
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		String statement = "SELECT DISTINCT VOTE_SUBMISSION_ID from POLL_VOTE where VOTE_POLL_ID = " + poll.getPollId().toString();
		q = session.createSQLQuery(statement);
		List results = q.list();
		if (results.size()>0)
			return results.size();
		
		return 0; 
	}

	public boolean userHasVoted(Long pollid, String userID) {
		DetachedCriteria d = DetachedCriteria.forClass(Vote.class)
		.add( Restrictions.eq("userId",userID) )
		.add( Restrictions.eq("pollId",pollid) );
		
	Collection pollCollection = getHibernateTemplate().findByCriteria(d);		
	//System.out.println("got " + pollCollection.size() + "votes for this poll");
	if (pollCollection.size()>0)
		return true;
	else
		return false;
	}

	public boolean userHasVoted(Long pollId) {
		
		return userHasVoted(pollId, UserDirectoryService.getCurrentUser().getId());
	}

	

}
