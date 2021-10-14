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
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.api.repository.EmailTemplateRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class EmailTemplateRepositoryImpl extends SpringCrudRepositoryImpl<EmailTemplate, Long> implements EmailTemplateRepository {

    @Transactional
    public Optional<EmailTemplate> findByKeyAndLocale(String key, String locale) {

        Session session = sessionFactory.getCurrentSession();
        List<EmailTemplate> templates = session.createCriteria(EmailTemplate.class)
            .add(Restrictions.eq("key", key))
            .add(Restrictions.eq("locale", locale)).list();
        return templates.size() > 0 ? Optional.of(templates.get(0)) : Optional.empty();
    }
}
