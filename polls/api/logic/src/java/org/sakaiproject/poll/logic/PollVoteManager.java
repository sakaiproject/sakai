/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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


package org.sakaiproject.poll.logic;

import java.util.List;

import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;

public interface PollVoteManager {

	
	public boolean saveVote(Vote vote);
	
	public void deleteVote(Vote Vote);
	
	public List getAllVotesForPoll(Poll poll);
	
	
	public boolean userHasVoted(Long pollid, String userID);
	
	/**
	 * Assumes current user
	 * @param pollid
	 * @return
	 */
	public boolean userHasVoted(Long pollid);
	
	public int getDisctinctVotersForPoll(Poll poll);
	
}
