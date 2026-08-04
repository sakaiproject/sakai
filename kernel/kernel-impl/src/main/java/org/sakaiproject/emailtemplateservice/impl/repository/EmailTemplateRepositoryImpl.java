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
package org.sakaiproject.emailtemplateservice.impl.repository;

import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.api.repository.EmailTemplateRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class EmailTemplateRepositoryImpl extends SpringCrudRepositoryImpl<EmailTemplate, Long> implements EmailTemplateRepository {

    @Transactional
    public Optional<EmailTemplate> findByKeyAndLocale(String key, String locale) {

        return sessionFactory.getCurrentSession()
            .createQuery("from EmailTemplate where key = :key and locale = :locale", EmailTemplate.class)
            .setParameter("key", key)
            .setParameter("locale", locale)
            .uniqueResultOptional();
    }

    @Transactional
    public Optional<EmailTemplate> findByKeyAndLocaleAndNotTemplateId(String key, String locale, Long templateId) {

        StringBuilder hql = new StringBuilder("from EmailTemplate where key = :key and locale = :locale");
        if (templateId != null) {
            hql.append(" and id != :templateId");
        }

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), EmailTemplate.class)
                .setParameter("key", key)
                .setParameter("locale", locale);

        if (templateId != null) {
            query.setParameter("templateId", templateId);
        }

        return query.uniqueResultOptional();
    }
}
