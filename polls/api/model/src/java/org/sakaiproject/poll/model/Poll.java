/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/uct/PollTool/trunk/api/model/src/java/org/sakaiproject/poll/model/Poll.java $
 * $Id: Poll.java 3733 2007-03-01 14:55:42Z david.horwitz@uct.ac.za $
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

package org.sakaiproject.poll.model;

import java.util.Date;
import java.util.List;
import org.sakaiproject.entity.api.Entity;

/**
 * @author az
 * This is the interface for our value object that holds a task
 * It is a basic POJO with only getters and setters
 */
public interface Poll extends java.io.Serializable, Entity {

	/**
	 * Get the id of the poll
	 * @return the polls ID
	 */
	public Long getPollId();
	
	public void setPollId(Long id);
	
	public String getOwner();
	
	public void setOwner(String owner);
	
	public void setText(String text);
	public String getText();
	
	public String getSiteId();
	
	public void setSiteId(String siteId);
	
	public Date getCreationDate();
	
	public void setCreationDate(Date creationDate);
	
	public List getPollOptions();
	
	public void setOptions(List options);
	
	public void addOption(Option option);
	
	public void setVoteOpen(Date date);
	
	public Date getVoteOpen();
	
	public void setVoteClose(Date Date);
	
	public Date getVoteClose();
	
	/**
	 * Set when to diplay the results
	 * 
	 * @param display
	 * 	String which can be:
	 * 	open - can be viewd at any time
	 * 	never - not diplayed
	 * 	afterVoting - after user has voted
	 * 	afterClosing - once the vote has closed
	 */
	public void setDisplayResult(String display);
	
	public String getDisplayResult();
	
	public void setLimitVoting (boolean limit);
	public boolean getLimitVoting();
	
	public String getDetails();
	public void setDetails(String details);
	
	/**
	 * Get the minimum number of  options that must be selected to vote 
	 * @return
	 */
	public void setMinOptions(int minVotes);
	
	public int getMinOptions();
	
	public void setMaxOptions(int minVotes);
	
	public int getMaxOptions();
	
	
	//need getters and setters to create votes
	
	public List getVotes();
	/**
	 * Set the votes list for this poll
	 * @param votes
	 */
	public void setVotes(List votes);
	
	/**
	 * Attach a vote to the list of votes for this poll
	 * @param vote
	 */
	public void addVote(Vote vote);
}
