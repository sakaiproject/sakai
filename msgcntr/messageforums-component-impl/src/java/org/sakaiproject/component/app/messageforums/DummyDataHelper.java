/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/DummyDataHelper.java $
 * $Id: DummyDataHelper.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.ControlPermissions;
import org.sakaiproject.api.app.messageforums.DateRestrictions;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ControlPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DateRestrictionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.LabelImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessagePermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;

/*
 * This helper provides dummy data for use by interface developers It uses model objects. Models are
 * hibernate object wrappers, which are used so that hibernate does not play dirty and try to save
 * objects on the interface. They also uses List rather than Set, which play nice in JSF tags.
 */
@Slf4j
public class DummyDataHelper implements DummyDataHelperApi
{
  private MessageForumsTypeManager typeMgr;

  public void init()
  {
     log.info("init()");
    ;
  }

  public Area getPrivateArea()
  {
    Area a1 = new AreaImpl();
    a1.setContextId("context1");
    a1.setHidden(Boolean.FALSE);
    a1.setName("Messages Area");
    a1.setCreated(new Date());
    a1.setCreatedBy("admin");
    a1.setModified(new Date());
    a1.setModifiedBy("admin");
    a1.setId(Long.valueOf(1));
    a1.setUuid("1");
    //a1.setPrivateForums(getPrivateForums());
    return a1;
  }

  private List getPrivateForums()
  {
    List privateForums = new ArrayList();
    PrivateForum pfm1 = new PrivateForumImpl();
    pfm1.setAttachments(getAttachments());
    pfm1.setCreated(new Date());
    pfm1.setCreatedBy("admin");
    pfm1.setExtendedDescription("extended description");
    pfm1.setId(Long.valueOf(9));
    pfm1.setUuid("9");
    pfm1.setModified(new Date());
    pfm1.setModifiedBy("the moderator");
    pfm1.setShortDescription("short description");
    pfm1.setTitle("Messages");
    pfm1.setTopics((getPrivateTopics()));
    // pfm1.setType(new TypeImpl());
    pfm1.setAutoForward(Boolean.TRUE);
    pfm1.setAutoForwardEmail("fish@indiana.edu");
    pfm1.setPreviewPaneEnabled(Boolean.TRUE);
    pfm1.setSortIndex(Integer.valueOf(2));
    pfm1.setTopics(getPrivateTopics());

    privateForums.add(pfm1);

    return privateForums;
  }

 
  private List getPrivateTopics()
  {
    List discussionTopics = new ArrayList();
    DiscussionTopic dtm = new DiscussionTopicImpl();
    dtm.setActorPermissions(getActorPermissions());
    dtm.setAttachments(getAttachments());
    dtm.setCreated(new Date());
    dtm.setCreatedBy("admin");
    dtm.setDateRestrictions(getDateRestrictions());
    dtm.setExtendedDescription("the extended description");
    dtm.setId(Long.valueOf(12));
    dtm.setUuid("12");
    dtm.setLabels(getLabels());
    dtm.setLocked(Boolean.TRUE);
    dtm.setModerated(Boolean.FALSE);
    dtm.setPostFirst(Boolean.FALSE);
    dtm.setModified(new Date());
    dtm.setModifiedBy("the moderator");
    dtm.setShortDescription("sort desc here...");
    dtm.setTitle("Received");
    dtm.setConfidentialResponses(Boolean.TRUE);
    dtm.setTypeUuid(typeMgr.getReceivedPrivateMessageType());
    dtm.setMutable(Boolean.FALSE);
    dtm.setSortIndex(Integer.valueOf(2));
    dtm.setMessages(getReceivedPrivateMessages());

    DiscussionTopic dtm1 = new DiscussionTopicImpl();
    dtm1.setActorPermissions(getActorPermissions());
    dtm1.setAttachments(getAttachments());
    dtm1.setCreated(new Date());
    dtm1.setCreatedBy("admin");
    dtm1.setDateRestrictions(getDateRestrictions());
    dtm1.setExtendedDescription("the extended description");
    dtm1.setId(Long.valueOf(1211));
    dtm1.setUuid("1211");
    dtm1.setLabels(getLabels());
    dtm1.setLocked(Boolean.TRUE);
    dtm1.setModerated(Boolean.FALSE);
    dtm1.setPostFirst(Boolean.FALSE);
    dtm1.setModified(new Date());
    dtm1.setModifiedBy("the moderator");
    dtm1.setShortDescription("sort desc here...");
    dtm1.setTitle("Sent");
    dtm1.setConfidentialResponses(Boolean.TRUE);
    dtm1.setMutable(Boolean.FALSE);
    dtm.setTypeUuid(typeMgr.getSentPrivateMessageType());
    dtm1.setSortIndex(Integer.valueOf(222));
    dtm1.setMessages(getSentPrivateMessages());

    DiscussionTopic dtm2 = new DiscussionTopicImpl();
    dtm2.setActorPermissions(getActorPermissions());
    dtm2.setAttachments(getAttachments());
    dtm2.setCreated(new Date());
    dtm2.setCreatedBy("admin");
    dtm2.setDateRestrictions(getDateRestrictions());
    dtm2.setExtendedDescription("the extended description");
    dtm2.setId(Long.valueOf(1233));
    dtm2.setUuid("1233");
    dtm2.setLabels(getLabels());
    dtm2.setLocked(Boolean.TRUE);
    dtm2.setModerated(Boolean.FALSE);
    dtm2.setPostFirst(Boolean.FALSE);  
    dtm2.setModified(new Date());
    dtm2.setModifiedBy("the moderator");
    dtm2.setShortDescription("sort desc here...");
    dtm2.setTitle("Deleted");
    dtm.setTypeUuid(typeMgr.getDeletedPrivateMessageType());
    dtm2.setConfidentialResponses(Boolean.TRUE);
    dtm2.setMutable(Boolean.FALSE);
    dtm2.setSortIndex(Integer.valueOf(2222));
    dtm2.setMessages(getDeletedPrivateMessages());

    DiscussionTopic dtm3 = new DiscussionTopicImpl();
    dtm3.setActorPermissions(getActorPermissions());
    dtm3.setAttachments(getAttachments());
    dtm3.setCreated(new Date());
    dtm3.setCreatedBy("admin");
    dtm3.setDateRestrictions(getDateRestrictions());
    dtm3.setExtendedDescription("the extended description");
    dtm3.setId(Long.valueOf(123));
    dtm3.setUuid("123");
    dtm3.setLabels(getLabels());
    dtm3.setLocked(Boolean.TRUE);
    dtm3.setModerated(Boolean.FALSE);
    dtm3.setPostFirst(Boolean.FALSE);
    dtm3.setModified(new Date());
    dtm3.setModifiedBy("the moderator");
    dtm3.setShortDescription("sort desc here...");
    dtm3.setTitle("Drafts");
    dtm.setTypeUuid(typeMgr.getDraftPrivateMessageType());
    dtm3.setConfidentialResponses(Boolean.TRUE);
    dtm3.setMutable(Boolean.FALSE);
    dtm3.setSortIndex(Integer.valueOf(2));
    dtm3.setMessages(getDraftedPrivateMessages());
 
    DiscussionTopic dtm13 = new DiscussionTopicImpl();
    dtm13.setActorPermissions(getActorPermissions());
    dtm13.setAttachments(getAttachments());
    dtm13.setCreated(new Date());
    dtm13.setCreatedBy("admin");
    dtm13.setDateRestrictions(getDateRestrictions());
    dtm13.setExtendedDescription("the extended description");
    dtm13.setId(Long.valueOf(132));
    dtm13.setUuid("132");
    dtm13.setLabels(getLabels());
    dtm13.setLocked(Boolean.TRUE);
    dtm13.setModerated(Boolean.FALSE);
    dtm13.setPostFirst(Boolean.FALSE);
    dtm13.setModified(new Date());
    dtm13.setModifiedBy("the moderator");
    dtm13.setShortDescription("sort desc here...");
    dtm13.setTitle("Personal Folders");
    dtm13.setConfidentialResponses(Boolean.TRUE);
    dtm13.setMutable(Boolean.TRUE);
    dtm13.setSortIndex(Integer.valueOf(2));
    dtm13.setMessages(null);
 
    discussionTopics.add(dtm);
    discussionTopics.add(dtm1);
    discussionTopics.add(dtm2);
    discussionTopics.add(dtm3);
    discussionTopics.add(dtm13);

    return discussionTopics;
  }

  private List getDraftedPrivateMessages()
  {
    List privateMessages = new ArrayList();
    PrivateMessage pmm1 = new PrivateMessageImpl();
    pmm1.setApproved(Boolean.TRUE);
    pmm1.setAttachments(getAttachments());
    pmm1.setAuthor("suzie q.");
    pmm1.setBody("Drafted :this is  the body 1");
    pmm1.setCreated(new Date());
    pmm1.setCreatedBy("john smith");
    pmm1.setId(Long.valueOf(3));
    pmm1.setInReplyTo(null);
    pmm1.setLabel("Normal");
    pmm1.setModified(new Date());
    pmm1.setModifiedBy("joe davis");
    pmm1.setTitle("Drafted the first message posted");
    pmm1.setUuid("3");
    pmm1.setExternalEmail(Boolean.TRUE);
    pmm1.setExternalEmailAddress("fun@hotmail.com");
    pmm1.setRecipients(new ArrayList()); // TODO: Real sakai users needed

    PrivateMessage pmm2 = new PrivateMessageImpl();
    pmm2.setApproved(Boolean.TRUE);
    pmm2.setAttachments(getAttachments());
    pmm2.setAuthor("suzie q.");
    pmm2.setBody("Drafted this is the body 2");
    pmm2.setCreated(new Date());
    pmm2.setCreatedBy("john smith");
    pmm2.setId(Long.valueOf(42));
    pmm2.setInReplyTo(pmm1);
    pmm2.setLabel("Normal");
    pmm2.setModified(new Date());
    pmm2.setModifiedBy("joe davis");
    pmm2.setTitle("the second message posted");
    pmm2.setUuid("42");
    pmm2.setExternalEmail(Boolean.FALSE);
    pmm2.setExternalEmailAddress(null);
    pmm2.setRecipients(new ArrayList()); // TODO: Real sakai users needed
    privateMessages.add(pmm1);
    privateMessages.add(pmm2);
    return privateMessages;

  }

  private List getDeletedPrivateMessages()
  {
    List privateMessages = new ArrayList();
    PrivateMessage pmm1 = new PrivateMessageImpl();
    pmm1.setApproved(Boolean.TRUE);
    pmm1.setAttachments(getAttachments());
    pmm1.setAuthor("suzie q.");
    pmm1.setBody("getDeletedPrivateMessages this is the body 1");
    pmm1.setCreated(new Date());
    pmm1.setCreatedBy("john smith");
    pmm1.setId(Long.valueOf(1213));
    pmm1.setInReplyTo(null);
    pmm1.setLabel("Normal");
    pmm1.setModified(new Date());
    pmm1.setModifiedBy("joe davis");
    pmm1.setTitle("getDeletedPrivateMessages the first message posted");
    pmm1.setUuid("admin");
    pmm1.setExternalEmail(Boolean.TRUE);
    pmm1.setExternalEmailAddress("fun@hotmail.com");
    pmm1.setRecipients(new ArrayList()); // TODO: Real sakai users needed

    PrivateMessage pmm2 = new PrivateMessageImpl();
    pmm2.setApproved(Boolean.TRUE);
    pmm2.setAttachments(getAttachments());
    pmm2.setAuthor("suzie q.");
    pmm2.setBody("getDeletedPrivateMessages this is the body 2");
    pmm2.setCreated(new Date());
    pmm2.setCreatedBy("john smith");
    pmm2.setId(Long.valueOf(1214));
    pmm2.setInReplyTo(pmm1);
    pmm2.setLabel("getDeletedPrivateMessages Normal");
    pmm2.setModified(new Date());
    pmm2.setModifiedBy("joe davis");
    pmm2.setTitle("getDeletedPrivateMessages the second message posted");
    pmm2.setUuid("1214");
    pmm2.setExternalEmail(Boolean.FALSE);
    pmm2.setExternalEmailAddress(null);
    pmm2.setRecipients(new ArrayList()); // TODO: Real sakai users needed
    privateMessages.add(pmm1);
    privateMessages.add(pmm2);
    return privateMessages;

  }

  private List getSentPrivateMessages()
  {
    List privateMessages = new ArrayList();
    PrivateMessage pmm1 = new PrivateMessageImpl();
    pmm1.setApproved(Boolean.TRUE);
    pmm1.setAttachments(getAttachments());
    pmm1.setAuthor("suzie q.");
    pmm1.setBody("this is the body 1");
    pmm1.setCreated(new Date());
    pmm1.setCreatedBy("john smith");
    pmm1.setId(Long.valueOf(13));
    pmm1.setInReplyTo(null);
    pmm1.setLabel("Normal");
    pmm1.setModified(new Date());
    pmm1.setModifiedBy("joe davis");
    pmm1.setTitle("the first message posted");
    pmm1.setUuid("3");
    pmm1.setExternalEmail(Boolean.TRUE);
    pmm1.setExternalEmailAddress("fun@hotmail.com");
    pmm1.setRecipients(new ArrayList()); // TODO: Real sakai users needed

    PrivateMessage pmm2 = new PrivateMessageImpl();
    pmm2.setApproved(Boolean.TRUE);
    pmm2.setAttachments(getAttachments());
    pmm2.setAuthor("suzie q.");
    pmm2.setBody("this is the body 2");
    pmm2.setCreated(new Date());
    pmm2.setCreatedBy("john smith");
    pmm2.setId(Long.valueOf(4));
    pmm2.setInReplyTo(pmm1);
    pmm2.setLabel("Normal");
    pmm2.setModified(new Date());
    pmm2.setModifiedBy("joe davis");
    pmm2.setTitle("the second message posted");
    pmm2.setUuid("14");
    pmm2.setExternalEmail(Boolean.FALSE);
    pmm2.setExternalEmailAddress(null);
    pmm2.setRecipients(new ArrayList()); // TODO: Real sakai users needed
    privateMessages.add(pmm1);
    privateMessages.add(pmm2);
    return privateMessages;

  }

  private List getReceivedPrivateMessages()
  {
    List privateMessages = new ArrayList();
    PrivateMessage pmm1 = new PrivateMessageImpl();
    pmm1.setApproved(Boolean.TRUE);
    pmm1.setAttachments(getAttachments());
    pmm1.setAuthor("suzie q.");
    pmm1.setBody("this is the body 1");
    pmm1.setCreated(new Date());
    pmm1.setCreatedBy("john smith");
    pmm1.setId(Long.valueOf(13));
    pmm1.setInReplyTo(null);
    pmm1.setLabel("Normal");
    pmm1.setModified(new Date());
    pmm1.setModifiedBy("joe davis");
    pmm1.setTitle("the first message posted");
    pmm1.setUuid("3");
    pmm1.setExternalEmail(Boolean.TRUE);
    pmm1.setExternalEmailAddress("fun@hotmail.com");
    pmm1.setRecipients(new ArrayList()); // TODO: Real sakai users needed

    PrivateMessage pmm2 = new PrivateMessageImpl();
    pmm2.setApproved(Boolean.TRUE);
    pmm2.setAttachments(getAttachments());
    pmm2.setAuthor("suzie q.");
    pmm2.setBody("this is the body 2");
    pmm2.setCreated(new Date());
    pmm2.setCreatedBy("john smith");
    pmm2.setId(Long.valueOf(4));
    pmm2.setInReplyTo(pmm1);
    pmm2.setLabel("Normal");
    pmm2.setModified(new Date());
    pmm2.setModifiedBy("joe davis");
    pmm2.setTitle("the second message posted");
    pmm2.setUuid("14");
    pmm2.setExternalEmail(Boolean.FALSE);
    pmm2.setExternalEmailAddress(null);
    pmm2.setRecipients(new ArrayList()); // TODO: Real sakai users needed
    privateMessages.add(pmm1);
    privateMessages.add(pmm2);
    return privateMessages;
  }

//**********************************************************************************
  public Area getDiscussionForumArea()
  {
    Area a2 = new AreaImpl();
    a2.setContextId("context2");
    a2.setHidden(Boolean.FALSE);
    a2.setName("Forums");
    a2.setCreated(new Date());
    a2.setCreatedBy("john doe");
    a2.setModified(new Date());
    a2.setModifiedBy("mary jane");
    a2.setId(Long.valueOf(2));
    a2.setUuid("2");
    //a2.setDiscussionForums(getDiscussionForums());
    return a2;
  }

  private List getDiscussionForums()
  {
    List dicussionForums = new ArrayList();
    dicussionForums.add(getDiscussionForumByID5());
   dicussionForums.add(getDiscussionForumByID6());
    return dicussionForums;
  }

  private DiscussionForum getDiscussionForumByID5()
  {
    DiscussionForum dfm1 = new DiscussionForumImpl();
    dfm1.setActorPermissions(getActorPermissions());
    dfm1.setAttachments(getAttachments());
    dfm1.setCreated(new Date());
    dfm1.setCreatedBy("admin");
    dfm1.setDateRestrictions(getDateRestrictions());
    dfm1
        .setExtendedDescription("This forum is used to discuss assigned case studies. You should follow the case study preparation instructions before posting.");
    dfm1.setId(Long.valueOf(5));
    dfm1.setUuid("5");
    dfm1.setLabels(getLabels());
    dfm1.setLocked(Boolean.FALSE);
    dfm1.setModerated(Boolean.TRUE);
    dfm1.setPostFirst(Boolean.FALSE);
    dfm1.setModified(new Date());
    dfm1.setModifiedBy("admin");
    dfm1
        .setShortDescription("This forum is used to discuss assigned case studies. You should follow the case study preparation instructions before posting.");
    dfm1.setTitle("Case Studies");
    dfm1.setTopics(list2set(getDiscussionTopics()));
    return dfm1;
  }

  private DiscussionForum getDiscussionForumByID6()
  {
    DiscussionForum dfm2 = new DiscussionForumImpl();
    dfm2.setActorPermissions(getActorPermissions());
    dfm2.setAttachments(getAttachments());
    dfm2.setCreated(new Date());
    dfm2.setCreatedBy("jim johnson");
    dfm2.setDateRestrictions(getDateRestrictions());
    dfm2.setExtendedDescription("the extended description 2");
    dfm2.setId(Long.valueOf(6));
    dfm2.setUuid("6");
    dfm2.setLabels(getLabels());
    dfm2.setLocked(Boolean.TRUE);
    dfm2.setModerated(Boolean.FALSE);
    dfm2.setPostFirst(Boolean.FALSE);
    dfm2.setModified(new Date());
    dfm2.setModifiedBy("the moderator");
    dfm2.setShortDescription("sort desc here...");
    dfm2.setTitle("disc forum 2");
    dfm2.setTopics(list2set(getDiscussionTopics()));
    return dfm2;
  }

  private List getForumMessages()
  {
    List forumMessages = new ArrayList();
    forumMessages.add(getMessageByID3());
    forumMessages.add(getMessageByID4());
    return forumMessages;
  }
  private Message getMessageByID3()
  {
    Message mm1 = new MessageImpl();
    mm1.setApproved(Boolean.TRUE);
    mm1.setAttachments(getAttachments());
    mm1.setAuthor("suzie q.");
    mm1.setBody("this is the body 1");
    mm1.setCreated(new Date());
    mm1.setCreatedBy("john smith");
    mm1.setId(Long.valueOf(3));
    mm1.setInReplyTo(null);
    mm1.setLabel("Normal");
    mm1.setModified(new Date());
    mm1.setModifiedBy("joe davis");
    mm1.setTitle("the first message posted");
    mm1.setUuid("3");
    return mm1;
  }
  private Message getMessageByID4()
  {
    Message mm2 = new MessageImpl();
    mm2.setApproved(Boolean.TRUE);
    mm2.setAttachments(getAttachments());
    mm2.setAuthor("suzie q.");
    mm2.setBody("this is the body 2");
    mm2.setCreated(new Date());
    mm2.setCreatedBy("john smith");
    mm2.setId(Long.valueOf(4));
    mm2.setInReplyTo(getMessageByID3());
    mm2.setLabel("Normal");
    mm2.setModified(new Date());
    mm2.setModifiedBy("joe davis");
    mm2.setTitle("the second message posted");
    mm2.setUuid("4");
     return mm2;
  }
  private List getDiscussionTopics()
  {
    List discussionTopics = new ArrayList();
    discussionTopics.add(getDiscussionTopicByID11());
    discussionTopics.add(getDiscussionTopicByID511());
    discussionTopics.add(getDiscussionTopicByID521());
    return discussionTopics;
  }

  //parent 5
  private DiscussionTopic getDiscussionTopicByID11()
  {
    DiscussionTopic dtm1 = new DiscussionTopicImpl();
    //dtm1.setBaseForum(getForumById("5"));
    dtm1.setActorPermissions(getActorPermissions());
    dtm1.setAttachments(getAttachments());
     dtm1.setCreated(new Date());
    dtm1.setCreatedBy("admin");
    dtm1.setDateRestrictions(getDateRestrictions());
    dtm1
        .setExtendedDescription("Extended: The case requires at lease two decisions in setting your strategy to advance the project: 1. Which customer groups to include in the project scope? 2. Should Customs be included as a development partner?");
    dtm1.setId(Long.valueOf(11));
    dtm1.setUuid("11");
    dtm1.setLabels(getLabels());
    dtm1.setLocked(Boolean.FALSE);
    dtm1.setModerated(Boolean.TRUE);
    dtm1.setPostFirst(Boolean.FALSE);
    dtm1.setModified(new Date());
    dtm1.setModifiedBy("the moderator");
    dtm1.setShortDescription("sort desc here...");
    dtm1.setTitle("Dubai Port Authority Case ");
    // dtm1.setType(new TypeImpl());
    dtm1.setConfidentialResponses(Boolean.TRUE);
    dtm1.setGradebook("gb2-1");
    dtm1.setGradebookAssignment("asst2");
    dtm1.setHourBeforeResponsesVisible(Integer.valueOf(2));
    dtm1.setMustRespondBeforeReading(Boolean.TRUE);
    dtm1.setMutable(Boolean.TRUE);
    dtm1.setSortIndex(Integer.valueOf(1));
    dtm1.setMessages(getForumMessages());
    return dtm1;
  }
  
  //parent 5
  private DiscussionTopic getDiscussionTopicByID511()
  {
    DiscussionTopic dtm1 = new DiscussionTopicImpl();
   // dtm1.setBaseForum(getForumById("5"));
    dtm1.setActorPermissions(getActorPermissions());
    dtm1.setAttachments(getAttachments());
    dtm1.setCreated(new Date());
    dtm1.setCreatedBy("admin");
    dtm1.setDateRestrictions(getDateRestrictions());
    dtm1
        .setExtendedDescription("Extended: The case requires at lease two decisions in setting your strategy to advance the project: 1. Which customer groups to include in the project scope? 2. Should Customs be included as a development partner?");
    dtm1.setId(Long.valueOf(11));
    dtm1.setUuid("511");
    dtm1.setLabels(getLabels());
    dtm1.setLocked(Boolean.FALSE);
    dtm1.setModerated(Boolean.TRUE);
    dtm1.setPostFirst(Boolean.FALSE);
    dtm1.setModified(new Date());
    dtm1.setModifiedBy("the moderator");
    dtm1.setShortDescription("Assess the technical choices rode by KPMG following the choice not to fund the Shadow Partner project. Do you believe they made wise or risky decisions? Why?");
    dtm1.setTitle("KPMG: One Giant Brain Case");
    // dtm1.setType(new TypeImpl());
    dtm1.setConfidentialResponses(Boolean.TRUE);
    dtm1.setGradebook("gb2-1");
    dtm1.setGradebookAssignment("asst2");
    dtm1.setHourBeforeResponsesVisible(Integer.valueOf(2));
    dtm1.setMustRespondBeforeReading(Boolean.TRUE);
    dtm1.setMutable(Boolean.TRUE);
    dtm1.setSortIndex(Integer.valueOf(1));
    dtm1.setMessages(getForumMessages());
    return dtm1;
  }
  
  //parent 5
  private DiscussionTopic getDiscussionTopicByID521()
  {
    DiscussionTopic dtm1 = new DiscussionTopicImpl();
    //dtm1.setBaseForum(getForumById("5"));
    dtm1.setActorPermissions(getActorPermissions());
    dtm1.setAttachments(getAttachments());
    dtm1.setCreated(new Date());
    dtm1.setCreatedBy("admin");
    dtm1.setDateRestrictions(getDateRestrictions());
    dtm1
        .setExtendedDescription("Extended: The case requires at lease two decisions in setting your strategy to advance the project: 1. Which customer groups to include in the project scope? 2. Should Customs be included as a development partner?");
    dtm1.setId(Long.valueOf(11));
    dtm1.setUuid("521");
    dtm1.setLabels(getLabels());
    dtm1.setLocked(Boolean.FALSE);
    dtm1.setModerated(Boolean.TRUE);
    dtm1.setPostFirst(Boolean.FALSE);
    dtm1.setModified(new Date());
    dtm1.setModifiedBy("the moderator");
    dtm1.setShortDescription("What lessons, if any, should the Company learn from the Maxfli experience regarding future development?");
    dtm1.setTitle("Maxfli Case");
    // dtm1.setType(new TypeImpl());
    dtm1.setConfidentialResponses(Boolean.TRUE);
    dtm1.setGradebook("gb2-1");
    dtm1.setGradebookAssignment("asst2");
    dtm1.setHourBeforeResponsesVisible(Integer.valueOf(2));
    dtm1.setMustRespondBeforeReading(Boolean.TRUE);
    dtm1.setMutable(Boolean.TRUE);
    dtm1.setSortIndex(Integer.valueOf(1));
    dtm1.setMessages(getForumMessages());
     return dtm1;
  }

  // helpers -- if someone needs to get access to one of these
  // just make it public... they were created so these object are
  // easy to still in the lists above

  private List getAttachments()
  {
    List attachments = new ArrayList();
    Attachment a1 = new AttachmentImpl();
    a1.setAttachmentId("attach1");
    a1.setAttachmentName("file 1.doc");
    a1.setAttachmentSize("24K");
    a1.setAttachmentType("application/msword");
    a1.setAttachmentUrl("http://www.something.com/afile");
    Attachment a2 = new AttachmentImpl();
    a2.setAttachmentId("attach2");
    a2.setAttachmentName("file 2.doc");
    a2.setAttachmentSize("243K");
    a2.setAttachmentType("application/msword");
    a2.setAttachmentUrl("http://www.something.com/anotherfile");
    attachments.add(a1);
    attachments.add(a2);
    return attachments;
  }

  private List getLabels()
  {
    List labels = new ArrayList();
    Label l1 = new LabelImpl();
    l1.setKey("group-key");
    l1.setValue("group");
    Label l2 = new LabelImpl();
    l2.setKey("partner-key");
    l2.setValue("partner");
    Label l3 = new LabelImpl();
    l3.setKey("alone-key");
    l3.setValue("alone");
    labels.add(l1);
    labels.add(l2);
    labels.add(l3);
    return labels;
  }

  private ActorPermissions getActorPermissions()
  {
    ActorPermissions apm = new ActorPermissionsImpl();
    // TODO: Not sure how sakai handles users - empty lists for now
    apm.setAccessors(new ArrayList());
    apm.setContributors(new ArrayList());
    apm.setModerators(new ArrayList());
    apm.setId(Long.valueOf(123));
    return null;
  }

  private ControlPermissions getControlPermissions()
  {
    ControlPermissions cpm = new ControlPermissionsImpl();
    cpm.setChangeSettings(Boolean.TRUE);
    cpm.setId(Long.valueOf(234));
    cpm.setMovePostings(Boolean.TRUE);
    cpm.setNewResponse(Boolean.TRUE);
    cpm.setNewTopic(Boolean.TRUE);
    cpm.setResponseToResponse(Boolean.TRUE);
    cpm.setRole("Not sure what sakai roles are");
    return cpm;
  }

  private DateRestrictions getDateRestrictions()
  {
    DateRestrictions drm = new DateRestrictionsImpl();
    drm.setHidden(new Date());
    drm.setHiddenPostOnSchedule(Boolean.TRUE);
    drm.setId(Long.valueOf(22));
    drm.setPostingAllowed(new Date());
    drm.setPostingAllowedPostOnSchedule(Boolean.TRUE);
    drm.setReadOnly(new Date());
    drm.setReadOnlyPostOnSchedule(Boolean.TRUE);
    drm.setVisible(new Date());
    drm.setVisiblePostOnSchedule(Boolean.TRUE);
    return drm;
  }

  private MessagePermissions getMessgePermissions()
  {
    MessagePermissions mpm = new MessagePermissionsImpl();
    mpm.setDeleteAny(Boolean.TRUE);
    mpm.setDeleteOwn(Boolean.TRUE);
    mpm.setId(Long.valueOf(22));
    mpm.setRead(Boolean.TRUE);
    mpm.setReadDrafts(Boolean.TRUE);
    mpm.setReviseAny(Boolean.TRUE);
    mpm.setReviseOwn(Boolean.TRUE);
    mpm.setRole("Not sure what sakai roles are");
    return mpm;
  }

  private List list2set(List list)
  {
    List set = new ArrayList();
    for (Iterator iter = list.iterator(); iter.hasNext();)
    {
      Object object = (Object) iter.next();
      set.add(object);
    }
    return set;
  }

  public boolean isPrivateAreaUnabled()
  {
    return true;
  }

  public void setTypeMgr(MessageForumsTypeManager typeMgr)
  {
    this.typeMgr = typeMgr;
  }

  public DiscussionForum getForumById(Long forumId)
  {
    if (forumId != null && forumId.equals(Long.valueOf(5)))
    {

      return getDiscussionForumByID5();
    }
    else
    {
      return getDiscussionForumByID6();
    }
  }

  public List getMessagesByTopicId(Long topicId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public DiscussionTopic getTopicById(Long topicId)
  {
    if(topicId.equals(Long.valueOf(511)))
    {
      return getDiscussionTopicByID511();
    }
    if(topicId.equals(Long.valueOf(521)))
    {
      return getDiscussionTopicByID521();
    }
    return getDiscussionTopicByID11();
  }

  public boolean hasNextTopic(DiscussionTopic topic)
  {
    if(topic.getUuid().equals("11"))
    {
      return true;
    }
    if(topic.getUuid().equals("511"))
    {
      return true;
    }
    return false;
  }

  public boolean hasPreviousTopic(DiscussionTopic topic)
  {
    if(topic.getUuid().equals("521"))
    {
      return true;
    }
    if(topic.getUuid().equals("511"))
    {
      return true;
    }
    return false;
  }

  public DiscussionTopic getNextTopic(DiscussionTopic topic)
  {
    if(topic.getUuid().equals("11"))
    {
      return getDiscussionTopicByID511();
    }
    if(topic.getUuid().equals("511"))
    {
      return getDiscussionTopicByID521();
    }
    return null;
  }

  public DiscussionTopic getPreviousTopic(DiscussionTopic topic)
  {
    if(topic.getUuid().equals("512"))
    {
      return getDiscussionTopicByID511();
    }
    if(topic.getUuid().equals("511"))
    {
      return getDiscussionTopicByID11();
    }
    return null;
  }

  public Message getMessageById(Long id)
  {
    if(id.equals(Long.valueOf(3)))
      return getMessageByID3();
    else
      return getMessageByID4();
  }
  

}
