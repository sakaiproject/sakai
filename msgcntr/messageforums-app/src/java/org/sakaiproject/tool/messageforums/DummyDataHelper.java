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

package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.ControlPermissions;
import org.sakaiproject.api.app.messageforums.DateRestrictions;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.OpenTopic;
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
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;

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
        Area a1 = new AreaImpl();
        a1.setContextId("context1");
        a1.setHidden(Boolean.FALSE);
        a1.setName("My Areas");
        a1.setCreated(new Date());
        a1.setCreatedBy("joe smith");
        a1.setModified(new Date());
        a1.setModifiedBy("amy jones");
        a1.setId(new Long(1));
        a1.setUuid("1");
        Area a2 = new AreaImpl();
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
        Message mm1 = new MessageImpl();
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
        Message mm2 = new MessageImpl();
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
        PrivateMessage pmm1 = new PrivateMessageImpl();
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
        pmm1.setRecipients(new HashSet()); // TODO: Real sakai users needed
        PrivateMessage pmm2 = new PrivateMessageImpl();
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
        pmm2.setRecipients(new HashSet()); // TODO: Real sakai users needed
        privateMessages.add(pmm1);
        privateMessages.add(pmm2);       
        return privateMessages;
    }
    
    public List getDiscussionForums() {
        List dicussionForums = new ArrayList();
        DiscussionForum dfm1 = new DiscussionForumImpl();
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
        dfm1.setTopics(list2set(getDiscussionTopics()));
     //   dfm1.setType(new Type());
        DiscussionForum dfm2 = new DiscussionForumImpl();
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
        dfm2.setTopics(list2set(getDiscussionTopics()));
        //dfm2.setType(new TypeImpl());
        dicussionForums.add(dfm1);
        dicussionForums.add(dfm2);
        return dicussionForums;
    }
    
    public List getPrivateForums() {
        List privateForums = new ArrayList();
        PrivateForum pfm1 = new PrivateForumImpl();
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
        pfm1.setTopics(list2set(getDiscussionTopics()));
      //  pfm1.setType(new TypeImpl());
        pfm1.setAutoForward(Boolean.TRUE);
        pfm1.setAutoForwardEmail("fish@indiana.edu");
        pfm1.setPreviewPaneEnabled(Boolean.TRUE);
        pfm1.setSortIndex(new Integer(2));
        PrivateForum pfm2 = new PrivateForumImpl();
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
        pfm2.setTopics(list2set(getDiscussionTopics()));
      //  pfm2.setType(new TypeImpl());
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
        DiscussionTopic dtm1 = new DiscussionTopicImpl();
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
       // dtm1.setType(new TypeImpl());
        dtm1.setConfidentialResponses(Boolean.TRUE);
        dtm1.setGradebook("gb2-1");
        dtm1.setGradebookAssignment("asst2");
        dtm1.setHourBeforeResponsesVisible(new Integer(2));
        dtm1.setMustRespondBeforeReading(Boolean.TRUE);
        dtm1.setMutable(Boolean.TRUE);
        dtm1.setSortIndex(new Integer(1));
        DiscussionTopic dtm2 = new DiscussionTopicImpl();
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
     //   dtm2.setType(new TypeImpl());
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

    // helpers -- if someone needs to get access to one of these
    // just make it public... they were created so these object are
    // easy to still in the lists above
    
    private Set getAttachments() {
        Set attachments = new HashSet();
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
    
    private Set getLabels() {
        Set labels = new HashSet();
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
    
    private ActorPermissions getActorPermissions() {
        ActorPermissions apm = new ActorPermissionsImpl();
        // TODO: Not sure how sakai handles users - empty lists for now
        apm.setAccessors(new HashSet());
        apm.setContributors(new HashSet());
        apm.setModerators(new HashSet());
        apm.setId(new Long(123));
        return null;
    }
    
    private ControlPermissions getControlPermissions() {
        ControlPermissions cpm = new ControlPermissionsImpl();
        cpm.setChangeSettings(Boolean.TRUE);
        cpm.setId(new Long(234));
        cpm.setMovePostings(Boolean.TRUE);
        cpm.setNewResponse(Boolean.TRUE);
        cpm.setNewTopic(Boolean.TRUE);
        cpm.setResponseToResponse(Boolean.TRUE);
        cpm.setRole("Not sure what sakai roles are");
        return cpm;
    }

    private DateRestrictions getDateRestrictions() {
        DateRestrictions drm = new DateRestrictionsImpl();
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

    private MessagePermissions getMessgePermissions() {
        MessagePermissions mpm = new MessagePermissionsImpl();
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
    
    private Set list2set(List list) {
        Set set = new HashSet();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object object = (Object) iter.next();
            set.add(object);
        }
        return set;
    }
    
}
