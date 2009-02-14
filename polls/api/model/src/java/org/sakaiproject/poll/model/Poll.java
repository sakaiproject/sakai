/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.poll.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Poll implements Entity  {

    private static final long serialVersionUID = 2L;
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

    private List<Option> options;
    private List<Vote> votes;

    private String displayResult = "open";
    private boolean limitVoting = true;

    private boolean currentUserVoted = false;
    private List<Vote> currentUserVotes = null;

    private String entityID;

    public Poll() {
        //set the defaults
        this.pollText = "";
        this.description = "";
        this.minOptions = 1;
        this.maxOptions = 1;
        this.limitVoting = true;
        this.voteOpen = new Date();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        this.voteClose = cal.getTime();
        this.displayResult = "open";

		this.options = new ArrayList();
		this.votes = new ArrayList();
    }

    /**
     * Get the id of the poll
     * @return the polls ID
     */
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

    /**
     * Get the minimum number of  options that must be selected to vote 
     * @return
     */
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

    public String getPollText() {
        return pollText;
    }

    public void setPollText(String pollText) {
        this.pollText = pollText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCurrentUserVoted() {
        return currentUserVoted;
    }

    public void setCurrentUserVoted(boolean currentUserVoted) {
        this.currentUserVoted = currentUserVoted;
    }
    
    public List<Vote> getCurrentUserVotes() {
        if (currentUserVotes == null) {
            return new ArrayList<Vote>();
        }
        return currentUserVotes;
    }
    
    public void setCurrentUserVotes(List<Vote> currentUserVotes) {
        this.currentUserVotes = currentUserVotes;
    }

    /**
     * Set when to display the results
     * 
     * @param display
     * 	String which can be:
     * 	open - can be viewd at any time
     * 	never - not diplayed
     * 	afterVoting - after user has voted
     * 	afterClosing - once the vote has closed
     */
    public void setDisplayResult(String value) {
        this.displayResult = value;
    }

    public String getDisplayResult() {
        return this.displayResult;
    }
    /**
     * Options and operators for votes and options
     */

    /**
     * Set the votes list for this poll
     * @param votes
     */
    public void setVotes(List<Vote> value) {
        this.votes = value;
    }

    public List<Vote> getVotes() {
        return this.votes;
    }

    /**
     * Attach a vote to the list of votes for this poll
     * @param vote
     */
    public void addVote(Vote vote) {
        votes.add(vote);

    }

    public void setOptions(List<Option> value) {
        this.options = value;
    }

    public List<Option> getPollOptions(){
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
        if (entityID == null) {
            entityID = id + "";
        }
        return entityID;
    }


    public void setId(String s) {
        entityID = s;
    }

    public ResourceProperties getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public Element toXml(Document doc, Stack stack) {
        Element poll = doc.createElement("poll");

        if (stack.isEmpty())
        {
            doc.appendChild(poll);
        }
        else
        {
            ((Element) stack.peek()).appendChild(poll);
        }

        stack.push(poll);

        poll.setAttribute("id", getId());
        poll.setAttribute("pollid", getPollId().toString());
        poll.setAttribute("title", getText());

        if (description != null) poll.setAttribute("description", description);

        poll.setAttribute("open-time", this.voteOpen.toString());
        poll.setAttribute("close-time", this.voteClose.toString());
        poll.setAttribute("limit-voting", Boolean.valueOf(limitVoting).toString());
        // properties
        //getProperties().toXml(doc, stack);
        //apppend the options as chidren


        stack.pop();

        return poll;
    }
}
