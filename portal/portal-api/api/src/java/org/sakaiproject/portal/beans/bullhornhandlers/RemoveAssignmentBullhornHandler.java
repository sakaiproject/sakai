/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RemoveAssignmentBullhornHandler extends AbstractBullhornHandler {

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(AssignmentConstants.EVENT_REMOVE_ASSIGNMENT);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {
        List<String> users = new ArrayList<>();

        String from = e.getUserId();
        String ref = e.getResource();
        String[] pathParts = ref.split("/");
        String assignmentId = pathParts[pathParts.length - 1];
        try {
            users = sessionFactory.getCurrentSession().createQuery("select toUser from BullhornAlert where event = :event and ref = :ref")
                    .setString("event", AssignmentConstants.EVENT_ADD_ASSIGNMENT)
                    .setString("ref", ref).list();
            // every graded user has probably received the addition event too, but it might have been added after creation
            users.addAll(sessionFactory.getCurrentSession().createQuery("select toUser from BullhornAlert where event = :event and ref like :ref")
                .setString("event", AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION)
                .setString("ref", ref.replace("/a/","/s/")+"%").list());
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {

                    sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where event = :event and ref = :ref")
                        .setString("event", AssignmentConstants.EVENT_ADD_ASSIGNMENT)
                        .setString("ref", ref).executeUpdate();
                    return null;
                });
            transactionTemplate.execute(status -> {

                    sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where event = :event and ref like :ref")
                        .setString("event", AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION)
                        .setString("ref", ref.replace("/a/","/s/")+"%").executeUpdate();
                    return null;
                });
        } catch (Exception e1) {
            log.error("Failed to delete bullhorn request event", e1);
        }

        users.forEach(countCache::remove);
        countCache.remove(from);
        return Optional.empty();
    }
}
