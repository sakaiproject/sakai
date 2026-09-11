/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.tags.impl;

import java.util.List;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.tags.api.TagAssociation;
import org.sakaiproject.tags.api.TagAssociationRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class TagAssociationRepositoryImpl extends SpringCrudRepositoryImpl<TagAssociation, String> implements TagAssociationRepository {
    @Override
    public List<TagAssociation> findTagAssociationByCollectionAndItem(String collectionId, String itemId) {
        return sessionFactory.getCurrentSession().createQuery(
            "select a from TagAssociation a, TagServiceTag t where a.tagId = t.tagId and a.itemId = :item and t.tagCollectionId = :collection", TagAssociation.class)
            .setParameter("item", itemId).setParameter("collection", collectionId).getResultList();
    }

    @Override
    public TagAssociation findTagAssociationByItemIdAndTagId(String itemId, String tagId) {
        return sessionFactory.getCurrentSession().createQuery(
            "from TagAssociation a where a.itemId = :item and a.tagId = :tag", TagAssociation.class)
            .setParameter("item", itemId).setParameter("tag", tagId).uniqueResult();
    }

    @Override
    @Transactional
    public void deleteByTagId(String tagId) {
        sessionFactory.getCurrentSession().createQuery("delete from TagAssociation a where a.tagId = :tag")
            .setParameter("tag", tagId).executeUpdate();
    }
}
