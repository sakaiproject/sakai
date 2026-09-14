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

package org.sakaiproject.poll.api.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.springframework.data.PersistableEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Entity
@Slf4j
@Table(name = "POLL_POLL")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Poll implements PersistableEntity<String> {

    public enum Access {
        SITE,
        GROUP
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "POLL_ID", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "POLL_OWNER", nullable = false, length = 99)
    private String owner;

    @Column(name = "POLL_SITE_ID", nullable = false, length = 99)
    private String siteId;

    @Column(name = "POLL_CREATION_DATE", nullable = false)
    private Instant creationDate;

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

    @Column(name = "POLL_VOTE_OPEN", nullable = false)
    private Instant voteOpen;

    @Column(name = "POLL_VOTE_CLOSE", nullable = false)
    private Instant voteClose;

    @Column(name = "POLL_DISPLAY_RESULT", nullable = false, length = 99)
    private String displayResult = "open";

    @Column(name = "POLL_LIMIT_VOTE", nullable = false)
    private boolean limitVoting = true;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderColumn(name = "OPTION_ORDER")
    @ToString.Exclude
    private List<Option> options = new ArrayList<>();

    @Column(name = "POLL_IS_PUBLIC", nullable = false)
    private boolean isPublic = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCESS_TYPE", nullable = false, length = 10)
    private Access typeOfAccess = Access.SITE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "POLL_GROUPS",
        joinColumns = @JoinColumn(name = "POLL_ID")
    )
    @Column(name = "GROUP_ID", length = 99, nullable = false)
    private Set<String> groupIds = new HashSet<>();

    public Poll() {
        this.text = "";
        this.description = "";
        this.minOptions = 1;
        this.maxOptions = 1;
        this.limitVoting = true;
        this.isPublic = false;
        this.typeOfAccess = Access.SITE;
        this.voteOpen = Instant.now();
        this.voteClose = Instant.now().plus(7, ChronoUnit.DAYS);
        this.displayResult = "open";
    }

    /**
     * Add an option to this poll and maintain bidirectional relationship.
     *
     * @param option the option to add
     */
    public void addOption(Option option) {
        options.add(option);
        option.setPoll(this);  // Maintain bidirectional sync
    }

    /**
     * Remove an option from this poll and maintain bidirectional relationship.
     *
     * @param option the option to remove
     */
    public void removeOption(Option option) {
        options.remove(option);
        option.setPoll(null);  // Maintain bidirectional sync
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
    private static final String ACCESS_TYPE = "access-type";
    private static final String GROUP_IDS = "groupIds";
    private static final String GROUP_ID = "groupId";

    public static Poll fromXML(Element element) {
        Poll poll = new Poll();
        poll.setId(element.getAttribute(ID));
        poll.setText(element.getAttribute(POLL_TEXT));
        poll.setDisplayResult(element.getAttribute(DISPLAY_RESULT));
        poll.setDescription(element.getAttribute(DESCRIPTION));
        DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        if (!"".equals(element.getAttribute(VOTE_OPEN))) {
            try {
                Date date = dformat.parse(element.getAttribute(VOTE_OPEN));
                poll.setVoteOpen(date.toInstant());
            } catch (ParseException e) {
                //should log this
            }
        }
        if (!"".equals(element.getAttribute(VOTE_CLOSE))) {
            try {
                Date date = dformat.parse(element.getAttribute(VOTE_CLOSE));
                poll.setVoteClose(date.toInstant());
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

        String accessType = element.getAttribute(ACCESS_TYPE);
        if (accessType != null && !accessType.isBlank()) {
            try {
                poll.setTypeOfAccess(Access.valueOf(accessType));
            } catch (IllegalArgumentException e) {
                log.warn("Ignoring invalid poll access type {} while reading XML for poll {}", accessType, poll.getId());
            }
        }

        Set<String> groupIds = new HashSet<>();
        if (poll.getTypeOfAccess() == Access.GROUP) {
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && GROUP_IDS.equals(child.getNodeName())) {
                    NodeList groupIdNodes = child.getChildNodes();
                    for (int j = 0; j < groupIdNodes.getLength(); j++) {
                        Node groupIdNode = groupIdNodes.item(j);
                        if (groupIdNode.getNodeType() == Node.ELEMENT_NODE && GROUP_ID.equals(groupIdNode.getNodeName())) {
                            String groupId = groupIdNode.getTextContent();
                            if (groupId != null && !groupId.trim().isEmpty()) {
                                groupIds.add(groupId);
                            }
                        }
                    }
                }
            }
        }
        poll.setGroupIds(groupIds);
        return poll;
    }

    public Element toXml(Document doc, Stack stack) {
        Element poll = doc.createElement("poll");

        if (stack.isEmpty()) {
            doc.appendChild(poll);
        } else {
            ((Element) stack.peek()).appendChild(poll);
        }

        stack.push(poll);

        poll.setAttribute(ID, getId());
        if (getId() != null) {
            poll.setAttribute(POLL_ID, getId());
        }
        poll.setAttribute(POLL_TEXT, getText());
        poll.setAttribute(MIN_OPTIONS, Integer.toString(getMinOptions()));
        poll.setAttribute(MAX_OPTIONS, Integer.toString(getMaxOptions()));

        if (description != null) {
            poll.setAttribute(DESCRIPTION, description);
        }

        DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        poll.setAttribute(VOTE_OPEN, dformat.format(Date.from(this.voteOpen)));
        poll.setAttribute(VOTE_CLOSE, dformat.format(Date.from(this.voteClose)));
        poll.setAttribute(LIMIT_VOTING, Boolean.toString(limitVoting));
        poll.setAttribute(DISPLAY_RESULT, this.displayResult);
        poll.setAttribute(ACCESS_TYPE, (this.typeOfAccess != null ? this.typeOfAccess : Access.SITE).name());

        if (groupIds != null && !groupIds.isEmpty()) {
            Element groupIdsElement = doc.createElement(GROUP_IDS);
            for (String groupId : groupIds) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    continue;
                }
                Element groupIdElement = doc.createElement(GROUP_ID);
                groupIdElement.setTextContent(groupId);
                groupIdsElement.appendChild(groupIdElement);
            }
            if (groupIdsElement.hasChildNodes()) {
                poll.appendChild(groupIdsElement);
            }
        }

        // properties
        //getProperties().toXml(doc, stack);
        //append the options as children

        stack.pop();

        return poll;
    }
}
