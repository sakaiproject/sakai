/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.beans.bullhornhandlers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.profile2.util.ProfileConstants;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FriendIgnoreBullhornHandler extends AbstractBullhornHandler {

    @Inject @Named("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Override
    public String getHandledEvent() {
        return ProfileConstants.EVENT_FRIEND_IGNORE;
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Map> countCache) {

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String to = pathParts[2];
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createQuery("delete BullhornAlert where event = :event and fromUser = :fromUser")
                .setString("event", ProfileConstants.EVENT_FRIEND_REQUEST)
                .setString("fromUser", to).executeUpdate();
            tx.commit();
        } catch (Exception e1) {
            log.error("Failed to delete bullhorn request event", e1);
            tx.rollback();
        } finally {
            session.close();
        }
        countCache.remove(from);
        return Optional.empty();
    }
}
