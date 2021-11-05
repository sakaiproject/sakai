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

import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import org.springframework.data.domain.Pageable;

public interface PostRepository extends SpringCrudRepository<Post, String> {

    List<Post> findByTopic_Id(String topicId);
    List<Post> findByTopic_IdAndParentPostIdIsNull(String topicId);
    List<Post> findByParentPostId(String parentPostId);
    Long countByParentPostId(String parentPostId);
    List<Post> findByParentThreadId(String parentThreadId);
    Integer deleteByTopic_Id(String topicId);
    Integer lockByTopic_Id(Boolean locked, String topicId);
    Integer lockByParentPostId(Boolean locked, String parentPostId);
    Integer lockBySiteId(String siteId, Boolean locked);
}
