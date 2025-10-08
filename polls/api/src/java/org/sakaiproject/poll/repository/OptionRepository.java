package org.sakaiproject.poll.repository;

import java.util.List;

import org.sakaiproject.poll.model.Option;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface OptionRepository extends SpringCrudRepository<Option, Long> {

    List<Option> findByPollIdOrderByOptionOrder(Long pollId);
}
