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
package org.sakaiproject.assignment.impl.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.model.UserNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradeAssignmentUserNotificationHandler extends AbstractUserNotificationHandler {

    @Autowired private AssignmentService assignmentService;
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    @Autowired private SessionFactory sessionFactory;
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    @Autowired private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        // Sometimes events are literally fired for LRS purposes. We don't want alerts for those.
        if (e.getLrsStatement() != null) {
            return Optional.empty();
        }

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String submissionId = pathParts[pathParts.length - 1];
        try {
            AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
            if (submission.getGradeReleased()) {
                Assignment assignment = submission.getAssignment();
                String title = assignment.getTitle();
                List<UserNotificationData> bhEvents = new ArrayList<>();
                submission.getSubmitters().forEach(to -> {

                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                    long currentCount = transactionTemplate.execute(status -> {
                        CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
                        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                        Root<UserNotification> root = cq.from(UserNotification.class);
                        cq.select(cb.count(root))
                            .where(
                                cb.equal(root.get("event"), AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION),
                                cb.equal(root.get("ref"), ref),
                                cb.equal(root.get("toUser"), to.getSubmitter())
                            );
                        return sessionFactory.getCurrentSession().createQuery(cq).uniqueResult();
                    });

                    if (currentCount == 0) {
                        try {
                            String url = assignmentService.getDeepLink(siteId, assignment.getId(), to.getSubmitter());
                            if (StringUtils.isNotBlank(url)) { 
                                bhEvents.add(new UserNotificationData(from, to.getSubmitter(), siteId, title, url, AssignmentConstants.TOOL_ID, false, null));
                            }
                        } catch(Exception exc) {
                            log.error("Error retrieving deep link for assignment {} and user {} on site {}", assignment.getId(), to.getSubmitter(), siteId, exc);
                        }
                    }
                });

                return Optional.of(bhEvents);
            }
        } catch (Exception ex) {
            log.error("Failed to find either the submission or the site", ex);
        }

        return Optional.empty();
    }
}
