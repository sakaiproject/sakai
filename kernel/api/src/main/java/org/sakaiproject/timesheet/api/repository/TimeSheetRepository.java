package org.sakaiproject.timesheet.api.repository;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.springframework.data.SpringCrudRepository;
import org.sakaiproject.timesheet.api.TimeSheetEntry;

public interface TimeSheetRepository extends SpringCrudRepository<TimeSheetEntry, Long> {
    Optional<List<TimeSheetEntry>> findByReference(String reference);
    Optional<List<TimeSheetEntry>> findAllByUserIdAndReference(String userId, String reference);
}
