/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/uct/PollTool/trunk/api/logic/src/java/org/sakaiproject/poll/logic/PollVoteManager.java $
 * $Id: PollVoteManager.java 3783 2007-03-05 06:04:22Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
