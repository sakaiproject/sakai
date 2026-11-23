/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class MessageForums extends AbstractWebService {

    private UIPermissionsManager uiPermissionsManager;

    @WebMethod(exclude = true)
    public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
        this.uiPermissionsManager = uiPermissionsManager;
    }

    /**
     * Key in the ThreadLocalManager for binding our current placement.
     */
    protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

    /**
     * Key in the ThreadLocalManager for binding our current tool.
     */
    protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";

    /**
     * Adds a message to an existing forum or if there are no forums to add, adds a forum
     * and then adds a message.
     *
     * @param sessionid the session to use
     * @param context   the context to use
     * @param forum     the forum title
     * @param user      the legacy author parameter
     * @param title     the message title
     * @param body      the message body
     * @return the sessionid if active, or "null" if not.
     */
    @WebMethod
    @Path("/addMessage")
    @Produces("text/plain")
    @GET
    public String addMessage(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context,
            @WebParam(name = "forum", partName = "forum") @QueryParam("forum") String forum,
            @WebParam(name = "topic", partName = "topic") @QueryParam("topic") String topic,
            @WebParam(name = "user", partName = "user") @QueryParam("user") String user,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "body", partName = "body") @QueryParam("body") String body) {
        Session session = establishSession(sessionid);
        String userId = session.getUserId();
        if (StringUtils.isBlank(userId)) {
            return "Failure";
        }

        Object previousPlacement = threadLocalManager.get(CURRENT_PLACEMENT);
        Object previousTool = threadLocalManager.get(CURRENT_TOOL);

        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSiteVisit(context);
            User author = userDirectoryService.getUser(userId);
            String authorName = StringUtils.defaultIfBlank(author.getDisplayName(), author.getDisplayId());

            ToolConfiguration tool = site.getToolForCommonId("sakai.forums");

            if (tool == null) {
                return "Tool sakai.forums not found in site=" + context;
            }

            // Lets go down and hack our essense into the thread
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());

            List<DiscussionForum> forums = messageForumsForumManager.getForumsForSite(site.getId());

            DiscussionTopic selectedTopic = null;
            DiscussionTopic anyTopic = null;
            DiscussionForum selectedForum = null;
            DiscussionForum anyForum = null;
            DiscussionTopic dTopic = null;

            for (DiscussionForum dForum : forums) {
                anyForum = dForum;
                if (forum.equals(dForum.getTitle())) selectedForum = dForum;
                log.debug("forum = " + dForum + " ID=" + dForum.getId());
            }

            if (selectedForum == null) selectedForum = anyForum;
            if (selectedForum == null) {
                if (!uiPermissionsManager.isNewForum()) {
                    return "Failure";
                }

                Area area = areaManager.getAreaByContextIdAndTypeId(site.getId(), messageForumsTypeManager.getDiscussionForumType());

                selectedForum = messageForumsForumManager.createDiscussionForum();
                selectedForum.setArea(area);
                selectedForum.setCreatedBy(userId);
                selectedForum.setTitle(forum);
                selectedForum.setDraft(false);
                selectedForum.setModerated(false);
                selectedForum.setPostFirst(false);
                if (!uiPermissionsManager.isNewTopic(selectedForum)) {
                    return "Failure";
                }

                if (area == null) {
                    area = areaManager.createArea(messageForumsTypeManager.getDiscussionForumType(), site.getId());
                    area.setName("AREA 51");
                    area.setEnabled(Boolean.TRUE);
                    area.setHidden(Boolean.TRUE);
                    area.setLocked(Boolean.FALSE);
                    area.setModerated(Boolean.FALSE);
                    area.setPostFirst(Boolean.FALSE);
                    area.setAutoMarkThreadsRead(false);
                    area.setSendEmailOut(Boolean.TRUE);
                    area.setAvailabilityRestricted(Boolean.FALSE);
                    area = areaManager.saveArea(area);
                    log.debug("Created area...");
                }

                selectedForum.setArea(area);
                selectedForum = messageForumsForumManager.saveDiscussionForum(selectedForum);
                log.debug("Created forum={}", forum);
                dTopic = messageForumsForumManager.createDiscussionForumTopic(selectedForum);
                dTopic.setTitle(topic);
                dTopic.setCreatedBy(userId);
                messageForumsForumManager.saveDiscussionForumTopic(dTopic, false);
                log.debug("Created topic=" + topic);
                forums = messageForumsForumManager.getForumsForSite(site.getId());
                selectedForum = null;
                for (DiscussionForum dForum : forums) {
                    anyForum = dForum;
                    if (forum.equals(dForum.getTitle())) selectedForum = dForum;
                    log.debug("forum = " + dForum + " ID=" + dForum.getId());
                }
            }

            if (selectedForum == null) selectedForum = anyForum;
            if (selectedForum == null) return "No forums found in site=" + context;

            for (Object o : selectedForum.getTopicsSet()) {
                dTopic = (DiscussionTopic) o;
                anyTopic = dTopic;
                if (topic.equals(dTopic.getTitle())) selectedTopic = dTopic;
                if (dTopic.getDraft().equals(Boolean.FALSE)) {
                    log.debug("Topic ID=" + dTopic.getId() + " title=" + dTopic.getTitle());
                }
            }

            if (selectedTopic == null) selectedTopic = anyTopic;
            if (selectedTopic == null) return "No topic";

            if (Boolean.TRUE.equals(selectedForum.getDraft()) || Boolean.TRUE.equals(selectedTopic.getDraft())
                    || Boolean.FALSE.equals(selectedForum.getAvailability()) || Boolean.FALSE.equals(selectedTopic.getAvailability())
                    || !uiPermissionsManager.isRead(selectedTopic, selectedForum, userId, site.getId())) {
                return "Failure";
            }

            boolean moderated = Boolean.TRUE.equals(selectedTopic.getModerated());
            boolean canModerate = moderated && uiPermissionsManager.isModeratePostings(selectedTopic, selectedForum, userId, site.getId());
            DiscussionTopic topicWithMsgs = (DiscussionTopic) discussionForumManager.getTopicByIdWithMessages(selectedTopic.getId());
            Message replyMessage = null;
            List<?> messages = topicWithMsgs.getMessages();
            if (messages != null) {
                for (Object entry : messages) {
                    Message message = (Message) entry;
                    if (!Boolean.TRUE.equals(message.getDraft()) && !Boolean.TRUE.equals(message.getDeleted())
                            && (!moderated || Boolean.TRUE.equals(message.getApproved()) || canModerate
                                    || userId.equals(message.getCreatedBy()))) {
                        replyMessage = message;
                    }
                }
            }
            boolean canPost = replyMessage == null
                    ? uiPermissionsManager.isNewResponse(selectedTopic, selectedForum, userId, site.getId())
                    : uiPermissionsManager.isNewResponseToResponse(selectedTopic, selectedForum, userId, site.getId());
            if (!canPost) {
                return "Failure";
            }

            Message aMsg;
            aMsg = messageForumsMessageManager.createDiscussionMessage();
            aMsg.setTitle(title);
            aMsg.setBody(body);
            aMsg.setAuthor(authorName);
            aMsg.setDraft(Boolean.FALSE);
            aMsg.setDeleted(Boolean.FALSE);
            if (!moderated || canModerate) {
                aMsg.setApproved(Boolean.TRUE);
            }
            aMsg.setTopic(selectedTopic);
            if (replyMessage != null) {
                aMsg.setInReplyTo(replyMessage);
            }
            discussionForumManager.saveMessage(aMsg);
            return "Success";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            threadLocalManager.set(CURRENT_PLACEMENT, previousPlacement);
            threadLocalManager.set(CURRENT_TOOL, previousTool);
        }

        return "Failure";
    }

}
