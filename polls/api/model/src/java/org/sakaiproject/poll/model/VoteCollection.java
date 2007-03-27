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

import org.sakaiproject.entity.api.Entity;
import java.util.List;

public interface VoteCollection {

	
	public void setId(String value);
	
	public String getId();
	
	public void setVotes(List votes);
	
	public List getVotes();
	
	
	public void setPollId (Long pid);
	public Long getPollId();

	public void setOption(String s);
	public String getOption();
	
	public void setOptionsSelected(String[] s);
	public String[] getOptionsSelected();
	
	public void setSubmissionStatus(String s);
	public String getSubmissionStatus();
	
}
