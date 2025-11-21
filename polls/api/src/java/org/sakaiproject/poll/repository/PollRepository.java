package org.sakaiproject.poll.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface PollRepository extends SpringCrudRepository<Poll, Long> {

    List<Poll> findBySiteIdOrderByCreationDateDesc(String siteId);

    List<Poll> findBySiteIdsOrderByCreationDate(List<String> siteIds);

    List<Poll> findOpenPollsBySiteIds(List<String> siteIds, Date now);

    Optional<Poll> findByUuid(String uuid);
}
