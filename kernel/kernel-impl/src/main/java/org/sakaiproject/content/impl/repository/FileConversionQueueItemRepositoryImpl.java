/**********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2013 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.content.api.repository.FileConversionQueueItemRepository;
import org.sakaiproject.content.hbm.FileConversionQueueItem;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
@Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
public class FileConversionQueueItemRepositoryImpl extends SimpleJpaRepository<FileConversionQueueItem, Long> implements FileConversionQueueItemRepository {

    private final EntityManager entityManager;

    public FileConversionQueueItemRepositoryImpl(EntityManager entityManager) {
        super(FileConversionQueueItem.class, entityManager);
        this.entityManager = entityManager;
    }

    public FileConversionQueueItemRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this(SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory));
    }

    @Override
    public List<FileConversionQueueItem> findByStatus(FileConversionQueueItem.Status status) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileConversionQueueItem> query = builder.createQuery(FileConversionQueueItem.class);
        Root<FileConversionQueueItem> root = query.from(FileConversionQueueItem.class);
        query.select(root).where(builder.equal(root.get("status"), status));
        return entityManager.createQuery(query).getResultList();
    }

    @Transactional(transactionManager = "jpaTransactionManager")
    @Override
    public <S extends FileConversionQueueItem> S save(S entity) {
        return super.save(entity);
    }

    @Transactional(transactionManager = "jpaTransactionManager")
    @Override
    public void delete(FileConversionQueueItem entity) {
        super.delete(entity);
    }
}
