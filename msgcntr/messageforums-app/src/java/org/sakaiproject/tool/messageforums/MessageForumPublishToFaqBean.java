/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@ManagedBean(name = MessageForumPublishToFaqBean.NAME)
@SessionScoped
@EqualsAndHashCode(callSuper = false)
public class MessageForumPublishToFaqBean extends SpringBeanAutowiringSupport implements Serializable {


    private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
    private static final Integer TITLE_MAX_LENGTH = 255;

    private String title;
    private String question;
    private String answer;
    private String siteId;
    private String userId;
    private Area discussionArea;
    private ResourceBundle resourceBundle;

    @Setter(AccessLevel.NONE)
    private Boolean canPost;
    @Setter(AccessLevel.NONE)
    private Boolean canReply;

    @Autowired
    private AreaManager areaManager;

    @Autowired
    private MessageForumsForumManager forumManager;

    @Autowired
    private MessageForumsMessageManager messageManager;

    @Autowired
    private DiscussionForumManager discussionForumManager;

    @Autowired
    private UIPermissionsManager permissionsManager;

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private SiteService siteService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    public static final String NAME = "mfPublishToFaqBean";


    @PostConstruct
    public void init() {
        log.debug("Initializing MessageForumPublishToFaqBean" + "(" + NAME + ")");

        Objects.requireNonNull(areaManager);
        Objects.requireNonNull(forumManager);
        Objects.requireNonNull(messageManager);
        Objects.requireNonNull(discussionForumManager);
        Objects.requireNonNull(permissionsManager);
        Objects.requireNonNull(toolManager);
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(userDirectoryService);

        siteId = toolManager.getCurrentPlacement().getContext();
        userId = userDirectoryService.getCurrentUser().getId();
        discussionArea = areaManager.getDiscussionArea(siteId);
        resourceBundle = ResourceBundle.getBundle(MESSAGECENTER_BUNDLE,
                siteService.getSiteLocale(siteId).orElse(Locale.getDefault()));

        DiscussionForum faqForum = forumManager.getFaqForumForArea(discussionArea);
        DiscussionTopic faqTopic = forumManager.getFaqTopicForForum(faqForum);
        if (faqForum != null) {
            if (faqTopic != null) {
                canPost = permissionsManager.isNewResponse(faqTopic, faqForum);
                canReply = permissionsManager.isNewResponseToResponse(faqTopic, faqForum);
                log.debug("FAQ Forum and FAQ Topic present; canPost = {}; canReply = {};");
            } else {
                canPost = canReply = permissionsManager.isNewTopic(faqForum);
                log.debug("FAQ Forum, but no FAQ Topic present; Can create new Topic? canPost = canReply = {};", canPost);
            }
        } else {
            canPost = canReply = permissionsManager.isNewForum();
            log.debug("No FAQ Forum present; Can create new Forum? canPost = canReply = {};", canPost);
        }
    }

    public void setMessage(Message message) {
        if (message != null) {
            title = message.getTitle();
            question = message.getBody();
        } else {
            title = null;
            question = null;
        }
        answer = null;
    }

    public void publishToFaq() {
        DiscussionForum faqForum = forumManager.getOrCreateFaqForumForArea(discussionArea);
        DiscussionTopic faqTopic = forumManager.getOrCreateFaqTopicForForum(faqForum);

        log.debug("Creating question message for Forum [{}] and Topic [{}]", faqForum, faqTopic);

        if (!Boolean.TRUE.equals(canPost)) {
            log.warn("User with id [{}] does not have permissions to create a new forum post", userId);
            return;
        }

        String questionTitlePrefix = resourceBundle.getString("pvt_question_title_prefix");
        String answerTitlePrefix = resourceBundle.getString("pvt_answer_title_prefix");
        // Use the longer prefix for the length, + 1 for the space character
        int prefixLength = Math.max(questionTitlePrefix.length(), answerTitlePrefix.length()) + 1;
        String title = StringUtils.substring(StringUtils.trim(this.title), 0, TITLE_MAX_LENGTH - prefixLength - 1);
        String question = StringUtils.trim(this.question);

        Message createdQuestionMessage = messageManager.createDiscussionMessage();
        createdQuestionMessage.setAuthor(userId);
        createdQuestionMessage.setTitle(questionTitlePrefix + " " + title);
        createdQuestionMessage.setBody(question);
        createdQuestionMessage.setTopic(faqTopic);
        createdQuestionMessage.setDeleted(Boolean.FALSE);
        createdQuestionMessage.setApproved(Boolean.TRUE);

        Message savedQuestionMessage = discussionForumManager.saveMessage(createdQuestionMessage);
        discussionForumManager.markMessageNotReadStatusForUser(savedQuestionMessage, true, userId);

        String answer = StringUtils.trim(this.answer);
        if (Boolean.TRUE.equals(canReply) && StringUtils.isNotEmpty(answer)) {
            log.debug("Creating answer message");

            Message createdAnswerMessage = messageManager.createDiscussionMessage();
            createdAnswerMessage.setAuthor(userId);
            createdAnswerMessage.setTitle(answerTitlePrefix + " " + title);
            createdAnswerMessage.setBody(answer);
            createdAnswerMessage.setTopic(faqTopic);
            createdAnswerMessage.setDeleted(Boolean.FALSE);
            createdAnswerMessage.setApproved(Boolean.TRUE);
            createdAnswerMessage.setInReplyTo(savedQuestionMessage);

            Message savedAnswerMessage = discussionForumManager.saveMessage(createdAnswerMessage);
            discussionForumManager.markMessageNotReadStatusForUser(savedAnswerMessage, true, userId);
        } else {
            log.debug("Not creating answer message");
        }
    }

}
