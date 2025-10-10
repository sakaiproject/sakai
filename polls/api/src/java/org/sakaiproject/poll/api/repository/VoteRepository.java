package org.sakaiproject.poll.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface VoteRepository extends SpringCrudRepository<Vote, Long> {

    List<Vote> findByPollId(Long pollId);

    List<Vote> findByPollIdAndOptionId(Long pollId, Long optionId);

    List<Vote> findByUserIdAndPollIds(String userId, List<Long> pollIds);

    List<Vote> findByUserIdAndPollId(String userId, Long pollId);

    Optional<Vote> findByVoteId(Long voteId);

    int countDistinctSubmissionIdByPollId(Long pollId);
}

