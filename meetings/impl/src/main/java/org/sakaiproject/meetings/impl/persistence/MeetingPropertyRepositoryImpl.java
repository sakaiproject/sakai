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
