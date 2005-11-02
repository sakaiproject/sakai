/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.messageforums.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.api.app.messageforums.model.ActorPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.AreaModel;
import org.sakaiproject.api.app.messageforums.model.AttachmentModel;
import org.sakaiproject.api.app.messageforums.model.ControlPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.DateRestrictionsModel;
import org.sakaiproject.api.app.messageforums.model.DiscussionForumModel;
import org.sakaiproject.api.app.messageforums.model.DiscussionTopicModel;
import org.sakaiproject.api.app.messageforums.model.LabelModel;
import org.sakaiproject.api.app.messageforums.model.MessageModel;
import org.sakaiproject.api.app.messageforums.model.MessagePermissionsModel;
import org.sakaiproject.api.app.messageforums.model.OpenForumModel;
import org.sakaiproject.api.app.messageforums.model.OpenTopicModel;
import org.sakaiproject.api.app.messageforums.model.PrivateForumModel;
import org.sakaiproject.api.app.messageforums.model.PrivateMessageModel;

/*
 * This helper provides dummy data for use by interface developers
 * It uses model objects.  Models are hibernate object wrappers, 
 * which are used so that hibernate does not play dirty and try to
 * save objects on the interface.  They also uses List rather than Set,
 * which play nice in JSF tags.
 */

public class DummyDataHelper {
    
    public List getAreas() {
        List areas = new ArrayList();
        AreaModel a1 = new AreaModelImpl();
        a1.setContextId("context1");
        a1.setHidden(Boolean.FALSE);
        a1.setName("My Areas");
        a1.setCreated(new Date());
        a1.setCreatedBy("joe smith");
        a1.setModified(new Date());
        a1.setModifiedBy("amy jones");
        a1.setId(new Long(1));
        a1.setUuid("1");
        AreaModel a2 = new AreaModelImpl();
        a2.setContextId("context2");
        a2.setHidden(Boolean.FALSE);
        a2.setName("My Areas 2");
        a2.setCreated(new Date());
        a2.setCreatedBy("john doe");
        a2.setModified(new Date());
        a2.setModifiedBy("mary jane");
        a2.setId(new Long(2));
        a2.setUuid("2");
        areas.add(a1);
        areas.add(a2);
        return areas;
    }
    
    public List getForumMessages() {
        List forumMessages = new ArrayList();
        MessageModel mm1 = new MessageModelImpl();
        mm1.setApproved(Boolean.TRUE);
        mm1.setAttachments(getAttachments());
        mm1.setAuthor("suzie q.");
        mm1.setBody("this is the body 1");
        mm1.setCreated(new Date());
        mm1.setCreatedBy("john smith");
        mm1.setGradebook("gb1");
        mm1.setGradebookAssignment("asst1");
        mm1.setId(new Long(3));
        mm1.setInReplyTo(null);
        mm1.setLabel("fun stuff");
        mm1.setModified(new Date());
        mm1.setModifiedBy("joe davis");
        mm1.setTitle("the first message posted");
        mm1.setUuid("3");
        MessageModel mm2 = new MessageModelImpl();
        mm2.setApproved(Boolean.TRUE);
        mm2.setAttachments(getAttachments());
        mm2.setAuthor("suzie q.");
        mm2.setBody("this is the body 2");
        mm2.setCreated(new Date());
        mm2.setCreatedBy("john smith");
        mm2.setGradebook("gb1");
        mm2.setGradebookAssignment("asst1");
        mm2.setId(new Long(4));
        mm2.setInReplyTo(mm1);
        mm2.setLabel("fun stuff");
        mm2.setModified(new Date());
        mm2.setModifiedBy("joe davis");
        mm2.setTitle("the second message posted");
        mm2.setUuid("4");
        forumMessages.add(mm1);
        forumMessages.add(mm2);
        return forumMessages;
    }
    
    public List getPrivateMessages() {
        List privateMessages = new ArrayList();
        PrivateMessageModel pmm1 = new PrivateMessageModelImpl();
        pmm1.setApproved(Boolean.TRUE);
        pmm1.setAttachments(getAttachments());
        pmm1.setAuthor("suzie q.");
        pmm1.setBody("this is the body 1");
        pmm1.setCreated(new Date());
        pmm1.setCreatedBy("john smith");
        pmm1.setGradebook("gb1");
        pmm1.setGradebookAssignment("asst1");
        pmm1.setId(new Long(3));
        pmm1.setInReplyTo(null);
        pmm1.setLabel("fun stuff");
        pmm1.setModified(new Date());
        pmm1.setModifiedBy("joe davis");
        pmm1.setTitle("the first message posted");
        pmm1.setUuid("3");
        pmm1.setExternalEmail(Boolean.TRUE);
        pmm1.setExternalEmailAddress("fun@hotmail.com");
        pmm1.setRecipients(new ArrayList()); // TODO: Real sakai users needed
        PrivateMessageModel pmm2 = new PrivateMessageModelImpl();
        pmm2.setApproved(Boolean.TRUE);
        pmm2.setAttachments(getAttachments());
        pmm2.setAuthor("suzie q.");
        pmm2.setBody("this is the body 2");
        pmm2.setCreated(new Date());
        pmm2.setCreatedBy("john smith");
        pmm2.setGradebook("gb1");
        pmm2.setGradebookAssignment("asst1");
        pmm2.setId(new Long(4));
        pmm2.setInReplyTo(pmm1);
        pmm2.setLabel("fun stuff");
        pmm2.setModified(new Date());
        pmm2.setModifiedBy("joe davis");
        pmm2.setTitle("the second message posted");
        pmm2.setUuid("4");
        pmm2.setExternalEmail(Boolean.FALSE);
        pmm2.setExternalEmailAddress(null);
        pmm2.setRecipients(new ArrayList()); // TODO: Real sakai users needed
        privateMessages.add(pmm1);
        privateMessages.add(pmm2);       
        return privateMessages;
    }
    
    public List getDiscussionForums() {
        List dicussionForums = new ArrayList();
        DiscussionForumModel dfm1 = new DiscussionForumModelImpl();
        dfm1.setActorPermissions(getActorPermissions());
        dfm1.setAttachments(getAttachments());
        dfm1.setControlPermissions(getControlPermissions());
        dfm1.setCreated(new Date());
        dfm1.setCreatedBy("joe johnson");
        dfm1.setDateRestrictions(getDateRestrictions());
        dfm1.setExtendedDescription("the extended description");
        dfm1.setId(new Long(5));
        dfm1.setUuid("5");
        dfm1.setLabels(getLabels());
        dfm1.setLocked(Boolean.FALSE);
        dfm1.setMessagePermissions(getMessgePermissions());
        dfm1.setModerated(Boolean.TRUE);
        dfm1.setModified(new Date());
        dfm1.setModifiedBy("the moderator");
        dfm1.setShortDescription("sort desc here...");
        dfm1.setTitle("disc forum 1");
        dfm1.setTopics(getDiscussionTopics());
        dfm1.setType(new TypeModelImpl());
        DiscussionForumModel dfm2 = new DiscussionForumModelImpl();
        dfm2.setActorPermissions(getActorPermissions());
        dfm2.setAttachments(getAttachments());
        dfm2.setControlPermissions(getControlPermissions());
        dfm2.setCreated(new Date());
        dfm2.setCreatedBy("jim johnson");
        dfm2.setDateRestrictions(getDateRestrictions());
        dfm2.setExtendedDescription("the extended description 2");
        dfm2.setId(new Long(6));
        dfm2.setUuid("6");
        dfm2.setLabels(getLabels());
        dfm2.setLocked(Boolean.TRUE);
        dfm2.setMessagePermissions(getMessgePermissions());
        dfm2.setModerated(Boolean.FALSE);
        dfm2.setModified(new Date());
        dfm2.setModifiedBy("the moderator");
        dfm2.setShortDescription("sort desc here...");
        dfm2.setTitle("disc forum 2");
        dfm2.setTopics(getDiscussionTopics());
        dfm2.setType(new TypeModelImpl());
        dicussionForums.add(dfm1);
        dicussionForums.add(dfm2);
        return dicussionForums;
    }
    
    public List getOpenForums() {
        List openForums = new ArrayList();
        OpenForumModel ofm1 = new OpenForumModelImpl();
        ofm1.setAttachments(getAttachments());
        ofm1.setControlPermissions(getControlPermissions());
        ofm1.setCreated(new Date());
        ofm1.setCreatedBy("joe johnson");
        ofm1.setExtendedDescription("the extended description");
        ofm1.setId(new Long(7));
        ofm1.setUuid("7");
        ofm1.setLocked(Boolean.FALSE);
        ofm1.setMessagePermissions(getMessgePermissions());
        ofm1.setModified(new Date());
        ofm1.setModifiedBy("the moderator");
        ofm1.setShortDescription("sort desc here...");
        ofm1.setTitle("disc forum 1");
        ofm1.setTopics(getDiscussionTopics());
        ofm1.setType(new TypeModelImpl());
        ofm1.setSortIndex(new Integer(2));
        OpenForumModel ofm2 = new OpenForumModelImpl();
        ofm2.setAttachments(getAttachments());
        ofm2.setControlPermissions(getControlPermissions());
        ofm2.setCreated(new Date());
        ofm2.setCreatedBy("jim johnson");
        ofm2.setExtendedDescription("the extended description 2");
        ofm2.setId(new Long(8));
        ofm2.setUuid("8");
        ofm2.setLocked(Boolean.TRUE);
        ofm2.setMessagePermissions(getMessgePermissions());
        ofm2.setModified(new Date());
        ofm2.setModifiedBy("the moderator");
        ofm2.setShortDescription("sort desc here...");
        ofm2.setTitle("disc forum 2");
        ofm2.setTopics(getDiscussionTopics());
        ofm2.setType(new TypeModelImpl());
        ofm2.setSortIndex(new Integer(1));
        openForums.add(ofm1);
        openForums.add(ofm2);        
        return openForums;
    }
    
    public List getPrivateForums() {
        List privateForums = new ArrayList();
        PrivateForumModel pfm1 = new PrivateForumModelImpl();
        pfm1.setAttachments(getAttachments());
        pfm1.setCreated(new Date());
        pfm1.setCreatedBy("joe johnson");
        pfm1.setExtendedDescription("the extended description");
        pfm1.setId(new Long(9));
        pfm1.setUuid("9");
        pfm1.setModified(new Date());
        pfm1.setModifiedBy("the moderator");
        pfm1.setShortDescription("sort desc here...");
        pfm1.setTitle("disc forum 1");
        pfm1.setTopics(getDiscussionTopics());
        pfm1.setType(new TypeModelImpl());
        pfm1.setAutoForward(Boolean.TRUE);
        pfm1.setAutoForwardEmail("fish@indiana.edu");
        pfm1.setPreviewPaneEnabled(Boolean.TRUE);
        pfm1.setSortIndex(new Integer(2));
        PrivateForumModel pfm2 = new PrivateForumModelImpl();
        pfm2.setAttachments(getAttachments());
        pfm2.setCreated(new Date());
        pfm2.setCreatedBy("jim johnson");
        pfm2.setExtendedDescription("the extended description 2");
        pfm2.setId(new Long(10));
        pfm2.setUuid("10");
        pfm2.setModified(new Date());
        pfm2.setModifiedBy("the moderator");
        pfm2.setShortDescription("sort desc here...");
        pfm2.setTitle("disc forum 2");
        pfm2.setTopics(getDiscussionTopics());
        pfm2.setType(new TypeModelImpl());
        pfm2.setAutoForward(Boolean.FALSE);
        pfm2.setAutoForwardEmail(null);
        pfm2.setPreviewPaneEnabled(Boolean.FALSE);
        pfm2.setSortIndex(new Integer(1));
        privateForums.add(pfm1);
        privateForums.add(pfm2);       
        return privateForums;
    }
    
    public List getDiscussionTopics() {
        List discussionTopics = new ArrayList();
        DiscussionTopicModel dtm1 = new DiscussionTopicModelImpl();
        dtm1.setActorPermissions(getActorPermissions());
        dtm1.setAttachments(getAttachments());
        dtm1.setControlPermissions(getControlPermissions());
        dtm1.setCreated(new Date());
        dtm1.setCreatedBy("joe johnson");
        dtm1.setDateRestrictions(getDateRestrictions());
        dtm1.setExtendedDescription("the extended description");
        dtm1.setId(new Long(11));
        dtm1.setUuid("11");
        dtm1.setLabels(getLabels());
        dtm1.setLocked(Boolean.FALSE);
        dtm1.setMessagePermissions(getMessgePermissions());
        dtm1.setModerated(Boolean.TRUE);
        dtm1.setModified(new Date());
        dtm1.setModifiedBy("the moderator");
        dtm1.setShortDescription("sort desc here...");
        dtm1.setTitle("disc topic 1");
        dtm1.setType(new TypeModelImpl());
        dtm1.setConfidentialResponses(Boolean.TRUE);
        dtm1.setGradebook("gb2-1");
        dtm1.setGradebookAssignment("asst2");
        dtm1.setHourBeforeResponsesVisible(new Integer(2));
        dtm1.setMustRespondBeforeReading(Boolean.TRUE);
        dtm1.setMutable(Boolean.TRUE);
        dtm1.setSortIndex(new Integer(1));
        DiscussionTopicModel dtm2 = new DiscussionTopicModelImpl();
        dtm2.setActorPermissions(getActorPermissions());
        dtm2.setAttachments(getAttachments());
        dtm2.setControlPermissions(getControlPermissions());
        dtm2.setCreated(new Date());
        dtm2.setCreatedBy("joe jones");
        dtm2.setDateRestrictions(getDateRestrictions());
        dtm2.setExtendedDescription("the extended description");
        dtm2.setId(new Long(12));
        dtm2.setUuid("12");
        dtm2.setLabels(getLabels());
        dtm2.setLocked(Boolean.TRUE);
        dtm2.setMessagePermissions(getMessgePermissions());
        dtm2.setModerated(Boolean.FALSE);
        dtm2.setModified(new Date());
        dtm2.setModifiedBy("the moderator");
        dtm2.setShortDescription("sort desc here...");
        dtm2.setTitle("disc topic 2");
        dtm2.setType(new TypeModelImpl());
        dtm2.setConfidentialResponses(Boolean.FALSE);
        dtm2.setGradebook("gb2-1");
        dtm2.setGradebookAssignment("asst2");
        dtm2.setHourBeforeResponsesVisible(new Integer(1));
        dtm2.setMustRespondBeforeReading(Boolean.FALSE);
        dtm2.setMutable(Boolean.FALSE);
        dtm2.setSortIndex(new Integer(2));
        discussionTopics.add(dtm1);
        discussionTopics.add(dtm2);
        return discussionTopics;
    }

    public List getOpenTopics() {
        List openTopics = new ArrayList();
        OpenTopicModel otm1 = new OpenTopicModelImpl();
        otm1.setAttachments(getAttachments());
        otm1.setControlPermissions(getControlPermissions());
        otm1.setCreated(new Date());
        otm1.setCreatedBy("lisa kins");
        otm1.setExtendedDescription("the extended description");
        otm1.setId(new Long(13));
        otm1.setUuid("13");
        otm1.setLocked(Boolean.FALSE);
        otm1.setMessagePermissions(getMessgePermissions());
        otm1.setModified(new Date());
        otm1.setModifiedBy("the moderator");
        otm1.setShortDescription("sort desc here...");
        otm1.setTitle("open topic 1");
        otm1.setType(new TypeModelImpl());
        otm1.setMutable(Boolean.TRUE);
        otm1.setSortIndex(new Integer(1));
        OpenTopicModel otm2 = new OpenTopicModelImpl();
        otm2.setAttachments(getAttachments());
        otm2.setControlPermissions(getControlPermissions());
        otm2.setCreated(new Date());
        otm2.setCreatedBy("johny adams");
        otm2.setExtendedDescription("the extended description");
        otm2.setId(new Long(14));
        otm2.setUuid("14");
        otm2.setLocked(Boolean.TRUE);
        otm2.setMessagePermissions(getMessgePermissions());
        otm2.setModified(new Date());
        otm2.setModifiedBy("the moderator");
        otm2.setShortDescription("sort desc here...");
        otm2.setTitle("open topic 2");
        otm2.setType(new TypeModelImpl());
        otm2.setMutable(Boolean.FALSE);
        otm2.setSortIndex(new Integer(2));
        openTopics.add(otm1);
        openTopics.add(otm2);        
        return openTopics;
    }

    // helpers -- if someone needs to get access to one of these
    // just make it public... they were created so these object are
    // easy to still in the lists above
    
    private List getAttachments() {
        List attachments = new ArrayList();
        AttachmentModel a1 = new AttachmentModelImpl();
        a1.setAttachmentId("attach1");
        a1.setAttachmentName("file 1.doc");
        a1.setAttachmentSize("24K");
        a1.setAttachmentType("application/msword");
        a1.setAttachmentUrl("http://www.something.com/afile");
        AttachmentModel a2 = new AttachmentModelImpl();
        a2.setAttachmentId("attach2");
        a2.setAttachmentName("file 2.doc");
        a2.setAttachmentSize("243K");
        a2.setAttachmentType("application/msword");
        a2.setAttachmentUrl("http://www.something.com/anotherfile");
        attachments.add(a1);
        attachments.add(a2);        
        return attachments;
    }
    
    private List getLabels() {
        List labels = new ArrayList();
        LabelModel l1 = new LabelModelImpl();
        l1.setKey("group-key");
        l1.setValue("group");
        LabelModel l2 = new LabelModelImpl();
        l2.setKey("partner-key");
        l2.setValue("partner");
        LabelModel l3 = new LabelModelImpl();
        l3.setKey("alone-key");
        l3.setValue("alone");
        labels.add(l1);
        labels.add(l2);
        labels.add(l3);
        return labels;
    }
    
    private ActorPermissionsModel getActorPermissions() {
        ActorPermissionsModel apm = new ActorPermissionsModelImpl();
        // TODO: Not sure how sakai handles users - empty lists for now
        apm.setAccessors(new ArrayList());
        apm.setContributors(new ArrayList());
        apm.setModerators(new ArrayList());
        apm.setId(new Long(123));
        return null;
    }
    
    private ControlPermissionsModel getControlPermissions() {
        ControlPermissionsModel cpm = new ControlPermissionsModelImpl();
        cpm.setChangeSettings(Boolean.TRUE);
        cpm.setId(new Long(234));
        cpm.setMovePostings(Boolean.TRUE);
        cpm.setNewResponse(Boolean.TRUE);
        cpm.setNewTopic(Boolean.TRUE);
        cpm.setResponseToResponse(Boolean.TRUE);
        cpm.setRole("Not sure what sakai roles are");
        return cpm;
    }

    private DateRestrictionsModel getDateRestrictions() {
        DateRestrictionsModel drm = new DateRestrictionsModelImpl();
        drm.setHidden(new Date());
        drm.setHiddenPostOnSchedule(Boolean.TRUE);
        drm.setId(new Long(22));
        drm.setPostingAllowed(new Date());
        drm.setPostingAllowedPostOnSchedule(Boolean.TRUE);
        drm.setReadOnly(new Date());
        drm.setReadOnlyPostOnSchedule(Boolean.TRUE);
        drm.setVisible(new Date());
        drm.setVisiblePostOnSchedule(Boolean.TRUE);
        return drm;
    }

    private MessagePermissionsModel getMessgePermissions() {
        MessagePermissionsModel mpm = new MessagePermissionsModelImpl();
        mpm.setDeleteAny(Boolean.TRUE);
        mpm.setDeleteOwn(Boolean.TRUE);
        mpm.setDoNew(Boolean.TRUE);
        mpm.setId(new Long(22));
        mpm.setRead(Boolean.TRUE);
        mpm.setReadDrafts(Boolean.TRUE);
        mpm.setReviseAny(Boolean.TRUE);
        mpm.setReviseOwn(Boolean.TRUE);
        mpm.setRole("Not sure what sakai roles are");
        return mpm;
    }
    
}
