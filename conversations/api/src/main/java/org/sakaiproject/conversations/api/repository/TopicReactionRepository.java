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

import org.sakaiproject.conversations.api.model.TopicReaction;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TopicReactionRepository extends SpringCrudRepository<TopicReaction, Long> {

    List<TopicReaction> findByTopic_IdAndUserId(String topicId, String userId);
    Integer deleteByTopic_Id(String topicId);
}
