/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.poll.api.repository;

import java.util.List;

import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface VoteRepository extends SpringCrudRepository<Vote, Long> {

    List<Vote> findByPollId(String pollId);

    List<Vote> findByOptionId(Long optionId);

    List<Vote> findByUserId(String userId);

    List<Vote> findByUserIdAndPollIds(String userId, List<String> pollIds);

    boolean existsByPollIdAndUserId(String pollId, String userId);

    int countDistinctSubmissionIds(String pollId);
}
