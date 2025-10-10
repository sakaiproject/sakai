package org.sakaiproject.poll.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface OptionRepository extends SpringCrudRepository<Option, Long> {

    List<Option> findByPollIdOrderByOptionOrder(Long pollId);

    Optional<Option> findByOptionId(Long optionId);
}

