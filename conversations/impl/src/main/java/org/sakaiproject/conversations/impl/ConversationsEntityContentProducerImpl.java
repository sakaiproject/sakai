/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsEvents;
import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import static org.sakaiproject.conversations.api.ConversationsReferenceReckoner.ConversationsReference;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.model.ConversationsComment;
import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.conversations.api.model.ConversationsTopic;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.conversations.api.repository.TagRepository;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversationsEntityContentProducerImpl implements EntityContentProducer {

    @Autowired private ConversationsCommentRepository commentRepository;
    @Autowired private ConversationsService conversationsService;
    @Autowired private ConversationsPostRepository postRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private ConversationsTopicRepository topicRepository;
    @Autowired private UserDirectoryService userDirectoryService;

    @Setter
    private SearchIndexBuilder searchIndexBuilder;
    @Autowired
    private SearchService searchService;
    @Autowired
    private ServerConfigurationService serverConfigurationService;

    private List<String> addingEvents = new ArrayList<>();
    private List<String> deletingEvents = new ArrayList<>();
    private List<String> refreshingEvents = new ArrayList<>();

    public void init() {

        if ("true".equals(serverConfigurationService.getString("search.enable", "false"))) {
            addingEvents.add(ConversationsEvents.TOPIC_CREATED.label);
            addingEvents.add(ConversationsEvents.POST_CREATED.label);
            addingEvents.add(ConversationsEvents.COMMENT_CREATED.label);
            deletingEvents.add(ConversationsEvents.TOPIC_DELETED.label);
            deletingEvents.add(ConversationsEvents.POST_DELETED.label);
            deletingEvents.add(ConversationsEvents.COMMENT_DELETED.label);
            refreshingEvents.add(ConversationsEvents.TOPIC_UPDATED.label);
            refreshingEvents.add(ConversationsEvents.POST_UPDATED.label);
            refreshingEvents.add(ConversationsEvents.COMMENT_UPDATED.label);
            addingEvents.forEach(searchService::registerFunction);
            refreshingEvents.forEach(searchService::registerFunction);
            deletingEvents.forEach(searchService::registerFunction);

            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    public Integer getAction(Event event) {

        log.debug("getAction({})", event.getEvent());

        String evt = event.getEvent();

        if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
        if (refreshingEvents.contains(evt)) return SearchBuilderItem.ACTION_REFRESH;
        if (deletingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;

        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    public boolean matches(String reference) {

        log.debug("matches({})", reference);

        return reference.startsWith(ConversationsService.REFERENCE_ROOT);
    }

    public boolean matches(Event event) {

        log.debug("matches({})", event.getEvent());

        String evt = event.getEvent();
        return addingEvents.contains(evt) || deletingEvents.contains(evt) || refreshingEvents.contains(evt);
    }

    public boolean canRead(String reference) {

        log.debug("canRead({})", reference);

        ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();
        switch (ref.getType()) {
            case "t":
                return conversationsService.currentUserCanViewTopic(topicRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid topic reference: " + reference)));
            case "p":
                return conversationsService.currentUserCanViewPost(postRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid post reference: " + reference)));
            case "c":
                return conversationsService.currentUserCanViewComment(commentRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid comment reference: " + reference)));
            default:
                return false;
        }
    }

    public String getId(String reference) {

        return ConversationsReferenceReckoner.reckoner().reference(reference)
            .reckon().getId();
    }

    public String getTool() {
        return ConversationsService.TOOL_ID;
    }

    public String translateTitle(String title) {
        return title;
    }

    public String getType(String reference) {

        ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();

        switch (ref.getType()) {
            case "t":
                return "topic";
            case "p":
                return "post";
            case "c":
                return "comment";
            default:
                return "";
        }
    }

    public String getSiteId(String reference) {

        log.debug("getSiteId({})", reference);

        return ConversationsReferenceReckoner.reckoner().reference(reference).reckon().getSiteId();
    }

    public Iterator<String> getSiteContentIterator(String siteId) {

        List<String> ids = topicRepository.findBySiteId(siteId).stream().map(t -> {
            return ConversationsReferenceReckoner.reckoner().topic(t).reckon().toString();
        }).collect(Collectors.toList());

        ids.addAll(postRepository.findBySiteId(siteId).stream().map(p -> {
            return ConversationsReferenceReckoner.reckoner().post(p).reckon().toString();
        }).collect(Collectors.toList()));

        ids.addAll(commentRepository.findBySiteId(siteId).stream().map(c -> {
            return ConversationsReferenceReckoner.reckoner().comment(c).reckon().toString();
        }).collect(Collectors.toList()));

        return ids.iterator();
    }

    public String getUrl(String reference) {

        log.debug("getUrl({})", reference);

        ConversationsReference cr = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();
        switch (cr.getType()) {
            case "t":
                return conversationsService.getTopicPortalUrl(cr.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No url for reference: " + reference));
            case "p":
                return conversationsService.getPostPortalUrl(null, cr.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No url for reference: " + reference));
            case "c":
                return conversationsService.getCommentPortalUrl(cr.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No url for reference: " + reference));
            default:
        }
        return "";
    }

    public String getTitle(String reference) {

        ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();
        StringBuilder sb = new StringBuilder();
        String creator = "";
        switch (ref.getType()) {
            case "t":
                ConversationsTopic topic = topicRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid topic reference: " + reference));
                if (!topic.getDraft() && !topic.getHidden()) {
                    sb.append(topic.getTitle());
                    creator = topic.getMetadata().getCreator();
                }
                break;
            case "p":
                ConversationsPost post = postRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid post reference: " + reference));
                if (!post.getDraft() && !post.getHidden()) {
                    sb.append("Post");
                    creator = post.getMetadata().getCreator();
                }
                break;
            case "c":
                ConversationsComment comment = commentRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid comment reference: " + reference));
                sb.append("Comment");
                creator = comment.getMetadata().getCreator();
            default:
        }
        return sb.toString();
    }

    public String getContent(String reference) {

        log.debug("getContent({})", reference);

        ConversationsReference ref = ConversationsReferenceReckoner.reckoner().reference(reference).reckon();
        StringBuilder sb = new StringBuilder();
        switch (ref.getType()) {
            case "t":
                ConversationsTopic topic = topicRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid topic reference: " + reference));
                if (!topic.getDraft() && !topic.getHidden()) {
                    sb.append(Jsoup.parse(topic.getMessage()).text());
                    topic.getTagIds().forEach(tagId -> {
                        tagRepository.findById(tagId).ifPresent(t -> sb.append(" ").append(t.getLabel()));
                    });
                }
                break;
            case "p":
                ConversationsPost post = postRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid post reference: " + reference));
                if (!post.getDraft() && !post.getHidden()) {
                    sb.append(Jsoup.parse(post.getMessage()).text());
                }
                break;
            case "c":
                ConversationsComment comment = commentRepository.findById(ref.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid comment reference: " + reference));
                sb.append(Jsoup.parse(comment.getMessage()).text());
                break;
            default:
        }
        return sb.toString();
    }
}
