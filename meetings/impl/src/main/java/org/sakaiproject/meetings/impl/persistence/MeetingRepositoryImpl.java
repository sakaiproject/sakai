/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.impl.persistence;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.TransientObjectException;
import org.hibernate.criterion.Restrictions;

import org.sakaiproject.meetings.api.persistence.MeetingRepository;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeetingRepositoryImpl extends SpringCrudRepositoryImpl<Meeting, String> implements MeetingRepository {

    @Resource private ServerConfigurationService serverConfigurationService;

    @Transactional
    public List<Meeting> findBySiteId(String siteId, boolean includeDeleted) {

        Criteria c = sessionFactory.getCurrentSession().createCriteria(Meeting.class)
            .add(Restrictions.eq("siteId", siteId));

        if (!includeDeleted) {
            c.add(Restrictions.ne("deleted", true));
        }

        return (List<Meeting>) c.list();
    }

    @Transactional
    public boolean deleteMeeting(String meetingId, boolean fullDelete) {

        Session session = sessionFactory.getCurrentSession();

        if (fullDelete) {
            session.delete(session.get(Meeting.class, meetingId));
        } else {
            Meeting meeting = (Meeting) session.get(Meeting.class, meetingId);
            meeting.setDeleted(true);
            session.merge(meeting);
        }

        return true;
    }

    @Transactional
    public int deleteBySiteId(String siteId) {

        return sessionFactory.getCurrentSession().createQuery("delete Meeting where siteId = :siteId")
            .setString("siteId", siteId).executeUpdate();
    }
}
