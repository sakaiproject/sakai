package org.sakaiproject.poll.dao;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.poll.model.Poll;

public interface PollDao extends GeneralGenericDao {
	
	/**
	 * Get the number of distinct voters on a poll
	 * @param poll
	 * @return
	 */
	 public int getDisctinctVotersForPoll(Poll poll);

}
