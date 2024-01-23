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
package org.sakaiproject.meetings.impl.persistence;

import java.util.Optional;

import org.hibernate.Session;
import org.sakaiproject.meetings.api.model.MeetingProperty;
import org.sakaiproject.meetings.api.persistence.MeetingPropertyRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MeetingPropertyRepositoryImpl extends BasicSerializableRepository<MeetingProperty, Long> implements MeetingPropertyRepository{

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    @Override
    public Optional<MeetingProperty> findFirstByMeetingIdAndName(String meetingId, String name) {
        MeetingProperty result = (MeetingProperty) getCurrentSession()
                .createQuery("from MeetingProperty where meeting.id = :id and name = :name")
                .setParameter("id", meetingId)
                .setParameter("name", name).uniqueResult();
        return  Optional.ofNullable(result);
    }

    @Override
    public void deletePropertiesByMeetingId(String meetingId) {
        getCurrentSession().createQuery("delete from MeetingProperty where meeting.id = :id").setParameter("id", meetingId).executeUpdate();
    }
    
    @Override
    public void deletePropertyByMeetingIdAndName(String meetingId, String propertyName) {
        getCurrentSession().createQuery("delete from MeetingProperty where meeting.id = :id and name = :name")
        	.setParameter("id", meetingId)
        	.setParameter("name", propertyName)
        	.executeUpdate();
    }

}
