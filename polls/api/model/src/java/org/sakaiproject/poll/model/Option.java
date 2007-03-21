/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/uct/PollTool/trunk/api/model/src/java/org/sakaiproject/poll/model/Option.java $
 * $Id: Option.java 3733 2007-03-01 14:55:42Z david.horwitz@uct.ac.za $
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

public interface Option {

	
	public void setId(Long getId);
	
	public Long getId();
	
	public void setOptionText(String option);
	
	public String getOptionText();
	
	public Long getPollId();
	public void setPollId(Long pollId);
	
	public void setStatus(String status);
	public String getStatus();
}
