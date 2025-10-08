/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.springframework.data.PersistableEntity;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "POLL_POLL")
public class Poll implements PersistableEntity<Long> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POLL_ID")
    private Long pollId;

    @Column(name = "POLL_OWNER", nullable = false, length = 255)
    private String owner;

    @Column(name = "POLL_SITE_ID", nullable = false, length = 255)
    private String siteId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "POLL_CREATION_DATE", nullable = false)
    private Date creationDate;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "POLL_TEXT", nullable = false)
    private String text;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "POLL_DETAILS")
    private String description;

    @Column(name = "POLL_MIN_OPTIONS", nullable = false)
    private int minOptions = 1;

    @Column(name = "POLL_MAX_OPTIONS", nullable = false)
    private int maxOptions = 1;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "POLL_VOTE_OPEN", nullable = false)
    private Date voteOpen;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "POLL_VOTE_CLOSE", nullable = false)
    private Date voteClose;

    @Transient
    private List<Vote> votes = new ArrayList<>();

    @Column(name = "POLL_DISPLAY_RESULT", nullable = false, length = 255)
    private String displayResult = "open";

    @Column(name = "POLL_LIMIT_VOTE", nullable = false)
    private Boolean limitVoting = Boolean.TRUE;

    @Transient
    private boolean currentUserVoted = false;

    @Transient
    private List<Option> options = new ArrayList<>();

    @Column(name = "POLL_IS_PUBLIC", nullable = false)
    private Boolean isPublic = Boolean.FALSE;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "POLL_UUID", nullable = false, length = 255)
    private String uuid;

    public Poll() {
        this.text = "";
        this.description = "";
        this.minOptions = 1;
        this.maxOptions = 1;
        this.limitVoting = Boolean.TRUE;
        this.isPublic = Boolean.FALSE;
        this.voteOpen = new Date();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        this.voteClose = cal.getTime();
        this.displayResult = "open";
    }

    @Override
    public Long getId() {
        return pollId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setVoteOpenStr(String value) {
        try {
            Date parsedDate = DATE_FORMAT.parse(value);
            if (parsedDate != null) {
                voteOpen = parsedDate;
            }
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getVoteOpenStr() {
        return DATE_FORMAT.format(voteOpen);
    }

    public void setVoteCloseStr(String value) {
        try {
            Date parsedDate = DATE_FORMAT.parse(value);
            if (parsedDate != null) {
                voteClose = parsedDate;
            }
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getVoteCloseStr() {
        return DATE_FORMAT.format(voteClose);
    }

    /**
     * Attach a vote to the list of votes for this poll.
     *
     * @param vote the vote to add
     */
    public void addVote(Vote vote) {
        votes.add(vote);
    }

    public void addOption(Option option) {
        options.add(option);
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
        .append(this.uuid)
        .append(this.owner)
        .append(this.siteId)
        .append(this.creationDate)
        .append(this.text)
        .toString();
    }

    public String getUrl() {
        return ServerConfigurationService.getAccessUrl() + "/poll/" + this.getUuid();
    }

    public String getReference() {

        return ServerConfigurationService.getAccessUrl() + "/poll/" + org.sakaiproject.entity.api.Entity.SEPARATOR + this.getUuid();
    }

    public String getUrl(String arg0) {

        return getUrl();
    }

    public String getReference(String arg0) {

        return getReference();
    }

	public ResourceProperties getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /* Constants used for conversion to and from XML */
    private static final String ID = "id";
    private static final String POLL_ID = "pollid";
    private static final String POLL_TEXT = "title";
    private static final String DESCRIPTION = "description";
    private static final String VOTE_OPEN = "open-time";
    private static final String VOTE_CLOSE = "close-time";
    private static final String LIMIT_VOTING = "limit-voting";
    private static final String MIN_OPTIONS = "min-options";
    private static final String MAX_OPTIONS = "max-options";
    private static final String DISPLAY_RESULT = "display-result";

    private static DateFormat getDateFormatForXML() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
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

        poll.setAttribute(ID, getUuid());
        if (getPollId() != null) {
            poll.setAttribute(POLL_ID, getPollId().toString());
        }
        poll.setAttribute(POLL_TEXT, getText());
        poll.setAttribute(MIN_OPTIONS, Integer.toString(getMinOptions()));
        poll.setAttribute(MAX_OPTIONS, Integer.toString(getMaxOptions()));

        if (description != null) {
            poll.setAttribute(DESCRIPTION, description);
        }

        DateFormat dformat  = getDateFormatForXML();
        poll.setAttribute(VOTE_OPEN, dformat.format(this.voteOpen));
        poll.setAttribute(VOTE_CLOSE, dformat.format(this.voteClose));
        poll.setAttribute(LIMIT_VOTING, Boolean.toString(limitVoting));
        poll.setAttribute(DISPLAY_RESULT, this.displayResult);

        // properties
        //getProperties().toXml(doc, stack);
        //append the options as children

        stack.pop();

        return poll;
    }

    public static Poll fromXML(Element element) {
        Poll poll = new Poll();
        poll.setUuid(element.getAttribute(ID));
        poll.setText(element.getAttribute(POLL_TEXT));
        poll.setDisplayResult(element.getAttribute(DISPLAY_RESULT));
        poll.setDetails(element.getAttribute(DESCRIPTION));
        DateFormat dformat  = getDateFormatForXML();
        if (!"".equals(element.getAttribute(VOTE_OPEN))) {
            try {
                poll.setVoteOpen(dformat.parse(element.getAttribute(VOTE_OPEN)));
            } catch (ParseException e) {
                //should log this
            }
        }
        if (!"".equals(element.getAttribute(VOTE_CLOSE))) {
            try {
                poll.setVoteClose(dformat.parse(element.getAttribute(VOTE_CLOSE)));
            } catch (ParseException e) {
                //should log this
            }
        }
        if (!"".equals(element.getAttribute(MIN_OPTIONS))) {
            try {
                poll.setMinOptions(Integer.parseInt(element.getAttribute(MIN_OPTIONS)));
            } catch (NumberFormatException e) {
                //should log this
            }
        }
        if (!"".equals(element.getAttribute(MAX_OPTIONS))) {
            try {
                poll.setMaxOptions(Integer.parseInt(element.getAttribute(MAX_OPTIONS)));
            } catch (NumberFormatException e) {
                //should log this
            }
        }
        poll.setLimitVoting(Boolean.parseBoolean(element.getAttribute(LIMIT_VOTING)));
        return poll;
    }
}
