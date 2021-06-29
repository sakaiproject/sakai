package org.sakaiproject.content.impl.persistence;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.content.api.persistence.FileConversionQueueItem;
import org.sakaiproject.content.api.persistence.FileConversionServiceRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class FileConversionServiceRepositoryImpl extends SpringCrudRepositoryImpl<FileConversionQueueItem, Long>  implements FileConversionServiceRepository {

    @Transactional
    public List<FileConversionQueueItem> findByStatus(FileConversionQueueItem.Status status) {

        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(FileConversionQueueItem.class)
                .add(Restrictions.eq("status", status)).list();
    }

    @Transactional
    public List<FileConversionQueueItem> findByReference(String reference) {

        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(FileConversionQueueItem.class)
                .add(Restrictions.eq("reference", reference)).list();
    }
}
