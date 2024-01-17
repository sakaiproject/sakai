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

import org.hibernate.Session;
import org.sakaiproject.meetings.api.model.MeetingAttendee;
import org.sakaiproject.meetings.api.persistence.MeetingAttendeeRepository;
import org.sakaiproject.serialization.BasicSerializableRepository;

public class MeetingAttendeeRepositoryImpl extends BasicSerializableRepository<MeetingAttendee, Long> implements MeetingAttendeeRepository{

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
    
    @Override
    public void removeAttendeesByMeetingId(String meetingId) {
        getCurrentSession().createQuery("delete from MeetingAttendee where meeting.id = :id").setParameter("id", meetingId).executeUpdate();
    }
    
    @Override
    public void removeSiteAndGroupAttendeesByMeetingId (String meetingId) {
        getCurrentSession().createQuery("delete from MeetingAttendee where meeting.id = :id and type in (1, 2)").setParameter("id", meetingId).executeUpdate();
    }
    
}
