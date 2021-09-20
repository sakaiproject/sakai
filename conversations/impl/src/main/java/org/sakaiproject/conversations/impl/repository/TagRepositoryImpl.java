package org.sakaiproject.conversations.impl.repository;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.repository.TagRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class TagRepositoryImpl extends SpringCrudRepositoryImpl<Tag, Long>  implements TagRepository {

    @Transactional
    public List<Tag> findBySiteId(String siteId) {

        return (List<Tag>) sessionFactory.getCurrentSession().createCriteria(Tag.class)
            .add(Restrictions.eq("siteId", siteId))
            .list();
    }
}
