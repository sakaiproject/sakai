package org.sakaiproject.content.api.persistence;

import java.util.List;

import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface FileConversionServiceRepository extends SpringCrudRepository<FileConversionQueueItem, Long> {

    List<FileConversionQueueItem> findByStatus(FileConversionQueueItem.Status status);
    List<FileConversionQueueItem> findByReference(String reference);
}
