/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.impl;

import java.util.List;
import java.util.Optional;
import javax.persistence.TypedQuery;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class TagRepositoryImpl extends SpringCrudRepositoryImpl<Tag, String> implements TagRepository {
    @Override
    @Transactional
    public Tag create(Tag tag) {
        sessionFactory.getCurrentSession().persist(tag);
        return tag;
    }

    @Override
    public List<Tag> findAllOrdered() {
        return query("from TagServiceTag t order by t.tagLabel, t.tagCollectionId").getResultList();
    }

    @Override
    public List<Tag> findByCollection(String collectionId, int offset, int limit) {
        return query("from TagServiceTag t where t.tagCollectionId = :collection order by t.tagLabel")
            .setParameter("collection", collectionId).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    public List<Tag> findByLabel(String label, String collectionId) {
        return query("from TagServiceTag t where t.tagLabel = :label and t.tagCollectionId = :collection")
            .setParameter("label", label).setParameter("collection", collectionId).getResultList();
    }

    @Override
    public List<Tag> findByPartialLabel(String label) {
        return query("from TagServiceTag t where t.tagLabel like :label order by t.tagCollectionId")
            .setParameter("label", "%" + label + "%").getResultList();
    }

    @Override
    public List<Tag> findByPrefix(String label, int offset, int limit) {
        return query("from TagServiceTag t where lower(t.tagLabel) like lower(:label) order by t.tagLabel, t.tagCollectionId")
            .setParameter("label", label + "%").setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    public Optional<Tag> findByExternalId(String externalId, String collectionId) {
        return findAllByExternalId(externalId, collectionId).stream().findFirst();
    }

    @Override
    public List<Tag> findAllByExternalId(String externalId, String collectionId) {
        return query("from TagServiceTag t where t.externalId = :external and t.tagCollectionId = :collection")
            .setParameter("external", externalId).setParameter("collection", collectionId).getResultList();
    }

    @Override
    public long countByCollection(String collectionId) {
        return sessionFactory.getCurrentSession().createQuery(
            "select count(t) from TagServiceTag t where t.tagCollectionId = :collection", Long.class)
            .setParameter("collection", collectionId).getSingleResult();
    }

    @Override
    public long countByPrefix(String label) {
        return sessionFactory.getCurrentSession().createQuery(
            "select count(t) from TagServiceTag t where lower(t.tagLabel) like lower(:label)", Long.class)
            .setParameter("label", label + "%").getSingleResult();
    }

    @Override
    public List<Tag> findOlderThan(String collectionId, long timestamp) {
        return query("from TagServiceTag t where t.tagCollectionId = :collection and t.lastModificationDate < :timestamp")
            .setParameter("collection", collectionId).setParameter("timestamp", timestamp).getResultList();
    }

    private TypedQuery<Tag> query(String jpql) {
        return sessionFactory.getCurrentSession().createQuery(jpql, Tag.class);
    }
}
