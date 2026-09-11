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
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagCollectionRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class TagCollectionRepositoryImpl extends SpringCrudRepositoryImpl<TagCollection, String> implements TagCollectionRepository {
    @Override
    @Transactional
    public TagCollection create(TagCollection collection) {
        sessionFactory.getCurrentSession().persist(collection);
        return collection;
    }

    @Override
    public List<TagCollection> findAllOrdered(int offset, int limit) {
        return sessionFactory.getCurrentSession().createQuery("from TagServiceCollection c order by c.name", TagCollection.class)
            .setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    public Optional<TagCollection> findByName(String name) {
        return sessionFactory.getCurrentSession().createQuery("from TagServiceCollection c where c.name = :name", TagCollection.class)
            .setParameter("name", name).getResultList().stream().findFirst();
    }

    @Override
    public Optional<TagCollection> findByExternalSourceName(String externalSourceName) {
        return sessionFactory.getCurrentSession().createQuery("from TagServiceCollection c where c.externalSourceName = :name", TagCollection.class)
            .setParameter("name", externalSourceName).getResultList().stream().findFirst();
    }
}
