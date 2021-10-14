/**
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
