/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.impl.persistence;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.microsoft.api.model.MicrosoftTeamArchiveRecord;
import org.sakaiproject.microsoft.api.persistence.MicrosoftTeamArchiveRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MicrosoftTeamArchiveRepositoryImpl extends BasicSerializableRepository<MicrosoftTeamArchiveRecord, Long> implements MicrosoftTeamArchiveRepository {

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public MicrosoftTeamArchiveRecord save(MicrosoftTeamArchiveRecord record) {
        sessionFactory.getCurrentSession().saveOrUpdate(record);
        return record;
    }

    @Override
    public List<MicrosoftTeamArchiveRecord> findByStatus(int status, int maxResults, int offset) {
        return (List<MicrosoftTeamArchiveRecord>) startCriteriaQuery()
                .add(Restrictions.eq("status", status))
                .setMaxResults(maxResults)
                .setFirstResult(offset)
                .list();
    }
}
