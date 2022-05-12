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
