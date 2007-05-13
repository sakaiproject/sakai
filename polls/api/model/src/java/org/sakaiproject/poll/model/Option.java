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



public class Option {

	
	private Long pollId;
	private Long id;
	private String text;
	private String status;
	
	public Option(){
		
	}	
	public Option(Long oId){
		this.id = oId;
	}
	
	public void setId(Long value) {
		// TODO Auto-generated method stub
		id = value;
	}

	public Long getId() {
		
		return id;
	}

	public void setOptionText(String option) {
		
		text = option;

	}

	public String getOptionText() {
		
		return text;
	}

	public Long getPollId(){
		return pollId;
	}
	
	public void setPollId(Long pollid) {
		this.pollId = pollid;
	}
	
	public void setStatus(String s) {
		this.status = s;
	}
	
	public String getStatus() {
		return this.status;
	}
}
