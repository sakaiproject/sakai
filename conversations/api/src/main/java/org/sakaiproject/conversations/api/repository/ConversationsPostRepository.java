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
package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.ConversationsPost;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import org.springframework.data.domain.Pageable;

public interface ConversationsPostRepository extends SpringCrudRepository<ConversationsPost, String> {

    List<ConversationsPost> findByTopicId(String topicId);
    List<ConversationsPost> findByTopicIdAndParentPostIdIsNull(String topicId);
    List<ConversationsPost> findByTopicIdAndMetadata_Creator(String topicId, String creatorId);
    List<ConversationsPost> findByParentPostId(String parentPostId);
    List<ConversationsPost> findBySiteId(String siteId);
    Long countByParentPostId(String parentPostId);
    List<ConversationsPost> findByParentThreadId(String parentThreadId);
    Integer deleteByTopicId(String topicId);
    Integer lockByTopicId(Boolean locked, String topicId);
    Integer lockByParentPostId(Boolean locked, String parentPostId);
    Integer lockBySiteId(String siteId, Boolean locked);
}
