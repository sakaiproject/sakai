package org.sakaiproject.poll.repository;

import java.util.List;

import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface VoteRepository extends SpringCrudRepository<Vote, Long> {

    List<Vote> findByPollId(String pollId);

    List<Vote> findByPollIdAndPollOption(String pollId, Long optionId);

    List<Vote> findByUserId(String userId);

    List<Vote> findByUserIdAndPollIds(String userId, List<String> pollIds);

    boolean existsByPollIdAndUserId(String pollId, String userId);

    int countDistinctSubmissionIds(String pollId);
}
