/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006,2007 The Sakai Foundation.
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

public interface Vote {

	
	public void setId(Long id);
	
	public Long getId();
	
	public void setUserId(String id);
	
	public String getUserId();
	
	public void setIp(String ip);
	
	public String getIp();
	
	public void setVoteDate(Date date);
	public Date getVoteDate();
	
	public void setPollOption(Long option);
	public Long getPollOption();
	
	public void setPollId(Long pollId);
	public Long getPollId();
	
	public void setSubmissionId(String id);
	public String getSubmissionId();
	
}
