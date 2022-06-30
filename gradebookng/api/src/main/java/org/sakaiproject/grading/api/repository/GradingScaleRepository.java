package org.sakaiproject.grading.api.repository;

import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.sakaiproject.grading.api.model.GradingScale;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface GradingScaleRepository extends SpringCrudRepository<GradingScale, Long> {

    List<GradingScale> findByUnavailable(Boolean unavailable);
    List<GradingScale> findByUnavailableAndUidNotIn(Boolean unavailable, Set<String> notTheseUids);
    List<GradingScale> findByUidIn(Set<String> theseUids);
}
