package org.sakaiproject.poll.api.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PollRepository extends SpringCrudRepository<Poll, Long> {

    List<Poll> findAllOrderByCreationDateAsc();

    List<Poll> findBySiteIdOrderByCreationDate(String siteId, boolean asc);

    List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds, boolean asc);

    List<Poll> findOpenPollsForSites(List<String> siteIds, Date now, boolean asc);

    Optional<Poll> findByPollId(Long pollId);

    Optional<Poll> findByUuid(String uuid);
}

