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
package org.sakaiproject.conversations.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.entity.api.Entity;

public interface ConversationsService {

    public static final String TOOL_ID = "sakai.conversations";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "conversations";

    public static final String SORT_NAME_ASCENDING = "nameAscending";
    public static final String SORT_NAME_DESCENDING = "nameDescending";
    public static final String SORT_TOPICS_CREATED_ASCENDING = "topicsCreatedAscending";
    public static final String SORT_TOPICS_CREATED_DESCENDING = "topicsCreatedDescending";
    public static final String SORT_TOPICS_VIEWED_ASCENDING = "topicsViewedAscending";
    public static final String SORT_TOPICS_VIEWED_DESCENDING = "topicsViewedDescending";
    public static final String SORT_POSTS_CREATED_ASCENDING = "postsCreatedAscending";
    public static final String SORT_POSTS_CREATED_DESCENDING = "postsCreatedDescending";
    public static final String SORT_REACTIONS_MADE_ASCENDING = "reactionsMadeAscending";
    public static final String SORT_REACTIONS_MADE_DESCENDING = "reactionsMadeDescending";

    public static final String PROP_THREADS_PAGE_SIZE = "conversations.threads.page.size";
    public static final String PROP_MAX_THREAD_DEPTH = "conversations.max.thread.depth";
    public static final String PROP_DISABLE_DISCUSSIONS = "conversations.disable.discussions";

    public static final String STATS_CACHE_NAME = "conversationsSortedStatsCache";
    public static final String POSTS_CACHE_NAME = "conversationsPostsCache";

    List<TopicTransferBean> getTopicsForSite(String siteId) throws ConversationsPermissionsException;
    Optional<String> getTopicPortalUrl(String topicId);
    TopicTransferBean saveTopic(TopicTransferBean topicBean) throws ConversationsPermissionsException;
    boolean deleteTopic(String topicId) throws ConversationsPermissionsException;
    void pinTopic(String topicId, boolean pinned) throws ConversationsPermissionsException;
    TopicTransferBean lockTopic(String topicId, boolean locked) throws ConversationsPermissionsException;
    void hideTopic(String topicId, boolean hidden) throws ConversationsPermissionsException;
    void bookmarkTopic(String topicId, boolean bookmarked) throws ConversationsPermissionsException;
    Map<Reaction, Integer> saveTopicReactions(String topicId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException;

    PostTransferBean savePost(PostTransferBean postBean) throws ConversationsPermissionsException;
    int getNumberOfThreadPages(String siteId, String topicId) throws ConversationsPermissionsException;
    Collection<PostTransferBean> getPostsByTopicId(String siteId, String topicId, Integer page, PostSort sort, String requestedPostId) throws ConversationsPermissionsException;
    void deletePost(String siteId, String topicId, String postId, boolean setTopicResolved) throws ConversationsPermissionsException;
    PostTransferBean upvotePost(String siteId, String topicId, String postId) throws ConversationsPermissionsException;
    PostTransferBean unUpvotePost(String siteId, String postId) throws ConversationsPermissionsException;
    PostTransferBean lockPost(String siteId, String topicId, String postId, boolean locked) throws ConversationsPermissionsException;
    PostTransferBean hidePost(String siteId, String topicId, String postId, boolean hiddn) throws ConversationsPermissionsException;
    Map<Reaction, Integer> savePostReactions(String topicId, String postId, Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException;
    void markPostsViewed(Set<String> postIds, String topicId) throws ConversationsPermissionsException;

    CommentTransferBean saveComment(CommentTransferBean commentBean) throws ConversationsPermissionsException;
    boolean deleteComment(String siteId, String commentId) throws ConversationsPermissionsException;

    List<Tag> createTags(List<Tag> tags) throws ConversationsPermissionsException;
    Tag saveTag(Tag tag) throws ConversationsPermissionsException;
    List<Tag> getTagsForSite(String siteId) throws ConversationsPermissionsException;
    void deleteTag(Long tagId) throws ConversationsPermissionsException;

    Settings getSettingsForSite(String siteId) throws ConversationsPermissionsException;
    Settings saveSettings(Settings settings) throws ConversationsPermissionsException;

    ConvStatus getConvStatusForSiteAndUser(String siteId, String userId) throws ConversationsPermissionsException;
    void saveConvStatus(ConvStatus convStatus) throws ConversationsPermissionsException;
    Map<String, Object> getSiteStats(String siteId, Instant from, Instant to, int page, String sort) throws ConversationsPermissionsException; 
}
