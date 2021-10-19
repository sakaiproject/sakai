/*
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
