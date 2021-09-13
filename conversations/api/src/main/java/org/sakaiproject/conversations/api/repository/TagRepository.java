package org.sakaiproject.conversations.api.repository;

import java.util.List;

import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface TagRepository extends SpringCrudRepository<Tag, Long> {

    List<Tag> findBySiteId(String siteId);
}
