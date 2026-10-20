/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.user.impl.repository;

import java.util.Optional;

import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.sakaiproject.user.api.model.PreferencesRecord;
import org.sakaiproject.user.api.repository.PreferencesRepository;

public class PreferencesRepositoryImpl extends SpringCrudRepositoryImpl<PreferencesRecord, String> implements PreferencesRepository {

    @Override
    public Optional<PreferencesRecord> findCurrentById(String id) {
        return sessionFactory.getCurrentSession()
                .createQuery("select new org.sakaiproject.user.api.model.PreferencesRecord(p.id, p.xml, p.version) "
                        + "from PreferencesRecord p where p.id = :id", PreferencesRecord.class)
                .setParameter("id", id)
                .setCacheable(true)
                .uniqueResultOptional();
    }

    @Override
    public int deleteByIdAndVersion(String id, Long version) {
        return sessionFactory.getCurrentSession()
                .createMutationQuery("delete from PreferencesRecord p where p.id = :id and p.version = :version")
                .setParameter("id", id)
                .setParameter("version", version)
                .executeUpdate();
    }
}
