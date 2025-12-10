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
