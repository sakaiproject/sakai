package org.sakaiproject.poll.repository;

import java.util.List;

import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface VoteRepository extends SpringCrudRepository<Vote, Long> {

    List<Vote> findByPollId(Long pollId);

    List<Vote> findByPollIdAndPollOption(Long pollId, Long optionId);

    List<Vote> findByUserId(String userId);

    List<Vote> findByUserIdAndPollIds(String userId, List<Long> pollIds);

    boolean existsByPollIdAndUserId(Long pollId, String userId);

    int countDistinctSubmissionIds(Long pollId);
}
