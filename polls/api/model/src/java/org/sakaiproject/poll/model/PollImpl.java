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
import java.util.List;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.sakaiproject.entity.api.Entity;

import org.apache.commons.lang.builder.ToStringBuilder;

public class PollImpl implements Poll {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String owner;
	private String siteId;
	private Date creationDate;
	private String pollText;
	private String description;
	
	private int minOptions = 1;
	private int maxOptions = 1;
	
	private Date voteOpen;
	private Date voteClose;
	
	private List options;
	private List votes;
	
	private String displayResult = "open";
	private boolean limitVoting = true;
	
	
	
	public PollImpl() {
		//set the defaults
		this.pollText = "";
		this.description = "";
		this.minOptions = 1;
		this.maxOptions = 1;
		this.limitVoting = true;
		this.voteOpen = new Date();
		this.voteClose = new Date(voteOpen.getTime() + (long)(7*24*60*60*100));
		this.displayResult = "open";
	}
	
	public Long getPollId() {
		return id;
	}

	public void setPollId(Long id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getText() {
		return pollText;
	}

	public void setText(String poll) {
		this.pollText = poll;
	}
	
	
	public void setMinOptions(int value) {
		this.minOptions = value;
	}
	
	public int getMinOptions() {
		return this.minOptions;
	}
	
	public void setMaxOptions(int value) {
		this.maxOptions = value;
	}
	
	public int getMaxOptions() {
		return this.maxOptions;
	}
	
	public void setVoteOpen(Date value) {
		this.voteOpen = value;
	}
	
	public Date getVoteOpen() {
		return this.voteOpen;
	}

	public void setVoteClose(Date value) {
		this.voteClose = value;
	}
	
	public Date getVoteClose() {
		return this.voteClose;
	}
	
	
	public void setDisplayResult(String value) {
		this.displayResult = value;
	}
	
	public String getDisplayResult() {
		return this.displayResult;
	}
	/**
	 * Options and operators for votes and options
	 */
	
	public void setVotes(List value) {
		this.votes = value;
	}
	
	public List getVotes() {
		return this.votes;
	}
	
	public void addVote(Vote vote) {
		votes.add(vote);
		
	}
	
	public void setOptions(List value) {
		this.options = value;
	}
	
	public List getPollOptions(){
		return this.options;
	}
	
	public void addOption(Option option) {
		this.options.add(option);
		
	}
	
	public void setLimitVoting(boolean value){
		this.limitVoting = value;
	}
	
	public boolean getLimitVoting(){
		return this.limitVoting;
		
	}
	
	
	
	public void setDetails(String value){
		this.description = value;
	}
	public String getDetails(){
		return this.description;
	}
	
	/*
	 * Basic comparison functions for objects
	 * Uses commons-lang to make it so we can be sure about comparisons as long
	 * as the data in the object is the same
	 */


	public String toString() {
		return new ToStringBuilder(this)
			.append(this.id)
			.append(this.owner)
			.append(this.siteId)
			.append(this.creationDate)
			.append(this.pollText)
			.toString();
	}

	/*
	 * Entity Methods 
	 */
	public String getUrl() {
		return ServerConfigurationService.getAccessUrl() + "/poll/" + this.getId();
	}

	public String getReference() {

		return ServerConfigurationService.getAccessUrl() + "/poll/" + Entity.SEPARATOR + this.getId();
	}

	public String getUrl(String arg0) {
		
		return getUrl();
	}

	public String getReference(String arg0) {
		
		return getReference();
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}


}
